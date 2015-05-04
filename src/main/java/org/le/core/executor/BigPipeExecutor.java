package org.le.core.executor;

import org.le.anno.Weight;
import org.le.bean.PipeProxy;
import org.le.core.PipeExecutor;
import org.le.core.concurrent.BigPipeWorker;
import org.le.core.factory.ThreadPoolExecutorFactoryImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BigPipeExecutor implements PipeExecutor {
    private ThreadPoolExecutor executor = ThreadPoolExecutorFactoryImpl.
            newInstance().instanceOfDefaultConfig();

    @Override
    public Object execute(PipeProxy pipe) {
        return null;
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes) {
        int pipesSize = pipes.size();
        Map<String, Object> renderResult = new ConcurrentHashMap<String, Object>(pipesSize);
        //create pipe worker higher weight will be push first to queue
        for (PipeProxy pipeProxy : pipes) {
            if (pipeProxy.getWeight() == Weight.HEIGHT)
                executor.submit(new BigPipeWorker(pipeProxy, renderResult));
        }
        for (PipeProxy pipeProxy : pipes) {
            if (pipeProxy.getWeight() == Weight.NORMALL)
                executor.submit(new BigPipeWorker(pipeProxy, renderResult));
        }
        for (PipeProxy pipeProxy : pipes) {
            if (pipeProxy.getWeight() == Weight.LOW)
                executor.submit(new BigPipeWorker(pipeProxy, renderResult));
        }
        return renderResult;
    }


}
