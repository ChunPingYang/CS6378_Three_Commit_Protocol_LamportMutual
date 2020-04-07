package utility;

/**
 * A class that contains various updated values that can be used by client and
 * server threads
 */
public class SharedDataAmongCoordThreads {
	private int transactionID;
	private boolean commitMade = false;
	private int commitedValue;
	private boolean failScenario1 = false;
	private boolean failScenario2 = false;
	private boolean failScenario3 = false;
	private long startTime;
	private long endTime;
	private boolean updateTime = false;
	private boolean updateEndTime = false;
	private boolean abortAfterRecovery = false;
	private boolean commitAfterRecovery = false;
	private int incrementSentPrepare = 0;
	private boolean requestInitiated = false;
	private boolean writeOutputFile = false;
	private boolean aborted = false;
	private boolean timeOut = false;
	private boolean writeFile = false;
	private boolean alreadySent = false;
	private int countAgreeFromCohort = 0;
	private int countAckFromCohort = 0;
	private boolean prepare = false;
	private boolean q1TimeOut = false;
	private boolean w1TimeOut = false;
	private int countTimeOut = 0;

	public void incrementTimeOut() {
		countTimeOut++;
	}

	public int getCountTimeOut() {
		return countTimeOut;
	}

	public boolean isW1TimeOut() {
		return w1TimeOut;
	}

	public void setW1TimeOut(boolean w1TimeOut) {
		this.w1TimeOut = w1TimeOut;
	}

	public boolean isQ1TimeOut() {
		return q1TimeOut;
	}

	public void setQ1TimeOut(boolean q1TimeOut) {
		this.q1TimeOut = q1TimeOut;
	}

	public boolean isFailScenario1() {
		return failScenario1;
	}

	public void setFailScenario1(boolean failScenario1) {
		this.failScenario1 = failScenario1;
	}

	public boolean isFailScenario2() {
		return failScenario2;
	}

	public void setFailScenario2(boolean failScenario2) {
		this.failScenario2 = failScenario2;
	}

	public boolean isFailScenario3() {
		return failScenario3;
	}

	public void setFailScenario3(boolean failScenario3) {
		this.failScenario3 = failScenario3;
	}

	public boolean isCommitAfterRecovery() {
		return commitAfterRecovery;
	}

	public void setCommitAfterRecovery(boolean commitAfterRecovery) {
		this.commitAfterRecovery = commitAfterRecovery;
	}

	public boolean isAbortAfterRecovery() {
		return abortAfterRecovery;
	}

	public void setAbortAfterRecovery(boolean abortAfterRecovery) {
		this.abortAfterRecovery = abortAfterRecovery;
	}

	public boolean isUpdateEndTime() {
		return updateEndTime;
	}

	public void setUpdateEndTime(boolean updateEndTime) {
		this.updateEndTime = updateEndTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(boolean updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isWriteFile() {
		return writeFile;
	}

	public void setWriteFile(boolean writeFile) {
		this.writeFile = writeFile;
	}

	public boolean isTimeOut() {
		return timeOut;
	}

	public void setTimeOut(boolean timeOut) {
		this.timeOut = timeOut;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public boolean isWriteOutputFile() {
		return writeOutputFile;
	}

	public void setWriteOutputFile(boolean writeOutputFile) {
		this.writeOutputFile = writeOutputFile;
	}

	public int getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}

	public void incrementSentP() {
		incrementSentPrepare++;
	}

	public int getIncrementSentPrepare() {
		return incrementSentPrepare;
	}

	public boolean isRequestInitiated() {
		return requestInitiated;
	}

	public void setRequestInitiated(boolean requestInitiated) {
		this.requestInitiated = requestInitiated;
	}

	public boolean isPrepare() {
		return prepare;
	}

	public void setPrepare(boolean prepare) {
		this.prepare = prepare;
	}

	private boolean commitSent = false;

	public boolean isAlreadySent() {
		return alreadySent;
	}

	public void setAlreadySent(boolean alreadySent) {
		this.alreadySent = alreadySent;
	}

	public boolean isCommitRequest() {
		return commitMade;
	}

	public void setCommitMade(boolean commitMade) {
		this.commitMade = commitMade;
	}

	public int getCommitedValue() {
		return commitedValue;
	}

	public void setCommitedValue(int commitedValue) {
		this.commitedValue = commitedValue;
	}

	public synchronized void incrementAgree() {
		countAgreeFromCohort++;
	}

	public synchronized int getCountAgreeFromCohort() {
		return countAgreeFromCohort;
	}

	public void incrementAck() {
		countAckFromCohort++;
	}

	public int getCountAckFromCohort() {
		return countAckFromCohort;
	}

	public boolean isCommitSent() {
		return commitSent;
	}

	public void setCommitSent(boolean commitSent) {
		this.commitSent = commitSent;
	}

}
