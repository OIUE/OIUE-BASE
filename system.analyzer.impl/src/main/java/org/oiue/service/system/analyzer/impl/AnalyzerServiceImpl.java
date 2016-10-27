package org.oiue.service.system.analyzer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.system.analyzer.AnalyzerService;
import org.oiue.service.system.analyzer.TimeLogger;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("rawtypes")
public class AnalyzerServiceImpl implements AnalyzerService {

	public static long _request_time=3000;
	
	public static List<Map<String,Object>> run_time_list = new ArrayList<Map<String,Object>>();

	private Logger logger;
	private LogService logService;
	public AnalyzerServiceImpl(LogService logService) {
		this.logger = logService.getLogger(this.getClass());
		this.logService=logService;
	}
	@Override
	public void updateProps(Dictionary props) {
		try {
			_request_time=Long.valueOf(props.get("request_time")+"");
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
		if(run_time_list.size()>2)
			sortFilter(run_time_list);
		logger.info(run_time_list+"");
		run_time_list = new ArrayList<Map<String,Object>>();
	}
	public static void sortFilter(List<Map<String,Object>> filterList) {
		Collections.sort(filterList, new Comparator<Map<String,Object>>() {
			@Override
			public int compare(Map<String,Object> o1, Map<String,Object> o2) {
				if (MapUtil.getLong(o1, "runTime") >MapUtil.getLong(o2, "runTime") ) {
					return -1;
				} else if (MapUtil.getLong(o1, "runTime") <MapUtil.getLong(o2, "runTime")) {
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

}
