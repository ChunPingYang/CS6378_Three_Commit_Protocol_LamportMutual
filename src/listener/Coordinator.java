package listener;

import utility.SharedDataAmongCoordThreads;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Coordinator {

    /**
     * Variables required for establishing connections among processes and
     * coordinator
     */
    private int PORT = 9001;
    private static int assignedProcessId;
    private static HashMap<Integer, Socket> connectionsToCoordinator;

    private ServerSocket coordinatorListenSocket = null;

    /**
     * Variables to store information obtained from Config file
     */
    private int interval;
    private int terminate;
    private int maxProcess;
    private String coordinatorHostName;

    /**
     * Variable arrays to store the process id, host name and port no of all the
     * processes in the topology
     */
    private static int[] portNoArray;
    private static int[] processIdArray;
    private static String[] hostNameArray;

    /**
     * A class that contains various updated values that can be used by client and
     * server threads
     */
    private SharedDataAmongCoordThreads data;

    /**
     * Default constructor that will initialize many variables
     */
    public Coordinator() {
        assignedProcessId = 1;
        connectionsToCoordinator = new HashMap<>();
        data = new SharedDataAmongCoordThreads();
    }

    /**
     * A method that will accept incoming connections from various processes,
     * will store the information of all the processes and then will act as a
     * normal process once all the READY messages have been received
     */
    public void start() {
        initialzeArray();

        try {
            // Start server at the given PORT
            coordinatorListenSocket = new ServerSocket(PORT);
//        coordinatorListenSocket.setReuseAddress(true);  //https://blog.csdn.net/qq_34444097/article/details/78966654
//        coordinatorListenSocket.setSoTimeout(1000 * 60 * 60);


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * A method to initialize the arrays where in process id, host name and port
     * no of all the processes in the topology is stored
     */
    private void initialzeArray() {
        if (maxProcess != 0) {
            processIdArray = new int[maxProcess + 1];
            hostNameArray = new String[maxProcess + 1];
            portNoArray = new int[maxProcess + 1];
        }
    }

    /**
     * Getters and Setters for private variables
     */
    public String getHostName() {
        return coordinatorHostName;
    }

    public void setHostName(String hostName) {
        this.coordinatorHostName = hostName;
    }

    public int getMaxProcess() {
        return maxProcess;
    }

    public void setMaxProcess(int maxProcess) {
        this.maxProcess = maxProcess;
    }

    public int getTerminate() {
        return terminate;
    }

    public void setTerminate(int terminate) {
        this.terminate = terminate;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
