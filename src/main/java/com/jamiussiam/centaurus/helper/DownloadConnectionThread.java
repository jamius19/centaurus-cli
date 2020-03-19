package com.jamiussiam.centaurus.helper;

import com.jamiussiam.centaurus.util.ReadableConsumerByteChannel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
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
    public volatile float progess;
    public int connectionNumber;
    long size = 0;

    public DownloadConnectionThread(String downloadUrl, long startByte, long endByte, String saveLocation, String partName,
                                    DownloadTask parentDownloadTask, int connectionNumber) {
        this.downloadUrl = downloadUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.saveLocation = saveLocation;
        this.partName = partName;
        this.parentDownloadTask = parentDownloadTask;
        this.connectionNumber = connectionNumber;

        this.size = endByte - startByte;

        //File file = new File(saveLocation + partName);
    }

    @Override
    public void run() {
        URL url = null;

        try {
            url = new URL(downloadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Range", String.format("bytes=%d-%d", startByte, endByte));
            httpURLConnection.connect();

            ReadableByteChannel rbc = Channels.newChannel(httpURLConnection.getInputStream());


            //logger.log(Level.DEBUG, String.format("%d downloaded %f kb of %f", connectionNumber, (bytesDownloaded / 1000f), (size / 1000f)));
            //this.progess = (downloadedBytes / (float) size);
            ReadableConsumerByteChannel rcbc = new ReadableConsumerByteChannel(rbc, this::setDownloadedBytes);

            FileOutputStream fos = new FileOutputStream(String.format("%s/%s", saveLocation, partName));
            fos.getChannel().transferFrom(rcbc, 0, Long.MAX_VALUE);

            logger.log(Level.DEBUG, String.format("Part %s Download Complete", connectionNumber));

            httpURLConnection.getInputStream().close();
            httpURLConnection.disconnect();
            rbc.close();
            fos.close();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }

    synchronized void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }
}
