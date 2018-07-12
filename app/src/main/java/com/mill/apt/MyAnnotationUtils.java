package com.mill.apt;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.InvocationTargetException;

public class MyAnnotationUtils {

    public static void bind(Activity activity) {
        //获取activity的decorView(根view)
        View view = activity.getWindow().getDecorView();
        bind(activity, view);
    }

    private static void bind(Object obj, View view) {
        String qualifiedName = obj.getClass().getName();
        //找到该activity对应的Bind类的名字
        String generateClass = qualifiedName + "_ViewBinding";
        //然后调用Bind类的构造方法,从而完成activity里view的初始化
        try {
            Class.forName(generateClass).getConstructor(obj.getClass(), View.class).newInstance(obj, view);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
