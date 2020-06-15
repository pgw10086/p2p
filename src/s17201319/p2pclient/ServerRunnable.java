package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import s17201319.resources.ByteUtils;
import s17201319.resources.IpInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * 接收下载请求，并创建发送子线程
 * @author 17201319
 */
public class ServerRunnable implements Runnable{
    private ClientSetting cs = ClientSetting.getClientSetting();
    private final int size = cs.getPacketSize();
    private int maxLink = cs.getMaxLink();
    private volatile boolean flag = true;
    private String serverIp;
    private Label linkLabel;
    private Label maxLabel;
    private volatile int currLink = 0;

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[size],size);
        try(DatagramSocket socket = new DatagramSocket(0)){
            OnlineRunnable online = new OnlineRunnable(socket,this);
            Client.pool.submit(online);
            serverIp = InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort();
            System.out.println(serverIp);
            while (flag){
                socket.receive(packet);
                IpInfo ipInfo = new IpInfo(packet.getAddress(),packet.getPort());
                BlockInfo blockInfo = (BlockInfo) ByteUtils.byteToObj(Arrays.copyOfRange(
                        packet.getData(),0, packet.getLength()));
                System.out.println(blockInfo.toString());
                Client.pool.submit(new SendRunnable(ipInfo,blockInfo,this));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLinkLabel(Label linkLabel) {
        this.linkLabel = linkLabel;
    }

    public void setMaxLabel(Label maxLabel) {
        this.maxLabel = maxLabel;
    }

    public int getMaxLink() {
        return maxLink;
    }

    public synchronized void setMaxLink(int maxLink) {
        this.maxLink = maxLink;
        updateLabel(String.valueOf(maxLink),maxLabel);
    }

    public void updateLabel(String info,Label label){
        Platform.runLater(() -> {
            label.setText(info);
        });
    }

    public int getCurrLink() {
        return currLink;
    }

    public synchronized void setCurrLink(int currLink) {
        this.currLink = currLink;
        updateLabel(String.valueOf(currLink),linkLabel);
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
        System.out.println(this.flag);
    }

    public String getServerIp() {
        return serverIp;
    }
}
