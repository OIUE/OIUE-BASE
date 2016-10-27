package org.oiue.service.system.analyzer.impl;

import java.util.Dictionary;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.system.analyzer.AnalyzerService;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            AnalyzerService analyzerService;
            private Logger logger;

            @Override
            public void removedService() {}

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                logger = logService.getLogger(getClass());
                
                analyzerService = new AnalyzerServiceImpl(logService);
                registerService(AnalyzerService.class, analyzerService);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                try {
                    if (props != null)
                        analyzerService.updateProps(props);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, LogService.class);
    }

    @Override
    public void stop() throws Exception {}
}
