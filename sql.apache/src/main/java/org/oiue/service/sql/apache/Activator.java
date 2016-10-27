package org.oiue.service.sql.apache;

import java.util.Dictionary;
import java.util.Hashtable;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.sql.SqlService;
public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private SqlService sqlService;
            private String baseDir;
            private Logger logger;
            @Override
            public void removedService() {
                sqlService.unregisterAll();}

            @Override
            public void addingService() {
                baseDir = getProperty("user.dir");
                
                LogService logService = getService(LogService.class);
                sqlService = new SqlServiceImpl(logService);
                logger = logService.getLogger(Activator.class);
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void updated(Dictionary<String, ?> props) {
                try {
                    String[] alias = sqlService.listDataSource();
                    for (String e : alias) {
                        sqlService.unregister(e);
                    }
                    String ds[] = String.valueOf(props.get("ds")).split(",");
                    for (String e : ds) {
                        try {
                            e = e.trim();
                            Dictionary dsProps = new Hashtable();
                            dsProps.put("driverClassName", props.get("ds." + e + ".driverClassName"));
                            dsProps.put("username", props.get("ds." + e + ".username"));
                            dsProps.put("password", props.get("ds." + e + ".password"));
                            dsProps.put("url", props.get("ds." + e + ".url").toString().replace("${user.dir}", baseDir));
                            dsProps.put("maxActive", props.get("ds." + e + ".maxActive"));
                            dsProps.put("maxIdle", props.get("ds." + e + ".maxIdle"));
                            dsProps.put("maxWait", props.get("ds." + e + ".maxWait"));
                            dsProps.put("validationQuery", props.get("ds." + e + ".validationQuery"));
                            sqlService.registerDataSource(e, dsProps);
                        } catch (Exception ex) {
                            logger.error("connect [" + e + "] config is error:" + ex.getMessage(), ex);
                        }
                    }
                    registerService(SqlService.class, sqlService);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, LogService.class);
    }

    @Override
    public void stop() throws Exception {}
}
