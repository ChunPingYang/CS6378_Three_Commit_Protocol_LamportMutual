package listener;

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

            //while(true) {
            //cohortSocket.setSoTimeout(10000);

            BufferedReader cohortBufferedReader =
                    new BufferedReader(
                            new InputStreamReader(cohortSocket.getInputStream()));
            PrintStream cohortPrintStream = new PrintStream(cohortSocket.getOutputStream());

            String inLine = null;
            inLine = cohortBufferedReader.readLine();
            if(inLine.split(StringConstants.SPACE)[1]
                        .startsWith(StringConstants.MESSAGE_REGISTER)) {
                // Register itself with the coordinator
                cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE
                        + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + serverPort[id] + StringConstants.SPACE + serverPort[id]);
                cohortPrintStream.flush();
            }


            while (((inLine = cohortBufferedReader.readLine()) != null) && (!(inLine.isEmpty()))) {
                System.out.println(inLine);


                // COMMIT REQ received
                if (inLine.split(StringConstants.SPACE)[1]
                        .startsWith(StringConstants.MESSAGE_COMMIT_REQUEST)) {

//                                System.out.println("Cohort " + pId + " received COMMIT_REQUEST from Coordinator");

                    //Handle request to be synchronized
                    cohort.request(inLine);
                    String clientId = inLine.split(StringConstants.SPACE)[3];
                    String fileId = inLine.split(StringConstants.SPACE)[4];
                    while (!data.isAgree(clientId, fileId)) {
                        System.out.println("waiting server.....");
                        Thread.sleep(500);
                    }
                    data.getAgreeMap().get(clientId).replace(fileId, false);

                    cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE + serverPort[id]);
                    cohortPrintStream.flush();

//                                System.out.println("Cohort " + pId + " sent AGREED to the Coordinator");
//                                System.out.println("Transition between the states for Cohort is : q" + pId
//                                        + " --> w" + pId);
                }

                // Prepare Message received
                if (inLine.split(StringConstants.SPACE)[1].equals(StringConstants.MESSAGE_PREPARE)
                        && !sentAck) {

//                                System.out.println("Cohort " + pId + " received PREPARE from the Coordinator");

                    cohortPrintStream.println(StringConstants.MESSAGE_ACK + StringConstants.SPACE + serverPort[id]);
                    cohortPrintStream.flush();
                    sentAck = true;

//                                System.out.println("Cohort " + pId + " sent ACK to the Coordinator");
                }

                // Commit Message received
                if (inLine.split(StringConstants.SPACE)[1]
                        .equals(StringConstants.MESSAGE_COMMIT)
                        && !isCommitted) {

                    String fileId = inLine.split(StringConstants.SPACE)[3];
                    String n_time = inLine.split(StringConstants.SPACE)[4];
                    String clientId = inLine.split(StringConstants.SPACE)[5];
                    outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server" + pId + "/file" + fileId);
                    fileAccessor.writeToOutputFile1(outputFile, "Client: " + clientId + " State: " + StringConstants.STATE_W + StringConstants.SPACE + "Server#: " + pId + StringConstants.SPACE + "File#: " + fileId + StringConstants.SPACE + n_time);

                    isCommitted = true;

                    cohortPrintStream.println(StringConstants.MESSAGE_COMMIT_COMPLETE + StringConstants.SPACE + serverPort[id]);
                    cohortPrintStream.flush();

//                                System.out.println("After COMMIT, transition between the states for Cohort is : p"
//                                                + pId + " --> c" + pId);
//
//                                System.out.println();
//                                System.out.println("...Cohort Terminates...");
//                                System.err.println();

                    break;
                }

                // Operation Read Message received
                if (inLine.split(StringConstants.SPACE)[1].equals(StringConstants.ACTION_READ)) {
                    String fileId = inLine.split(StringConstants.SPACE)[1];
                    outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server" + pId + "/file" + fileId);
                    if (outputFile.exists()) {
                        boolean isExist = fileAccessor.readFileAndValidateExist(outputFile);
                        if (!isExist) {
                            cohortPrintStream.println(StringConstants.MESSAGE_FILE_NOT_EXIST + StringConstants.SPACE);
                            cohortPrintStream.flush();
                        }
                    } else {
                            cohortPrintStream.println(StringConstants.MESSAGE_FILE_NOT_EXIST + StringConstants.SPACE);
                            cohortPrintStream.flush();
                    }

                    break;

                }

                if (inLine.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_SHUTDOWN)) {
//                        cohortSocket.shutdownInput();
//                        cohortSocket.shutdownOutput();
//                        cohortSocket.close();
                    cohortListener.close();
                }


                // Abort Message received
                if (inLine.split(StringConstants.SPACE)[1].equals(StringConstants.MESSAGE_ABORT)
                        && !isAborted) {

                }

            }


        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            System.out.println("Socket status: " + cohortSocket.isClosed());
            System.out.println("Inputstream status: " + cohortSocket.isInputShutdown());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
