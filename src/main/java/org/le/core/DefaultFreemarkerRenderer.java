package org.le.core;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.le.Exception.FtlRenderException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class DefaultFreemarkerRenderer implements FreemarkerRenderer {
    private static DefaultFreemarkerRenderer instance = new DefaultFreemarkerRenderer();
    private Configuration cfg;

    private void init() {
        cfg = new freemarker.template.Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringLoader);
        cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
    }

    private DefaultFreemarkerRenderer() {
        init();
    }

    public static DefaultFreemarkerRenderer newIntance() {
        return instance;
    }

    @Override
    public Object render(String ftl, Map<String, Object> context) {
        String renderResult = "";
        try {
            Template template = new Template(ftl, new StringReader(ftl), cfg);
            StringWriter writer = new StringWriter();
            template.process(context, writer);
            renderResult = writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            throw new FtlRenderException(e);
        }
        return renderResult;
    }
}
