package org.oiue.test;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class TestLog {

	@Test
	public void logTest() {
		Logger.debug("这是调用封装的Logger输出日志");
		LoggerFactory.getLogger(TestLog.class).info("常规方法输出日志");
	}
}