package s17201319.trackingServer;

import s17201319.resources.ByteUtils;
import s17201319.resources.IniRwUtils;
import s17201319.resources.IpInfo;
import s17201319.resources.Message;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 当一个对等方进入p2p网络时，必须向追踪器登记，并周期性的通知追踪器其仍在网络中。
 */
public class TrackingDevice {
    //线程池大小
    private static final int THREAD_COUNT = 100;
    //传输文件大小
    private static final int SIZE = 1024 * 63;
    //存有的文件数量
    private static volatile int fileNum;
    //线程池
    public static ExecutorService pool;

    public static void main(String[] args) {
        //接收文件存储位置
        File file = new File("D:\\P2PDownload");
        fileNum = file.list().length;
        pool = Executors.newFixedThreadPool(THREAD_COUNT);
        pool.submit(new OnLineRecordRunnable());
        pool.submit(new Tracking());
        ServerRunnable serverRunnable = new ServerRunnable();
        pool.submit(serverRunnable);
    }

    static class Tracking implements Runnable {

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(9999)) {
                DatagramPacket request = new DatagramPacket(new byte[SIZE], SIZE);
                System.out.println("追踪器地址：" + InetAddress.getLocalHost().getHostAddress());
                //循环接收，根据message的type判断进行何种操作
                while (true) {
                    socket.receive(request);
                    //获取发送方的ip信息
                    IpInfo ipInfo = new IpInfo(request.getAddress(), request.getPort());
                    byte[] in = Arrays.copyOfRange(request.getData(), 0, request.getLength());
                    //将字节数组转换为对象，获取信息
                    Message message = (Message) ByteUtils.byteToObj(in);
                    if (message.getType() == 0){
                        String info = new String(message.getMsg());
                        String[] inSplit = info.split(":");
                        System.out.println("接收文件名：" + inSplit[0] + " 文件大小：" + inSplit[1]
                                + "字节，文件块数量：" + inSplit[2]);
                        //将接收任务交给子线程来做
                        pool.submit(new ReceiveFilesRunnable(ipInfo,new FileMsg(
                                inSplit[0],Long.parseLong(inSplit[1]),Long.parseLong(inSplit[2]))));
                    }else if (message.getType() == 1){
                        String md5 = new String(message.getMsg());
                        System.out.println(md5);
                        List<String> md5IpList = new ArrayList<>();
                        for (String ip :
                                OnLineRecordRunnable.getOnline().keySet()) {
                            if (OnLineRecordRunnable.getOnline().get(ip).contains(md5)){
                                md5IpList.add(ip);
                            }
                        }
                        System.out.println(md5IpList);
                        byte[] bytes = ByteUtils.objToByte(md5IpList);
                        if (bytes == null){
                            System.out.println("对象转字节失败");
                            return;
                        }
                        DatagramPacket packet = new DatagramPacket(bytes,bytes.length,ipInfo.getIp(),ipInfo.getPort());
                        socket.send(packet);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getFileNum(){
        return fileNum;
    }

    public static synchronized void addFileNum(){
        fileNum ++;
    }
}
