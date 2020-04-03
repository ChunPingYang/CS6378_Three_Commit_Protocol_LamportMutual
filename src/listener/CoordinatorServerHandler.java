package listener;

import model.StringConstants;
import utility.FileAccessor;
import utility.SharedDataAmongCoordThreads;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CoordinatorServerHandler extends Thread{

    /**
     * Variables required for establishing connections
     */
    private Socket cohortSocket = null;
    private PrintWriter printWriter = null;
    private DataInputStream dataInputStream = null;

    /**
     * Boolean variables required to not allow the coordinator to send the same
     * data multiple times to all cohorts
     */
    private boolean isAborted;
    private boolean isCommitted;
    private boolean isCommitRequest;
    private boolean isPrepareSentToAllCohorts;
    private boolean coordinatorFail = false;

    /**
     * Variables required for computation
     */
    private String stringInputStream;
    private int maxCohort;
    private int processId;

    /**
     * Variable to access shared data among different handler threads
     */
    private SharedDataAmongCoordThreads sharedDataAmongCoordThreads;


    /**
     * Variable to access file methods
     */
    private FileAccessor fileaccessor;

    /**
     * A parameterized constructor that initializes its local variables
     */
    public CoordinatorServerHandler(Socket cohortSocket, int maxCohort, DataInputStream inputStream, int pId,
                                    SharedDataAmongCoordThreads sharedDataAmongCoordThreads) {
        this.cohortSocket = cohortSocket;
        this.maxCohort = maxCohort;
        this.dataInputStream = inputStream;
        this.processId = pId;
        this.sharedDataAmongCoordThreads = sharedDataAmongCoordThreads;

        isAborted = false;
        isCommitted = false;
        isCommitRequest = false;
        isPrepareSentToAllCohorts = false;

//        stateLogFile = new File(System.getProperty("user.dir") + "/StateInfo_Coordinator");
//        outputLogFile = new File(System.getProperty("user.dir") + "/Output_Coordinator");

        fileaccessor = new FileAccessor();
    }


    @Override
    public void run() {
        while (true) {

            try {
                printWriter = new PrintWriter(cohortSocket.getOutputStream());
                //printWriter.println(processId + StringConstants.SPACE); //TODO 不懂為什麼要加空白
                printWriter.println(processId + StringConstants.SPACE);
                printWriter.flush();

//                if (sharedDataAmongCoordThreads.isCommitRequest() && !isCommitRequest && !coordinatorFail) {
//
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
