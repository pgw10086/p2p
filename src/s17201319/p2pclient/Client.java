package s17201319.p2pclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import s17201319.resources.TorrentUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 17201319
 */
public class Client extends Application {

    //线程池大小
    private static final int THREAD_COUNT = 1000;
    public static ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    //String[0]为文件路径，[1]为文件大小（B）
    private List<String[]> paths = new ArrayList<>();
    //文件行对应的进度信息
    private List<Label[]> labels = new ArrayList<>();
    private ClientSetting cs = ClientSetting.getClientSetting();
    private static Label linkLabel = new Label();
    private static Label maxLabel = new Label();
    private static ServerRunnable server = new ServerRunnable();
    private static int dlNum = 0;

    @Override
    public void start(Stage primaryStage) {
        GridPane mainPane = new GridPane();
        initGridPane(mainPane);

        GridPane upListPane = new GridPane();
        initGridPane(upListPane);

        GridPane dlPane = new GridPane();
        initGridPane(dlPane);
        GridPane upBtPane = new GridPane();
        initGridPane(upBtPane);
        GridPane dlBtPane = new GridPane();
        initGridPane(dlBtPane);
        GridPane maxLinkPane = new GridPane();
        initGridPane(maxLinkPane);
        GridPane pathPane = new GridPane();
        initGridPane(pathPane);
        mainPane.add(upBtPane, 0, 0);
        mainPane.add(upListPane, 0, 1);
        mainPane.add(dlPane, 0, 3);
        mainPane.add(dlBtPane, 0, 2);
        mainPane.add(maxLinkPane, 0, 4);
        mainPane.add(pathPane, 0, 5);

        Button btFileChooser = new Button("添加上传文件");
        upBtPane.add(btFileChooser, 0, 0);
        btFileChooser.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String[] info = {file.getPath(), String.valueOf(file.length())};
                paths.add(info);
                updateUpList(upListPane);
            }
        });

        Button btClear = new Button("清空上传文件");
        upBtPane.add(btClear, 1, 0);
        btClear.setOnMouseClicked(event -> {
            clearUpList(upListPane);
        });

        Button btUpFiles = new Button("上传所有文件");
        upBtPane.add(btUpFiles, 2, 0);
        btUpFiles.setOnMouseClicked(event -> {
            for (int i = 0; i < paths.size(); i++) {
                String[] fileInfo = paths.get(i);
                pool.submit(new UpLoadRunnable(fileInfo[0], labels.get(i)));
            }
        });

        Button btDownload = new Button("种子文件下载");
        dlBtPane.add(btDownload, 0, 0);
        btDownload.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose torrent");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String info = file.getPath();
                ArrayList<Label> labels = new ArrayList<>();
                labels.add(new Label());
                pool.submit(new DownLoadRunnable(TorrentUtils.analysisTorrent(info), dlPane));

            }
        });

        Button btDlClear = new Button("清空下载列表");
        btDlClear.setOnMouseClicked(event -> {
            dlPane.getChildren().clear();
        });
        dlBtPane.add(btDlClear, 1, 0);

        Button btShutdown = new Button("结束所有线程");
        btShutdown.setOnMouseClicked(event -> {
            server.setFlag(false);
            pool.shutdownNow();
            primaryStage.close();
        });
        mainPane.add(btShutdown, 0, 6);

        maxLinkPane.add(new Label("设置最大连接数:"), 0, 0);
        TextField text = new TextField(String.valueOf(cs.getMaxLink()));
        maxLinkPane.add(text, 1, 0);
        Button btMaxLink = new Button("确认");
        maxLinkPane.add(btMaxLink, 2, 0);
        btMaxLink.setOnMouseClicked(event -> {
            server.setMaxLink(Integer.parseInt(text.getText()));
        });
        maxLinkPane.add(new Label("当前连接数："), 0, 1);
        maxLinkPane.add(linkLabel, 1, 1);
        maxLinkPane.add(new Label("最大连接数："), 0, 2);
        maxLinkPane.add(maxLabel, 1, 2);

        Button btDlPath = new Button("打开下载路径");
        btDlPath.setOnMouseClicked(event -> {
            exec("explorer " + cs.getDownloadPath());
        });
        pathPane.add(btDlPath, 0, 0);

        Button btBtPath = new Button("打开种子文件路径");
        btBtPath.setOnMouseClicked(event -> {
            exec("explorer " + cs.getTorrentPath());
        });
        pathPane.add(btBtPath, 1, 0);

        Scene scence = new Scene(mainPane, 960, 660);
        primaryStage.setTitle("p2p下载客户端");
        primaryStage.setScene(scence);
        primaryStage.show();
    }

    public void updateUpList(GridPane pane) {
        Platform.runLater(() -> {
            if (paths.size() == 1) {
                pane.add(new Label("路径"), 0, 0);
                pane.add(new Label("大小（KB）"), 1, 0);
            }
            String[] fileInfo = paths.get(paths.size() - 1);
            pane.add(new Label(fileInfo[0]), 0, paths.size());
            pane.add(new Label(String.valueOf(Integer.parseInt(fileInfo[1]) / 1024)), 1, paths.size());
            Label label = new Label("0%");
            Label label2 = new Label("");
            labels.add(new Label[]{label, label2});
            pane.add(label, 2, paths.size());
            pane.add(label2, 3, paths.size());
        });
    }

    public void clearUpList(GridPane pane) {
        paths.clear();
        labels.clear();
        Platform.runLater(() -> {
            pane.getChildren().clear();
        });
    }

    public void initGridPane(GridPane pane) {
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setAlignment(Pos.BASELINE_LEFT);
    }

    public static int getDlNum() {
        return dlNum;
    }

    public static void setDlNum(int dlNum) {
        Client.dlNum = dlNum;
    }

    public static void exec(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getServerIp() {
        return server.getServerIp();
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            ClientSetting.getClientSetting().setTrackingIp(args[0]);
        }
        linkLabel.setText(String.valueOf(server.getCurrLink()));
        maxLabel.setText(String.valueOf(server.getMaxLink()));
        server.setLinkLabel(linkLabel);
        server.setMaxLabel(maxLabel);
        pool.submit(server);
        launch();
//        pool.submit(new UpLoadRunnable("D:\\大学作业\\zeal-portable-0.6.1-windows-x64.zip"));
//        pool.submit(new UpLoadRunnable("D:\\百度破解\\EasyU_3.5.2019.0928.7z"));
    }
}
