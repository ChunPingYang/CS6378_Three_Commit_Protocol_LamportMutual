package utility;

import listener.Cohort;
import listener.Coordinator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *  
 */
public class ReadConfigFile {
	private String fileLocation = null;
	private BufferedReader readConfigFile = null;
	private Coordinator processCoordinator = null;
	private Cohort otherProcess = null;
	private String readLineFromFile = null;

	public ReadConfigFile(boolean isCoordinator) {
		if (isCoordinator) {
			processCoordinator = new Coordinator();
		} else {
			otherProcess = new Cohort();
		}
	}

	public Coordinator getConfigFileData(String fileName) {
		fileLocation = System.getProperty("user.dir") + "/src/" + fileName;
		try {
			readConfigFile = new BufferedReader(new FileReader(fileLocation));
			try {

				while ((readLineFromFile = readConfigFile.readLine()) != null) {
					if (readLineFromFile.length() > 0) {
						String[] inputLine = readLineFromFile.split(" ");

						if (inputLine[0].equals("COORDINATOR")) {
							processCoordinator.setHostName(inputLine[1]);
						}

						if (inputLine[0].equals("NUMBER")) {
							processCoordinator.setMaxProcess(Integer.parseInt(inputLine[inputLine.length - 1]));
						}

//						if (inputLine[0].equals("TERMINATE")) {
//							processCoordinator.setTerminate(Integer.parseInt(inputLine[1]));
//						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return processCoordinator;
	}


	// If the invoked process is not a coordinator
	public Cohort getConfigDataForProcess(String fileName) {
		fileLocation = System.getProperty("user.dir") + "/src/" + fileName;
		try {
			readConfigFile = new BufferedReader(new FileReader(fileLocation));
			try {
				while ((readLineFromFile = readConfigFile.readLine()) != null) {

					if (readLineFromFile.length() > 0) {
						String[] inputLine = readLineFromFile.split(" ");

						if (inputLine[0].equals("COORDINATOR")) {
							otherProcess.setCoordinatorHostName(inputLine[1]);
						}

						if (inputLine[0].equals("NUMBER")) {
							otherProcess.setMaxProcess(Integer.parseInt(inputLine[inputLine.length - 1]));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return otherProcess;
	}


}