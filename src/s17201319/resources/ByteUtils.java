package s17201319.resources;

import java.io.*;

/**
 * 字节数组转换类
 * @author 18716
 */
public final class ByteUtils {
    /**
     * 将字节数组转换为对象
     * @param bytes 对应的字节数组
     * @return 一个Object对象
     */
    public static Object byteToObj(byte[] bytes){
        Object object = null;
        try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            object = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 将对象转换为字节数组
     * @param object 要转换的对象
     * @return 字节数组
     */
    public static byte[] objToByte(Object object){
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
