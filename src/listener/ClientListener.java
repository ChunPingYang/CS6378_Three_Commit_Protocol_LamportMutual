package listener;

import model.CSMessage;
import model.StringConstants;
import utility.FileAccessor;
import utility.SharedDataAmongCohortCoordThreads;

import java.io.*;
import java.net.*;

public class ClientListener implements Runnable{

    private Socket cohortSocket;
    private ServerSocket cohortListener;

    private Cohort cohort;
    private SharedDataAmongCohortCoordThreads data;

    //Transition State
    private boolean isCommitted;
    private boolean isAborted;
    private boolean sentAck;

    private FileAccessor fileAccessor;
    private File outputFile;

    private int[] serverPort;
    private int id; // process id index
    private int pId; // process id


    public ClientListener(ServerSocket cohortListener, Cohort cohort, Socket cohortSocket, int id, int[] serverPort, FileAccessor fileAccessor, SharedDataAmongCohortCoordThreads data){
        this.cohort = cohort;
        this.cohortListener = cohortListener;
        this.cohortSocket = cohortSocket;
        this.isAborted = false;
        this.isCommitted = false;
        this.sentAck = false;
        this.fileAccessor = fileAccessor;
        this.serverPort = serverPort;
        this.id = id;
        this.pId = id+1;
        this.data = data;
    }

    @Override
    public void run() {

        try {

            ObjectOutputStream oos = new ObjectOutputStream(cohortSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(cohortSocket.getInputStream());

            CSMessage delivered = (CSMessage)ois.readObject();
            if(delivered.getMessage().equals(StringConstants.MESSAGE_REGISTER))
            {
                CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                StringConstants.MESSAGE_AGREED,
                                                delivered.getProcessId(),
                                                delivered.getClientId(),
                                                delivered.getFileId(),
                                                delivered.getN_time(),
                                                delivered.getOtherServers());
                oos.writeObject(sent);
                oos.flush();
            }

            CSMessage received = null;
            while ((received = (CSMessage)ois.readObject()) != null) {
                System.out.println(received.toString());


                // COMMIT REQ received
                if (received.getMessage().equals(StringConstants.MESSAGE_COMMIT_REQUEST)) {

//                  System.out.println("Cohort " + pId + " received COMMIT_REQUEST from Coordinator");

                    //Handle request to be synchronized
                    cohort.request(received);
                    String clientId = String.valueOf(received.getClientId());
                    String fileId = String.valueOf(received.getFileId());
                    boolean x = data.isAgree(clientId, fileId);
                    boolean y = data.isChannelDisabled();
                    while (!x && !y) {
                        System.out.println("waiting server.....");
                        Thread.sleep(500);
                        x = data.isAgree(clientId, fileId);
                        y = data.isChannelDisabled();
                    }
                    if(x && !y){data.getAgreeMap().get(clientId).replace(fileId, false);}
                    if(y){
                        data.setChannelDisabled(false);
                        String file = String.valueOf(received.getFileId());
                        //cohort.getMutexes().get(file).getMessageQueue().clear();
                    }

                        CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                StringConstants.MESSAGE_AGREED,
                                                received.getProcessId(),
                                                received.getClientId(),
                                                received.getFileId(),
                                                received.getN_time(),
                                                received.getOtherServers());
                        oos.writeObject(sent);
                        oos.flush();

//                  System.out.println("Cohort " + pId + " sent AGREED to the Coordinator");
//                  System.out.println("Transition between the states for Cohort is : q" + pId
//                                        + " --> w" + pId);
                }

                // Prepare Message received
                if (received.getMessage().equals(StringConstants.MESSAGE_PREPARE)
                        && !sentAck)
                {

//                  System.out.println("Cohort " + pId + " received PREPARE from the Coordinator");

                    sentAck = true;

                    CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                    StringConstants.MESSAGE_ACK,
                                                    received.getProcessId(),
                                                    received.getClientId(),
                                                    received.getFileId(),
                                                    received.getN_time(),
                                                    received.getOtherServers());
                    oos.writeObject(sent);
                    oos.flush();

//                  System.out.println("Cohort " + pId + " sent ACK to the Coordinator");
                }

                // Commit Message received
                if (received.getMessage().equals(StringConstants.MESSAGE_COMMIT)
                        && !isCommitted)
                {

                    int fileId = received.getFileId();
                    int n_time = received.getN_time();
                    int clientId = received.getClientId();

                    outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server" + pId + "/file" + fileId);
                    fileAccessor.writeToOutputFile1(outputFile, "Client: " + clientId +
                            " State: " + StringConstants.STATE_W + StringConstants.SPACE +
                            "Server#: " + pId + StringConstants.SPACE +
                            "File#: " + fileId + StringConstants.SPACE +
                            "Sequence#: " + n_time);

                    isCommitted = true;

                    CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                    StringConstants.MESSAGE_COMMIT_COMPLETE,
                                                    received.getProcessId(),
                                                    received.getClientId(),
                                                    received.getFileId(),
                                                    received.getN_time(),
                                                    received.getOtherServers());
                    oos.writeObject(sent);
                    oos.flush();

//                  System.out.println("After COMMIT, transition between the states for Cohort is : p"
//                                                + pId + " --> c" + pId);
//
//                  System.out.println();
//                  System.out.println("...Cohort Terminates...");
//                  System.err.println();

                }

                // Operation Read Message received
                if (received.getMessage().equals(StringConstants.ACTION_READ)) {
                    int fileId = received.getFileId();
                    outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server" + pId + "/file" + fileId);
                    if (outputFile.exists()) {
                        boolean isExist = fileAccessor.readFileAndValidateExist(outputFile);
                        if (!isExist) {

                            CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                            StringConstants.MESSAGE_FILE_NOT_EXIST,
                                                            received.getProcessId(),
                                                            received.getClientId(),
                                                            received.getFileId(),
                                                            received.getN_time(),
                                                            received.getOtherServers());
                            oos.writeObject(sent);
                            oos.flush();
                        }
                    } else {

                            CSMessage sent = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                            StringConstants.MESSAGE_FILE_NOT_EXIST,
                                                            received.getProcessId(),
                                                            received.getClientId(),
                                                            received.getFileId(),
                                                            received.getN_time(),
                                                            received.getOtherServers());
                            oos.writeObject(sent);
                            oos.flush();
                    }

                    //break;
                }

                if (received.getMessage().equals(StringConstants.MESSAGE_SHUTDOWN)) {
                    System.out.println("shut down.....");
                    cohortListener.close();
                }


                // Abort Message received
                if (received.getMessage().equals(StringConstants.MESSAGE_ABORT)
                        && !isAborted) {

                }

            }


        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            System.out.println("Socket status: " + cohortSocket.isClosed());
            System.out.println("Inputstream status: " + cohortSocket.isInputShutdown());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
