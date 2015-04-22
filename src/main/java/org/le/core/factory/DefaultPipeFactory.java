package org.le.core.factory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.le.Exception.PipeClassDefinationException;
import org.le.Exception.PipeFieldInjectException;
import org.le.anno.Param;
import org.le.bean.Pipe;
import org.le.bean.PipeProxy;
import org.le.bean.PipeSupport;
import org.le.core.Cache;
import org.le.core.SimpleMemaryCache;
import org.le.util.InjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;


public class DefaultPipeFactory implements PipeFactory {

    public static DefaultPipeFactory instance = new DefaultPipeFactory();

    private ActionInvocation invocation;
    private Cache cache = SimpleMemaryCache.newInstance();
    private SpringBeanFactory springBeanFactory = SpringBeanFactory.newInstance();

    private DefaultPipeFactory() {

    }

    public static DefaultPipeFactory newInstance() {
        return instance;
    }

    @Override
    public PipeProxy create(String className, ActionInvocation invocation) {
        this.invocation = invocation;
        Map<String, Object> context = InjectUtils.getFieldValueWithAnnoParamFromObject(invocation.getAction());
        PipeProxy pipeProxy = null;
        Object pipe = null;
        Class pipeClazz = (Class) cache.get(className);
        try {
            if (pipeClazz == null) {
                pipeClazz = Class.forName(className);
                cache.add(className, pipeClazz);
            }
            pipe = pipeClazz.newInstance();
        } catch (Exception e) {
            throw new PipeClassDefinationException("create pipe failed,please check class name config right:" + className);
        }
        injectBusinessPipeField(pipe, context);
        injectSpringBeanField(pipe);
        if (pipe != null && pipe instanceof PipeSupport)
            injectPipeSupportField(pipe);
        if (pipe != null && pipe instanceof Pipe)
            return new PipeProxy(className, (Pipe) pipe);
        else
            throw new PipeClassDefinationException("pipe must implements inteface Pipe :" + className);
    }

    @Override
    public List<PipeProxy> create(List<String> classNames, ActionInvocation invocation) {
        List<PipeProxy> pipes = new ArrayList<PipeProxy>();
        for (String className : classNames)
            pipes.add(create(className, invocation));
        return pipes;
    }

    private void injectBusinessPipeField(Object desc, Map<String, Object> context) {
        Field[] fields = desc.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Param.class) != null) {
                String fieldName = field.getName();
                Object injectValue = context.get(fieldName);
                if (injectValue == null)
                    throw new PipeFieldInjectException("please check [" + fieldName +
                            "] exsited in action and has annotation param");
                else {
                    field.setAccessible(true);
                    try {
                        field.set(desc, context.get(fieldName));
                    } catch (IllegalAccessException e) {
                        //becase has set field accessible so do not throw this exception
                    }
                }
            }
        }
    }

    private void injectPipeSupportField(Object pipeSupport) {
        Field[] fields = PipeSupport.class.getDeclaredFields();
        ActionContext actionContext = invocation.getInvocationContext();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if ("request".equals(fieldName)) {
                    HttpServletRequest request = (HttpServletRequest) actionContext.get(ServletActionContext.HTTP_REQUEST);
                    field.set(pipeSupport, request);
                } else if ("response".equals(fieldName)) {
                    field.set(pipeSupport, actionContext.get(ServletActionContext.HTTP_RESPONSE));
                } else if ("servletContext".equals(fieldName)) {
                    field.set(pipeSupport, actionContext.get(ServletActionContext.SERVLET_CONTEXT));
                } else {
                    HttpServletRequest request = (HttpServletRequest) actionContext.get(ServletActionContext.HTTP_REQUEST);
                    field.set(pipeSupport, request.getCookies());
                }
            }
        } catch (IllegalAccessException e) {
        }
    }

    private void injectSpringBeanField(Object pipe) {
        Field[] fields = pipe.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Autowired.class) != null) {
                field.setAccessible(true);
                String fieldName = field.getName();
                try {
                    field.set(pipe, springBeanFactory.getBean(fieldName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
