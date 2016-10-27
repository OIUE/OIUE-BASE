package org.oiue.service.cache.impl;

import java.util.Dictionary;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private CacheServiceManagerImpl cacheServiceManager;

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);

                cacheServiceManager = new CacheServiceManagerImpl(logService);
                registerService(CacheServiceManager.class, cacheServiceManager);
                registerService(CacheService.class, cacheServiceManager);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                cacheServiceManager.updated(props);
            }
        }, LogService.class);
    }

    @Override
    public void stop() throws Exception {}
}
