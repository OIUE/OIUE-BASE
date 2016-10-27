package org.oiue.service.cache.tree.zookeeper.curator;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.cache.tree.ChangeEvent;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.json.JSONUtil;

@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class CacheTreeServiceImpl implements CacheTreeService {

    private Logger logger;
    private CuratorFramework client = null;
    private String nameSpace = "LeAutoSystemService";
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 100, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    private Map<String, List<?>> localCache = new ConcurrentHashMap<>();

    public CacheTreeServiceImpl(LogService logService) {
        logger = logService.getLogger(getClass());
    }

    public void updated(Dictionary props){
        logger.info("props=" + props);
        String zk_connection_string = props.get("zookeeperConnStr") + "";
        int sessionTimeout = 5000;
        try {
            sessionTimeout = Integer.parseInt(props.get("sessionTimeout") + "");
        } catch (Throwable e) {
        }
        int connectionTimeout = 5000;
        try {
            connectionTimeout = Integer.parseInt(props.get("connectionTimeout") + "");
        } catch (Throwable e) {
        }
        int reConnection = 5000;
        try {
            reConnection = Integer.parseInt(props.get("reConnection") + "");
        } catch (Throwable e) {
        }
        logger.info("connect to zookeeper:connection :[" + zk_connection_string + "],session time out :[" + sessionTimeout + "],reconnection time :[" + reConnection + "]");
        client = CuratorFrameworkFactory.builder().connectString(zk_connection_string).sessionTimeoutMs(sessionTimeout).connectionTimeoutMs(connectionTimeout).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println("stateChanged:client="+client+",newState="+newState);
            }

        });
    }

    @Override
    public String create(String path, Object data) {
        byte[] byteData;
        if (data instanceof String) {
            byteData = ((String) data).getBytes();
        } else {
            byteData = JSONUtil.getJSONString(data).getBytes();
        }

        String newpath = path;
        try {
            client.create().withMode(CreateMode.PERSISTENT).forPath(newpath, byteData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // 创建一个有序的 ZNode
        if (logger.isInfoEnabled())
            logger.info("create zookeeper node ({} => {})" + "|" + newpath + "|" + data);
        return newpath;
    }

    @Override
    public String createTemp(String path, Object data) {
        byte[] byteData;
        if (data instanceof String) {
            byteData = ((String) data).getBytes();
        } else {
            byteData = JSONUtil.getJSONString(data).getBytes();
        }

        String newpath = path;
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(newpath, byteData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // 创建一个临时性且有序的 ZNode
        if (logger.isInfoEnabled())
            logger.info("create zookeeper node ({} => {})" + "|" + newpath + "|" + data);
        return newpath;
    }

    @Override
    public List<Object> getChildren(final String path) {
        List<Object> dataList;
        try {
            List<String> nodeList = client.getChildren().usingWatcher(new Watcher() {

                @Override
                public void process(WatchedEvent event) {

                }
            }).forPath(path);
            dataList = new ArrayList<>(); // 用于存放 path 所有子节点中的数据
            for (String node : nodeList) {
                // 获取path的子节点中的数据
//                byte[] data = null;
//                String str = new String(data);
                String str = node;
                dataList.add(str.startsWith("{") ? JSONUtil.parserStrToMap(str) : str.startsWith("[") ? JSONUtil.parserStrToList(str) : str);
            }
            localCache.put(path, dataList);
            if (logger.isInfoEnabled()) {
                logger.info("node data: {}" + dataList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dataList;
    }
    
    public byte[] getData(final String path) {
        byte[] data;
        try {
            data = client.getData().usingWatcher(new Watcher() {
                
                @Override
                public void process(WatchedEvent event) {
                    
                }
            }).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public List<Object> getChildren(final String path, final ChangeEvent changeEvent) {
        List<Object> dataList;
        try {
            List<String> nodeList = client.getChildren().usingWatcher(new Watcher() {

                @Override
                public void process(WatchedEvent event) {
//                                      changeEvent.change(nodeList);
                }
            }).forPath(path);
            dataList = new ArrayList<>(); // 用于存放 path 所有子节点中的数据
            for (String node : nodeList) {
                // 获取path的子节点中的数据
                byte[] data = null;
                String str = new String(data);

                dataList.add(str.startsWith("{") ? JSONUtil.parserStrToMap(str) : str.startsWith("[") ? JSONUtil.parserStrToList(str) : str);
            }
            if (logger.isInfoEnabled()) {
                logger.info("node data: {}" + dataList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dataList;
    }

    @Override
    public boolean setData(String path, byte[] data, int version) {
        return false;
    }

    @Override
    public boolean delete(String path, int version) {
        String newpath = path;
        try {
            client.delete().deletingChildrenIfNeeded().forPath(newpath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // 创建一个临时性且有序的 ZNode
        if (logger.isInfoEnabled())
            logger.info("delete zookeeper node ({} => {})" + "|" + newpath );
        return false;
    }

    @Override
    public boolean registerChangeEvent(final String path, final String eventName, final ChangeEvent changeEvent) {
        List<String> nodeList;
        try {
            //            nodeList = zk.getChildren(path, new Watcher() {
            //                @Override
            //                public void process(WatchedEvent event) {
            //                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
            //                        getChildren(path, changeEvent); // 若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
            //                    }
            //                }
            //            });
            //            List<String> dataList = new ArrayList<>(); // 用于存放 path 所有子节点中的数据
            //            for (String node : nodeList) {
            //                byte[] data = zk.getData(path + "/" + node, false, null); // 获取
            //                                                                          // path
            //                                                                          // 的子节点中的数据
            //                dataList.add(new String(data));
            //            }
            //            if (logger.isInfoEnabled()) {
            //                logger.info("node data: {}" + dataList);
            //            }
            //            changeEvent.change(nodeList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean unRegisterChangeEvent(String path, String eventName) {
        return false;
    }

    @Override
    public void stop() {
        if (client != null)
            client.close();
    }

}
