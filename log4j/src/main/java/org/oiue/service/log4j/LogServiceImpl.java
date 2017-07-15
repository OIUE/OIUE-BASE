package org.oiue.service.log4j;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

@SuppressWarnings("serial")
public class LogServiceImpl implements LogService,Serializable {
	private Logger logger = new LoggerImpl(this.getClass().getName());
	@SuppressWarnings("rawtypes")
	public void updateConfigure(Dictionary props) {
		if (props == null) {
			java.util.logging.LogManager.getLogManager().reset();
			PropertyConfigurator.configure(new Properties());
			return;
		}

		Enumeration e = props.keys();
		Properties properties = new Properties();

		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object value = props.get(key);
			properties.put(key, value);
		}

		if (logger != null) {
			logger.info("configure update, reset all exist loggers.");
		}
		java.util.logging.LogManager.getLogManager().reset();
		PropertyConfigurator.configure(properties);

		if (logger != null)
			logger.info("update configure");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	@Override
	public Logger getLogger(String name) {
		try {
			return new LoggerImpl(name);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
