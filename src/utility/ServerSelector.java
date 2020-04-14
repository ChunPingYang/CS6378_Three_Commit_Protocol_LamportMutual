package utility;

import model.CSMessage;
import model.Message;
import model.StringConstants;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;

public class ServerSelector {

    /**
     * Servers configuration
     */
    private String[] serverAdd;
    private int[] serverPort;

    public void readServerConfig(String[] adds, int[] ports){
        this.serverAdd = adds;
        this.serverPort = ports;
    }

    public void select(int id){

        //while(true) {

            try {

                System.out.println(serverAdd[id] + ", " + serverPort[id]);
                Socket socket = new Socket(serverAdd[id], serverPort[id]);
                socket.setSoTimeout(5000);
                int processId = id;
//                PrintStream selectorPrintStream = new PrintStream(socket.getOutputStream());
//                selectorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
//                selectorPrintStream.flush();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                CSMessage push = new CSMessage(StringConstants.ROLE_COORDINATOR,
                                                StringConstants.MESSAGE_REGISTER,
                                                processId,
                                                0,
                                                0,
                                                0,
                                                new HashSet<>());
                oos.writeObject(push);
                oos.flush();

//                BufferedReader bufferReader = new BufferedReader(
//                        new InputStreamReader(socket.getInputStream()));

                CSMessage received = null;
                while ((received = (CSMessage)ois.readObject()) != null) {

                    if (received.getMessage().equals(StringConstants.MESSAGE_AGREED)) {
//                        selectorPrintStream.println(StringConstants.MESSAGE_SHUTDOWN + StringConstants.SPACE);
//                        selectorPrintStream.flush();
                        CSMessage sent = new CSMessage(StringConstants.ROLE_SHUTDOWNER,
                                                        StringConstants.MESSAGE_SHUTDOWN,
                                                        processId, 0, 0, 0, new HashSet<>());
                        oos.writeObject(sent);
                        oos.flush();
                    }

                }

                //Close Connection
                //stopConnection(selectorPrintStream,bufferReader,socket);
            } catch (SocketTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }

        //}
    }

    public void stopConnection(PrintStream selectorPrintStream,BufferedReader bufferReader,Socket socket){
        try {
            selectorPrintStream.close();
            bufferReader.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
