package s17201319.trackingServer;

import s17201319.p2pclient.ClientSetting;
import s17201319.resources.ByteUtils;
import s17201319.resources.IniRwUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * 每隔固定时间告知追踪器我在线上，并把本客户端所含有的资源信息告知服务端
 * @author 17201319
 */
public class OnlineRunnable implements Runnable{
    private TrackingSetting ts = TrackingSetting.getTs();
    private String ip = ts.getTrackingIp();
    private int port = ts.getOnlinePort();
    private String md5Path;
    private DatagramSocket socket;
    private ServerRunnable server;

    public OnlineRunnable(String md5Path, DatagramSocket socket, ServerRunnable server) {
        this.md5Path = md5Path;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (true){
                List<String> list = new ArrayList<>(
                        IniRwUtils.getAllProperties(md5Path).keySet());
                list.add(String.valueOf(server.getMaxLink() - server.getCurrLink()));
                byte[] bytes = ByteUtils.objToByte(list);
                if (bytes == null){
                    System.out.println("对象转字节失败");
                    return;
                }
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(ip),port);
                socket.send(packet);
                Thread.sleep(500);
            }
        } catch (SocketException e) {
            System.out.println("online线程结束");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("online线程结束");
        }
    }
}
