package main;

import listener.Cohort;
import listener.Coordinator;
import utility.ReadConfigFile;
import utility.ServerSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author dsr170230
 * @version 7.0
 */
//TODO 當多個客戶端傳送時，傳送的伺服器都相同時，可以跑，但不完全一樣時，不能跑

public class InvokeProject {
	private static ReadConfigFile extractFromConfigFile;
	private static Coordinator coordinatorProcess;
	private static Cohort otherProcess;
	private static ServerSelector selectorProcess;
	private static String[] serverList = {"127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1"};
	private static int[] portsArr = new int[]{5000,5001,5002,5003,5004,5005,5006};


	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args[0].equals("coordinator"))
		{
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

		} else if(args[0].equals("cohort")) {

			extractFromConfigFile = new ReadConfigFile(false);

			System.out.println("Enter server index:");
			Scanner in = new Scanner(System.in);
			int index = in.nextInt();
			int[] ids = new int[]{0,1,2,3,4,5,6};
			otherProcess = new Cohort();
			otherProcess = extractFromConfigFile.getConfigDataForProcess(args[1]);
			otherProcess.readServerConfig(serverList,portsArr);
			otherProcess.initCohort(ids[index]);
			otherProcess.initServerToServer(ids,ids[index]);

			otherProcess.start(ids[index]);

		} else {

		    System.out.println("Enter server index being disabled:");
            Scanner in = new Scanner(System.in);
            int index = in.nextInt();
            int[] ids = new int[]{0,1,2,3,4,5,6};
			selectorProcess = new ServerSelector();
            selectorProcess.readServerConfig(serverList,portsArr);

            selectorProcess.select(ids[index]);
        }
	}
}
