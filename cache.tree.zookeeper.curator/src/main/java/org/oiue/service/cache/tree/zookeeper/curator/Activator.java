package org.oiue.service.cache.tree.zookeeper.curator;

import java.util.Map;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			CacheTreeServiceImpl cacheTreeService;
			@Override
			public void removedService() {
				cacheTreeService.stop();
			}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);

				Logger log = logService.getLogger(this.getClass());

				ClassLoader ccl = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(Runnable.class.getClassLoader());
				try {
					try {
						cacheTreeService = new CacheTreeServiceImpl(logService);
					} finally {
						Thread.currentThread().setContextClassLoader(ccl);
					}
					registerService(CacheTreeService.class, cacheTreeService);
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
				}
			}

			@Override
			public void updatedConf(Map<String, ?> props) {
				cacheTreeService.updated(props);
			}
		}, LogService.class);
	}

	@Override
	public void stop() {
	}
}
