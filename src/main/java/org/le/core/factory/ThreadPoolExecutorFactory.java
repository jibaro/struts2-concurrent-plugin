package org.le.core.factory;

import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPoolExecutorFactory {

    ThreadPoolExecutor instanceOfDefaultConfig();
}
