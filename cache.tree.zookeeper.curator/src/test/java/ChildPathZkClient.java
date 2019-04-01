import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ZKClient
 * 
 * @author jiangzhixiong
 * @email xingxuan_jzx@foxmail.com
 * @date 2015年10月12日 下午5:18:21
 */
public class ChildPathZkClient {
	// ip和端口url
	private String url;
	// 需要监听的base path
	private String basePath;
	
	private static CuratorFramework client = null;
	private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 100, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());
	
	public void init() {
		if (basePath == null) {
			basePath = "o2o/zk/cache";
		}
		client = CuratorFrameworkFactory.builder().namespace(basePath).connectString(url).sessionTimeoutMs(5000).connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 0)).build();
		client.start();
		/**
		 * 监听子节点的变化情况
		 */
		watchChild("/");
	}
	
	protected static void watchChild(String path) {
		PathChildrenCache childrenCache = new PathChildrenCache(client, path, true, false, executor);
		ZkPathListener listener = new ZkPathListener();
		listener.setPathChildrenCache(childrenCache);
		childrenCache.getListenable().addListener(listener, executor);
		try {
			childrenCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public static void main(String[] args) {
		CountDownLatch latch = new CountDownLatch(1);
		client = CuratorFrameworkFactory.builder().namespace("o2o/zk/cache").connectString("192.168.200.98:2181").sessionTimeoutMs(5000).connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
		client.start();
		
		/**
		 * 监听子节点的变化情况
		 */
		watchChild("/");
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}