import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 * ZK监听器
 * 
 * @author jiangzhixiong
 * @email xingxuan_jzx@foxmail.com
 * @date 2015年10月12日 下午5:18:38
 */
public class ZkPathListener implements PathChildrenCacheListener {
//	@SuppressWarnings("unused")
//	private final Logger logger = Logger.getLogger(ZkPathListener.class);
	@SuppressWarnings("unused")
	private PathChildrenCache pathChildrenCache;
	
	@Override
	public void childEvent(CuratorFramework paramCuratorFramework, PathChildrenCacheEvent event) {
		switch (event.getType()) {
			case CHILD_ADDED:
				// System.out.println(defaultSerializer.deserialize(event.getData().getData()));
				break;
			case CHILD_UPDATED:
				// System.out.println(defaultSerializer.deserialize(event.getData().getData()));
				break;
			case CHILD_REMOVED:
				// System.out.println(defaultSerializer.deserialize(event.getData().getData()));
				break;
			default:
				break;
		}
	}
	
	public void setPathChildrenCache(PathChildrenCache childrenCache) {
		
	}
}