package s17201319.trackingServer;

import s17201319.resources.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

/**
 * @author 172013119
 */
public class OnLineRecordRunnable implements Runnable{
    //packet大小
    private final int SIZE = 1024 * 63;

    //ip:port + 该ip所拥有的资源md5
    private static volatile Map<String,List<String>> online = new HashMap<>();
    private volatile Map<String,Long> onlineTime = new HashMap<>();

    @Override
    public void run() {
        TrackingDevice.pool.submit(new CheckTimeRunnable());
        DatagramPacket packet = new DatagramPacket(new byte[SIZE],SIZE);
        try(DatagramSocket socket = new DatagramSocket(9990)){
            while (true){
                socket.receive(packet);
                System.out.println(online.toString());
                List<String> list = (List<String>) ByteUtils.byteToObj(
                        Arrays.copyOfRange(packet.getData(),0,packet.getLength()));
                if (Integer.parseInt(list.get(list.size() - 1)) > 0){
                    list.remove(list.size() - 1);
                    online.put(packet.getAddress().getHostAddress() + ":" + packet.getPort(),list);
                    onlineTime.put(packet.getAddress().getHostAddress() + ":" + packet.getPort()
                            ,System.currentTimeMillis());
                }else {
                    online.remove(packet.getAddress().getHostAddress() + ":" + packet.getPort());
//                    onlineTime.remove(packet.getAddress().getHostAddress() + ":" + packet.getPort());

                }
//                System.out.println(online.keySet().toString());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<String>> getOnline() {
        return online;
    }

    class CheckTimeRunnable implements Runnable{
        @Override
        public void run() {
            while (true){
                for (String ip :
                        onlineTime.keySet()) {
                    if (System.currentTimeMillis() - onlineTime.get(ip) > 1000){
                        if (online.containsKey(ip)){
                            System.out.println(ip + "连接中断");
                            online.remove(ip);
                        }
//                        onlineTime.remove(ip);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
