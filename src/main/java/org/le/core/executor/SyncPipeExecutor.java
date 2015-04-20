package org.le.core.executor;

import org.le.Exception.FtlRenderException;
import org.le.Exception.PipeFtlReadExcption;
import org.le.bean.PipeProxy;
import org.le.core.DefaultFreemarkerRenderer;
import org.le.core.FreemarkerRenderer;
import org.le.core.PipeExecutor;
import org.le.util.InjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncPipeExecutor implements PipeExecutor {

    private static SyncPipeExecutor instance = new SyncPipeExecutor();
    private FreemarkerRenderer renderer = DefaultFreemarkerRenderer.newIntance();

    private SyncPipeExecutor() {
    }


    public static SyncPipeExecutor newInstance() {
        return instance;
    }

    @Override
    public Object execute(PipeProxy pipe) {
        pipe.execute();
        Map<String, Object> context = InjectUtils.getFieldValueForFreemarker(pipe.getPipe());
        String ftl = pipe.getFtl();
        try {
            return renderer.render(ftl, context);
        } catch (FtlRenderException e) {
            throw new FtlRenderException("render [" + pipe.getFtlPath() + "] error!");
        }
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes){
        Map<String, Object> result = new HashMap<String, Object>();
        for (PipeProxy pipe : pipes)
            result.put(pipe.getKey(), execute(pipe));
        return result;
    }
}
