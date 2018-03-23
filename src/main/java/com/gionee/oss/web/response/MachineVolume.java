package com.gionee.oss.web.response;

/**
 * Created by yeqy on 2017/8/7.
 */
public class MachineVolume {
    private long totalSpace;
    private long usableSpace;
    private long maxFileSize;
    private long avgFileSize;
    private long fileCount;

    public MachineVolume(long totalSpace, long usableSpace, long maxFileSize, long avgFileSize, long fileCount) {
        this.totalSpace = totalSpace;
        this.usableSpace = usableSpace;
        this.maxFileSize = maxFileSize;
        this.fileCount = fileCount;
        this.avgFileSize = avgFileSize;
    }

    public MachineVolume() {
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public long getAvgFileSize() {
        return avgFileSize;
    }

    public void setAvgFileSize(long avgFileSize) {
        this.avgFileSize = avgFileSize;
    }
}
