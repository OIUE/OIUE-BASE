package org.oiue.service.log4j;

import java.io.Serializable;

import org.oiue.service.log.Logger;

@SuppressWarnings("serial")
public class LoggerImpl implements Logger,Serializable {
	private org.slf4j.Logger logger;

	public LoggerImpl(org.slf4j.Logger logger) {
		this.logger = logger;
	}

	@Override
	public void debug(String msg) {
		logger.debug(msg);

	}

	@Override
	public void debug(String msg, Throwable t) {
		logger.debug(msg, t);

	}

	@Override
	public void error(String msg) {
		logger.error(msg);

	}

	@Override
	public void error(String msg, Throwable t) {
		logger.error(msg, t);

	}

	@Override
	public void info(String msg) {
		logger.info(msg);

	}

	@Override
	public void info(String msg, Throwable t) {
		logger.info(msg, t);

	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void trace(String msg) {
		logger.trace(msg);
	}

	@Override
	public void trace(String msg, Throwable t) {
		logger.trace(msg, t);

	}

	@Override
	public void warn(String msg) {
		logger.warn(msg);

	}

	@Override
	public void warn(String msg, Throwable t) {
		logger.warn(msg, t);

	}
}
