package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.util.ReadableConsumerByteChannel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

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
    long size = 0;

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

            ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());


            ReadableConsumerByteChannel rcbc = new ReadableConsumerByteChannel(rbc,(b)->{
                logger.log(Level.DEBUG, String.format("%d downloaded %f kb", connectionNo, (b / 1000f)));
            });

            FileOutputStream fos = new FileOutputStream(String.format("%s/%s", saveLocation, partName));
            fos.getChannel().transferFrom(rcbc, 0, Long.MAX_VALUE);

            logger.log(Level.DEBUG, String.format("Part %s Download Complete"));

            //in.close();
            //fs.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }
}
