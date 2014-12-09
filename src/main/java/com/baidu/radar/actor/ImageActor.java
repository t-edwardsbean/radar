package com.baidu.radar.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.baidu.radar.message.Image;
import com.baidu.radar.tools.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import scala.Option;
import scala.concurrent.duration.Duration;
import scala.runtime.AbstractFunction0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 由ImageActor负责下载图片
 * Created by edwardsbean on 14-12-2.
 */
public class ImageActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private DefaultHttpClient httpClient = new DefaultHttpClient();

    public ImageActor() {
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Image) {
            Image image = (Image) message;
            downloadImage(image);
        } else {
            unhandled(message);
        }
    }

    private void downloadImage(Image image) throws IOException {
        HttpGet httpGet = new HttpGet(image.getUrl());
        File file = new File(image.getFileName());
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
             outputStream = new FileOutputStream(file);
             inputStream = response.getEntity().getContent();
            byte buff[] = new byte[4096];
            int counts;
            while ((counts = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, counts);
            }
            log.debug("该图片下载完毕");
        } catch (IOException e) {
            log.warning("图片文件保存失败,", e);
            throw e;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warning("文件流无法关闭");
            }
            httpGet.releaseConnection();
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
