package org.le.util;

import org.le.Exception.PipeFtlReadExcption;
import org.le.anno.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ViewAnnotationUtils {

    public static String generateKey(Object o){
        View view = o.getClass().getAnnotation(View.class);
        String key = view.key();
        return key;
    }
    public static String generateFtlPath(Object o){
        View view = o.getClass().getAnnotation(View.class);
        String ftlPath = view.ftlPath();
        return ftlPath;
    }

    public static String generateFtl(Object o) {
        String ftlPath = generateFtlPath(o);
        InputStream inputStream = ViewAnnotationUtils.class.getClassLoader().getResourceAsStream(ftlPath);
        if (inputStream == null) {
            throw new PipeFtlReadExcption("can not read ftl file. please check file path[" + ftlPath
                    + "] ftl file must in resources directory");
        }
        Reader reader = new InputStreamReader(inputStream);
        StringBuilder ftlBuilder = new StringBuilder();
        int len = -1;
        char[] buff = new char[1024];
        try {
            while ((len = reader.read(buff)) > 0) {
                ftlBuilder.append(new String(buff, 0, len));
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
        return ftlBuilder.toString();
    }
}
