package s17201319.p2pclient;

import java.io.Serializable;

/**
 * 从一个客户端下载的所有文件块编号
 * @author 17201319
 */
public class BlockInfo implements Serializable {
    //编号起始位置
    private int start;
    //编号结束位置
    private int end;
    //文件识别码
    private String md5;

    public BlockInfo(int start, int end, String md5) {
        this.start = start;
        this.end = end;
        this.md5 = md5;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getMd5() {
        return md5;
    }


    @Override
    public String toString() {
        return this.getStart() + ":" + this.getEnd() + ":" + this.getMd5();
    }
}
