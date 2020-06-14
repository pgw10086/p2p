package s17201319.trackingServer;

public class FileMsg {
    //文件名
    private String name;
    private long totalLength;
    private long size;

    public FileMsg(String name, long totalLength, long size) {
        this.name = name;
        this.totalLength = totalLength;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
