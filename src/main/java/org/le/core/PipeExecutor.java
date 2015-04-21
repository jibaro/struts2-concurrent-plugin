package org.le.core;

import org.le.bean.PipeProxy;

import java.util.List;
import java.util.Map;

public interface PipeExecutor {

    Object execute(PipeProxy pipe);

    Map<String, Object> execute(List<PipeProxy> pipes);
}
