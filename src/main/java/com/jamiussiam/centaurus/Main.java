package com.jamiussiam.centaurus;

import com.jamiussiam.centaurus.helper.DownloadTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {
    public final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        String urlS = "http://www.webpage-maker.com/files/wpm.exe";
        String urlM = "https://download.ccleaner.com/ccsetup564.exe";
        String urlNoSizeNoResume = "http://www.rihatetaw-ler.com/obqnp9m%7Cx478r/FFSetupLite.exe";
        String urlL = "https://github.com/meetfranz/franz/releases/download/v5.4.0/franz-setup-5.4.0.exe";
        String urlXL = "http://mirror.dhakacom.com/ubuntu-releases/xenial/ubuntu-16.04.6-desktop-amd64.iso";


        String saveFolder = System.getProperty("user.home") + "/Desktop/dmanager/";
        logger.log(Level.INFO, "Starting Download with 8 connections.");

        DownloadTask downloadTask = new DownloadTask(urlM, saveFolder, 8);
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.start();
        downloadThread.join();
        logger.log(Level.INFO, "Download Complete.");
    }
}
