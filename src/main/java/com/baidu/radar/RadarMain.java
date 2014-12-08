package com.baidu.radar;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.baidu.radar.actor.DispatcherActor;
import com.baidu.radar.spider.ImagePipeline;
import com.baidu.radar.spider.WeatherProcessor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by edwardsbean on 14-12-2.
 */
public class RadarMain {
    public static ActorSystem system;
    public static ApplicationContext applicationContext;
    public static Config config;
    public static final Logger log = LoggerFactory.getLogger(RadarMain
            .class);

    public static void main(String[] args) {
        try {
            config = ConfigFactory.load();
            system = ActorSystem.create("actorSystem", config);
            final ActorRef dispatcher = system.actorOf(Props.create(DispatcherActor.class), "dispatcherActor");
            Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
                @Override
                public void run() {
                    log.info("关闭actorSystem");
                    dispatcher.tell(PoisonPill.getInstance(), ActorRef.noSender());
                    system.awaitTermination();
                }
            });
            System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
            applicationContext = new ClassPathXmlApplicationContext("quartz.xml");
        } catch (Exception e) {
            log.error("程序异常退出", e);
        }
    }
}
