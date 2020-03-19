package com.jamiussiam.centaurus.structure;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;

public class FileDetails {
    public final static Logger logger = Logger.getLogger(FileDetails.class);
    private final String FILE_PART_EXTENSION = "cen";

    HashMap<Integer, LinkedList<String>> fileParts = new HashMap<>();
    private String fileName;

    public FileDetails(int connections, String fileName) {
        this.fileName = fileName;

        for (int i = 0; i < connections; i++) {
            fileParts.put(i, new LinkedList<>());
        }
    }

    public LinkedList<String> getSubParts (int connectionNo) {
        return fileParts.get(connectionNo);
    }

    public String newSubPart(int connectionNo) {
        String partName = String.format("%s_%s_%s.%s", fileName, connectionNo,
                fileParts.get(connectionNo).size(), FILE_PART_EXTENSION);

        logger.log(Level.DEBUG, String.format("File Part Name: %s_%s", fileName, partName));

        fileParts.get(connectionNo).add(partName);
        return partName;
    }
}
