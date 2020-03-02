package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadConnectionThread implements Runnable {
    public final static Logger logger = Logger.getLogger(DownloadConnectionThread.class);

    public String downloadUrl;
    public long startByte;
    public long endByte;
    public String saveLocation;
    public String partName;
    public DownloadTask parentDownloadTask;
    public long downloadedBytes;
    public float progess;
    public int connectionNo;

    public DownloadConnectionThread(String downloadUrl, long startByte, long endByte, String saveLocation, String partName,
                                    DownloadTask parentDownloadTask, int connectionNo) {
        this.downloadUrl = downloadUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.saveLocation = saveLocation;
        this.partName = partName;
        this.parentDownloadTask = parentDownloadTask;
        this.connectionNo = connectionNo;

        //File file = new File(saveLocation + partName);
    }

    @Override
    public void run() {
        URL url = null;
        try {
            url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            conn.connect();

            InputStream in = conn.getInputStream();
            logger.log(Level.DEBUG, String.format("File Path %s/%s", saveLocation, partName));
            FileOutputStream fs = new FileOutputStream(String.format("%s/%s", saveLocation, partName));


            long totalSize = endByte - startByte;

            for (int b = in.read(), count = 0; count <= endByte && b != -1; b = in.read(), count++) {
                fs.write(b);
                downloadedBytes++;
                progess = (float) count / (float) totalSize * 100f;
            }

            logger.log(Level.DEBUG, String.format("Part %s Download Complete", partName));

            in.close();
            fs.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }
}
