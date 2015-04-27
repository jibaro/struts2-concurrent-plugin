package org.le.core.extention.downgrade;

import org.le.bean.PipeProxy;

public interface PipeDowngrade {

    void backup(PipeProxy pipe, Object pipeResult);

    Object downgrade(PipeProxy pipe);
}
