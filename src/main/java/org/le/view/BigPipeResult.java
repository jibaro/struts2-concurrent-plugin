package org.le.view;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.le.Exception.FtlRenderException;
import org.le.Exception.PipeFtlReadExcption;
import org.le.anno.ExecuteType;
import org.le.anno.View;
import org.le.bean.PipeProxy;
import org.le.core.*;
import org.le.core.executor.BigPipeExecutor;
import org.le.core.executor.ConcurrentPipeExecutor;
import org.le.core.executor.SyncPipeExecutor;
import org.le.core.factory.DefaultPipeFactory;
import org.le.core.factory.PipeFactory;
import org.le.util.InjectUtils;
import org.le.util.ViewAnnotationUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * bigpipe result for struts2
 * has 3 modes:
 * sync: synchronized render pagelet
 * concurrent: concurrent render pagelet in server,
 * after all pagelet renderd and merge them as one html reponse to browse
 * bigpipe: concurrent render pagelet in server, different from concurrent mode ,
 * bigpipe mode will flush pagelet result to brower as soon as one pagelet renderd
 * choose mode just set annotation View at struts action.
 * For example @View(ftlPath = "/index.ftl", type = ExecuteType.BIGPIPE) in bigpipe mode. just choose ExecuteType
 *
 * @Author lepdou
 */
public class BigPipeResult extends StrutsResultSupport {

    private PipesParse pipesParse = DefaultPipesParse.newInstance();
    private PipeFactory pipeFactory = DefaultPipeFactory.newInstance();
    private PipeExecutor syncPipeExecutor = SyncPipeExecutor.newInstance();
    private PipeExecutor concurrentPipeExecutor = new ConcurrentPipeExecutor();
    private PipeExecutor bigpipeExecutor = new BigPipeExecutor();
    private FreemarkerRenderer renderer = DefaultFreemarkerRenderer.newIntance();
    private BigpipeSupportStrategy bigpipeSupportStrategy = SimpleBigpipeSupport.newInstance();

    private Object action;

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        //todo delete just for test
        long startTime = new Date().getTime();
        this.action = invocation.getAction();
        List<String> pipeClazzs = pipesParse.getPipes(finalLocation);
        List<PipeProxy> pipes = pipeFactory.create(pipeClazzs, invocation);
        PrintWriter writer = getWrite(invocation);
        //action's content for ftl render
        Map<String, Object> executeResults = buildFrameworkExecuteContext(pipes);
        switch (getExecuteType()) {
            case SYNC:
                executeResults.putAll(syncPipeExecutor.execute(pipes));
                doResponse(writer, executeResults, startTime);
                break;
            case CONCURRNET:
                executeResults.putAll(concurrentPipeExecutor.execute(pipes));
                doResponse(writer, executeResults, startTime);
                break;
            case BIGPIPE: {
                //first render html framework base action content
                renderPageFrameworkAndFlush(writer, executeResults, startTime);
                //get pipes renderd result and flush
                executeResults = bigpipeExecutor.execute(pipes);
                List<String> flushedPipe = new ArrayList<String>();
                AtomicInteger flushedCounter = new AtomicInteger(0);
                while (true) {
                    if (executeResults.size() > flushedCounter.get()) {
                        responsePipeToClient(executeResults, flushedPipe, flushedCounter, writer);
                    } else if (flushedCounter.get() == pipes.size()) {
                        //all pipes have flush to browse close the html
                         closeHtml(writer);
                        break;
                    }
                    //sleep. prevent ask too times
                    Thread.sleep(5);
                }
            }
        }
    }

    private Map<String, Object> buildFrameworkExecuteContext(List<PipeProxy> pipes) {
        Map<String, Object> actionContext = new HashMap<String, Object>();
        for (PipeProxy pipeProxy : pipes)
            actionContext.put(pipeProxy.getKey(), "");
        //获取action中的参数
        actionContext.putAll(InjectUtils.getFieldValueForFreemarker(action));
        return actionContext;
    }

    private PrintWriter getWrite(ActionInvocation invocation) throws IOException {
        ActionContext ctx = invocation.getInvocationContext();
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        PrintWriter writer = response.getWriter();
        return writer;
    }

    private ExecuteType getExecuteType() {
        View view = action.getClass().getAnnotation(View.class);
        return view.type();
    }

    private void doResponse(PrintWriter writer, Map<String, Object> executeResults, long startTime) {
        //todo delete   just for test
        long endTime = new Date().getTime();
        executeResults.put("startTime", startTime);
        executeResults.put("endTime", endTime);
        executeResults.put("consumeTime", endTime - startTime);
        String result = renderer.render(ViewAnnotationUtils.generateFtl(action), executeResults).toString();
        writer.println(result);
        writer.flush();
    }

    private void renderPageFrameworkAndFlush(PrintWriter writer, Map<String, Object> executeResults, long startTime) {
        //todo delete. just for test
        long endTime = new Date().getTime();
        executeResults.put("consumeTime", endTime - startTime);
        String ftl = ViewAnnotationUtils.generateFtl(action);
        String framework = renderer.render(ftl, executeResults).toString();
        writer.println(bigpipeSupportStrategy.execute(framework));
        writer.flush();
    }

    private void responsePipeToClient(Map<String, Object> executeResults, List<String> flushedPipe,
                                      AtomicInteger flushedCounter, PrintWriter writer){
        for (Map.Entry entry : executeResults.entrySet()) {
            if (!flushedPipe.contains(entry.getKey())) {
                flushedCounter.incrementAndGet();
                flushedPipe.add((String) entry.getKey());
                writer.println(entry.getValue());
                writer.flush();
            }
        }
    }

    private void closeHtml(PrintWriter writer){
        writer.print("</body>\n</html>");
        writer.flush();
    }

}
