package org.le.core.factory;

import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringBeanFactory {
    private static ApplicationContext applicationContext = WebApplicationContextUtils.
            getWebApplicationContext(ServletActionContext.getServletContext());
    private static SpringBeanFactory instance = new SpringBeanFactory();
    private SpringBeanFactory(){}

    public static SpringBeanFactory newInstance(){
        return instance;
    }

    public  Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
