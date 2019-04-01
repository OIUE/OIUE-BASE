package org.oiue.service.log4j;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Proxy;

@SuppressWarnings("serial")
public class LoggerImpl implements org.oiue.service.log.Logger, Serializable {
	
	private Logger logger = null;
	
	public LoggerImpl(String name) {
		try {
			Enhancer eh = new Enhancer();
			eh.setSuperclass(org.apache.log4j.Logger.class);
			eh.setCallbackType(LogInterceptor.class);
			Class c = eh.createClass();
			Enhancer.registerCallbacks(c, new LogInterceptor[] { new LogInterceptor(LoggerImpl.class.getName()) });
			
			Constructor<org.apache.log4j.Logger> constructor = c.getConstructor(String.class);
			org.apache.log4j.Logger loggerProxy = constructor.newInstance(name);
			
			LoggerRepository loggerRepository = LogManager.getLoggerRepository();
			org.apache.log4j.spi.LoggerFactory lf = ReflectionUtil.getFieldValue(loggerRepository, "defaultFactory");
			Object loggerFactoryProxy = Proxy.newProxyInstance(LoggerFactory.class.getClassLoader(), new Class[] { LoggerFactory.class }, new NewLoggerHandler(loggerProxy));
			
			ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", loggerFactoryProxy);
			logger = org.slf4j.LoggerFactory.getLogger(name);
			ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", lf);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException("初始化Logger失败", e);
		}
	}
	
	private class LogInterceptor implements MethodInterceptor {
		private String name;
		
		public LogInterceptor(String name) {
			this.name = name;
		}
		
		@Override
		public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
			// 只拦截log方法。
			if (objects.length != 4 || !method.getName().equals("log"))
				return methodProxy.invokeSuper(o, objects);
			// try {throw new RuntimeException();} catch (Exception e)
			// {e.printStackTrace();}
			objects[0] = name;
			return methodProxy.invokeSuper(o, objects);
		}
	}
	
	private class NewLoggerHandler implements InvocationHandler {
		private final org.apache.log4j.Logger proxyLogger;
		
		public NewLoggerHandler(org.apache.log4j.Logger proxyLogger) {
			this.proxyLogger = proxyLogger;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			return proxyLogger;
		}
	}
	
	@Override
	public void debug(String msg, Object... arguments) {
		logger.debug(msg, arguments);
	}
	
	@Override
	public void debug(String msg, Throwable t) {
		logger.debug(msg, t);
	}
	
	@Override
	public void error(String msg, Object... arguments) {
		logger.error(msg, arguments);
	}
	
	@Override
	public void error(String msg, Throwable t) {
		logger.error(msg, t);
	}
	
	@Override
	public void info(String msg, Object... arguments) {
		logger.info(msg, arguments);
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
	public void trace(String msg, Object... arguments) {
		logger.trace(msg, arguments);
	}
	
	@Override
	public void trace(String msg, Throwable t) {
		logger.trace(msg, t);
	}
	
	@Override
	public void warn(String msg, Object... arguments) {
		logger.warn(msg, arguments);
	}
	
	@Override
	public void warn(String msg, Throwable t) {
		logger.warn(msg, t);
	}
	
	@Override
	public void log(int level, String s, Throwable throwable) {
		switch (level) {
			case LOG_DEBUG:
				System.out.println("DEBUG: " + s);
				break;
			case LOG_ERROR:
				System.out.println("ERROR: " + s);
				if (throwable != null) {
					if ((throwable instanceof BundleException) && (((BundleException) throwable).getNestedException() != null)) {
						throwable = ((BundleException) throwable).getNestedException();
					}
					throwable.printStackTrace();
				}
				break;
			case LOG_INFO:
				System.out.println("INFO: " + s);
				break;
			case LOG_WARNING:
				System.out.println("WARNING: " + s);
				break;
			default:
				System.out.println("UNKNOWN[" + level + "]: " + s);
		}
	}
}
