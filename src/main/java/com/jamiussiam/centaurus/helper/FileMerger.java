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
    private String saveLocation;

    private LinkedList<FileInputStream> downloadedFileParts;

    private FileDetails fileDetails;

    private String fileName;

    private int connectionCount;


    public FileMerger(String saveLocation, String fileName, FileDetails fileDetails, int connectionCount) throws FileNotFoundException {
        this.saveLocation = saveLocation;
        this.fileName = fileName;
        this.fileDetails = fileDetails;
        this.connectionCount = connectionCount;

        downloadedFileParts = new LinkedList<>();

        try {
            FileOutputStream fs = new FileOutputStream(this.saveLocation + fileName);
            fs.flush();
            fs.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
            throw new FileNotFoundException("Cannot create Merged File");
        }

    }

    @Override
    public void run() {
        try {

            for (int i = 0; i < connectionCount; i++) {
                LinkedList<String> fileParts = fileDetails.getSubParts(i);

                for (String part : fileParts) {
                    File temp = new File(String.format("%s/%s", saveLocation, part));
                    downloadedFileParts.add(new FileInputStream(temp));
                }
            }

            FileOutputStream fs = new FileOutputStream(saveLocation + fileName, true);

            FileChannel fsOut = fs.getChannel();

            long lastBytePosition = 0;

           for (FileInputStream inputStream : downloadedFileParts) {
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
