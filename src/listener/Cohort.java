package listener;

import model.StringConstants;
import utility.FileAccessor;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
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
     * Servers configuration
     */
    private String[] serverAdd;
    private int[] serverPort;
    private int Id; //which server

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

    public void readServerConfig(String[] adds, int[] ports){
        this.serverAdd = adds;
        this.serverPort = ports;
    }

    /**
     * A method that would be executed by the thread
     */
    public void start(int id) {

        try {

                int pId = id+1;
                // Establish a connection to the Coordinator
                ServerSocket cohortListenSocket = new ServerSocket(serverPort[id]);
                System.out.println(cohortListenSocket.getInetAddress().toString() + ", " + cohortListenSocket.getLocalPort());
                cohortSocket = cohortListenSocket.accept();

                cohortBufferedReader =
                        new BufferedReader(
                                new InputStreamReader(cohortSocket.getInputStream()));
                cohortPrintStream = new PrintStream(cohortSocket.getOutputStream());

                String inline = null;
                inline = cohortBufferedReader.readLine();
                if(inline.startsWith(StringConstants.MESSAGE_REGISTER)) {
                    // Register itself with the coordinator
                    cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE
                            + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + serverPort[id] + StringConstants.SPACE + serverPort[id]);
                    cohortPrintStream.flush();
                }

                while(true) {

                    if(!startComputation){

                        startComputation = true;

                        boolean hasCommunicationStarted = false;

                        while (((inline = cohortBufferedReader.readLine()) != null) && (!(inline.isEmpty()))) {
                            System.out.println(inline);

                            hasCommunicationStarted = true;

                            // COMMIT REQ received
                            if (inline.split(StringConstants.SPACE)[0]
                                    .startsWith(StringConstants.MESSAGE_COMMIT_REQUEST)) {

                                System.out.println("Cohort " + pId + " received COMMIT_REQUEST from Coordinator");

                                cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE+ serverPort[id]);
                                cohortPrintStream.flush();

                                System.out.println("Cohort " + pId + " sent AGREED to the Coordinator");
                                System.out.println("Transition between the states for Cohort is : q" + pId
                                        + " --> w" + pId);
                            }

                            // Prepare Message received
                            if (inline.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_PREPARE)
                                    && !sentAck){

                                System.out.println("Cohort " + pId + " received PREPARE from the Coordinator");

                                cohortPrintStream.println(StringConstants.MESSAGE_ACK + StringConstants.SPACE + serverPort[id]);
                                cohortPrintStream.flush();
                                sentAck = true;

                                System.out.println("Cohort " + pId + " sent ACK to the Coordinator");
                            }

                            // Commit Message received
                            if (inline.split(StringConstants.SPACE)[0]
                                    .equals(StringConstants.MESSAGE_COMMIT)) {

                                String fileId = inline.split(StringConstants.SPACE)[2];
                                outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server"+pId+"/file"+fileId);
                                fileAccessor.writeToOutputFile1(outputFile,StringConstants.STATE_W + StringConstants.SPACE + pId + StringConstants.SPACE + fileId);

                                System.out.println("After COMMIT, transition between the states for Cohort is : p"
                                                + pId + " --> c" + pId);

                                System.out.println();
                                System.out.println("...Cohort Terminates...");
                                System.err.println();

                                break;
                            }

                            // Abort Message received
                            if (inline.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_ABORT)
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
