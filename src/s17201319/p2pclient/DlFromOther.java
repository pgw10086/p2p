package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import s17201319.resources.ByteUtils;
import s17201319.resources.FileBlock;
import s17201319.resources.IpInfo;
import s17201319.resources.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 下载子线程
 * @author 17201319
 */
public class DlFromOther implements Runnable{

    private ClientSetting cs = ClientSetting.getClientSetting();
    //装文件块的列表
    private ArrayList<FileBlock> fileBlocks = new ArrayList<>();
    //包大小
    private final int size = cs.getPacketSize();
    //子线程id
    private int id;
    //连接客户端的ip信息
    private IpInfo ipInfo;
    //下载文件块任务
    private BlockInfo blockInfo;
    //文件块起始位置
    private int start;
    //文件块结束位置
    private int end;
    //识别
    private String md5;
    //传输完成后上传的对象
    private ClientBlock clientBlock;
    //ui
    private Label label;

    public DlFromOther(int id, IpInfo ipInfo, BlockInfo blockInfo, ClientBlock clientBlock, Label label) {
        this.ipInfo = ipInfo;
        this.clientBlock = clientBlock;
        this.id = id;
        this.blockInfo = blockInfo;
        this.start = blockInfo.getStart();
        this.end = blockInfo.getEnd();
        this.md5 = blockInfo.getMd5();
        this.label = label;
    }

    //更新label
    public void upLabel(String s){
        Platform.runLater(() -> {
            label.setText(s);
        });
    }

    @Override
    public void run() {
        DatagramPacket response;
        try(DatagramSocket socket = new DatagramSocket(0)){
            //超时结束接收线程
            socket.setSoTimeout(15000);
            //序列号
            BlockInfo blockInfo = new BlockInfo(start,end,md5);
//            //序列号
            byte[] first = ByteUtils.objToByte(blockInfo);
            //将收到的序列号发送回去，让接收方向这个端口号发送文件
            response = new DatagramPacket(
                    first, first.length, ipInfo.getIp(), ipInfo.getPort());
            try {
                socket.send(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            upLabel("尝试连接中");
            for (int i = 0; i < 3; i++) {
                try{
                    socket.receive(response);
                    ipInfo.setPort(response.getPort());
//                    System.out.println(ipInfo.getPort());
                    break;
                }catch (SocketTimeoutException e){
                    System.out.println(Thread.currentThread().getName());
                    if (i == 2){
                        System.out.println("目前无法连接客户端，请稍后再试");
                        upLabel("目前无法连接客户端，请稍后再试");
                        return;
                    }
                }
            }
            DatagramPacket request = new DatagramPacket(new byte[size], size);
            //直到接收结束
            while (true){
                socket.receive(request);
                byte[] in = Arrays.copyOfRange(request.getData(), 0, request.getLength());
                Message message = (Message) ByteUtils.byteToObj(in);
                FileBlock fileBlock = (FileBlock) ByteUtils.byteToObj(message.getMsg());
                /*文件块id应该大于文件块长度
                    使用确认重传机制，当接收该id并发送回去，发送方才将下一个文件发送过来
                 */
                if (fileBlock.getId() > fileBlocks.size() + start){
                    fileBlocks.add(fileBlock);
                }
                //序列号
                byte[] responseInfo = (message.getId() + "").getBytes();
                //将收到的序列号发送回去
                response = new DatagramPacket(
                        responseInfo, responseInfo.length, ipInfo.getIp(), ipInfo.getPort());
                try {
                    socket.send(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                upLabel(String.format("%.2f",fileBlocks.size() * 100.0 / (end - start + 1)) + "%");
                if (allReceive()){
                    System.out.println("接收完成");
                    upLabel("接收完成");
                    clientBlock.put(id,fileBlocks);
                    break;
                }
            }
            System.out.println("接收线程" + Thread.currentThread().getName() + "结束");
        } catch (SocketException e) {
        } catch (IOException e) {
            System.out.println("接收超时，" + Thread.currentThread().getName() + "结束");
            upLabel("超时");
            clientBlock.upLabel("接收超时，种子文件下载失败");
        }
    }

    public boolean allReceive(){
        if ((end - start + 1) == fileBlocks.size()){
            return true;
        }else {
            return false;
        }
    }
}
