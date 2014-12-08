package com.baidu.radar.spider;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.baidu.radar.message.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.Map;

/**
 * Created by edwardsbean on 14-12-1.
 */
public class ImagePipeline implements Pipeline {
    public static final Logger log = LoggerFactory.getLogger(ImagePipeline.class);

    private ActorSelection dispatcher;

    public ImagePipeline setDownloader(ActorSelection dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> items = resultItems.getAll();
        log.debug("准备处理下载雷达图");
        if (items.get("fileName") != null && items.get("images") != null) {
            try {
                dispatcher.tell(new Images(items.get("fileName").toString(), (List<String>) items.get("images")), ActorRef.noSender());
            } catch (Exception e) {
                log.warn("发送消息给dispatcherActor出错", e);
            }
        }
    }
}
