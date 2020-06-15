package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import s17201319.resources.ByteUtils;
import s17201319.resources.FileBlock;
import s17201319.resources.IpInfo;
import s17201319.resources.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 上传线程
 * @author 17201319
 */
public class UpLoadRunnable implements Runnable{
    private ClientSetting cs = ClientSetting.getClientSetting();

    private final String uploadFilePath;
    //packet的大小
    private final int SIZE = cs.getPacketSize();
    //重传次数
    private final int retransmission = cs.getRetransmission();
    //上传的文件
    private List<FileBlock> upData = new ArrayList<>();
    //
    private IpInfo ipInfo;

    private String ip = cs.getTrackingIp();
    private int port = cs.getTrackingPort();
    private Label[] label;

    private String schedule = "0%";

    public UpLoadRunnable(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public UpLoadRunnable(String uploadFilePath, Label[] label) {
        this.uploadFilePath = uploadFilePath;
        this.label = label;
        upLabel(schedule);
    }

    public void upLabel(String s){
        Platform.runLater(() -> {
            label[0].setText(s);
        });
    }

    @Override
    public void run() {
        int sendId = 0;
        try(DatagramSocket socket = new DatagramSocket(0)){
            socket.setSoTimeout(5000);
            ipInfo  = new IpInfo(InetAddress.getByName(ip),port);
            File file = new File(uploadFilePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] infos = new byte[SIZE - 1024];
            //文件名，文件长度，块大小
            StringBuilder sb = new StringBuilder();
            sb.append(file.getName());
            sb.append(":");
            sb.append(file.length());
            sb.append(":");
            //将文件读取到内存中，并分块
            int len;
            System.out.println("处理文件" + file.getName() + "中...");
            upLabel("处理文件中...");
            while ((len = fis.read(infos)) != -1){
                FileBlock fileBlock = new FileBlock(file.getName(),upData.size() + 1,
                        Arrays.copyOfRange(infos,0,len));
                upData.add(fileBlock);
            }
            fis.close();
            upLabel("处理结束");
            sb.append(upData.size());
            Message message = new Message(file.getName(),sendId,0,sb.toString().getBytes());
            byte[] bytes = ByteUtils.objToByte(message);
            if (bytes == null){
                System.out.println("对象转字节失败");
                return;
            }
            DatagramPacket fileInfoPacket = new DatagramPacket(
                    bytes,bytes.length,ipInfo.getIp(),ipInfo.getPort());
            socket.send(fileInfoPacket);
            DatagramPacket response = new DatagramPacket(new byte[SIZE],SIZE);
            upLabel("链接追踪器...");
            for (int i = 0; i < 3; i++) {
                try{
                    socket.receive(response);
                    ipInfo.setPort(response.getPort());
                    sendId ++;
                    System.out.println(ipInfo.getPort());
                    break;
                }catch (SocketTimeoutException e){
                    System.out.println(Thread.currentThread().getName());
                    if (i == 2){
                        System.out.println("目前无法连接服务器，请稍后再试");
                        upLabel("目前无法连接服务器，请稍后再试");
                        return;
                    }
                }
            }
            //文件块发送失败次数
            int failTime = 0;
            //发送该文件所有文件块
            for (int i = 0; i < upData.size(); i++) {
                //多于2个文件块发送失败
                if (failTime > 2){
                    System.out.println("发送线程" + Thread.currentThread().getName() + "可能暂时无法连接服务器，请稍后再试");
                    upLabel("可能暂时无法连接服务器，请稍后再试");
                    break;
                }
                Message block = new Message(file.getName(),sendId,1, ByteUtils.objToByte(upData.get(i)));
                byte[] info = ByteUtils.objToByte(block);
                DatagramPacket fileBlock = new DatagramPacket(
                        info,info.length,ipInfo.getIp(),ipInfo.getPort());
                if (!sendFile(socket,fileBlock,0)){
                    i --;
                    failTime ++;
                }else {
                    sendId ++;
                    failTime = 0;
                }
                schedule = String.format("%.2f",(sendId - 1)  * 100.0 / upData.size()) + "%";
                upLabel(schedule);
//                System.out.println("上传进度：" + String.format("%.2f",(sendId - 1)  * 100.0 / upData.size()) + "%");
            }

            upLabel("传输完成");
            System.out.println("发送线程" + Thread.currentThread().getName() + "结束");
            System.out.println("开始生成torrent文件");
            Client.pool.submit(new GenerateTorrentRunnable(uploadFilePath,sb.toString(),label[1]));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendFile(DatagramSocket socket,DatagramPacket fileInfoPacket,int n) throws IOException {
        if (n > retransmission){
            return false;
        }
        socket.send(fileInfoPacket);
        n ++;
        try {
            socket.receive(fileInfoPacket);
        }catch (SocketTimeoutException e){
            System.out.println("发送线程" + Thread.currentThread().getName() + "服务器未响应，重新发送" + n + "次");
            //超时重传
            return sendFile(socket, fileInfoPacket, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
