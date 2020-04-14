package listener;

import com.sun.tools.javac.util.StringUtils;
import model.CSMessage;
import model.StringConstants;
import utility.SharedDataAmongCoordThreads;

import java.io.*;
import java.net.*;
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
        //initializeArray();

        //int[] servers = selectServer(fileId);
        List<Integer> servers = selectServer1(fileId);
        //System.out.println(Arrays.toString(servers));
        System.out.println(servers.toString());

        if(StringConstants.ACTION_WRITE.equalsIgnoreCase(action))
        {
            write(servers,fileId,clientId);

        }else if(StringConstants.ACTION_READ.equalsIgnoreCase(action))
        {
            read(servers,fileId,clientId);
        }

    }

    public synchronized void stopConnection(PrintStream coordinatorPrintStream,BufferedReader bufferReader,Socket socket){
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

    public List<Integer> selectServer1(int fileId){
        List<Integer> list = new ArrayList<>();
        list.add(fileId%5);
        list.add((fileId+1)%5);
        list.add((fileId+2)%5);
        return list;
    }

    public void read(List<Integer> servers, int fileId, int clientId){

        Random rand = new Random();
        int pidIndex = rand.nextInt(3);

        try {

            System.out.println("Server: "+serverAdd[servers.get(pidIndex)]+", Port: "+serverPort[servers.get(pidIndex)]);
            Socket socket = new Socket(serverAdd[servers.get(pidIndex)], serverPort[servers.get(pidIndex)]);
            int processId = servers.get(pidIndex);
//            PrintStream coordinatorPrintStream = new PrintStream(socket.getOutputStream());
//            coordinatorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
//            coordinatorPrintStream.flush();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            CSMessage push = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                            StringConstants.MESSAGE_REGISTER,
                                            processId,
                                            clientId,
                                            fileId,
                                            0,
                                            new HashSet<>());
            oos.writeObject(push);
            oos.flush();

//            BufferedReader bufferReader = new BufferedReader(
//                    new InputStreamReader(socket.getInputStream()));

            CSMessage received = null;
            while ((received = (CSMessage)ois.readObject()) != null) {

                if (received.getMessage().equals(StringConstants.MESSAGE_AGREED))
                {
//                    coordinatorPrintStream.println(StringConstants.ACTION_READ + StringConstants.SPACE + fileId);
//                    coordinatorPrintStream.flush();
                    CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                    StringConstants.ACTION_READ,
                                                    processId,
                                                    clientId,
                                                    fileId,
                                             0,
                                                    new HashSet<>());
                    oos.writeObject(sent);
                    oos.flush();
                }

                if(received.getMessage().equals(StringConstants.MESSAGE_FILE_NOT_EXIST)){
                    System.out.println("The file does not exist");
                    //TODO 關閉socket
                }
            }

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void write(List<Integer> servers, int fileId, int clientId){

        try {

            int n_time = 0;
            while (n_time++ <= 30) {

                data.initializeSharedData();

                final int count = n_time;

                // build connections from cohorts
                for (int i = 0; i < servers.size(); i++) {
                    final int pidIndex = i;
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {

//                              System.out.println(serverAdd[servers[pidIndex]] + ", " + serverPort[servers[pidIndex]]);
                                Socket socket = new Socket(serverAdd[servers.get(pidIndex)], serverPort[servers.get(pidIndex)]);
                                //int processId = servers[pidIndex] + 1;
                                int processId = servers.get(pidIndex);
//                                PrintStream coordinatorPrintStream = new PrintStream(socket.getOutputStream());
//                                coordinatorPrintStream.println(StringConstants.ROLE_COORDINATOR + StringConstants.SPACE +
//                                                                StringConstants.MESSAGE_REGISTER + StringConstants.SPACE +
//                                                                processId);
//                                coordinatorPrintStream.flush();

                                //the servers sent from current one
                                Set<Integer> otherServers = new HashSet<>();
                                for(int i=0;i<servers.size();i++){
                                    if(i!=pidIndex){
                                        otherServers.add(servers.get(i));
                                    }
                                }

                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                                CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                                    StringConstants.MESSAGE_REGISTER,
                                                                    processId,
                                                                    clientId,
                                                                    fileId,
                                                                    count,
                                                                    otherServers);
                                oos.writeObject(sent);
                                oos.flush();

//                                BufferedReader bufferReader = new BufferedReader(
//                                        new InputStreamReader(socket.getInputStream()));
//                                String inLine = bufferReader.readLine();

                                CSMessage received = (CSMessage)ois.readObject();
                                // If received message is AGREED[REGISTER]
                                if (received.getMessage().equals(StringConstants.MESSAGE_AGREED)) {

                                    if (pidIndex + 1 == servers.size()) { //TODO 所有伺服器都連接上後
//                                    CoordinatorClientHandler c = new CoordinatorClientHandler(variable, data);
//                                    c.start();
                                        data.setCommitMade(true);
                                    }

                                    new CoordinatorServerHandler(socket,
                                                                    servers.size(),
                                                                    ois,
                                                                    oos,
                                                                    processId,
                                                                    fileId,
                                                                    clientId,
                                                                    count,
                                                                    otherServers,
                                                                    data).start();
//                                  System.out.println("Server: "+serverPort[servers[pidIndex]]+" serversCommitStatus: "+data.isServersCommitted());
                                }


                                //Close //TODO 改變CSMessage
                                //stopConnection(coordinatorPrintStream, bufferReader, socket);

                            } catch (SocketTimeoutException | ClassNotFoundException e){
                                e.printStackTrace();
                                //System.out.println(serverAdd[servers[pidIndex]] + ", " + serverPort[servers[pidIndex]]);
                            } catch (IOException e) {
                                e.printStackTrace();

                                //impose let others channel works
                                data.setCommitMade(true);
                                data.incrementAgree();
                                data.incrementAck();
                                data.incrementCommitCompletedFromCohort();
                            }


                        }

                    }).start();

                }

                //used to validate all servers done commit
                boolean serversCommitStatus = false;
                while (!serversCommitStatus) {
                    serversCommitStatus = data.isServersCommitted();
                    //System.out.println("serversCommitStatus: "+serversCommitStatus);

                    if (serversCommitStatus) {
//                        System.out.println("all servers commit........");
                    }
                }

                Thread.sleep(5000);
            }

            /*

            stopConnection();
*/
        }catch(Exception e){
            e.printStackTrace();
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
