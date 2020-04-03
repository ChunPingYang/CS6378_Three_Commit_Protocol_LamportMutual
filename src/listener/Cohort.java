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
    //private DataInputStream cohortDataInputStream = null;
    private BufferedReader cohortDataInputStream = null;
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

            while(true) {

                // Establish a connection to the Coordinator
                cohortSocket = new Socket(coordinatorHostName, coordinatorPort);
                //cohortDataInputStream = new DataInputStream(cohortSocket.getInputStream());
                cohortDataInputStream =
                        new BufferedReader(
                                new InputStreamReader(cohortSocket.getInputStream()));
                cohortPrintStream = new PrintStream(cohortSocket.getOutputStream());

                // Register itself with the coordinator
                cohortPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE
                        + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + PORT + StringConstants.SPACE);
                cohortPrintStream.flush();

//                readInputStream = cohortDataInputStream.readLine();
//                int value = 0;
//                while ((value = cohortDataInputStream.read()) != 0) {
                    //System.out.println("echo: " + cohortDataInputStream.readLine());
//                }
                String inLine = null;
                while (((inLine = cohortDataInputStream.readLine()) != null) && (!(inLine.isEmpty()))) {
                    System.out.println(inLine);
                }

                stopConnection();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void stopConnection(){
        try {
            cohortDataInputStream.close();
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
