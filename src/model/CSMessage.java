package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CSMessage implements Serializable {

    private String role;
    private String message;
    private int processId;
    private int clientId;
    private int fileId;
    private int n_time;
    private Set<Integer> otherServers;

    public CSMessage(String role, String message, int processId, int clientId, int fileId, int n_time, Set<Integer> otherServers) {
        this.role = role;
        this.message = message;
        this.processId = processId;
        this.clientId = clientId;
        this.fileId = fileId;
        this.n_time = n_time;
        this.otherServers = otherServers;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getN_time() {
        return n_time;
    }

    public void setN_time(int n_time) {
        this.n_time = n_time;
    }

    public Set<Integer> getOtherServers() {
        return otherServers;
    }

    public void setOtherServers(Set<Integer> otherServers) {
        this.otherServers = otherServers;
    }

    @Override
    public String toString(){
        return role + "," + message  + "," + processId + "," + clientId + "," + fileId + "," + n_time + "," + otherServers.toString();
    }

}
