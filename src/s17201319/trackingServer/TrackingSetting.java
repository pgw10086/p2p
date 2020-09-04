package s17201319.trackingServer;

import s17201319.resources.TorrentUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TrackingSetting {
    private static TrackingSetting ts;
    private int size = 1024 * 63;
    private String trackingIp = InetAddress.getLocalHost().getHostAddress();
    //追踪器端口号
    private int trackingPort = 9999;
    //追踪器告知在线的端口号
    private int onlinePort = 9990;
    private String downloadPath = "D:\\P2PDownload";

    private TrackingSetting() throws UnknownHostException {

    }

    public static TrackingSetting getTs(){
        if (ts == null){
            try {
                ts = new TrackingSetting();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return ts;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getTrackingPort() {
        return trackingPort;
    }

    public int getOnlinePort() {
        return onlinePort;
    }

    public String getTrackingIp() {
        return trackingIp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
