package com.baidu.radar.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.baidu.radar.message.Image;
import com.baidu.radar.tools.HttpUtil;
import org.apache.http.HttpResponse;
import scala.Option;
import scala.concurrent.duration.Duration;
import scala.runtime.AbstractFunction0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 由ImageActor负责下载图片
 * Created by edwardsbean on 14-12-2.
 */
public class ImageActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private HttpUtil httpUtil = new HttpUtil();

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Image) {
            Image image = (Image) message;
            downloadImage(image);
        } else {
            unhandled(message);
        }
    }

    private void downloadImage(Image image) throws Exception {
        HttpResponse response = httpUtil.doGet(image.getUrl(), null);
        File file = new File(image.getFileName());
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = response.getEntity().getContent();
            byte buff[] = new byte[4096];
            int counts;
            while ((counts = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, counts);
            }
            inputStream.close();
            outputStream.close();
            httpUtil.releaseConnection();
            log.debug("该图片下载完毕");
        } catch (Exception e) {
            log.warning("图片文件保存失败,", e);
        }
    }

    @Override
    public void preStart() throws Exception {
        log.info("preStart");
    }

    @Override
    public void postStop() throws Exception {
        log.info("postStop");
        super.postStop();
    }


    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        Object m = message.getOrElse(new AbstractFunction0<Object>() {
            @Override
            public Object apply() {
                return "空消息";
            }
        });
        log.warning("imageActor Restarting due to [{}] when processing [{}]", reason.getMessage(), m.toString());
        //访问外网失败，延迟3s,重新访问
        ActorSystem system = getContext().system();
        system.scheduler().scheduleOnce(Duration.apply(3, TimeUnit.SECONDS),
                getSelf(), m, system.dispatcher(), ActorRef.noSender());
    }
}
