package org.oiue.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Proxy;

public class Logger {
	private static org.slf4j.Logger logger;
	private static final String FQCN = Logger.class.getName();
	
	static {
		try {
			Enhancer eh = new Enhancer();
			eh.setSuperclass(org.apache.log4j.Logger.class);
			eh.setCallbackType(LogInterceptor.class);
			Class c = eh.createClass();
			Enhancer.registerCallbacks(c, new LogInterceptor[] { new LogInterceptor() });
			
			Constructor<org.apache.log4j.Logger> constructor = c.getConstructor(String.class);
			org.apache.log4j.Logger loggerProxy = constructor.newInstance(Logger.class.getName());
			
			LoggerRepository loggerRepository = LogManager.getLoggerRepository();
			org.apache.log4j.spi.LoggerFactory lf = ReflectionUtil.getFieldValue(loggerRepository, "defaultFactory");
			Object loggerFactoryProxy = Proxy.newProxyInstance(LoggerFactory.class.getClassLoader(), new Class[] { LoggerFactory.class }, new NewLoggerHandler(loggerProxy));
			
			ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", loggerFactoryProxy);
			logger = org.slf4j.LoggerFactory.getLogger(Logger.class.getName());
			ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", lf);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException("初始化Logger失败", e);
		}
	}
	
	private static class LogInterceptor implements MethodInterceptor {
		@Override
		public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
			// 只拦截log方法。
			if (objects.length != 4 || !method.getName().equals("log"))
				return methodProxy.invokeSuper(o, objects);
			objects[0] = FQCN;
			return methodProxy.invokeSuper(o, objects);
		}
	}
	
	private static class NewLoggerHandler implements InvocationHandler {
		private final org.apache.log4j.Logger proxyLogger;
		
		public NewLoggerHandler(org.apache.log4j.Logger proxyLogger) {
			this.proxyLogger = proxyLogger;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			return proxyLogger;
		}
	}
	
	// 剩下的Logger需要封装的方法可以根据自己的需要来实现
	// 我个人认为slf4j的api足够好用了，所以大部分只是写了一些类似下面的代码
	public static void debug(String msg) {
		logger.debug(msg);
	}
}