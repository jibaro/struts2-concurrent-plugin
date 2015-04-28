package org.le.util;

import org.le.Exception.PipeActionAnnotationException;
import org.le.Exception.PipeFtlReadExcption;
import org.le.anno.View;
import org.le.core.Cache;
import org.le.core.SimpleMemaryCache;

import java.io.*;
import java.util.Map;

public class ViewAnnotationUtils {
    private static final Cache ftlCache = SimpleMemaryCache.newInstance();

    public static String generateKey(Object o){
        View view = o.getClass().getAnnotation(View.class);
        if(view == null){
            throw new PipeActionAnnotationException("pipe components must " +
                    "have View annotation:" + o.getClass().getName());
        }
        String key = view.key();
        return key;
    }
    public static String generateFtlPath(Object o){
        View view = o.getClass().getAnnotation(View.class);
        if(view == null){
            throw new PipeActionAnnotationException("pipe components must " +
                    "have View annotation:" + o.getClass().getName());
        }
        String ftlPath = view.ftlPath();
        return ftlPath;
    }

    public static String generateFtl(Object o) {
        String ftlPath = generateFtlPath(o);
        String ftl = (String) ftlCache.get(ftlPath);
        if(ftl != null)
            return ftl;
        InputStream inputStream = ViewAnnotationUtils.class.getClassLoader().getResourceAsStream(ftlPath);
        if (inputStream == null) {
            throw new PipeFtlReadExcption("can not read ftl file. please check file path[" + ftlPath
                    + "] ftl file must in resources directory");
        }
        Reader reader = null;
        try {
            reader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder ftlBuilder = new StringBuilder();
        int len = -1;
        char[] buff = new char[1024];
        try {
            while ((len = reader.read(buff)) > 0) {
                ftlBuilder.append(new String(buff, 0 , len));
            }
        } catch (IOException e) {
            throw new PipeFtlReadExcption("can not read ftl file. please check file path[" + ftlPath
                    + "] ftl file must in resources directory");
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ftl = ftlBuilder.toString();
        ftlCache.add(ftlPath, ftl);
        return ftl;
    }
}
