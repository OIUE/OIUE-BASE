import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
 
/**
 * ZK链接
 * 
 * @author jiangzhixiong
 * @email xingxuan_jzx@foxmail.com
 * @date 2015年10月12日 下午5:18:21
 */
public class TreeClient {
    // ip和端口url
    private String url;
    // 需要监听的base path
    private String basePath;
 
    private static CuratorFramework client = null;
    private static TreeCache cache = null;
    private static ZkTreeListener listener = new ZkTreeListener();
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
 
    public void init() throws Throwable {
        if (basePath == null) {
            basePath = "o2o/zk/cache";
        }
        // 修改重连次数，使用最初的线程进行重连监听，不重新新建线程 ExponentialBackoffRetry(1000, 0)
        client = CuratorFrameworkFactory.builder().namespace(basePath).connectString(url).sessionTimeoutMs(5000).connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 10)).build();
        client.start();
        /**
         * 监听子节点的变化情况
         */
        watchChild("/product");
        watchChild("/switch");
 
    }
 
    protected static void watchChild(String path) throws Exception {
        // 改用TreeCacheListener，免除循环监听子节点的问题
        cache = new TreeCache(client, path);
        cache.getListenable().addListener(listener, executorService);
        cache.start();
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
                .retryPolicy(new ExponentialBackoffRetry(1000, 0)).build();
        client.start();
 
        /**
         * 监听子节点的变化情况
         */
        watchChild("/product");
                watchChild("/switch");
        latch.await();
    }
 
}