package org.le.core.factory;

import com.opensymphony.xwork2.ActionInvocation;
import org.le.Exception.PipeClassDefinationException;
import org.le.Exception.PipeConfigurationException;
import org.le.Exception.PipeFieldInjectException;
import org.le.Exception.PipeFtlReadExcption;
import org.le.bean.PipeProxy;

import java.util.List;

/**
 * create pipe class
 */
public interface PipeFactory {

    PipeProxy create(String className, ActionInvocation invocation)
            throws PipeConfigurationException, PipeClassDefinationException, PipeFieldInjectException, PipeFtlReadExcption;

    List<PipeProxy> create(List<String> classNames, ActionInvocation invocation)
            throws PipeConfigurationException, PipeClassDefinationException, PipeFieldInjectException, PipeFtlReadExcption;
}
