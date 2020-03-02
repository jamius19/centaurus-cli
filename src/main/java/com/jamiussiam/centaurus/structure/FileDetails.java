package com.jamiussiam.centaurus.structure;

import com.jamiussiam.centaurus.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;

public class FileDetails {
    public final static Logger logger = Logger.getLogger(FileDetails.class);
    HashMap<Integer, LinkedList<String>> fileParts = new HashMap<>();

    public FileDetails(int connections) {
        for (int i = 0; i < connections; i++) {
            fileParts.put(i, new LinkedList<>());
        }
    }

    public LinkedList<String> getSubParts (int connectionNo) {
        return fileParts.get(connectionNo);
    }

    public String newSubPart(int connectionNo) {
        String partName = String.format("%s_%s", connectionNo, fileParts.get(connectionNo).size());
        logger.log(Level.DEBUG, String.format("File Part Name: %s", partName));

        fileParts.get(connectionNo).add(partName);
        return partName;
    }
}
