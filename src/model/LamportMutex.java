package model;

import listener.Cohort;
import utility.SharedDataAmongCohortCoordThreads;

import java.io.IOException;
import java.util.*;

public class LamportMutex {

    // once a request is sent, do not send request until the last request is done
    private boolean sendRequest = false;
    private List<Message> messageQueue;
    // a set to store replies needed from neighbors to enter critical section
    private Set<Integer> pendingReplies;
    // a set to store requests needed from neighbors to enter critical section
    //private Set<Integer> pendingRequests;
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

    public Message headMessage(){ return messageQueue.get(0); }

    //public synchronized List<Message> getMessageQueue(){ return messageQueue;}

    public synchronized void makeRequest(Message request) throws IOException {
        if(!messageQueue.isEmpty() || sendRequest) {
            System.err.println("last request not finished");
            return;
        }
//        synchronized (messageQueue) {
            messageQueue.add(request);
//        }
        //System.err.println("after adding to queue request: "+messageQueue.size());

        Set<Integer> set = new HashSet<>(request.getNeighbors());
        pendingReplies = Collections.synchronizedSet(set);
        //pendingRequests = Collections.synchronizedSet(request.getNeighbors());
        cohort.broadcast(request);
        sendRequest = true;
    }

    public synchronized void release(int clock,SharedDataAmongCohortCoordThreads data) throws IOException {
//        String clientId = message.getClientId();
//        String fileId = message.getFileId();

        Message toRelease = messageQueue.remove(0);
        sendRequest = false;

        Map<String,Map<String,Boolean>> agreeMap = data.getAgreeMap();
        agreeMap.get(toRelease.getClientId()).replace(toRelease.getFileId(),true); //can deliver "agree" message to coordinator

        //TODO 這邊有點奇怪
        Message toSend = new Message(clock,
                                    String.valueOf(cohort.getId()),
                                    toRelease.getTo(),
                                    StringConstants.LAMPORT_RELEASE,
                                    toRelease.getClientId(),
                                    toRelease.getFileId(),
                                    toRelease.getSeqNum(),
                                    StringConstants.ROLE_COHORT,
                                    toRelease.getNeighbors());

        cohort.broadcast(toSend);
    }

    public synchronized boolean isAvailable(){
        return !sendRequest;
    }

    public Message getRequest(Message request) throws InterruptedException{
        if(!request.getType().equals(StringConstants.LAMPORT_REQUEST)){
            System.err.println("it's not a request!");
            return null;
        }

//        boolean x;
////        synchronized(messageQueue){
//            x = messageQueue.isEmpty();
////        }
//        while(x){
//            System.err.println("Message Queue is Empty");
//            Thread.sleep(1000);
////            synchronized(messageQueue){
////                x = messageQueue.isEmpty();
////            }
//            x = messageQueue.isEmpty();
//        }
//
//        while(!headMessage().getRole().equals(StringConstants.ROLE_COORDINATOR)){
//            System.err.println("Message is not from coordinator");
//            Thread.sleep(500);
//        }
//
//        Message head = headMessage();
//        boolean y = head.getClientId().equals(request.getClientId())
//                        && head.getFileId().equals(request.getFileId())
//                        && head.getSeqNum().equals(request.getSeqNum());
//        while(!y)
//        {
//            System.err.println("the head message is not the same as request");
//            Thread.sleep(1000);
//            y = head.getClientId().equals(request.getClientId())
//                    && head.getFileId().equals(request.getFileId())
//                    && head.getSeqNum().equals(request.getSeqNum());
//        }
//
//        pendingRequests.remove(Integer.parseInt(request.getFrom()));

        messageQueue.add(request);

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

    }

    public synchronized void getRelease(Message release) throws IOException {
        if (release.getType().equals(StringConstants.LAMPORT_RELEASE)){
            // if receives a release message, should also write to file
            cohort.append_to_file(release.getFileId(),release.getClientId(),release.getSeqNum());
            Message top = messageQueue.remove(0);
            if(!top.getFrom().equals(release.getFrom())) {
                System.err.println("message not equal!");
            }
        }
        else{
            System.err.println("it's not a release!");
        }
    }

    public synchronized boolean canEnterCriticalSection(){
        String pId = String.valueOf(cohort.getId());
        if(sendRequest && pendingReplies.isEmpty() && (!messageQueue.isEmpty() && messageQueue.get(0).getFrom().equals(pId))){
            return true;
        }
        return false;
    }


}
