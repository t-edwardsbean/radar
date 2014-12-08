package com.baidu.radar.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import com.baidu.radar.message.Image;
import com.baidu.radar.message.Images;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;

/**
 * 分发消息，由ImageActor负责下载图片
 * Created by edwardsbean on 14-12-2.
 */
public class DispatcherActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private BloomFilter<CharSequence> bloomFilter;

    private static SupervisorStrategy strategy = new OneForOneStrategy(-1,
            Duration.create("10 second"), new Function<Throwable, SupervisorStrategy.Directive>() {
        public SupervisorStrategy.Directive apply(Throwable t) {
            if (t instanceof ConnectTimeoutException) {
                //网络问题，重试
                return restart();
            } else if (t instanceof SocketTimeoutException) {
                //网络问题，重试
                return restart();
            }else {
                //图片不存在
                return resume();
            }
        }
    }, false);

    Router router;

    {
        List<Routee> routees = new ArrayList<Routee>();
        for (int i = 0; i < 4; i++) {
            ActorRef r = getContext().actorOf(Props.create(ImageActor.class));
            routees.add(new ActorRefRoutee(r));
        }
        router = new Router(new RoundRobinRoutingLogic(), routees);
    }

    @Override
    public void preStart() throws Exception {
        //read bloom filter from file
        File file = new File("bloomfilter" + File.separator + "bloomfilter.data");
        if (file.exists() && file.length() > 0) {
            bloomFilter = (BloomFilter<CharSequence>) SerializationUtils.deserialize(new FileInputStream(file));
        } else {
            log.info("第一次使用，初始化bloomfilter文件");
            File root = new File("bloomfilter");
            root.mkdirs();
            bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.01);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Images) {
            Images images = (Images) message;
            log.debug("为城市创建目录:" + images.getDirName());
            File root = new File("storage" + File.separator + images.getDirName());
            root.mkdirs();
            for (String url : images.getImages()) {
                //2. use bloom filter,image is duplicate
                if (!bloomFilter.mightContain(url)) {
                    //3. dispatcher message
                    bloomFilter.put(url);
                    Image image = new Image(root + File.separator + Images.getFileName(url), url);
                    router.route(image, ActorRef.noSender());
                } else
                    log.debug("该图片已下载过");
            }
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() throws Exception {
        log.info("postStop,save bloom filter");
        File file = new File("bloomfilter" + File.separator + "bloomfilter.data");
        SerializationUtils.serialize(bloomFilter, new FileOutputStream(file));
        getContext().system().shutdown();
    }
}
