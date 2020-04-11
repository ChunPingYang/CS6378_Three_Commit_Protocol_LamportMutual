package listener;

import com.sun.tools.javac.util.StringUtils;
import model.StringConstants;
import utility.SharedDataAmongCoordThreads;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
    private String inLine;
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
     * fileId range from 1 to n
     */
    public void start(int clientId,int fileId, String action) {
        initializeArray();

        int[] servers = selectServer(fileId);

        if(StringConstants.ACTION_WRITE.equalsIgnoreCase(action))
        {
            write(servers,fileId,clientId);

        }else if(StringConstants.ACTION_READ.equalsIgnoreCase(action))
        {
            Random rand = new Random();
            int pidIndex = rand.nextInt(3);

            try {

                System.out.println("Server: "+serverAdd[servers[pidIndex]]+", Port: "+serverPort[servers[pidIndex]]);
                Socket socket = new Socket(serverAdd[servers[pidIndex]], serverPort[servers[pidIndex]]);
                PrintStream coordinatorPrintStream = new PrintStream(socket.getOutputStream());
                int processId = servers[pidIndex] + 1;
                coordinatorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
                coordinatorPrintStream.flush();

                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String inLine = null;
                while (((inLine = bufferReader.readLine()) != null) && (!(inLine.isEmpty()))) {

                    if (inLine.split(StringConstants.SPACE)[0]
                            .startsWith(StringConstants.MESSAGE_AGREED))
                    {
                        coordinatorPrintStream.println(StringConstants.ACTION_READ + StringConstants.SPACE + fileId);
                        coordinatorPrintStream.flush();
                    }

                    if(inLine.split(StringConstants.SPACE)[0]
                            .startsWith(StringConstants.MESSAGE_FILE_NOT_EXIST)){
                        System.out.println("The file does not exist");
                    }
                }

            }catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopConnection(PrintStream coordinatorPrintStream,BufferedReader bufferReader,Socket socket){
        try {
            coordinatorPrintStream.close();
            bufferReader.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
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
    private void initializeArray() {
        if (maxProcess != 0) {
            processIdArray = new int[maxProcess + 1];
            hostNameArray = new String[maxProcess + 1];
            portNoArray = new int[maxProcess + 1];
        }
    }

    //TODO update/insert;read, 這邊mod要改成7
    public int[] selectServer(int fileId){
        return new int[]{fileId%5,(fileId+1)%5,(fileId+2)%5};
    }

    public void write(int[] servers, int fileId, int clientId){

        try {

            int n_time = 0;
            while(n_time++ <= 10) {

                data.initializeSharedData();

                final int count = n_time;

                // build connections from cohorts
                for (int i = 0; i < servers.length; i++) {
                    final int pidIndex = i;
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {

//                              System.out.println(serverAdd[servers[pidIndex]] + ", " + serverPort[servers[pidIndex]]);
                                Socket socket = new Socket(serverAdd[servers[pidIndex]], serverPort[servers[pidIndex]]);
                                int processId = servers[pidIndex] + 1;
                                PrintStream coordinatorPrintStream = new PrintStream(socket.getOutputStream());
                                coordinatorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
                                coordinatorPrintStream.flush();


                                BufferedReader bufferReader = new BufferedReader(
                                        new InputStreamReader(socket.getInputStream()));

                                String inLine = bufferReader.readLine();
                                // If received message is AGREED[REGISTER]
                                if (inLine.startsWith(StringConstants.MESSAGE_AGREED)) {

                                    if (pidIndex+1 == servers.length) { //TODO 所有伺服器都連接上後
//                                    CoordinatorClientHandler c = new CoordinatorClientHandler(variable, data);
//                                    c.start();
                                        //break;
                                        data.setCommitMade(true);
                                    }


                                    new CoordinatorServerHandler(socket, servers.length, bufferReader, processId, fileId, clientId, count, data).start();
//                                  System.out.println("Server: "+serverPort[servers[pidIndex]]+" serversCommitStatus: "+data.isServersCommitted());
                                }


                                //Close
                                stopConnection(coordinatorPrintStream,bufferReader,socket);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }

                    }).start();

                }

                //used to validate all servers done commit
                boolean serversCommitStatus = false;
                while(!serversCommitStatus) {
                    serversCommitStatus = data.isServersCommitted();
                    //System.out.println("serversCommitStatus: "+serversCommitStatus);

                    if(serversCommitStatus){
//                        System.out.println("all servers commit........");
                    }
                }

//                Thread.sleep(3000);
            }

            /*

            stopConnection();
*/
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void read(){

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
