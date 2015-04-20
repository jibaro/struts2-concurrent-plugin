package org.le.util;

import org.le.anno.Param;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectUtils {

    public static Map<String, Object> getFieldValueWithAnnoParamFromObject(Object o) {
        Map<String, Object> extMap = new HashMap<String, Object>();
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Param.class) != null) {
                extMap.put(field.getName(), getFieldValue(o, field));
            }
        }
        return extMap;
    }

    public static Map<String, Object> getFieldValueWithGetterMethod(Object o) {
        Map<String, Object> extMap = new HashMap<String, Object>();
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (hasGetterMethod(o, field)) {
                extMap.put(field.getName(), getFieldValue(o, field));
            }
        }
        return extMap;
    }

    public static Map<String,Object> getFieldValueForFreemarker(Object o){
        Map<String, Object> extMap = new HashMap<String, Object>();
        extMap.putAll(getFieldValueWithAnnoParamFromObject(o));
        extMap.putAll(getFieldValueWithGetterMethod(o));
        return extMap;
    }

    private static Object getFieldValue(Object o, Field field) {
        field.setAccessible(true);
        try {
            return field.get(o);
        } catch (IllegalAccessException e) {
            return null;
            //becase has set field accessible so do not throw this exception
        }
    }



    private static boolean hasGetterMethod(Object o, Field field) {
        List<String> methods = getMethods(o);
        return methods.contains(("get" + field.getName()).toLowerCase());
    }

    private static List<String> getMethods(Object o) {
        Method[] methods = o.getClass().getDeclaredMethods();
        List<String> result = new ArrayList<String>(methods.length);
        for (Method method : methods) {
            result.add(method.getName().toLowerCase());
        }
        return result;
    }
}