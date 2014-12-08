package com.baidu.radar.schedule;

import akka.actor.ActorSelection;
import com.baidu.radar.RadarMain;
import com.baidu.radar.spider.ImagePipeline;
import com.baidu.radar.spider.WeatherProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;

/**
 * Created by edwardsbean on 14-12-2.
 */
public class SpiderJob {
    public static final Logger log = LoggerFactory.getLogger(SpiderJob.class);

    public void run() {
        try {
            log.info("定时调度,抓取雷达数据");
            ActorSelection dispatcher = RadarMain.system.actorSelection("akka://actorSystem/user/dispatcherActor");
            Spider.create(new WeatherProcessor())
                    .addUrl(RadarMain.config.getString("weather-index"))
                    .addPipeline(new ImagePipeline().setDownloader(dispatcher))
                    .thread(3)
                    .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(1000)))
                    .run();
            log.info("定时调度：图片链接采集完毕");
        } catch (Exception e) {
            log.warn("调度过程中发生异常", e);
        }
    }
}
