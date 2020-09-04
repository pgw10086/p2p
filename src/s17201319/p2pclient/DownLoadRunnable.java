package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import s17201319.resources.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;


/**
 * 下载线程，从追踪器获取ip链表并创建下载子线程
 * @author 17201319
 */
public class DownLoadRunnable implements Runnable{
    private ClientSetting cs = ClientSetting.getClientSetting();
    private int size = cs.getPacketSize();
//    private String ip = cs.getTrackingIp();
//    private int port = cs.getTrackingPort();
    private String ip;
    private int port;
    private Torrent torrent;
    //每个下载接收文件块的数量
    private int blockNum;
    //面板
    private GridPane gridPane;


    public DownLoadRunnable(Torrent torrent, GridPane gridPane) {
        this.torrent = torrent;
        this.gridPane = gridPane;
        String[] ipInfo = torrent.getAnnounce().split(":");
        ip = ipInfo[0];
        port = Integer.parseInt(ipInfo[1]);
    }

    @Override
    public void run() {//17201319
        Client.setDlNum(Client.getDlNum() + 1);
        if (IniRwUtils.getValueByKey(cs.getMd5Path() ,torrent.getMd5()) != null){
            Label infoLabel = new Label("md5.ini中存在" + torrent.getFileName() + "md5值\n"
            + "请打开种子文件目录的.ini确认文件所在路径");
            Platform.runLater(() -> gridPane.add(infoLabel,0,0));
            return;
        }
        try(DatagramSocket socket = new DatagramSocket(0)) {
            socket.setSoTimeout(5000);
            Message message = new Message(torrent.getFileName(),0,1,torrent.getMd5().getBytes());
            byte[] bytes = ByteUtils.objToByte(message);
            if (bytes == null){
                System.out.println("对象转字节失败");
                return;
            }
            DatagramPacket packet = new DatagramPacket(
                    bytes,bytes.length, InetAddress.getByName(ip),port);
            socket.send(packet);
            DatagramPacket response = new DatagramPacket(new byte[size],size);
            Label infoLabel = new Label();
            Platform.runLater(() -> gridPane.add(infoLabel,(Client.getDlNum() - 1) * 2,0));
            upLabel(infoLabel,"尝试连接追踪器...");
            for (int i = 0; i < 3; i++) {
                try{
                    socket.receive(response);
                    break;
                }catch (SocketTimeoutException e){
                    System.out.println(Thread.currentThread().getName());
                    if (i == 2){
                        System.out.println("目前无法连接客户端，请稍后再试");
                        upLabel(infoLabel, torrent.getFileName() + "下载失败");
                        return;
                    }
                }
            }
            List<String> list = (List<String>) ByteUtils.byteToObj(Arrays.copyOfRange(response.getData(),0,response.getLength()));
            //除去自己的IP
            list.remove(Client.getServerIp());
            System.out.println("含有种子文件的在线客户端：" + list);
            if (list.size() == 0){
                upLabel(infoLabel,"无可下载客户端");
            }else {
                upLabel(infoLabel,"下载文件：" + torrent.getFileName());
            }
            if (torrent.getFileBlockSize() < list.size()){
                ArrayList<Label> labels = new ArrayList<>();
                blockNum = 1;
                for (int i = 0; i < torrent.getFileBlockSize(); i++) {
                    labels.add(new Label());
                }

                //文件块存储对象
                ClientBlock clientBlock = new ClientBlock(blockNum,torrent,torrent.getFileBlockSize(),infoLabel);
                for (int i = 1; i <= torrent.getFileBlockSize(); i++) {
                    //将ip放在面板上
                    addIpJd(list.get(i - 1),i,labels.get(i - 1));
                    BlockInfo blockInfo;
                    //为最后一个接收子线程
                    if (i == list.size()){
                        blockInfo = new BlockInfo((i - 1) * blockNum + 1,torrent.getFileBlockSize(),torrent.getMd5());
                    }else {
                        blockInfo = new BlockInfo((i - 1) * blockNum + 1,i * blockNum,torrent.getMd5());
                    }
                    //提交线程到线程池中
                    Client.pool.submit(new DlFromOther(i,IpInfo.createIpInfo(list.get(i - 1)),blockInfo,clientBlock,labels.get(i -1)));
                }
            }else {
                blockNum = torrent.getFileBlockSize() / list.size();
                if (torrent.getFileBlockSize() % list.size() > 0){
                    blockNum ++;
                }
                ArrayList<Label> labels = new ArrayList<>();

                for (int i = 0; i < list.size(); i++) {
                    labels.add(new Label());
                }

                //文件块存储对象
                ClientBlock clientBlock = new ClientBlock(blockNum,torrent,list.size(),infoLabel);
                for (int i = 1; i <= list.size(); i++) {
                    //将ip放在面板上
                    addIpJd(list.get(i - 1),i,labels.get(i - 1));
                    BlockInfo blockInfo;
                    //为最后一个接收子线程
                    if (i == list.size()){
                        blockInfo = new BlockInfo((i - 1) * blockNum + 1,torrent.getFileBlockSize(),torrent.getMd5());
                    }else {
                        blockInfo = new BlockInfo((i - 1) * blockNum + 1,i * blockNum,torrent.getMd5());
                    }
                    //提交线程到线程池中
                    Client.pool.submit(new DlFromOther(i,IpInfo.createIpInfo(list.get(i - 1)),blockInfo,clientBlock,labels.get(i -1)));
                }
            }

        } catch (SocketException e) {
            System.out.println("异常结束");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addIpJd(String ip,int i,Label label){
        Platform.runLater(() -> {
            int num = Client.getDlNum();
            gridPane.add(new Label(ip),(num - 1) * 2,i);
            gridPane.add(label,(num - 1) * 2 + 1,i);
        });
    }

    //更新label
    public void upLabel(Label label,String s){
        Platform.runLater(() -> {
            label.setText(s);
        });
    }
}
