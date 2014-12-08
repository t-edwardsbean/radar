package com.baidu.radar.message;

import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by edwardsbean on 14-12-2.
 */
public class Images {
    //城市名
    private String dirName;
    private List<String> images;

    public Images(String dirName, List<String> images) {
        this.dirName = dirName;
        this.images = images;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public static String getFileName(String url) {
        int begin = url.lastIndexOf("_");
        long time = Long.parseLong(url.substring(begin + 1, url.length() - 9));
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
            Date realTime = DateUtils.addHours(df.parse(time + ""), 8);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmm");
            return df2.format(realTime) + ".gif";
        } catch (Exception e) {
            System.out.println(e);
        }
        return time + ".gif";
    }
}
