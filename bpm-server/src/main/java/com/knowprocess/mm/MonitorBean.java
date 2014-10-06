package com.knowprocess.mm;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorBean {
    static boolean stop = false;
    static MemoryPoolMXBean tenuredGenPool = null;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MonitorBean.class);

    public MonitorBean() {
        LOGGER.info("Creating monitor bean...");
    }

    /**
     * 
     * @throws Exception
     *             If low memory condition is detected.
     */
    public void initMemoryMonitor() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            // see http://www.javaspecialists.eu/archive/Issue092.html
            if (pool.getType() == MemoryType.HEAP
                    && pool.isUsageThresholdSupported()) {
                tenuredGenPool = pool;
            }
        }

        // setting the threshold to 80% usage of the memory
        tenuredGenPool.setCollectionUsageThreshold((int) Math
                .floor(tenuredGenPool.getUsage().getMax() * 0.8));
        tenuredGenPool.setUsageThreshold((int) Math.floor(tenuredGenPool
                .getUsage().getMax() * 0.8));

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        emitter.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object hb) {
                if (n.getType()
                        .equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
                    // this is th warning we want
                    LOGGER.error("memory collection threshold exceeded !!! : \n       ");
                    File f = new File("java-memory-low");
                    try {
                        f.createNewFile();
                        LOGGER.debug("Created file at:" + f.getAbsolutePath());
                    } catch (IOException e) {
                        LOGGER.error("Unable to create marker file");
                    }
                    stop = true;
                    throw new RuntimeException("Low memory condition detected");

                } else if (n.getType().equals(
                        MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    // just FYI
                    LOGGER.warn("memory threshold exceeded !!! : \n       ");
                }
            }
        }, null, null);
    }

}
