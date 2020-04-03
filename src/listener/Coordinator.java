package listener;

import model.StringConstants;
import utility.SharedDataAmongCoordThreads;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
    private static int assignedProcessId;
    private static HashMap<Integer, Socket> connectionsToCoordinator;

    private DataInputStream inputStream = null;
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

            System.out.println("Coordinator Process started at port: " + PORT);

            // Store the information about coordinator itself in its arrays
            processIdArray[assignedProcessId - 1] = assignedProcessId;
            hostNameArray[assignedProcessId - 1] = InetAddress.getLocalHost().getHostName(); //TODO 這邊到時候要換成學校伺服器
            portNoArray[assignedProcessId - 1] = PORT;

            // Accept incoming connections from cohorts
            while (true) {
                //assignedProcessId++;
                socket = coordinatorListenSocket.accept();
                //connectionsToCoordinator.put(assignedProcessId, socket);

                inputStream = new DataInputStream(socket.getInputStream());
                readInputStream = inputStream.readLine();
                // If received message is REGISTER
                if (readInputStream.startsWith(StringConstants.MESSAGE_REGISTER)) {

                    // Check if the id is already present in the array
                    if (!searchTable(assignedProcessId)) {
                        // Print the received request from the process
                        System.out.println("Received: " + readInputStream);
                    }

                    /*
                     * After insertion, create a new thread other computations
                     * then the register would be handled
                     */
                    new CoordinatorServerHandler(socket, maxProcess, inputStream, assignedProcessId, data).start();

                    /*
                     * Once all cohorts register, engage the coordinator
                     * computation
                     */
                    if (assignedProcessId == maxProcess) {
//                        CoordinatorClientHandler c = new CoordinatorClientHandler(variable, data);
//                        c.start();
//                        break;
                    }

                    assignedProcessId++;
                }
            }

            /*
            socket = coordinatorListenSocket.accept();
            inputStream = new DataInputStream(socket.getInputStream());
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(StringConstants.SPACE);
            printWriter.flush();

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
