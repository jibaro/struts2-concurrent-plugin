package org.le.core.executor;

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
        try {
            pipe.execute();
            Map<String, Object> context = InjectUtils.getFieldValueForFreemarker(pipe.getPipe());
            String ftl = pipe.getFtl();
            Object renderResult = renderer.render(ftl, context);
            pipe.setRenderResult(renderResult);
            return renderResult;
        } catch (Exception e) {
            e.printStackTrace();
            return generateExceptionToPrintStack(e);
        }
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (PipeProxy pipe : pipes)
            result.put(pipe.getKey(), execute(pipe));
        return result;
    }

    private String generateExceptionToPrintStack(Exception e) {
        StringBuilder result = new StringBuilder();
        result.append("<div style='background-color: #eee;font-size:9px;font-family: " +
                "Consolas,Menlo,Monaco;height:250px;overflow:scroll'>");
        result.append("<font style='color:red'>")
                .append(e.toString())
                .append("</font></br>");
        for (StackTraceElement element : e.getStackTrace()) {
            result.append(element.toString() + "</br>");
        }
        result.append("</div>");
        return result.toString();
    }
}
