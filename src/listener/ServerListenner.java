package listener;

import model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ServerListenner implements Runnable{
    private Cohort cohort;
    private ObjectInputStream ois;
    public ServerListenner(Cohort cohort, ObjectInputStream ois){
        this.cohort = cohort;
        this.ois = ois;

    }

    @Override
    public void run() {
        try {
            // can not new a ObjectInputStream here, one socket can only been initialized with one inputStream

            System.out.println("serverListener "+cohort.getId()+" starts listening");
            while(true){
                Message received = (Message)ois.readObject();
                System.out.println("Server receives message from server"+ (Integer.parseInt(received.getFrom()))+
                                    " Sequence number: "+received.getSeqNum()+
                                    " Client: "+received.getClientId());
                cohort.processMessage(received);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
