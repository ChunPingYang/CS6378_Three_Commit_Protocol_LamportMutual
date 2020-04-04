package listener;

import model.StringConstants;
import utility.FileAccessor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class Cohort {
    /**
     * Variables to establish a connection with coordinator and neighboring
     * process
     */
    private int PORT = 5005;
    private int coordinatorPort = 9001;

    private Socket cohortSocket = null;
    private BufferedReader cohortBufferedReader = null;
    private PrintStream cohortPrintStream = null;

    /**
     * Variables to store information from the Config file
     */
    private int maxCohort;
    private String coordinatorHostName;

    /**
     * Variables required for computation
     */
    private int pId;
    private String readInputStream;

    private boolean isAborted;
    private String choice;
    private boolean sentAck;
    private boolean startComputation;
    private boolean recoveryDone;
    private boolean recoveryCheckDone;
    private String string;
    private int lastvalue;
    private int transactionId;
    private String state;

    /** Variable to access file methods */
    private FileAccessor fileAccessor;
    private File stateLogFile;
    private File outputFile;
    private FileReader fileReader, outputReader;
    private BufferedReader bufferedReader = null, outputBufferedReader = null;
    private long length;

    /**
     * Variables to calculate the timeout of cohort
     */
    private Date startTime;
    private Date endTime;
    private long timeOut;
    private long duration;

    /**
     * Default constructor to initialize the variables
     */
    public Cohort() {
        fileAccessor = new FileAccessor();
        isAborted = false;
        sentAck = false;
        recoveryDone = false;
        startComputation = false;
        recoveryCheckDone = false;

        lastvalue = 0;
        transactionId = 0;
    }

    /**
     * A method that would be executed by the thread
     */
    public void start() {

        try {

                // Establish a connection to the Coordinator
                cohortSocket = new Socket(coordinatorHostName, coordinatorPort);
                //cohortDataInputStream = new DataInputStream(cohortSocket.getInputStream());
                cohortBufferedReader =
                        new BufferedReader(
                                new InputStreamReader(cohortSocket.getInputStream()));
                cohortPrintStream = new PrintStream(cohortSocket.getOutputStream());

                // Register itself with the coordinator
                cohortPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE
                        + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + PORT + StringConstants.SPACE);
                cohortPrintStream.flush();

                System.out.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE
                    + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + PORT + StringConstants.SPACE);


                // Fetch the process id assigned by the Coordinator
                readInputStream = cohortBufferedReader.readLine();
                pId = Integer.parseInt(readInputStream.split(StringConstants.SPACE)[0]);
                System.out.println("Process Id: " + pId);

                while(true) {
                    String inLine = null;
                    inLine = cohortBufferedReader.readLine();

                    if(!startComputation){

                        startComputation = true;

                        boolean hasCommunicationStarted = false;

                        while (((inLine = cohortBufferedReader.readLine()) != null) && (!(inLine.isEmpty()))) {
                            System.out.println(inLine);

                            hasCommunicationStarted = true;

                            // COMMIT REQ received
                            if (inLine.split(StringConstants.SPACE)[0]
                                    .startsWith(StringConstants.MESSAGE_COMMIT_REQUEST)) {

                                System.out.println("Cohort " + pId + " received COMMIT_REQUEST from Coordinator");

                                cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE);
                                cohortPrintStream.flush();

                                System.out.println("Cohort " + pId + " sent AGREED to the Coordinator");
                                System.out.println("Transition between the states for Cohort is : q" + pId
                                        + " --> w" + pId);
                            }

                            // Prepare Message received
                            if (inLine.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_PREPARE)
                                    && !sentAck){

                                System.out.println("Cohort " + pId + " received PREPARE from the Coordinator");

                                cohortPrintStream.println(StringConstants.MESSAGE_ACK + StringConstants.SPACE);
                                cohortPrintStream.flush();
                                sentAck = true;

                                System.out.println("Cohort " + pId + " sent ACK to the Coordinator");
                            }

                            // Commit Message received
                            if (inLine.split(StringConstants.SPACE)[0]
                                    .equals(StringConstants.MESSAGE_COMMIT)) {

                                System.out.println("After COMMIT, transition between the states for Cohort is : p"
                                                + pId + " --> c" + pId);

                                System.out.println();
                                System.out.println("...Cohort Terminates...");
                                System.err.println();

                                break;
                            }

                            // Abort Message received
                            if (inLine.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_ABORT)
                                    && !isAborted) {

                            }

                        }

                    }


                }
                //stopConnection();



        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void stopConnection(){
        try {
            cohortBufferedReader.close();
            cohortPrintStream.close();
            cohortSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Getters and Setters to access the private variables
     */
    public int getMaxProcess() {
        return maxCohort;
    }

    public void setMaxProcess(int maxProcess) {
        this.maxCohort = maxProcess;
    }

    public String getCoordinatorHostName() {
        return coordinatorHostName;
    }

    public void setCoordinatorHostName(String coordinatorHostName) {
        this.coordinatorHostName = coordinatorHostName;
    }
}
