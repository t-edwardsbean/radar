package com.baidu.radar.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

/**
 * 数据源：
 * 1. http://www.weather.com.cn/static/html/product_ld.shtml
 * 2. http://www.moc.cma.gov.cn/1?p_p_id=moc_radar_WAR_moc_portlet_INSTANCE_vqfu&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_moc_radar_WAR_moc_portlet_INSTANCE_vqfu_struts_action=%2Fmoc%2Fradar%2Fview&_moc_radar_WAR_moc_portlet_INSTANCE_vqfu_x_param=bad_x_value
 * TODO:
 * 1. quartz定时调度，5分钟
 * 2. 编写一个Pipeline用于下载图片，保存到本地
 * 3. 去除已经下载的图片url
 * Created by edwardsbean on 14-11-28.
 */
public class WeatherProcessor implements PageProcessor {

    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(3000)
            .setTimeOut(10000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        //城市名
        page.putField("fileName", page.getHtml().xpath("//h1[@class='weatheH1']/span/strong/text()").toString());
        Selectable result = page.getHtml().xpath("//div[@class='TimezDown']/select/option/@value");
        //城市的雷达图片链接
        page.putField("images", result.regex("(http://i\\.weather\\.com\\.cn/i/product/pic/l/.*)").all());
        if (page.getResultItems().get("fileName") == null) {
            page.setSkip(true);
        }
        page.addTargetRequests(page.getHtml().xpath("//area/@href").all());
    }

    @Override
    public Site getSite() {
        return site;
    }
}
