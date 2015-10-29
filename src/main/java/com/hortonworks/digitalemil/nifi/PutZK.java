package com.hortonworks.digitalemil.nifi;

import org.apache.curator.framework.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.apache.curator.test.TestingServer;

public class PutZK {

	public static void main(String[] args) {
		putZK(args[0], args[1], args[2], args[3].getBytes());
	}

	public static void putZK(String connection, String path, String name,
			byte[] bytes) {
		CuratorFramework client = null;
		// PathChildrenCache cache = null;
		TestingServer server= null;
		try {
			server = new TestingServer();
			try {
				client = CuratorFrameworkFactory.newClient(connection,
						new ExponentialBackoffRetry(1000, 3));
				client.start();
				String zkpath = ZKPaths.makePath(path, name);

				try {
					client.setData().forPath(zkpath, bytes);
				} catch (KeeperException.NoNodeException e) {
					client.create().creatingParentContainersIfNeeded()
							.forPath(zkpath, bytes);
				}
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				CloseableUtils.closeQuietly(client);
			}
		} catch (Exception e) {

		} finally {
			if(server!= null)
				CloseableUtils.closeQuietly(server);
		}
	}
}