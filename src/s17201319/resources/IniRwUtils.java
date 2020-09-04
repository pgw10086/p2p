package s17201319.resources;

import s17201319.p2pclient.Client;
import s17201319.p2pclient.ClientSetting;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class IniRwUtils {

    public static void updateMd5Ini(String md5Path,String filePath){

        File md5File = new File(md5Path);
        if (!md5File.exists()){
            try {
                System.out.println("创建文件" + md5File.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try(RandomAccessFile raf = new RandomAccessFile(md5File,"rw")) {
            String info = Md5Utils.getFileMD5String(new File(filePath)) + "=" + filePath + "\r\n";
            raf.seek(raf.length());
            raf.write(info.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //根据Key读取Value
    public static String getValueByKey(String md5Path, String key) {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(md5Path)))) {
            String info;
            while ((info = br.readLine()) != null){
                String[] fileInfo = info.split("=");
                if (fileInfo[0].equals(key)){
                    return fileInfo[1];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //读取Properties的全部信息
    public static Map<String,String> getAllProperties(String md5Path) throws IOException {
        File file = new File(md5Path);
        File p = new File(ClientSetting.getClientSetting().getTorrentPath());
        if (!p.exists()){
            p.mkdirs();
        }
        if (!file.exists()){
            file.createNewFile();
        }
        Map<String,String> map = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(md5Path)))) {
            String info;
            while ((info = br.readLine()) != null){
                String[] fileInfo = info.split("=");
                map.put(fileInfo[0],fileInfo[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("另一个程序正在使用此文件，进程无法访问。");
        }
        return map;
    }
//
//    public static void main(String[] args) throws IOException {
//        System.out.println(getValueByKey("D:\\P2PDownload\\md5.ini","c7e088b47d6695020d9879c14e592411"));
//    }
}
