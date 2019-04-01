package org.oiue.service.system.analyzer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.service.system.analyzer.TimeLogger;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("rawtypes")
public class AnalyzerServiceImpl implements AnalyzerService {
	
	public static long _request_time = 3000;
	
	public static LinkedBlockingQueue<Map<String, Object>> run_time_list = new LinkedBlockingQueue<>();
	public static List<Map<String, Object>> run_time_list_tmp = new ArrayList<Map<String, Object>>();
	
	private Logger logger;
	private LogService logService;
	private CacheServiceManager cacheService;
	private String cacheType;
	private String cacheName;
	private String uCacheName;
	private String server_id;
	private Consumer consumer = null;
	@SuppressWarnings("unused")
	private Map props = null;
	static boolean runAnalyzer = true;
	
	public AnalyzerServiceImpl(LogService logService, CacheServiceManager cacheService) {
		this.logger = logService.getLogger(this.getClass());
		this.logService = logService;
		this.cacheService = cacheService;
	}
	
	@Override
	public void updateProps(Map props) {
		this.props = props;
		server_id = MapUtil.getString(props, "server_id", "127.0.0.1");
		cacheType = MapUtil.getString(props, "cacheType", "storage");
		cacheName = MapUtil.getString(props, "cacheName", "05063e95-e477-4f97-b1a8-8661fdafd46c");
		uCacheName = MapUtil.getString(props, "uCacheName", "8a5c5466-c944-4ac9-8e61-7bb9d9fa2246");
		runAnalyzer = MapUtil.getBoolean(props, "runAnalyzer", false);
		try {
			_request_time = Long.valueOf(props.get("request_time") + "");
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		if (runAnalyzer && consumer == null) {
			consumer = new Consumer();
			consumer.setName("SystemAnalyzerService");
			consumer.start();
		}
		if (run_time_list_tmp.size() > 2)
			sortFilter(run_time_list_tmp);
		logger.info(run_time_list_tmp + "");
		run_time_list_tmp = new ArrayList<Map<String, Object>>();
	}
	
	public static void sortFilter(List<Map<String, Object>> filterList) {
		Collections.sort(filterList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				if (MapUtil.getLong(o1, "runTime") > MapUtil.getLong(o2, "runTime")) {
					return -1;
				} else if (MapUtil.getLong(o1, "runTime") < MapUtil.getLong(o2, "runTime")) {
					return 1;
				} else {
					return 0;
				}
			}
		});
	}
	
	@Override
	public TimeLogger getLogger(Class c) {
		return new TimeLoggerImpl(logService);
	}
	
	class Consumer extends Thread {
		@Override
		public void run() {
			while (runAnalyzer) {
				Map per = null;
				try {
					per = run_time_list.take();
					
					String component_instance_event_id = MapUtil.getString(per, "component_instance_event_id");
					if (component_instance_event_id != null) {
						// component_instance_event_id,server_id,para.client_ip,para.user_id,startTime,endTime,para,desc
						List perl = new ArrayList<>();
						Object para = per.get("para");
						perl.add(component_instance_event_id);
						perl.add(server_id);
						perl.add((para instanceof Map) ? MapUtil.getString((Map) para, "client_ip") : null);
						perl.add(MapUtil.getString(per, "user_id"));
						perl.add(MapUtil.getLong(per, "startTime"));
						perl.add(MapUtil.getLong(per, "endTime"));
						perl.add((para instanceof Map) ? JSONUtil.parserToStr((Map) para) : para);
						perl.add(MapUtil.getString(per, "desc"));
						perl.add(MapUtil.getInt(per, "status"));

						String source = MapUtil.getString(per, "source","user");
						cacheService.getCacheService(cacheType).put("user".equals(source)?uCacheName:cacheName, perl, Type.MANY);
					} else {
						long start = MapUtil.getLong(per, "startTime");
						long end = MapUtil.getLong(per, "endTime");
						long run_time = end - start;
						if (run_time > AnalyzerServiceImpl._request_time) {
							per.put("runTime", run_time);
							synchronized (AnalyzerServiceImpl.run_time_list_tmp) {
								if (AnalyzerServiceImpl.run_time_list_tmp.size() > 1000) {
									AnalyzerServiceImpl.sortFilter(AnalyzerServiceImpl.run_time_list_tmp);
									logger.info(AnalyzerServiceImpl.run_time_list_tmp + "");
									AnalyzerServiceImpl.run_time_list_tmp = new ArrayList<Map<String, Object>>();
								}
							}
							AnalyzerServiceImpl.run_time_list_tmp.add(per);
						}
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			consumer = null;
		}
		
	}
	
}
