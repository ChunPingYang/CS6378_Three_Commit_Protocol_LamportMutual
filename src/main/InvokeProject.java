package main;

import listener.Cohort;
import listener.Coordinator;
import utility.ReadConfigFile;

/**
 * 
 * @author dsr170230
 * @version 7.0
 */

public class InvokeProject {
	private static ReadConfigFile extractFromConfigFile;
	private static Coordinator coordinatorProcess;
	private static Cohort otherProcess;


	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args[0].equals("-c")) {
			extractFromConfigFile = new ReadConfigFile(true);
			coordinatorProcess = new Coordinator();
			coordinatorProcess = extractFromConfigFile.getConfigFileData(args[1]);
//			coordinatorProcess.start();
		} else {
			extractFromConfigFile = new ReadConfigFile(false);
			otherProcess = new Cohort();
			otherProcess = extractFromConfigFile.getConfigDataForProcess(args[0]);
//			otherProcess.start();
		}
	}
}
