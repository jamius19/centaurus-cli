package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.structure.FileDetails;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class FileMerger implements Runnable {

    public final static Logger logger = Logger.getLogger(FileMerger.class);


    // Save location for the file
    private String saveLoc;

    private LinkedList<FileInputStream> inputStreams;

    private FileDetails fileDetails;

    private String fileName;

    private int connectionCount;


    public FileMerger(String saveLoc, String fileName, FileDetails fileDetails, int connectionCount) {
        this.saveLoc = saveLoc;
        this.fileName = fileName;
        this.fileDetails = fileDetails;
        this.connectionCount = connectionCount;

        inputStreams = new LinkedList<>();

        try {
            FileOutputStream fs = new FileOutputStream(this.saveLoc + fileName);
            fs.flush();
            fs.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

    }

    @Override
    public void run() {
        try {

            for (int i = 0; i < connectionCount; i++) {
                LinkedList<String> fileParts = fileDetails.getSubParts(i);

                for (String part : fileParts) {
                    File temp = new File(String.format("%s/%s",saveLoc, part));
                    inputStreams.add(new FileInputStream(temp));
                }
            }

            FileOutputStream fs = new FileOutputStream(saveLoc + fileName, true);

            FileChannel fsOut = fs.getChannel();

            long lastBytePosition = 0;

           for (FileInputStream inputStream : inputStreams) {
               FileChannel fsIn = inputStream.getChannel();
               fsOut.transferFrom(fsIn, lastBytePosition, fsIn.size());
               lastBytePosition += fsIn.size();
               fsIn.close();
               inputStream.close();
           }

            fsOut.close();

            fs.flush();
            fs.close();

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
