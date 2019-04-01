package org.oiue.service.system.analyzer.impl;

import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.system.analyzer.TimeLogger;
import org.oiue.tools.Application;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("unused")
public class TimeLoggerImpl implements TimeLogger {
	private Logger logger;
	
	public TimeLoggerImpl(LogService logService) {
		this.logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public boolean isDebugEnabled() {
		return AnalyzerServiceImpl.runAnalyzer;
	}
	
	@Override
	public void debug(Map<String, Object> param) {
		String classLine = Application.getClassLine("");
		
		long start = MapUtil.getLong(param, "startTime");
		long end = MapUtil.getLong(param, "endTime");
		long run_time = end - start;
		if (run_time > AnalyzerServiceImpl._request_time) {
			param.put("runTime", run_time);
			// synchronized (AnalyzerServiceImpl.run_time_list) {
			// if (AnalyzerServiceImpl.run_time_list.size() > 1000) {
			// AnalyzerServiceImpl.sortFilter(AnalyzerServiceImpl.run_time_list);
			// logger.info(AnalyzerServiceImpl.run_time_list + "");
			// AnalyzerServiceImpl.run_time_list = new ArrayList<Map<String, Object>>();
			// }
			// }
			AnalyzerServiceImpl.run_time_list.offer(param);
		}
	}
}