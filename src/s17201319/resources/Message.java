package s17201319.resources;

import java.io.Serializable;

/**
 * @author 18716
 */
public class Message implements Serializable {
    private String fileName;
    private long id;//唯一id
    private int type;//类型0预上传信息，1欲下载信息
    private byte[] msg;//发送的内容

    public Message(String fileName, long id, int type, byte[] msg) {
        this.fileName = fileName;
        this.id = id;
        this.type = type;
        this.msg = msg;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
