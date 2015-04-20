package org.le.core.executor;

import org.le.bean.PipeProxy;
import org.le.core.PipeExecutor;
import org.le.core.concurrent.ConcurrentPipeWorker;
import org.le.core.factory.ThreadPoolExecutorFactoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ConcurrentPipeExecutor implements PipeExecutor {
    private ThreadPoolExecutor executor = ThreadPoolExecutorFactoryImpl.
            newInstance().instanceOfDefaultConfig();

    private SyncPipeExecutor syncPipeExecutor = SyncPipeExecutor.newInstance();

    @Override
    public Object execute(PipeProxy pipe){
        return syncPipeExecutor.execute(pipe);
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes){
        int pipesSize = pipes.size();
        Map<Future, String> futureStringMap = new HashMap<Future, String>();
        List<Future> renderResultFuture = new ArrayList<Future>(pipesSize);
        CountDownLatch latch = new CountDownLatch(pipesSize);
        //create pipe worker
        for (PipeProxy pipeProxy : pipes) {
            Future future = executor.submit(new ConcurrentPipeWorker(pipeProxy, latch));
            renderResultFuture.add(future);
            futureStringMap.put(future, pipeProxy.getKey());
        }
        Map<String, Object> renderResult = new HashMap<String, Object>(pipesSize);
        try {
            latch.await();
            collectRenderResult(renderResultFuture,futureStringMap,renderResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return renderResult;
    }

    private void collectRenderResult(List<Future> renderResultFuture, Map<Future, String> futureStringMap,
                                     Map<String, Object> renderResult) {
        for (Future future : renderResultFuture) {
            try {
                Object result = future.get();
                renderResult.put(futureStringMap.get(future), result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
