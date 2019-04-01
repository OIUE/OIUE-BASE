import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * TreeCache ZK监听器
 * 
 * @author jiangzhixiong
 * @email xingxuan_jzx@foxmail.com
 * @date 2015年10月12日 下午5:18:38
 */
public class ZkTreeListener implements TreeCacheListener {
	
	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) {
		System.out.println(event.getData().getPath());
		switch (event.getType()) {
			case NODE_ADDED:
				if (event.getData().getData() == null) {
					break;
				}
				break;
			case NODE_UPDATED:
				if (event.getData().getData() == null) {
					break;
				}
				break;
			default:
				break;
		}
	}
}