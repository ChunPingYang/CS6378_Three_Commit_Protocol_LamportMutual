package listener;

import utility.FileAccessor;

import java.io.*;
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
    private DataInputStream cohortDataInputStream = null;
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
