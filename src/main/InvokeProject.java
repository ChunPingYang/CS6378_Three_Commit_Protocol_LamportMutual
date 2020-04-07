package main;

import listener.Cohort;
import listener.Coordinator;
import utility.ReadConfigFile;

import java.util.Scanner;

/**
 * 
 * @author dsr170230
 * @version 7.0
 */

public class InvokeProject {
	private static ReadConfigFile extractFromConfigFile;
	private static Coordinator coordinatorProcess;
	private static Cohort otherProcess;
	private static String[] serverList = {"127.0.0.1","127.0.0.1","127.0.0.1"};
	private static int[] portsArr = new int[]{5000,5001,5002};

	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args[0].equals("-c")) {
			extractFromConfigFile = new ReadConfigFile(true);
			coordinatorProcess = new Coordinator();
			coordinatorProcess = extractFromConfigFile.getConfigFileData(args[1]);
			coordinatorProcess.readServerConfig(serverList,portsArr);

			coordinatorProcess.start();
		} else {
			extractFromConfigFile = new ReadConfigFile(false);
			otherProcess = new Cohort();
			otherProcess = extractFromConfigFile.getConfigDataForProcess(args[0]);

			System.out.println("Enter server number:");
			Scanner in = new Scanner(System.in);
			int index = in.nextInt();

			int[] ids = new int[]{0,1,2};
			otherProcess.readServerConfig(serverList,portsArr);

			otherProcess.start(ids[index]);
		}
	}
}
