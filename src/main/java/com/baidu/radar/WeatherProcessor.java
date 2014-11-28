package com.baidu.radar;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 数据源：
 * 1. http://www.weather.com.cn/static/html/product_ld.shtml
 * 2. http://www.moc.cma.gov.cn/1?p_p_id=moc_radar_WAR_moc_portlet_INSTANCE_vqfu&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_moc_radar_WAR_moc_portlet_INSTANCE_vqfu_struts_action=%2Fmoc%2Fradar%2Fview&_moc_radar_WAR_moc_portlet_INSTANCE_vqfu_x_param=bad_x_value
 * TODO:
 * 1. 雷达时间和雷达图片链接如何对上号
 * 2. 编写一个Pipeline用于下载图片
 * Created by edwardsbean on 14-11-28.
 */
public class WeatherProcessor  implements PageProcessor {

    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        page.putField("城市", page.getHtml().xpath("//h1[@class='weatheH1']/span/strong/text()").toString());
        //TODO 时间和连接要对上号
        page.putField("雷达时间", page.getHtml().xpath("//select[@name='slide3']/option/text()"));
        page.putField("雷达图片链接", page.getHtml().links().regex("(http://i\\.weather\\.com\\.cn/i/product/pic/l/.*)").all());
        if (page.getResultItems().get("城市")==null){
            page.setSkip(true);
        }
        page.addTargetRequests(page.getHtml().xpath("//area/@href").all());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new WeatherProcessor())
                .addUrl("http://www.weather.com.cn/static/html/product_ld.shtml")
                .addPipeline(new ConsolePipeline())
                .thread(5)
                .run();
    }
}
