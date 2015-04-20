package org.le.core.factory;

import com.opensymphony.xwork2.ActionInvocation;
import org.le.Exception.PipeClassDefinationException;
import org.le.Exception.PipeFieldInjectException;
import org.le.Exception.PipeFtlReadExcption;
import org.le.anno.Param;
import org.le.bean.Pipe;
import org.le.bean.PipeProxy;
import org.le.util.InjectUtils;

import java.lang.reflect.Field;
import java.util.*;


public class DefaultPipeFactory implements PipeFactory {

    public static DefaultPipeFactory instance = new DefaultPipeFactory();

    private DefaultPipeFactory() {

    }

    public static DefaultPipeFactory newInstance() {
        return instance;
    }

    @Override
    public PipeProxy create(String className, ActionInvocation invocation) {
        Map<String, Object> context = InjectUtils.getFieldValueWithAnnoParamFromObject(invocation.getAction());
        PipeProxy pipeProxy = null;
        Object pipe = null;
        Class pipeClazz = null;
        try {
            pipeClazz = Class.forName(className);
            pipe = pipeClazz.newInstance();
        } catch (Exception e) {
            throw new PipeClassDefinationException("create pipe failed,please check class name config right:" + className);
        }
        inject(pipe, context);
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

    private void inject(Object desc, Map<String, Object> context) {
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

}
