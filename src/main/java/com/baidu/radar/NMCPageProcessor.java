package com.baidu.radar;

import org.apache.log4j.Logger;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;

/**
 * Created by edwardsbean on 14-11-28.
 */
public class NMCPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        //提取之后的
        page.addTargetRequests(page.getHtml().links().regex("(http://www.nmc.gov.cn/publish/radar/\\w+\\.htm)").all());
//        page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
        page.putField("城市", page.getUrl().regex("http://www.nmc.gov.cn/publish/radar/(\\w+)\\.htm").toString());
//        page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
        page.putField("时间", page.getHtml().xpath("//li[@class='bottval']/a/span/text()").toString());
        if (page.getResultItems().get("城市")==null){
            //skip this page
            page.setSkip(true);
        }
//        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new NMCPageProcessor())
                .addUrl("http://www.nmc.gov.cn/publish/radar/stationindex.htm")
                .addPipeline(new ConsolePipeline())
                .thread(5)
                .run();
    }
}