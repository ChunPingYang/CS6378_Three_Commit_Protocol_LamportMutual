package main;

import listener.Cohort;
import listener.Coordinator;
import utility.ReadConfigFile;

import java.util.ArrayList;
import java.util.List;
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
	private static String[] serverList = {"127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1"};
	private static int[] portsArr = new int[]{5000,5001,5002,5003,5004};

	//TODO update/insert;read, 這邊mod要改成7
	public static List<Integer> selectServer(int fileId){
		List<Integer> list = new ArrayList<Integer>();
		list.add(fileId%5);
		list.add((fileId+1)%5);
		list.add((fileId+2)%5);
		return list;
	}
	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args[0].equals("-c")) {
			extractFromConfigFile = new ReadConfigFile(true);
			coordinatorProcess = new Coordinator();
			coordinatorProcess = extractFromConfigFile.getConfigFileData(args[1]);
			coordinatorProcess.readServerConfig(serverList,portsArr);

			System.out.println("Enter Client number:");
			Scanner clientIdInput = new Scanner(System.in);
			int clientId = clientIdInput.nextInt();
			System.out.println("Enter file number:");
			Scanner fileIdInput = new Scanner(System.in);
			int fileId = fileIdInput.nextInt();
			System.out.println("Enter action:");
			Scanner actionInput = new Scanner(System.in);
			String action = actionInput.next();
			coordinatorProcess.start(clientId,fileId,action);
		} else {
			extractFromConfigFile = new ReadConfigFile(false);
			otherProcess = new Cohort();
			otherProcess = extractFromConfigFile.getConfigDataForProcess(args[0]);

			System.out.println("Enter server number:");
			Scanner in = new Scanner(System.in);
			int index = in.nextInt();
			int[] ids = new int[]{0,1,2,3,4};
			otherProcess.readServerConfig(serverList,portsArr);
			otherProcess.initServerToServer(ids,ids[index]);


			otherProcess.start(ids[index]);
		}
	}
}
