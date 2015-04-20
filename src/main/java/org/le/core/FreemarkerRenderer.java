package org.le.core;

import java.util.Map;

public interface FreemarkerRenderer {

    Object render(String ftl,Map<String,Object> context);
}
