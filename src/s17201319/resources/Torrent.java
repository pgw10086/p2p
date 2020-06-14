package s17201319.resources;

public class Torrent {
    //trackingServer地址
    private String announce;
    //文件名
    private String fileName;
    //文件大小
    private int fileSize;
    //文件块数量
    private int fileBlockSize;
    //md5校验值
    private String md5;

    public Torrent() {
    }

    public Torrent(String announce, String fileName, int fileSize, int fileBlockSize, String md5) {
        this.announce = announce;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileBlockSize = fileBlockSize;
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileBlockSize() {
        return fileBlockSize;
    }

    public void setFileBlockSize(int fileBlockSize) {
        this.fileBlockSize = fileBlockSize;
    }


}
