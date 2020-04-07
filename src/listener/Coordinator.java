package listener;

import model.StringConstants;
import utility.SharedDataAmongCoordThreads;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Coordinator {

    /**
     * Test
     */
    private PrintWriter printWriter = null;

    /**
     * Variables required for establishing connections among processes and
     * coordinator
     */
    private int PORT = 9001;
    private String readInputStream;
    private String inline;
    private static int assignedProcessId;
    private int variable;
    private static HashMap<Integer, Socket> connectionsToCoordinator;

    private DataInputStream inputStream = null;
    //private BufferedReader bufferReader = null;
    private Socket socket = null;
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
     * Servers configuration
     */
    private String[] serverAdd;
    private int[] serverPort;

    /**
     * Default constructor that will initialize many variables
     */
    public Coordinator() {
        assignedProcessId = 1;
        connectionsToCoordinator = new HashMap<>();
        data = new SharedDataAmongCoordThreads();
    }

    public void readServerConfig(String[] adds, int[] ports){
        this.serverAdd = adds;
        this.serverPort = ports;
    }
    /**
     * A method that will accept incoming connections from various processes,
     * will store the information of all the processes and then will act as a
     * normal process once all the READY messages have been received
     */
    public void start() {
        initialzeArray();

        try {

            System.out.println("Coordinator Process started at port: " + PORT);

            // Store the information about coordinator itself in its arrays0
            processIdArray[assignedProcessId - 1] = assignedProcessId;
            hostNameArray[assignedProcessId - 1] = InetAddress.getLocalHost().getHostName(); //TODO 這邊到時候要換成學校伺服器
            portNoArray[assignedProcessId - 1] = PORT;

            // build connections from cohorts
            for(int i=0;i<maxProcess;i++){
                final int pidIndex = i;
                final Coordinator that = this;
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            System.out.println(serverAdd[pidIndex] + ", " + serverPort[pidIndex]);
                            Socket socket = new Socket(serverAdd[pidIndex], serverPort[pidIndex]);
                            int processId = pidIndex+1;
                            PrintStream coordinatorPrintStream = new PrintStream(socket.getOutputStream());
                            coordinatorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
                            coordinatorPrintStream.flush();


                            BufferedReader bufferReader = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream()));
                            inline = bufferReader.readLine();

                            // If received message is AGREED[REGISTER]
                            if (inline.startsWith(StringConstants.MESSAGE_AGREED)) {

                                // Check if the id is already present in the array
                                if (!searchTable(assignedProcessId)) {
                                    // Print the received request from the process
                                    System.out.println("Received: " + inline);
                                }

                                if (processId == maxProcess) { //TODO 所有伺服器都連接上後
//                                    CoordinatorClientHandler c = new CoordinatorClientHandler(variable, data);
//                                    c.start();
                                    //break;
                                    data.setCommitMade(true);
                                }

                                //that.incrementProcessId();
                                //int pid = that.getProcessId();
                                new CoordinatorServerHandler(socket, maxProcess, bufferReader, processId, data).start();

                            }




                        }catch(IOException e){
                            e.printStackTrace();
                        }


                    }

                }).start();


            }

            /*

            stopConnection();
*/
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopConnection(){
        try {
            inputStream.close();
            printWriter.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                coordinatorListenSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * A method to search whether the generated assigned Id is already present
     * in the array
     */
    private boolean searchTable(int processId) {
        for (int i = 0; i < maxProcess; i++) {
            if (processIdArray[i] == processId)
                return true;
        }
        return false;
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
    public synchronized void incrementProcessId(){
        assignedProcessId++;
    }

    public synchronized int getProcessId(){
        return assignedProcessId;
    }

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
