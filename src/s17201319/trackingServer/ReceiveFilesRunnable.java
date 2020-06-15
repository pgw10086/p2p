package s17201319.trackingServer;

import s17201319.resources.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import static s17201319.resources.IniRwUtils.updateMd5Ini;

/**
 * 接收子线程
 * @author 17201319
 */
public class ReceiveFilesRunnable implements Runnable {
    //发送方的ip信息
    private IpInfo ipInfo;
    //包文件大小
    private final int SIZE = 1024 * 63;
    //装文件块的列表
    private ArrayList<FileBlock> fileBlocks = new ArrayList<>();
    //文件信息，为了生成文件块数量等信息
    private FileMsg fileMsg;

    public ReceiveFilesRunnable(IpInfo ipInfo, FileMsg fileMsg) {
        this.ipInfo = ipInfo;
        this.fileMsg = fileMsg;
    }

    @Override
    public void run() {
        DatagramPacket response;
        try(DatagramSocket socket = new DatagramSocket(0)){
            //超时结束接收线程
            socket.setSoTimeout(15000);
            //序列号
            byte[] first = (0 + "").getBytes();
            //将收到的序列号发送回去，让接收方向这个端口号发送文件
            response = new DatagramPacket(
                    first, first.length, ipInfo.getIp(), ipInfo.getPort());
            try {
                socket.send(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DatagramPacket request = new DatagramPacket(new byte[SIZE], SIZE);
            //直到接收结束
            while (true){
                socket.receive(request);
                byte[] in = Arrays.copyOfRange(request.getData(), 0, request.getLength());
                Message message = (Message) ByteUtils.byteToObj(in);
                FileBlock fileBlock = (FileBlock) ByteUtils.byteToObj(message.getMsg());
//                System.out.println(fileBlock.getId() + "  " + fileBlocks.size());
                //。。。
                if (fileBlock.getId() > fileBlocks.size()){
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
                if (allReceive()){
                    createFile();
                    break;
                }
            }
            System.out.println("接收线程" + Thread.currentThread().getName() + "结束");
        } catch (SocketException e) {
        } catch (IOException e) {
            System.out.println("接收超时，" + Thread.currentThread().getName() + "结束");
        }
    }

    public void createFile() {
        //对list排序
        fileBlocks.sort(FileBlock::compareTo);
        File file = new File("D:\\P2PDownload");
        if (!file.exists()){
            System.out.println("创建文件夹" + file.mkdir());
        }
        String fileName = file.getPath() + "\\" +
                TrackingDevice.getFileNum() + "_" + fileMsg.getName();
        //将接收的信息写入文件
        try(FileOutputStream fos = new FileOutputStream(fileName)){
            for (int i = 0; i < fileMsg.getSize(); i++) {
//                    System.out.println(fileBlocks.get(i).getId());
                byte[] in = fileBlocks.get(i).getInfo();
                fos.write(in);
            }
            System.out.println("文件" + TrackingDevice.getFileNum() + "_" +
                    fileMsg.getName() + "传输结束");
            TrackingDevice.addFileNum();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //添加md5信息，用作文件ID
        updateMd5Ini(file.getPath() + "\\" + "md5.ini",fileName);
    }

    public boolean allReceive(){
        if (fileMsg.getSize() == fileBlocks.size()){
            return true;
        }else {
            return false;
        }
    }
}
