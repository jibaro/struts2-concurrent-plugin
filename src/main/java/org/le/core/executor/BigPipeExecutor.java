package org.le.core.executor;

import org.le.bean.PipeProxy;
import org.le.core.PipeExecutor;
import org.le.core.concurrent.BigPipeWorker;
import org.le.core.factory.ThreadPoolExecutorFactoryImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BigPipeExecutor implements PipeExecutor{
    private ThreadPoolExecutor executor = ThreadPoolExecutorFactoryImpl.
            newInstance().instanceOfDefaultConfig();

    @Override
    public Object execute(PipeProxy pipe){
        return null;
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes){
        int pipesSize = pipes.size();
        Map<String, Object> renderResult = new ConcurrentHashMap<String, Object>(pipesSize);
        //create pipe worker
        for (PipeProxy pipeProxy : pipes) {
            executor.submit(new BigPipeWorker(pipeProxy, renderResult));
        }
        return renderResult;
    }


}
