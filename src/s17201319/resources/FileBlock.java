package s17201319.resources;

import java.io.Serializable;

/**
 * 文件块
 * @author 17201319
 */
public class FileBlock implements Comparable<FileBlock>, Serializable {
    //文件名
    private String name;
    //文件块唯一ID
    private long id;
    //文件块内容
    private byte[] info;

    public FileBlock() {
    }

    public FileBlock(String name, long id, byte[] info) {
        this.name = name;
        this.id = id;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getInfo() {
        return info;
    }

    public void setInfo(byte[] info) {
        this.info = info;
    }

    @Override
    public int compareTo(FileBlock o) {
        if (o.getId() > this.getId()){
            return 1;
        }else if (o.getId() == this.getId()){
            return 0;
        }else {
            return 0;
        }
    }
}
