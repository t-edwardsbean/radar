package com.baidu.radar.message;

/**
 * Created by edwardsbean on 14-12-2.
 */
public class Image {
    private String fileName;
    private String url;

    public Image(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Image{" +
                "fileName='" + fileName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
