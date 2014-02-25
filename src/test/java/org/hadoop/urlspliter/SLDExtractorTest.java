package org.hadoop.urlspliter;

import org.junit.Assert;
import org.junit.Test;

public class SLDExtractorTest {
    @Test
    public void Test(){
        Assert.assertEquals(SLDExtractor.getInstace().extract("http://bj.58.com.cn/aaaa.html?asfadsfasd"), "58.com.cn");
        Assert.assertEquals(SLDExtractor.getInstace().extract("http://bj.58.com/aaaa.html?asfadsfasd"), "58.com");
        Assert.assertEquals(SLDExtractor.getInstace().extract("http://www.edu.cn"), "www.edu.cn");
        Assert.assertEquals(SLDExtractor.getInstace().extract("https://www.edu.cn"), "www.edu.cn");
        Assert.assertEquals(SLDExtractor.getInstace().extract("www.edu.cn/"), "www.edu.cn");
        Assert.assertEquals(SLDExtractor.getInstace().extract("http://www.edu.cn/?p=http://www.baidu.com"), "www.edu.cn");
        Assert.assertEquals(SLDExtractor.getInstace().extract("https://10.189.192.134"), "EMPTY");
        Assert.assertEquals(SLDExtractor.getInstace().extract("https://www.xlvector.cn"), "xlvector.cn");
    }
}
