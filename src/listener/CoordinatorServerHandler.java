package listener;

import model.StringConstants;
import utility.FileAccessor;
import utility.SharedDataAmongCoordThreads;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class CoordinatorServerHandler extends Thread{

    /**
     * Variables required for establishing connections
     */
    private Socket cohortSocket = null;
    private PrintWriter printWriter = null;
    private DataInputStream dataInputStream = null;
    private BufferedReader bufferReader = null;

    /**
     * Boolean variables required to not allow the coordinator to send the same
     * data multiple times to all cohorts
     */
    private boolean isAborted;
    private boolean isCommitted;
    private boolean isCommitRequest;
    private boolean isPrepareSentToAllCohorts;
    private boolean coordinatorFail = false;

    /**
     * Variables required for computation
     */
    private String stringInputStream;
    private int maxCohort;
    private int processId;

    /**
     * Variable to access shared data among different handler threads
     */
    private SharedDataAmongCoordThreads sharedDataAmongCoordThreads;


    /**
     * Variable to access file methods
     */
    private FileAccessor fileaccessor;

    /**
     * A parameterized constructor that initializes its local variables
     */
    public CoordinatorServerHandler(Socket cohortSocket, int maxCohort, BufferedReader bufferReader, int pId,
                                    SharedDataAmongCoordThreads sharedDataAmongCoordThreads) {
        this.cohortSocket = cohortSocket;
        this.maxCohort = maxCohort;
        this.bufferReader = bufferReader;
        this.processId = pId;
        this.sharedDataAmongCoordThreads = sharedDataAmongCoordThreads;

        isAborted = false;
        isCommitted = false;
        isCommitRequest = false;
        isPrepareSentToAllCohorts = false;

//        stateLogFile = new File(System.getProperty("user.dir") + "/StateInfo_Coordinator");
//        outputLogFile = new File(System.getProperty("user.dir") + "/Output_Coordinator");

        fileaccessor = new FileAccessor();
    }


    @Override
    public void run() {

        while (true) {

            try {
                printWriter = new PrintWriter(cohortSocket.getOutputStream());
                printWriter.println(processId + StringConstants.SPACE); //TODO 不懂為什麼要加空白
                printWriter.flush();

                //isCommitRequest: send commit_request
                if (sharedDataAmongCoordThreads.isCommitRequest() && !isCommitRequest && !coordinatorFail) {

                    isCommitRequest = true;
                    printWriter.println(StringConstants.MESSAGE_COMMIT_REQUEST + StringConstants.SPACE);
                    printWriter.flush();

                    System.out.println(
                            "Coordinator sent COMMIT_REQUEST message to all Cohorts. The state chagnges from Q1 --> W1");

                    String inLine = null;
                    while (((inLine = bufferReader.readLine()) != null) && (!(inLine.isEmpty()))) {
                        System.out.println(inLine);

                        if (inLine.split(StringConstants.SPACE)[0]
                                .startsWith(StringConstants.MESSAGE_AGREED)) {

                            sharedDataAmongCoordThreads.incrementAgree();
                            System.out.println("Coordinator received AGREED from "
                                    + sharedDataAmongCoordThreads.getCountAgreeFromCohort() + " Cohort");

                            //TODO 要等待所有的伺服器數量
                            if (sharedDataAmongCoordThreads.getCountAgreeFromCohort() != maxCohort
                                    && !isCommitted) {


                            }

                        }

                        // Received AGREED Message from all cohorts
                        if (sharedDataAmongCoordThreads.getCountAgreeFromCohort() == maxCohort
                                && !isPrepareSentToAllCohorts && !coordinatorFail) {
                            isPrepareSentToAllCohorts = true;
                            System.out.println(
                                    "Coordinator received AGREED from all Cohorts. Transition from w1 --> p1");

                            printWriter.println(StringConstants.MESSAGE_PREPARE + StringConstants.SPACE);
                            printWriter.flush();

                            System.out.println("Coordinator sent PREPARE to all Cohorts");
                        }

                        // Received ACK Message
                        if (inLine.split(StringConstants.SPACE)[0]
                                .startsWith(StringConstants.MESSAGE_ACK) && !coordinatorFail) {
                            sharedDataAmongCoordThreads.incrementAck();
                            System.out.println("Coordinator received ACK from "
                                    + sharedDataAmongCoordThreads.getCountAckFromCohort() + " Cohort(s)");

                            //Wait for other cohorts to send ack
                            while (sharedDataAmongCoordThreads.getCountAckFromCohort() != maxCohort) {
                                Thread.sleep(1000);
                            }
                        }

                        // Received ACK Message from all
                        if (sharedDataAmongCoordThreads.getCountAckFromCohort() == maxCohort && !coordinatorFail) {

                            if(sharedDataAmongCoordThreads.getCountAckFromCohort() == maxCohort
                                    && !isCommitted && !coordinatorFail){
                                isCommitted = true;

                                printWriter.println(StringConstants.MESSAGE_COMMIT + StringConstants.SPACE);
                                printWriter.flush();

                                System.out.println("Coordinator sent COMMIT to all cohorts");

                                System.out.println(
                                        "Transition between the states for Coordinator is : p1 --> c1");

                                System.out.println("...Coordinator Thread terminates...");
                                System.out.println();
                                break;
                            }
                        }

                        // Received ABORT Message
                        if (inLine.split(StringConstants.SPACE)[0]
                                .startsWith(StringConstants.MESSAGE_ABORT) && !isAborted && !coordinatorFail) {

                        }

                        if (sharedDataAmongCoordThreads.isAborted() && !coordinatorFail) {

                        }
                    }

                    // After recovery, abort
                    if ((sharedDataAmongCoordThreads.isAbortAfterRecovery() && !isAborted && !coordinatorFail)) {

                    }
                    // After recovery, commit
                    if (sharedDataAmongCoordThreads.isCommitAfterRecovery() && !isCommitted && !coordinatorFail) {

                    }
                }

                }catch(IOException e){
                    e.printStackTrace();
                }catch(Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
