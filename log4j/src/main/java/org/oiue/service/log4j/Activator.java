package org.oiue.service.log4j;

import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private LogServiceImpl logService = null;
			boolean logReg = true;
			
			@Override
			public void removedService() {}
			
			@Override
			public void addingService() {
				logService = new LogServiceImpl();
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
				logService.updateConfigure(props);
				if (logReg) {
					registerService(LogService.class, logService);
					logReg = false;
				}
			}
		});
	}
	
	@Override
	public void stop() {}
}
