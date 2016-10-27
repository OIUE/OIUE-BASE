import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
 
/**
 * ZK链接
 * 
 * @author jiangzhixiong
 * @email xingxuan_jzx@foxmail.com
 * @date 2015年10月12日 下午5:18:21
 */
public class ZkNodeClient {
    // ip和端口url
    private String url;
    // 需要监听的base path
    private String basePath;
        private static NodeCache nodeCache;
    private static CuratorFramework client = null;
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
 
    public void init() throws Throwable {
        if (basePath == null) {
            basePath = "o2o/zk/cache";
        }
        client = CuratorFrameworkFactory.builder().namespace(basePath).connectString(url).sessionTimeoutMs(5000).connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 0)).build();
        client.start();
        nodeCache = new NodeCache(client, "/");
        /**
         * 监听子节点的变化情况
         */
        watchChild();
         
    }
 
    protected static void watchChild() throws Exception {
         
        nodeCache.getListenable().addListener(new NodeCacheListener() {
 
            @Override
            public void nodeChanged() throws Exception {
                System.out.println(nodeCache.getCurrentData().getPath() + ":" + nodeCache.getCurrentData().getData());
            }
        }, EXECUTOR_SERVICE);
        nodeCache.start();
    }
 
    public String getUrl() {
        return url;
    }
 
    public void setUrl(String url) {
        this.url = url;
    }
 
    public String getBasePath() {
        return basePath;
    }
 
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
 
    public static void main(String[] args) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        client = CuratorFrameworkFactory.builder().namespace("o2o/zk/cache").connectString("192.168.200.98:2181").sessionTimeoutMs(5000).connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
 
        /**
         * 监听子节点的变化情况
         */
        watchChild();
        latch.await();
    }
 
}