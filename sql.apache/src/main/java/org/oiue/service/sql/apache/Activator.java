package org.oiue.service.sql.apache;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.sql.SqlService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		FrameActivator tracker = this;
		this.start(new MulitServiceTrackerCustomizer() {
			private SqlService sqlService;
			private String baseDir;
			private Logger logger;
			
			@Override
			public void removedService() {
				sqlService.unregisterAll();
			}
			
			@Override
			public void addingService() {
				baseDir = getProperty("user.dir");
				
				LogService logService = getService(LogService.class);
				sqlService = new SqlServiceImpl(logService);
				logger = logService.getLogger(Activator.class);
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void updatedConf(Map<String, ?> props) {
				try {
					String[] alias = sqlService.listDataSource();
					for (String e : alias) {
						sqlService.unregister(e);
					}
					String ds[] = String.valueOf(tracker.getProperty("ds")).split(",");
					for (String e : ds) {
						try {
							e = e.trim();
							Dictionary dsProps = new Hashtable();
							dsProps.put("driverClassName", tracker.getProperty("ds." + e + ".driverClassName"));
							dsProps.put("username", tracker.getProperty("ds." + e + ".username"));
							dsProps.put("password", tracker.getProperty("ds." + e + ".password"));
							dsProps.put("url", tracker.getProperty("ds." + e + ".url").toString().replace("${user.dir}", baseDir));
							dsProps.put("maxActive", tracker.getProperty("ds." + e + ".maxActive"));
							dsProps.put("maxIdle", tracker.getProperty("ds." + e + ".maxIdle"));
							dsProps.put("maxWait", tracker.getProperty("ds." + e + ".maxWait"));
							dsProps.put("validationQuery", tracker.getProperty("ds." + e + ".validationQuery"));
							sqlService.registerDataSource(e, dsProps);
						} catch (Exception ex) {
							logger.error("connect [" + e + "] config is error:" + ex.getMessage(), ex);
						}
					}
					registerService(SqlService.class, sqlService);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
