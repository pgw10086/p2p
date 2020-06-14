package s17201319.p2pclient;

/**
 * 客户端的所有设置，使用单例模式
 * 可以考虑在启动时读取配置文件来初始化这个对象
 * @author 17201319
 */
public class ClientSetting {

    private static ClientSetting clientSetting;

    //包大小
    private int packetSize = 1024 * 63;
    //追踪器地址
    private String trackingIp = "10.30.1.88";
    //追踪器端口号
    private int trackingPort = 9999;
    //追踪器告知在线的端口号
    private int onlinePort = 9990;
    //种子文件保存地址
    private  String torrentPath = "D:\\torrent3";
    //文件下载地址
    private  String downloadPath = "D:\\clientDownload3";
    //重发次数
    private int retransmission = 7;
    //该地址储存客户端所存的文件
    private String md5Path = torrentPath + "\\md5.ini";
    //最大链接数
    private int maxLink = 0;

    private ClientSetting() {
    }

    public static ClientSetting getClientSetting(){
        if (clientSetting == null){
            clientSetting = new ClientSetting();
        }
        return clientSetting;
    }

    public static void setClientSetting(ClientSetting clientSetting) {
        ClientSetting.clientSetting = clientSetting;
    }

    public String getMd5Path() {
        return md5Path;
    }

    public void setMaxLink(int maxLink) {
        this.maxLink = maxLink;
    }

    public int getMaxLink() {
        return maxLink;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getRetransmission() {
        return retransmission;
    }

    public int getOnlinePort() {
        return onlinePort;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public String getTorrentPath() {
        return torrentPath;
    }

    public String getTrackingIp() {
        return trackingIp;
    }

    public int getTrackingPort() {
        return trackingPort;
    }

    public void setTrackingIp(String trackingIp) {
        this.trackingIp = trackingIp;
    }
}
