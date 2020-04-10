package listener;

import model.StringConstants;
import utility.FileAccessor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

class ClientThread extends Thread{

    private Socket cohortSocket;
    private boolean isCommitted;
    private boolean isAborted;
    private boolean sentAck;

    private FileAccessor fileAccessor;
    private File outputFile;

    private int[] serverPort;
    private int id;
    private int pId;

    public ClientThread(Socket cohortSocket,int id,int[] serverPort, FileAccessor fileAccessor){
        this.cohortSocket = cohortSocket;
        this.isAborted = false;
        this.isCommitted = false;
        this.sentAck = false;
        this.fileAccessor = fileAccessor;
        this.serverPort = serverPort;
        this.id = id;
        this.pId = id+1;
    }

    @Override
    public void run() {

        try {

            BufferedReader cohortBufferedReader =
                    new BufferedReader(
                            new InputStreamReader(cohortSocket.getInputStream()));
            PrintStream cohortPrintStream = new PrintStream(cohortSocket.getOutputStream());

            String inline = null;
            inline = cohortBufferedReader.readLine();
            if(inline.startsWith(StringConstants.MESSAGE_REGISTER)) {
                // Register itself with the coordinator
                cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE
                        + InetAddress.getLocalHost().getHostName() + StringConstants.SPACE + serverPort[id] + StringConstants.SPACE + serverPort[id]);
                cohortPrintStream.flush();
            }


            while (((inline = cohortBufferedReader.readLine()) != null) && (!(inline.isEmpty()))) {
//                              System.out.println(inline);

                // COMMIT REQ received
                if (inline.split(StringConstants.SPACE)[0]
                        .startsWith(StringConstants.MESSAGE_COMMIT_REQUEST)) {

//                                System.out.println("Cohort " + pId + " received COMMIT_REQUEST from Coordinator");

                    cohortPrintStream.println(StringConstants.MESSAGE_AGREED + StringConstants.SPACE+ serverPort[id]);
                    cohortPrintStream.flush();

//                                System.out.println("Cohort " + pId + " sent AGREED to the Coordinator");
//                                System.out.println("Transition between the states for Cohort is : q" + pId
//                                        + " --> w" + pId);
                }

                // Prepare Message received
                if (inline.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_PREPARE)
                        && !sentAck){

//                                System.out.println("Cohort " + pId + " received PREPARE from the Coordinator");

                    cohortPrintStream.println(StringConstants.MESSAGE_ACK + StringConstants.SPACE + serverPort[id]);
                    cohortPrintStream.flush();
                    sentAck = true;

//                                System.out.println("Cohort " + pId + " sent ACK to the Coordinator");
                }

                // Commit Message received
                if (inline.split(StringConstants.SPACE)[0]
                        .equals(StringConstants.MESSAGE_COMMIT)
                            && !isCommitted) {

                    String fileId = inline.split(StringConstants.SPACE)[2];
                    String n_time = inline.split(StringConstants.SPACE)[3];
                    String clientId = inline.split(StringConstants.SPACE)[4];
                    outputFile = new File(System.getProperty("user.dir") + "/src/resources/Server"+pId+"/file"+fileId);
                    fileAccessor.writeToOutputFile1(outputFile,"Client: " + clientId + " State: " + StringConstants.STATE_W + StringConstants.SPACE + "Server#: " + pId + StringConstants.SPACE + "File#: " + fileId + StringConstants.SPACE + n_time);

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

                // Abort Message received
                if (inline.split(StringConstants.SPACE)[0].equals(StringConstants.MESSAGE_ABORT)
                        && !isAborted) {

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
