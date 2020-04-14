package listener;

import model.CSMessage;
import model.StringConstants;
import utility.FileAccessor;
import utility.SharedDataAmongCoordThreads;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

public class CoordinatorServerHandler{

    /**
     * Variables required for establishing connections
     */
    private Socket cohortSocket = null;
    private PrintWriter printWriter = null;
    private DataInputStream dataInputStream = null;
    //private BufferedReader bufferReader = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    /**
     * Boolean variables required to not allow the coordinator to send the same
     * data multiple times to all cohorts
     */
    private boolean isAborted;
    private boolean isCommitted;
    private boolean isCommitRequest;
    private boolean isPrepareSentToAllCohorts;
    private boolean coordinatorFail = false;
    private boolean isCommitCompleted;

    /**
     * Variables required for computation
     */
    private String stringInputStream;
    private int maxCohort;
    private int processId;
    private int fileId;
    private int n_time;
    private int clientId;
    private Set<Integer> otherServers;

    /**
     * Variable to access shared data among different handler threads
     */
    private SharedDataAmongCoordThreads sharedDataAmongCoordThreads;


    /**
     * Variable to access file methods
     */
    private FileAccessor fileaccessor;

    /**
     * Variables to access the log files for states of three phase protocol and
     * output files
     */
    private File outputFile;

    /**
     * A parameterized constructor that initializes its local variables
     */
    public CoordinatorServerHandler(Socket cohortSocket,
                                    int maxCohort,
                                    ObjectInputStream ois,
                                    ObjectOutputStream oos,
                                    int pId,
                                    int fileId,
                                    int clientId,
                                    int n_time,
                                    Set<Integer> otherServers,
                                    SharedDataAmongCoordThreads sharedDataAmongCoordThreads) {
        this.cohortSocket = cohortSocket;
        this.maxCohort = maxCohort;
        this.ois = ois;
        this.oos = oos;
        this.processId = pId;
        this.fileId = fileId;
        this.n_time = n_time;
        this.clientId = clientId;
        this.otherServers = otherServers;
        this.sharedDataAmongCoordThreads = sharedDataAmongCoordThreads;

        isAborted = false;
        isCommitted = false;
        isCommitRequest = false;
        isPrepareSentToAllCohorts = false;

//        stateLogFile = new File(System.getProperty("user.dir") + "/StateInfo_Coordinator");
//        outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server"+pId+"/file"+fileId);

        fileaccessor = new FileAccessor();
    }


    public void start() {

        try {

                //oos = new ObjectOutputStream(cohortSocket.getOutputStream());

                while (true) {

                    //isCommitRequest: send commit_request
                    if (sharedDataAmongCoordThreads.isCommitRequest() && !isCommitRequest && !coordinatorFail) {

                        isCommitRequest = true;
//                        printWriter.println(StringConstants.ROLE_COORDINATOR + StringConstants.SPACE +
//                                            StringConstants.MESSAGE_COMMIT_REQUEST + StringConstants.SPACE +
//                                            processId + StringConstants.SPACE +
//                                            clientId + StringConstants.SPACE +
//                                            fileId + StringConstants.SPACE +
//                                            n_time + StringConstants.SPACE +
//                                            otherServers[0]+":"+otherServers[1]);
//                        printWriter.flush();
                        CSMessage push = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                        StringConstants.MESSAGE_COMMIT_REQUEST,
                                                        processId,
                                                        clientId,
                                                        fileId,
                                                        n_time,
                                                        otherServers);
                        oos.writeObject(push);
                        oos.flush();

//                        System.out.println(
//                                "Coordinator sent COMMIT_REQUEST message to all Cohorts. The state chagnges from Q1 --> W1");

                        CSMessage received = null;
                        while ((received = (CSMessage)ois.readObject()) != null) {
                            //System.out.println(inLine);

                            if (received.getMessage().equals(StringConstants.MESSAGE_AGREED)) {

                                sharedDataAmongCoordThreads.incrementAgree();
//                                System.out.println("Coordinator received AGREED from "
//                                        + sharedDataAmongCoordThreads.getCountAgreeFromCohort() + " Cohort");

                                //TODO 要等待所有的伺服器數量
                                while(sharedDataAmongCoordThreads.getCountAgreeFromCohort() != maxCohort
                                        && !isCommitted) {
                                    Thread.sleep(1000);
                                }

                            }

                            // Received AGREED Message from all cohorts
                            if (sharedDataAmongCoordThreads.getCountAgreeFromCohort() == maxCohort
                                    && !isPrepareSentToAllCohorts && !coordinatorFail) {
                                isPrepareSentToAllCohorts = true;
//                                System.out.println(
//                                        "Coordinator received AGREED from all Cohorts. Transition from w1 --> p1");

//                                printWriter.println(StringConstants.ROLE_COORDINATOR + StringConstants.SPACE +
//                                                    StringConstants.MESSAGE_PREPARE + StringConstants.SPACE+
//                                                    processId);
//                                printWriter.flush();
                                CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                                StringConstants.MESSAGE_PREPARE,
                                                                processId,
                                                                clientId,
                                                                fileId,
                                                                n_time,
                                                                otherServers);
                                oos.writeObject(sent);
                                oos.flush();

//                                System.out.println("Coordinator sent PREPARE to all Cohorts");
                            }

                            // Received ACK Message
                            if (received.getMessage().equals(StringConstants.MESSAGE_ACK)
                                    && !coordinatorFail)
                            {
                                sharedDataAmongCoordThreads.incrementAck();
//                                System.out.println("Coordinator received ACK from "
//                                        + sharedDataAmongCoordThreads.getCountAckFromCohort() + " Cohort(s)");

                                //Wait for other cohorts to send ack
                                while (sharedDataAmongCoordThreads.getCountAckFromCohort() != maxCohort) {
                                    Thread.sleep(1000);
                                }
                            }

                            // Received ACK Message from all
                            if (sharedDataAmongCoordThreads.getCountAckFromCohort() == maxCohort && !coordinatorFail) {

                                if (sharedDataAmongCoordThreads.getCountAckFromCohort() == maxCohort
                                        && !isCommitted && !coordinatorFail) {
                                    isCommitted = true;

//                                    printWriter.println(StringConstants.ROLE_COORDINATOR + StringConstants.SPACE +
//                                                        StringConstants.MESSAGE_COMMIT + StringConstants.SPACE +
//                                                        processId + StringConstants.SPACE +
//                                                        fileId + StringConstants.SPACE +
//                                                        n_time + StringConstants.SPACE + clientId);
//                                    printWriter.flush();
                                        CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                                        StringConstants.MESSAGE_COMMIT,
                                                                        processId,
                                                                        clientId,
                                                                        fileId,
                                                                        n_time,
                                                                        otherServers);
                                        oos.writeObject(sent);
                                        oos.flush();

//                                    System.out.println("Coordinator sent COMMIT to all cohorts");
//
//                                    System.out.println(
//                                            "Transition between the states for Coordinator is : p1 --> c1");

//                                    System.out.println("...Coordinator Thread terminates...");
//                                    System.out.println();
//                                    break;
                                }

                            }

                            // Received COMMIT_COMPLETE Message
                            if (received.getMessage().equals(StringConstants.MESSAGE_COMMIT_COMPLETE)
                                    && !isCommitCompleted && !coordinatorFail)
                            {
                                sharedDataAmongCoordThreads.incrementCommitCompletedFromCohort();
//                                System.out.println("Coordinator received COMMIT_COMPLETE from "
//                                        + sharedDataAmongCoordThreads.getCountCommitCompletedFromCohort() + " Cohort(s)");

                                //Wait for other cohorts to send commit_complete
                                while (sharedDataAmongCoordThreads.getCountCommitCompletedFromCohort() != maxCohort) {
                                    Thread.sleep(1000);
                                }

                                isCommitCompleted = true;
                                sharedDataAmongCoordThreads.setServersCommitted(true);

//                                System.out.println("...Coordinator Thread terminates...");
//                                System.out.println();

                                break;
                            }

                            // Received ABORT Message
                            if (received.getMessage().equals(StringConstants.MESSAGE_ABORT)
                                    && !isAborted && !coordinatorFail) {

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

                    if(isCommitCompleted){break;}
                }

            }catch (SocketTimeoutException e){
                System.out.println("Time out.....");
                e.getMessage();
            }catch(IOException e){
                e.printStackTrace();
            }catch(Exception e) {
                e.printStackTrace();
            }

    }


}
