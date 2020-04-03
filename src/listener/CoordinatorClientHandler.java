package listener;

import utility.FileAccessor;
import utility.SharedDataAmongCoordThreads;

import java.io.DataInputStream;
import java.io.File;
import java.net.Socket;

public class CoordinatorClientHandler extends Thread{

    private int variable;
    private SharedDataAmongCoordThreads sharedData;

    public CoordinatorClientHandler(int variable, SharedDataAmongCoordThreads data) {
        this.variable = variable;
        this.sharedData = data;
    }

    @Override
    public void run() {

        sharedData.setCommitMade(true);

    }
}
