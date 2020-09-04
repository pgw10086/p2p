package s17201319.resources;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
        private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
        private static MessageDigest messagedigest = null;//输出一个固定长度的散列值安全单向散列函数
        static{
            try{
                //使用MD5算法生成
                messagedigest = MessageDigest.getInstance("MD5");
            }catch(NoSuchAlgorithmException nsaex){
//                System.err.println(Md5Utils.class.getName()+"初始化失败，MessageDigest不支持MD5Util。");
                nsaex.printStackTrace();
            }
        }

        public static String getFileMD5String(File file) throws IOException {
            FileInputStream in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            return bufferToHex(messagedigest.digest());
        }

        public static String getMD5String(String s) {
            return getMD5String(s.getBytes());
        }

        public static String getMD5String(byte[] bytes) {
            messagedigest.update(bytes);
            return bufferToHex(messagedigest.digest());
        }

        private static String bufferToHex(byte bytes[]) {
            return bufferToHex(bytes, 0, bytes.length);
        }

        private static String bufferToHex(byte bytes[], int m, int n) {
            StringBuffer stringbuffer = new StringBuffer(2 * n);
            int k = m + n;
            for (int l = m; l < k; l++) {
                appendHexPair(bytes[l], stringbuffer);
            }
            return stringbuffer.toString();
        }


        private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
            char c0 = hexDigits[(bt & 0xf0) >> 4];
            char c1 = hexDigits[bt & 0xf];
            stringbuffer.append(c0);
            stringbuffer.append(c1);
        }

        public static boolean checkMd5(String password, String md5PwdStr) {
            String s = getMD5String(password);
            return s.equals(md5PwdStr);
        }

        public static String getMd5(String path) throws IOException {
            long begin = System.currentTimeMillis();
            File big = new File(path);
            String md5=getFileMD5String(big);
            long end = System.currentTimeMillis();
            return "md5:"+md5+" time:"+((end-begin)/1000)+"s";
        }
//
        public static void main(String[] args) throws IOException {
            System.out.println(getMd5("D:\\P2PDownload\\13_21.png"));
            System.out.println(getMd5("D:\\大学作业\\大三下\\java网络编程\\大作业\\21.png"));
        }



}
