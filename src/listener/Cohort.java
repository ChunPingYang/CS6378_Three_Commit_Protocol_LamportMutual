package listener;

import model.LamportClock;
import model.Message;
import utility.FileAccessor;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
    private Map<String,LamportClock> clocks;
    private Map<String, Request> requests;

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

    public void initServerToServer(int[] ids, int currentServerId){
        //numNeighbors = ids.length-1;
        //incomingNeibs = Collections.synchronizedMap(new HashMap<>());
        //outcomingNeibs =  Collections.synchronizedMap(new HashMap<>());
        //incomingChannels = Collections.synchronizedMap(new HashMap<>());
        //outcomingChannels = Collections.synchronizedMap(new HashMap<>());
        //neighbors = Collections.synchronizedSet(new HashSet<>());
        clocks = Collections.synchronizedMap(new HashMap<>());
        requests =  Collections.synchronizedMap(new HashMap<>());

        //TODO file list hard code
        String[] a = new String[]{"file1","file2","file3","file4"};
        List<String> fileList = Arrays.asList(a);
        for(String file:fileList){
            clocks.put(file,new LamportClock());
            requests.put(file,new Request());
        }

        try {
            cohortListener = new ServerSocket(serverPort[currentServerId]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0;i<ids.length;i++){
            if(ids[i]==currentServerId) continue;
            else{
                final int other = i;

                //Connect to other servers
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isConnected = false;
                        while(!isConnected){
                            try {
                                // connect to other servers
                                Socket socket = new Socket(serverAdd[other],serverPort[other]);
                                //outcomingNeibs.put(ids[other],socket);
                                //outcomingChannels.put(ids[other],new ObjectOutputStream(socket.getOutputStream()));
                                isConnected = true;
                                System.out.println("connect to "+other);
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
                if (ids[i] != currentServerId) {
                    Socket socket = cohortListener.accept();
                    //incomingNeibs.put(ids[i], socket);
                    //incomingChannels.put(ids[i],new ObjectInputStream(socket.getInputStream()));
                    //neighbors.add(ids[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("get Incoming neibors ready!");
        System.out.println("server connections ready!");

    }

    /**
     * A method that would be executed by the thread
     */
    public void start(int id) {

        try {

            FileAccessor fileAccessor = new FileAccessor();

            while(true) {
                Socket cohortSocket = cohortListener.accept();
                System.out.println("Server: "+ InetAddress.getLocalHost().getHostName()+", Port: "+serverPort[id]);
                new Thread((new ClientThread(cohortSocket,id,serverPort,fileAccessor))).start();
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

class Request{

    // once a request is sent, do not send request until the last request is done
    private boolean sendRequest = false;
    private List<Message> messageQueue;

    public Request(){
        messageQueue = Collections.synchronizedList(new ArrayList<Message>(){
            public synchronized boolean add(Message message) {
                boolean ret = super.add(message);
                Collections.sort(messageQueue);
                return ret;
            }
        });
    }

    public Message headMessage(){
        return messageQueue.get(0);
    }

    public synchronized void makeRequest(Message request) throws IOException {
        if(!messageQueue.isEmpty() || sendRequest) {
            System.err.println("last request not finished");
            return;
        }
        messageQueue.add(request);
        sendRequest = true;
    }

    public synchronized void release() throws IOException {
        Message top = messageQueue.remove(0);
    }

    public synchronized boolean isAvailable(){
        return !sendRequest;
    }
}