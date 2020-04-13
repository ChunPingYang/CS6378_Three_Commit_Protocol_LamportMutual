package model;

import listener.Cohort;
import utility.SharedDataAmongCohortCoordThreads;

import java.io.IOException;
import java.util.*;

public class LamportMutex {

    // once a request is sent, do not send request until the last request is done
    private boolean sendRequest = false;
    private volatile List<Message> messageQueue;
    // a set to store replies needed from neighbors to enter critical section
    private Set<Integer> pendingReplies;
    // a set to store requests needed from neighbors to enter critical section
    private Set<Integer> pendingRequests;
    private Cohort cohort;

    public LamportMutex(Cohort cohort){
        this.cohort = cohort;
        messageQueue = Collections.synchronizedList(new ArrayList<Message>(){
            public synchronized boolean add(Message message) {
                boolean ret = super.add(message);
                Collections.sort(messageQueue);
                return ret;
            }
        });
    }

    public synchronized Message headMessage(){ return messageQueue.get(0); }

    public synchronized void makeRequest(Message request) throws IOException {
        if(!messageQueue.isEmpty() || sendRequest) {
            System.err.println("last request not finished");
            return;
        }
        messageQueue.add(request);

        pendingReplies = Collections.synchronizedSet(request.getNeighbors());
        pendingRequests = Collections.synchronizedSet(request.getNeighbors());
        cohort.broadcast(request);
        sendRequest = true;
    }

    public synchronized void release(Message message,SharedDataAmongCohortCoordThreads data) throws IOException {
        String clientId = message.getClientId();
        String fileId = message.getFileId();
        Map<String,Map<String,Boolean>> agreeMap = data.getAgreeMap();
        agreeMap.get(clientId).replace(fileId,true); //can deliver "agree" message to coordinator

        messageQueue.remove(0);
        sendRequest = false;
    }

    public synchronized boolean isAvailable(){
        return !sendRequest;
    }

    public synchronized Message getRequest(Message request) throws InterruptedException{
        if(!request.getType().equals(StringConstants.LAMPORT_REQUEST)){
            System.err.println("it's not a request!");
            return null;
        }

        while(messageQueue.isEmpty()){
            System.err.println("Message Queue is Empty");
            Thread.sleep(500);
        }

        while(!headMessage().getRole().equals(StringConstants.ROLE_COORDINATOR)){
            System.err.println("Message is not from coordinator");
            Thread.sleep(500);
        }

        Message head = headMessage();
        while(!(head.getClientId().equals(request.getClientId())
                && head.getFileId().equals(request.getFileId())
                && head.getSeqNum().equals(request.getSeqNum())))
        {
            System.err.println("the head message is not the same as request");
            Thread.sleep(500);
        }

        pendingRequests.remove(Integer.parseInt(request.getFrom()));

        Message reply = new Message(cohort.getClocks().get(request.getFileId()).getClock(),
                request.getTo(),
                request.getFrom(),
                StringConstants.LAMPORT_REPLY,
                request.getClientId(),
                request.getFileId(),
                request.getSeqNum(),
                StringConstants.ROLE_COHORT,
                request.getNeighbors());

        return reply;
    }

    public synchronized void getReply(Message reply, SharedDataAmongCohortCoordThreads data){
        if(!reply.getType().equals(StringConstants.LAMPORT_REPLY)){
            System.err.println("it's not a reply!");
            return ;
        }
        pendingReplies.remove(Integer.parseInt(reply.getFrom()));

//        if(pendingReplies.isEmpty())
//        {
//            //messageQueue.remove(0);
//            //sendRequest = false;
//            String clientId = reply.getClientId();
//            String fileId = reply.getFileId();
//            Map<String,Map<String,Boolean>> agreeMap = data.getAgreeMap();
//            agreeMap.get(clientId).replace(fileId,true); //can deliver "agree" message to coordinator
//        }
    }

    public synchronized boolean canEnterCriticalSection(){
        String pId = String.valueOf(cohort.getId());
        if(sendRequest && pendingReplies.isEmpty() && pendingRequests.isEmpty() && (!messageQueue.isEmpty() && messageQueue.get(0).getFrom().equals(pId))){
            return true;
        }
        return false;
    }


}
