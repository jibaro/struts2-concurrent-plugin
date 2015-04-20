package org.le.core;

import org.apache.commons.lang3.StringUtils;
import org.le.Exception.PipeConfigurationException;

import java.util.Arrays;
import java.util.List;

/**
 * 默认pipes配置格式以逗号隔开：
 * Class1,Class2.....
 */
public class DefaultPipesParse implements PipesParse{

    private static DefaultPipesParse instance = new DefaultPipesParse();
    private DefaultPipesParse(){

    }

    public static DefaultPipesParse newInstance(){
        return instance;
    }

    @Override
    public List<String> getPipes(String pipes) {
        if(StringUtils.isEmpty(pipes))
            throw new PipeConfigurationException("pipes can not exception! please check config in struts*.xml");
        return Arrays.asList(pipes.replace("\n","").replace(" ","").split(","));
    }
}
