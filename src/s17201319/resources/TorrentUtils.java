package s17201319.resources;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TorrentUtils {

    public static void createTorrent(Torrent torrent,String btPath){
        try(PrintWriter pw = new PrintWriter(new FileOutputStream(btPath))){
            pw.println("announce=" + torrent.getAnnounce());
            pw.println("name=" + torrent.getFileName());
            pw.println("size=" + torrent.getFileSize());
            pw.println("blockSize=" + torrent.getFileBlockSize());
            pw.println("md5=" + torrent.getMd5());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Torrent analysisTorrent(String path){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            Map<String,String> map = new HashMap<>();
            String info;
            while ((info = br.readLine()) != null){
                String[] s = info.split("=");
                map.put(s[0],s[1]);
            }
            return new Torrent(map.get("announce"),map.get("name"), Integer.parseInt(map.get("size")),
                    Integer.parseInt(map.get("blockSize")),map.get("md5"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }
}
