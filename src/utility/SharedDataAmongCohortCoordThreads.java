package utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SharedDataAmongCohortCoordThreads {

    /**
     * A class that contains various updated values that can be used by one server and client threads
     *
     */
    private Map<String, Map<String,Boolean>> agreeMap;
    private volatile boolean channelDisabled = false;

    public SharedDataAmongCohortCoordThreads(int maxCoordinator){
        String[] fileList = new String[]{"1","2","3","4"};
        agreeMap = Collections.synchronizedMap(new HashMap<>());
        for(int i=0;i<maxCoordinator;i++){
            Map<String,Boolean> fileMap = Collections.synchronizedMap(new HashMap<>());
            for(String fileId:fileList){
                fileMap.put(fileId,false);
            }
            String clientId = String.valueOf((i+1));
            agreeMap.put(clientId,fileMap);
        }
    }

    public Map<String, Map<String, Boolean>> getAgreeMap() {
        return agreeMap;
    }

    public boolean isAgree(String clientId,String fileId){
        return agreeMap.get(clientId).get(fileId);
    }

    public synchronized boolean isChannelDisabled() {
        return channelDisabled;
    }

    public synchronized void setChannelDisabled(boolean channelDisabled) {
        this.channelDisabled = channelDisabled;
    }
}
