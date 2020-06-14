package s17201319.p2pclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import s17201319.resources.IniRwUtils;
import s17201319.resources.Md5Utils;
import s17201319.resources.Torrent;
import s17201319.resources.TorrentUtils;

import java.io.File;
import java.io.IOException;

/**
 * 创建种子文件
 * @author 17201319
 */
public class GenerateTorrentRunnable implements Runnable {
    private ClientSetting cs = ClientSetting.getClientSetting();
    private final String announce = cs.getTrackingIp() + ":" + cs.getTrackingPort();
    private String torrentPath = cs.getTorrentPath();
    private String filePath;
    private String fileInfo;
    private Label label;

    public GenerateTorrentRunnable(String filePath, String fileInfo, Label label) {
        this.filePath = filePath;
        this.fileInfo = fileInfo;
        this.label = label;
    }

    @Override
    public void run() {//17201319
        upLabel("生成torrent文件中...");
        init();
        Torrent torrent = new Torrent();
        String[] fileInfos = fileInfo.split(":");
        torrent.setAnnounce(announce);
        torrent.setFileName(fileInfos[0]);
        torrent.setFileSize(Integer.parseInt(fileInfos[1]));
        torrent.setFileBlockSize(Integer.parseInt(fileInfos[2]));
        upLabel("生成md5信息中...");
        try {
            torrent.setMd5(Md5Utils.getFileMD5String(new File(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        IniRwUtils.updateMd5Ini(torrentPath + "\\md5.ini",filePath);
        upLabel("生成文件中...");
        TorrentUtils.createTorrent(torrent, torrentPath + "\\" + torrent.getFileName() + ".torrent");
        upLabel("torrent地址：" + torrentPath + "\\" + torrent.getFileName() + ".torrent");
    }
    public void init() {
        File file = new File(torrentPath);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("创建文件夹" + file.mkdir());
        }
    }

    public void upLabel(String s) {
        Platform.runLater(() -> {
            label.setText(s);
        });
    }
}
