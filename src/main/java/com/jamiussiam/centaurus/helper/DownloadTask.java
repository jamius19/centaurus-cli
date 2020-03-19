package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.structure.FileDetails;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

public class DownloadTask implements Runnable {
    public final static Logger logger = Logger.getLogger(DownloadTask.class);

    private final double BYTE_TO_MB = 1_048_576;
    private final double NANO_TO_SEC = 1_000_000_000f;

    String url;
    String fileExtension;
    String saveLocation;
    int connectionCount;
    FileDetails downloadFileDetails;
    LinkedList<DownloadConnectionThread> downloadConnections;
    LinkedList<Thread> downloadThreads;
    double downloadProgress;
    double dlSpeedPerSecond;

    long downloadedBytes;
    long startTime;

    private long fileSize;

    public DownloadTask(String url, String saveLocation, int connectionCount) {
        logger.log(Level.INFO, String.format("Donwload URL: %s", url));
        this.url = url;
        this.saveLocation = saveLocation;
        this.connectionCount = connectionCount;
        this.downloadConnections = new LinkedList<>();
        this.downloadThreads = new LinkedList<>();
        this.downloadFileDetails = new FileDetails(connectionCount, getFileName());
    }

    @Override
    public void run() {
        try {
            createConnectionThreads();
        } catch (IOException e) {
            logger.log(Level.ERROR, "Cannot open download URL.");
            e.printStackTrace();
            return;
        }


        startConnectionThread();

        // Calculate Download Time
        long startTime = this.startTime = System.nanoTime();
        while (!isCompleted()) {

            logger.log(Level.INFO, String.format("Downloaded: %.2f%%\tSpeed: %.3f", getProgress() * 100f, dlSpeedPerSecond));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.log(Level.ERROR, "Thread sleep interrupted!");
                e.printStackTrace();
            }
        }

        // Calculate Download Time
        long endTime = System.nanoTime();

        logger.log(Level.INFO, String.format("Download complete. Time taken: %f s",
                (endTime - startTime) / NANO_TO_SEC));
        logger.log(Level.INFO, "Starting parts merging.");

        try {
            // Start File Merging
            FileMerger fileMerger = new FileMerger(saveLocation, getFileName(),
                    downloadFileDetails, connectionCount);

            // Calculate Merging Time
            startTime = System.nanoTime();
            Thread fileMergerThread = new Thread(fileMerger);

            fileMergerThread.start();
            while (fileMergerThread.isAlive())
                Thread.sleep(500);

            // Calculate Merging Time
            endTime = System.nanoTime();

            logger.log(Level.INFO, String.format("Merging complete. Time taken: %f s",
                    (endTime - startTime) / NANO_TO_SEC));
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, "File cannot be merged.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Thead sleep interrupted!");
            e.printStackTrace();
        }
    }

    private void createConnectionThreads() throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        httpConnection.setRequestMethod("HEAD");
        httpConnection.connect();

        if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            httpConnection.disconnect();

            httpConnection = (HttpURLConnection) new URL(url).openConnection();
        }

        String acceptRangesValue = httpConnection.getHeaderField("Accept-Ranges");

        if(acceptRangesValue == null || !acceptRangesValue.equalsIgnoreCase("bytes"))
            connectionCount = 1;


        if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            logger.log(Level.DEBUG, "URL Okay");

            fileSize = httpConnection.getContentLengthLong();
            long partSize = fileSize / connectionCount;
            long lastBytePosition = 0;

            logger.log(Level.DEBUG, String.format("Download Size:\t%d \tPart Size:\t%d \tNumber of connection:\t%d",
                    fileSize, partSize, connectionCount));


            for (int i = 0; i < connectionCount; i++) {
                if (i != connectionCount - 1) {
                    downloadConnections.add(new DownloadConnectionThread(url, lastBytePosition,
                            lastBytePosition + partSize, saveLocation, downloadFileDetails.newSubPart(i),
                            this, i));

                    lastBytePosition += partSize + 1;
                } else {
                    downloadConnections.add(new DownloadConnectionThread(url, lastBytePosition, fileSize,
                            saveLocation, downloadFileDetails.newSubPart(i), this, i));
                }

                downloadThreads.add(new Thread(downloadConnections.get(i)));
            }
        } else {
            logger.log(Level.ERROR, String.format("Status Code: %d", httpConnection.getResponseCode()));
            throw new IOException("URL not reachable.");
        }

        httpConnection.disconnect();
    }

    private void startConnectionThread() {
        for (int i = 0; i < connectionCount; i++) {
            downloadThreads.get(i).start();
        }
    }

    private double getProgress() {
        downloadedBytes = 0;

        for(DownloadConnectionThread i : downloadConnections) {
            downloadedBytes += i.downloadedBytes;
        }


        /*double elapsedTimeInSecond = (System.nanoTime() - lastDLSpeedTime) / 1_000_000_000f;

        if(elapsedTimeInSecond >= 1) {
            long deltaSize = latestDownloadedBytes - downloadedBytes;
            dlSpeedPerSecond = deltaSize / 1_000_000f;
            lastDLSpeedTime = System.nanoTime();
        }*/

        //logger.log(Level.DEBUG, String.format("%d %f %f", deltaSize, elapsedTimeInSecond, dlSpeedPerSecond));
        //logger.log(Level.DEBUG, String.format("DL Bytes: %d\t Total Size: %d", downloadedBytes, fileSize));

        double elapsedTimeInSecond = (System.nanoTime() - startTime) / NANO_TO_SEC;
        dlSpeedPerSecond = (downloadedBytes / elapsedTimeInSecond) / BYTE_TO_MB;

        return downloadProgress  = (downloadedBytes / (double) fileSize);
    }

    private String getFileName() {
        if (fileExtension != null) return fileExtension;

        int index = -1;
        for (int i = url.length() - 1; i >= 0; i--) {
            if (url.charAt(i) == '/' || url.charAt(i) == '\\') {
                index = i;
                break;
            }
        }

        fileExtension = url.substring(index + 1);
        fileExtension = fileExtension.trim();
        return (index == -1 ? null : fileExtension);
    }

    boolean isCompleted() {
        for (Thread thread : downloadThreads) {
            if (thread.isAlive())
                return false;
        }

        return true;
    }
}
