package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.Main;
import com.jamiussiam.centaurus.structure.FileDetails;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.LinkedList;

public class DownloadTask implements Runnable {
    public final static Logger logger = Logger.getLogger(DownloadTask.class);

    String url;
    String fileExt;
    String saveLocation;
    int connectionCount;
    FileDetails downloadFile;
    LinkedList<DownloadConnectionThread> downloadConnections;
    LinkedList<Thread> downloadThreads;

    public DownloadTask(String url, String saveLocation, int connectionCount) {
        this.url = url;
        this.saveLocation = saveLocation;
        this.connectionCount = connectionCount;
        this.downloadConnections = new LinkedList<>();
        this.downloadThreads = new LinkedList<>();

        this.downloadFile = new FileDetails(connectionCount);
    }

    @Override
    public void run() {
        try {
            long size = new URL(url).openConnection().getContentLengthLong();
            long partSize = size / connectionCount;
            long lastBytePosition = 0;

            logger.log(Level.DEBUG, String.format("Download Size:\t%d \nPart Size:\t%d \nNumber of connection:\t%d", size, partSize, connectionCount));


            long startTime = System.nanoTime();
            for (int i = 0; i < connectionCount; i++) {
                if (i != connectionCount - 1) {
                    downloadConnections.add(new DownloadConnectionThread(url, lastBytePosition, lastBytePosition + partSize, saveLocation, downloadFile.newSubPart(i), this, i));
                    lastBytePosition += partSize + 1;
                } else {
                    downloadConnections.add(new DownloadConnectionThread(url, lastBytePosition, size, saveLocation, downloadFile.newSubPart(i), this, i));
                }

                downloadThreads.add(new Thread(downloadConnections.get(i)));
                downloadThreads.get(i).start();
            }

            while (!isCompleted())
                Thread.sleep(500);

            long endTime = System.nanoTime();

            Main.logger.log(Level.DEBUG, String.format("Download complete. Time taken: %f s", (endTime - startTime) / 1_000_000_000f));
            Main.logger.log(Level.DEBUG, "Starting parts merging");

            startTime = System.nanoTime();
            FileMerger fileMerger = new FileMerger(saveLocation, fileExt(), downloadFile, connectionCount);
            Thread fileMergerThread = new Thread(fileMerger);

            fileMergerThread.start();
            while (fileMergerThread.isAlive())
                Thread.sleep(500);

            endTime = System.nanoTime();

            Main.logger.log(Level.DEBUG, String.format("Merging complete. Time taken: %f s", (endTime - startTime) / 1_000_000_000f));


        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }

    private String fileExt() {
        if (fileExt != null) return fileExt;

        int index = -1;
        for (int i = url.length() - 1; i >= 0; i--) {
            if (url.charAt(i) == '/' || url.charAt(i) == '\\') {
                index = i;
                break;
            }
        }
        fileExt = url.substring(index + 1, url.length());
        fileExt = fileExt.trim();
        return ((index == -1) ? null : fileExt);
    }

    boolean isCompleted(){
        for (Thread thread : downloadThreads) {
            if(thread.isAlive())
                return false;
        }

        return true;
    }
}
