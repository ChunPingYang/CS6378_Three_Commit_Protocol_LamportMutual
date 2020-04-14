package model;

/**
 * A class that contains the names of the messages that would be used throughout
 * in the system
 */

public class StringConstants {
	public static String SPACE = " ";
	public static String MESSAGE_REGISTER = "REGISTER";

	public static String MESSAGE_COMMIT_REQUEST = "COMMIT_REQUEST";
	public static String MESSAGE_AGREED = "AGREED";
	public static String MESSAGE_PREPARE = "PREPARE";
	public static String MESSAGE_COMMIT = "COMMIT";
	public static String MESSAGE_ABORT = "ABORT";
	public static String MESSAGE_ACK = "ACK";
	public static String MESSAGE_COMMIT_COMPLETE = "COMMIT_COMPLETE";
	public static String MESSAGE_FILE_NOT_EXIST = "FILE_NOT_EXIST";
	public static String MESSAGE_SHUTDOWN = "SHUTDOWN";

	public static String STATE_Q1 = "Q1";  //init
	public static String STATE_W1 = "W1"; //wait
	public static String STATE_P1 = "P1";  //prepare-commit
	public static String STATE_A1 = "A1";  //abort
	public static String STATE_C1 = "C1";  //commit

	public static String STATE_Q = "Q";
	public static String STATE_W = "W";
	public static String STATE_P = "P";
	public static String STATE_A = "A";
	public static String STATE_C = "C";

	public static String ACTION_WRITE = "W";
	public static String ACTION_READ = "R";

	public static String ROLE_COORDINATOR = "COORDINATOR";
	public static String ROLE_COHORT = "COHORT";

	public static final String LAMPORT_REQUEST = "REQUEST";
	public static final String LAMPORT_REPLY = "REPLY";

}
