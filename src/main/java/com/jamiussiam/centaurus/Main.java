package com.jamiussiam.centaurus;

import com.jamiussiam.centaurus.helper.DownloadTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
    public final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        String urlI1 = "https://download.ccleaner.com/ccsetup563.exe";
        String saveLoc = System.getProperty("user.home") + "/Desktop/dmanager/";
        logger.log(Level.DEBUG, "Starting Download with 8 connections.");

        DownloadTask downloadTask = new DownloadTask(urlI1, saveLoc, 8);
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.start();
        downloadThread.join();
        logger.log(Level.DEBUG, "Download Complete.");
    }
}
