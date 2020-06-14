package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import s17201319.resources.FileBlock;
import s17201319.resources.Torrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static s17201319.resources.IniRwUtils.updateMd5Ini;

/**
 * 用于多线程下载，将文件块存储在这个对象中，使用同步锁对多个线程对文件块的修改进行限制
 * @author 17201319
 */
public class ClientBlock {
    private static Map<Integer, ArrayList<FileBlock>> map = new HashMap<>();
    private int blockNum;
    //种子文件信息，包含了文件信息
    private Torrent torrent;
    //下载子线程数量
    private int ipSize;
    private Label label;

    public ClientBlock(int blockNum, Torrent torrent, int ipSize, Label label) {
        this.blockNum = blockNum;
        this.torrent = torrent;
        this.ipSize = ipSize;
        this.label = label;
    }

    //更新label
    public void upLabel(String s){
        Platform.runLater(() -> {
            label.setText(s);
        });
    }

    /**
     * 下载子线程结束后将其接收到的文件块放入map中
     * @param id 子线程id
     * @param fileBlocks 子线程接收的文件块
     */
    public synchronized void put(Integer id, ArrayList<FileBlock> fileBlocks){
        map.put(id,fileBlocks);
        //每次放入时判断是否所有子线程都将文件块放入
        if (map.size() == ipSize){
            ArrayList<FileBlock> list = new ArrayList<>();
            //把所有文件块放入链表中
            for (int n :
                    map.keySet()) {
                list.addAll(map.get(n));
            }
            //文件块长度相同
            if (list.size() == torrent.getFileBlockSize()){
                upLabel("创建文件...");
                createFile(list,torrent);
            }else {
                System.out.println("文件块不完整" + list.size() +":" + torrent.getFileBlockSize());
            }
        }
    }



    /**
     * 创建文件
     * @param fileBlocks 文件的所有文件块
     * @param torrent 包含文件信息的种子文件
     */
    public void createFile(ArrayList<FileBlock> fileBlocks, Torrent torrent) {
        System.out.println("开始创建文件");
        //对list排序
        fileBlocks.sort(FileBlock::compareTo);
        File file = new File(ClientSetting.getClientSetting().getDownloadPath());
        if (!file.exists()){
            System.out.println("创建文件夹" + file.mkdir());
        }
        String filePath = file.getPath() + "\\" + torrent.getFileName();
        //将接收的信息写入文件
        try(FileOutputStream fos = new FileOutputStream(filePath)){
            for (int i = 0; i < torrent.getFileBlockSize(); i++) {
//                    System.out.println(fileBlocks.get(i).getId());
                byte[] in = fileBlocks.get(i).getInfo();
                fos.write(in);
            }
            System.out.println("文件" + torrent.getFileName() + "传输结束");
            upLabel("传输完成");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //添加md5信息，用作文件ID
        updateMd5Ini(ClientSetting.getClientSetting().getMd5Path(),filePath);
    }
}
