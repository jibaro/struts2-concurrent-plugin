package org.le.core.executor;

import org.le.bean.PipeProxy;
import org.le.core.DefaultFreemarkerRenderer;
import org.le.core.FreemarkerRenderer;
import org.le.core.PipeExecutor;
import org.le.core.extention.downgrade.PipeDowngrade;
import org.le.util.InjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncPipeExecutor implements PipeExecutor {

    private static SyncPipeExecutor instance = new SyncPipeExecutor();
    private FreemarkerRenderer renderer = DefaultFreemarkerRenderer.newIntance();
    private PipeDowngrade downgrade;
    private boolean devMode;

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
            if (downgrade != null)
                downgrade.backup(pipe, renderResult);
            pipe.setRenderResult(renderResult);
            return renderResult;
        } catch (Exception e) {
            if(isDevMode()){
                return generateExceptionToPrintStack(e);
            }else{
                if (downgrade != null) {
                    Object backupResult = downgrade.downgrade(pipe);
                    if (backupResult != null)
                        return backupResult;
                }else {
                    return "";
                }
            }
        }
        return "";
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
        result.append("<div style=\"background-color: #eee;font-size:9px;font-family: " +
                "Consolas,Menlo,Monaco;height:250px;overflow:scroll\">");
        result.append("<font style=\"color:red\">")
                .append(e.toString())
                .append("</font></br>");
        for (StackTraceElement element : e.getStackTrace()) {
            result.append(element.toString() + "</br>");
        }
        result.append("</div>");
        return result.toString();
    }

    public PipeDowngrade getDowngrade() {
        return downgrade;
    }

    public void setDowngrade(PipeDowngrade downgrade) {
        this.downgrade = downgrade;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }
}
