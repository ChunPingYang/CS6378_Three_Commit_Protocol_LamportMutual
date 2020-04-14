package listener;

import model.*;
import utility.FileAccessor;
import utility.SharedDataAmongCohortCoordThreads;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

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
    private ServerSocket cohortListener = null;

    /**
     * Variables to store information from the Config file
     */
    private int maxCohort;
    private String coordinatorHostName;
    private int maxCoordinator;

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
    private SharedDataAmongCohortCoordThreads data;

    private Map<String,LamportClock> clocks;
    private Map<String, LamportMutex> mutexes;
    private Map<Integer,Socket> incomingNeibs;
    private Map<Integer,Socket> outcomingNeibs;
    private Map<Integer,ObjectInputStream> incomingChannels;
    private Map<Integer,ObjectOutputStream> outcomingChannels;
    private Set<Integer> neighbors;
    private static int numNeighbors;

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

    public synchronized Map<String, LamportClock> getClocks() {
        return clocks;
    }

    public synchronized int getId() {
        return pId;
    }

    public void readServerConfig(String[] adds, int[] ports){
        this.serverAdd = adds;
        this.serverPort = ports;
    }

    /**
     * Default initialize the variables
     */
    public void initCohort(int id){
        fileAccessor = new FileAccessor();
        isAborted = false;
        isCommitted = false;
        sentAck = false;
        recoveryDone = false;
        startComputation = false;
        recoveryCheckDone = false;

        lastvalue = 0;
        transactionId = 0;

        pId = id;

        data = new SharedDataAmongCohortCoordThreads(maxCoordinator);
    }

    public void initServerToServer(int[] ids, int id){
        //numNeighbors = ids.length-1;
        incomingNeibs = Collections.synchronizedMap(new HashMap<>());
        outcomingNeibs =  Collections.synchronizedMap(new HashMap<>());
        incomingChannels = Collections.synchronizedMap(new HashMap<>());
        outcomingChannels = Collections.synchronizedMap(new HashMap<>());
        neighbors = Collections.synchronizedSet(new HashSet<>());
        clocks = Collections.synchronizedMap(new HashMap<>());
        mutexes =  Collections.synchronizedMap(new HashMap<>());

        //TODO file list hard code
        String[] a = new String[]{"1","2","3","4"};
        List<String> fileList = Arrays.asList(a);
        for(String fileId:fileList){
            clocks.put(fileId,new LamportClock());
            mutexes.put(fileId,new LamportMutex(this));
        }

        try {
            cohortListener = new ServerSocket(serverPort[id]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0;i<ids.length;i++){
            if(ids[i]==id) continue;
            else{
                final int other = i;

                //Connect to other servers
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isConnected = false;
                        while(!isConnected && !data.isChannelDisabled()){
                            try {
                                // connect to other servers
                                Socket socket = new Socket(serverAdd[other], serverPort[other]);
                                outcomingNeibs.put(ids[other], socket);
                                outcomingChannels.put(ids[other], new ObjectOutputStream(socket.getOutputStream()));
                                isConnected = true;
                                System.out.println("connect to " + other);
                            } catch (IOException e) {
                                try{
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                System.out.println("waiting for other servers to start");
                                isConnected = false;
                            }
                        }
                    }
                }).start();
            }
        }

        for(int i=0;i<ids.length;i++){
            try {
                if (ids[i] != id) {
                    Socket socket = cohortListener.accept();
                    incomingNeibs.put(ids[i], socket);
                    incomingChannels.put(ids[i],new ObjectInputStream(socket.getInputStream()));
                    neighbors.add(ids[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //start serverListener, lister to neibors's message(Lamport Message)
        for(int i=0;i<ids.length;i++){
            if(ids[i]!=pId)
                new Thread(new ServerListenner(this,incomingChannels.get(ids[i]))).start();
        }

        System.out.println("get Incoming neibors ready!");
        System.out.println("server connections ready!");

    }

    public synchronized void initAfterChannelDisabled(){
        data = new SharedDataAmongCohortCoordThreads(maxCoordinator);
        incomingNeibs = Collections.synchronizedMap(new HashMap<>());
        outcomingNeibs =  Collections.synchronizedMap(new HashMap<>());
        incomingChannels = Collections.synchronizedMap(new HashMap<>());
        outcomingChannels = Collections.synchronizedMap(new HashMap<>());
        neighbors = Collections.synchronizedSet(new HashSet<>());
        clocks = Collections.synchronizedMap(new HashMap<>());
        mutexes =  Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * A method that would be executed by the thread
     */
    public void start(int currentServerId) {

        try {

            //FileAccessor fileAccessor = new FileAccessor();

            while(true) {
                Socket cohortSocket = cohortListener.accept();
                System.out.println("Server: "+ InetAddress.getLocalHost().getHostName()+", Port: "+serverPort[currentServerId]);
                new Thread((new ClientListener(cohortListener,this,cohortSocket,currentServerId,serverPort,fileAccessor,data))).start();
            }

        }catch(IOException e){
            System.out.println(" ");
            System.out.println(e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
        }

//        try{
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
//
//        try {
//            cohortListener = new ServerSocket(serverPort[currentServerId]);
//            start(currentServerId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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

    // when ClientListener receives a message from a client, first try to check if the critical section of giving file is available
    // if it is empty, do request and broadcast
    public synchronized void request(CSMessage message) throws InterruptedException, IOException {
        System.err.println("request: "+message.toString());
        String processId = String.valueOf(message.getProcessId());
        String clientId = String.valueOf(message.getClientId());
        String fileId = String.valueOf(message.getFileId());
        String seqNum = String.valueOf(message.getN_time());
        Set<Integer> otherServers = message.getOtherServers();

        LamportClock clock = clocks.get(fileId);
        clock.increment();
        LamportMutex mutex = mutexes.get(fileId);
        while(!mutex.isAvailable()){
            System.err.println("Last request not finished......");
            Thread.sleep(1000);
        }

//        HashSet<Integer> otherServers = new HashSet<>();
//        otherServers.add(Integer.parseInt(othersCohorts.split(":")[0]));
//        otherServers.add(Integer.parseInt(othersCohorts.split(":")[1]));

        Message request = new Message(clock.getClock(),
                                        processId,
                                        processId,
                                        StringConstants.LAMPORT_REQUEST,
                                        clientId,
                                        fileId,
                                        seqNum,
                                        StringConstants.ROLE_COORDINATOR,
                                        otherServers);
        mutex.makeRequest(request);
    }

    // broadcast for request
    // for request, it may wait for a time to be broadcast(last operation not finished), so the clock time should be the time of message
    // but not the current clock
    public synchronized void broadcast(Message message) throws IOException {
        Set<Integer> otherServers = message.getNeighbors();
        for(int neib:neighbors){
            if(otherServers.contains(neib))
            {
                Message toSend = new Message(message.getClock(),
                                                String.valueOf(this.getId()),
                                                String.valueOf(neib),
                                                message.getType(),
                                                message.getClientId(),
                                                message.getFileId(),
                                                message.getSeqNum(),
                                                StringConstants.ROLE_COHORT,
                                                otherServers);

                outcomingChannels.get(neib).writeObject(toSend);
                outcomingChannels.get(neib).flush();
            }
        }
    }

    // wrapper method for different types of message
    // server process messages according to its type
    public synchronized void processMessage(Message received) throws IOException,InterruptedException {
        String fileId = received.getFileId();
        LamportClock clock = this.clocks.get(fileId);
        clock.msgEvent(received);
        String type = received.getType();
        LamportMutex mutex = mutexes.get(fileId);

        switch (type) {
            case StringConstants.LAMPORT_REQUEST:
                Message reply = mutex.getRequest(received);
                sendReply(reply);
                break;
            case StringConstants.LAMPORT_REPLY:
                mutex.getReply(received,data);
                break;
            default:
                System.err.println("not correct type!");
                break;
        }


        // after receive reply message, server might go to critical section
        if(mutex.canEnterCriticalSection()){
            mutex.release(received,data); //release resource, move to the next request
        }
    }

    public synchronized void sendReply(Message reply){
        int outNeib = Integer.parseInt(reply.getTo());
        try {
            outcomingChannels.get(outNeib).writeObject(reply);
            outcomingChannels.get(outNeib).flush();
        } catch (IOException e) {
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

    public void setMaxCoordinator(int maxCoordinator){
        this.maxCoordinator = maxCoordinator;
    }
}
