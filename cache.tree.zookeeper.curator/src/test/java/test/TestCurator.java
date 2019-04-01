package test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class TestCurator {
	CuratorFramework client;
	
	public TestCurator() {
		String url = "";
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 1);
		client = CuratorFrameworkFactory.newClient(url, retryPolicy);
		client.start();
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				System.out.println();
			}
			
		});
	}
}
