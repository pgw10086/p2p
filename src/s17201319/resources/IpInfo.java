package s17201319.resources;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 储存ip即端口号
 * @author 17201319
 */
public class IpInfo implements Serializable {
    private InetAddress ip;
    private int port;

    public IpInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static IpInfo createIpInfo(String in){
        IpInfo ipInfo = null;
        String[] info = in.split(":");
        try {
            ipInfo = new IpInfo(InetAddress.getByName(info[0]),Integer.parseInt(info[1]));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ipInfo;
    }
}
