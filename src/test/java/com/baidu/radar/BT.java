package com.baidu.radar;

import com.baidu.radar.message.Images;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by edwardsbean on 14-12-1.
 */
public class BT {
    BloomFilterDuplicateRemover bloomFilterDuplicateRemover;
    BloomFilter<CharSequence> bloomFilter;
    @Before
    public void setUp() throws Exception {
        bloomFilterDuplicateRemover = new BloomFilterDuplicateRemover(1000000);
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.01);

    }

    @Test
    public void testBloomRemover() throws Exception {
        Request request = new Request("http://baidu.com");
        Assert.assertFalse(bloomFilterDuplicateRemover.isDuplicate(request, null));
        Assert.assertTrue(bloomFilterDuplicateRemover.isDuplicate(request, null));

    }

    @Test
    public void testBloom() throws Exception {
        String url = "http://baidu.com";
        boolean isDuplicate = bloomFilter.mightContain(url);
        if (!isDuplicate) {
            bloomFilter.put(url);
        }
        Assert.assertTrue(bloomFilter.mightContain(url));
    }

    @Test
    public void testGetFileName() throws Exception {
        String url = "http://i.weather.com.cn/i/product/pic/l/sevp_aoc_rdcp_sldas_ebref_az9010_l88_pi_20141201015000000.gif";
        System.out.println(Images.getFileName(url));

    }

    @Test
    public void testBloomfilterSerialize() throws Exception {
        testBloom();
        File root = new File("bloomfilter");
        root.mkdirs();
        File file = new File("bloomfilter" + File.separator + "bloomfilter.data");
        SerializationUtils.serialize(bloomFilter, new FileOutputStream(file));
    }

    @Test
    public void testDeserialize() throws Exception {
        File file = new File("bloomfilter" + File.separator + "bloomfilter.data");
        BloomFilter<CharSequence> fileBloomFilter = (BloomFilter<CharSequence>) SerializationUtils.deserialize(new FileInputStream(file));
        String url = "http://baidu.com";
        Assert.assertTrue(fileBloomFilter.mightContain(url));
    }

    @Test
    public void testparseTime() throws Exception {
//        String time = "20141202081000000";
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = format.format(new Date());
        System.out.println(time);
    }

}
