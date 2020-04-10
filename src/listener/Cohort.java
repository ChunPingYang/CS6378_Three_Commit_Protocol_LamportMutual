package listener;

import utility.FileAccessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    //private Socket cohortSocket = null;
    //private BufferedReader cohortBufferedReader = null;
    //private PrintStream cohortPrintStream = null;

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

    private boolean isCommitted;
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
        isCommitted = false;
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
            // Establish a connection to the Coordinator
            ServerSocket cohortListener = new ServerSocket(serverPort[id]);

            FileAccessor fileAccessor = new FileAccessor();

            while(true) {
                Socket cohortSocket = cohortListener.accept();
                new ClientThread(cohortSocket,id,serverPort,fileAccessor).start();
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

//    public void stopConnection(){
//        try {
//            cohortBufferedReader.close();
//            cohortPrintStream.close();
//            cohortSocket.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }

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
