package s17201319.trackingServer;

import s17201319.p2pclient.BlockInfo;
import s17201319.p2pclient.ClientSetting;
import s17201319.resources.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 发送线程
 * @author 17201319
 */
public class SendRunnable implements Runnable{
    private ClientSetting cs = ClientSetting.getClientSetting();
    private String uploadFilePath;
    //含头含尾
    private int start;
    private int end;
    //上传的文件
    private List<FileBlock> upData = new ArrayList<>();
    //packet的大小
    private final int size = cs.getPacketSize();
    //重传次数
    private final int retransmission = cs.getRetransmission();


    private IpInfo ipInfo;


    private String md5;

    private BlockInfo blockInfo;


    public SendRunnable(IpInfo ipInfo, BlockInfo blockInfo) {
        this.ipInfo = ipInfo;
        this.blockInfo = blockInfo;
        this.start = blockInfo.getStart();
        this.end = blockInfo.getEnd();
        this.md5 = blockInfo.getMd5();
    }

    @Override
    public void run() {
        int sendId = 0;

        try(DatagramSocket socket = new DatagramSocket(0)){
            socket.setSoTimeout(2000);
            DatagramPacket response;
            System.out.println("...");
            byte[] first = (0 + "").getBytes();
            //将收到的序列号发送回去，让接收方向这个端口号发送信息
            response = new DatagramPacket(
                    first, first.length, ipInfo.getIp(), ipInfo.getPort());
            try {
                socket.send(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ipInfo  = new IpInfo(ipInfo.getIp(),ipInfo.getPort());
            uploadFilePath = IniRwUtils.getValueByKey("D:\\P2PDownload\\md5.ini",md5);
            File file = new File(uploadFilePath);
            byte[] infos = new byte[size - 1024];
            System.out.println("处理文件" + file.getName() + "中...");
            System.out.println(file.getPath());
            try(RandomAccessFile raf = new RandomAccessFile(file,"rw")) {
                int num = 0;
                raf.seek((start - 1) * (size - 1024));
                while (num < (end - start + 1)) {
//                    System.out.println(upData.size());
                    num++;
                    int blockLen = raf.read(infos);
                    FileBlock fileBlock = new FileBlock(file.getName(), num + start,
                            Arrays.copyOfRange(infos,0,blockLen));
                    upData.add(fileBlock);
                }
            }catch (IOException e){
                System.out.println("文件冲突");
                e.printStackTrace();
            }
            System.out.println("处理结束");
            //文件块发送失败次数
            int failTime = 0;
            System.out.println(upData.size() + ":" + start + ":" + end);
            //发送该文件所有文件块
            for (int i = 0; i < upData.size(); i++) {
                //多于2个文件块发送失败
                if (failTime > 2){
                    System.out.println("发送线程" + Thread.currentThread().getName() + "可能暂时无法连接服务器，请稍后再试");
//                    upLabel("发送线程" + Thread.currentThread().getName() + "可能暂时无法连接服务器，请稍后再试");
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
                Thread.sleep(10);
//                schedule = String.format("%.2f",(sendId - 1)  * 100.0 / upData.size()) + "%";
//                System.out.println("上传进度：" + String.format("%.2f",(sendId - 1)  * 100.0 / upData.size()) + "%");
            }
            System.out.println("发送线程" + Thread.currentThread().getName() + "结束");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {

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
