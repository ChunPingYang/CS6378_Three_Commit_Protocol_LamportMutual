package model;

import java.io.Serializable;
import java.util.HashSet;

public class Message implements Serializable,Comparable<Message> {

    enum MessageType{
        REQUEST, REPLY
    };
    private final int clock;
    private final String from;
    private final String to;
    private final String type;
    private final String fileId;
    private final String seqNum;
    //private final String otherServers;
    private final String clientId;
    private final String role;
    private HashSet<Integer> neighbors;

//    public Message(int clock, int from , int to, String type, String fileName, String content){
//        this.clock = clock;
//        this.from = from;
//        this.to=to;
//        this.type = type;
//        this.fileName = fileName;
//        this.content = content;
//    }

    public Message(int clock, String from, String to, String type, String clientId, String fileId, String seqNum, String role,HashSet<Integer> neighbors){
        this.clock = clock;
        this.clientId = clientId;
        this.from = from;
        this.to = to;
        this.type = type;
        this.fileId = fileId;
        this.seqNum = seqNum;
        this.role = role;
        this.neighbors = new HashSet<>(neighbors);
    }

    public int getClock(){
        return this.clock;
    }

    public String getFrom() { return this.from; }

    public String getTo() { return this.to; }

    public String getType() { return this.type; }

    public String getClientId() { return this.clientId;}

    public String getFileId() { return fileId; }

    public String getSeqNum() { return seqNum; }

    public String getRole() { return role; }

    public HashSet<Integer> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(HashSet<Integer> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public int compareTo(Message o) {
        if(this.clock <o.getClock()){
            return -1;
        }
        else if (this.clock>o.getClock()){
            return 1;
        }
        else{
            return Integer.parseInt(this.getClientId()) - Integer.parseInt(o.getClientId());
        }
    }
//    @Override
//    public String toString(){
//        return clock + "," + from  + "," + to + "," + type + "," + fileName;
//    }
}
