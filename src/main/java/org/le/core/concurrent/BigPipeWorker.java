package org.le.core.concurrent;

import org.le.bean.PipeProxy;
import org.le.core.executor.SyncPipeExecutor;

import java.util.Map;

public class BigPipeWorker implements Runnable {

    private SyncPipeExecutor syncPipeExecutor = SyncPipeExecutor.newInstance();
    private PipeProxy pipeProxy;
    private Map<String, Object> renderResult;

    public BigPipeWorker(PipeProxy pipeProxy, Map<String, Object> renderResult) {
        this.pipeProxy = pipeProxy;
        this.renderResult = renderResult;
    }

    @Override
    public void run() {
            Object result = syncPipeExecutor.execute(pipeProxy);
            //构造script输出
            renderResult.put(pipeProxy.getKey(), buildJsonResult(result));

    }

    private String buildJsonResult(Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"application/javascript\">")
                .append("\nreplace(\"")
                .append(pipeProxy.getKey())
                .append("\",\'")
                .append(result.toString().replaceAll("\n","")).append("\');\n</script>");
        return sb.toString();
    }
}
