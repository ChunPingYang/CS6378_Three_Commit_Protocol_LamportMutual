package utility;

import model.StringConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
//TODO 改成ObjectInputStream
                System.out.println(serverAdd[id] + ", " + serverPort[id]);
                Socket socket = new Socket(serverAdd[id], serverPort[id]);
                socket.setSoTimeout(5000);
                int processId = id + 1;
                PrintStream selectorPrintStream = new PrintStream(socket.getOutputStream());
                selectorPrintStream.println(StringConstants.MESSAGE_REGISTER + StringConstants.SPACE + processId);
                selectorPrintStream.flush();

                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String inLine = null;
                while (((inLine = bufferReader.readLine()) != null) && (!(inLine.isEmpty()))) {

                    if (inLine.split(StringConstants.SPACE)[0]
                            .startsWith(StringConstants.MESSAGE_AGREED)) {
                        selectorPrintStream.println(StringConstants.MESSAGE_SHUTDOWN + StringConstants.SPACE);
                        selectorPrintStream.flush();
                    }

                }

                //Close Connection
                //stopConnection(selectorPrintStream,bufferReader,socket);
            } catch (SocketTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
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
