package com.mill.compiler;

import com.mill.annotation.BindView;
import com.mill.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.View;

public class ViewBindHelper {


    public static void parseBindView(Element element, Map<TypeElement, List<CodeBlock.Builder>> codeBuilderMap) {
        // 获取最里面的节点， 具体实际可能就是 某个Activity对象
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        // 这个view是哪个类 Class
        String typeMirror = element.asType().toString();
        // 注解的值，具体实际可能就是 R.id.xxx
        int annotationValue = element.getAnnotation(BindView.class).value();
        // 这个view对象 名称
        String name = element.getSimpleName().toString();

        //创建代码块
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", name); //$L是占位符，会把后面的 name 参数拼接到 $L 所在的地方
        builder.add("($L)source.findViewById($L)", typeMirror, annotationValue);

        List<CodeBlock.Builder> codeList = codeBuilderMap.get(enclosingElement);
        if (codeList == null) {
            codeList = new ArrayList<>();
            codeBuilderMap.put(enclosingElement, codeList);
        }
        codeList.add(builder);
    }

    public static void parseListenerView(Element element, Map<TypeElement, List<CodeBlock.Builder>> codeBuilderMap) {
        //获取最里面的节点
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        List<CodeBlock.Builder> codeList = codeBuilderMap.get(enclosingElement);
        if (codeList == null) {
            codeList = new ArrayList<>();
            codeBuilderMap.put(enclosingElement, codeList);
        }

        //注解的值
        int[] annotationValue = element.getAnnotation(OnClick.class).value();
        String name = element.getSimpleName().toString();

        //创建代码块
        for (int value : annotationValue) {
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.add("source.findViewById($L).setOnClickListener(new android.view.View.OnClickListener() { public void onClick(View v) { target.$L(v); }})", value, name);
            codeList.add(builder);
        }
    }

    public static void writeBindView(TypeElement enclosingElement, List<CodeBlock.Builder> codeList, Filer filer) {
        // enclosingElement ，暗指 某个Activity.
        // 先拿到 Activity 所在包名
        String packageName = enclosingElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        // 再拿到 Activity 类名
        String className = enclosingElement.getSimpleName().toString();

        // 再拿到 Activity 是 哪个类
        TypeName type = TypeName.get(enclosingElement.asType());//此元素定义的类型
        if (type instanceof ParameterizedTypeName) {
            type = ((ParameterizedTypeName) type).rawType;
        }

        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");

        // 创建构造方法 如果 Activity是 MainActivity，则会有 生成如下构造方法
//        public MainActivity_ViewBinding(final MainActivity target, final View source) {
//            target.btn1 = (android.widget.Button)source.findViewById(2131165217);
//            source.findViewById(2131165217).setOnClickListener(new android.view.View.OnClickListener() { public void onClick(View v) { target.onBtn1Click(v); }});
//        }
        MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, "target", Modifier.FINAL)
                .addParameter(ClassName.get("android.view", "View"), "source", Modifier.FINAL);
        for (CodeBlock.Builder codeBuilder : codeList) {
            //方法里面 ，代码是什么
            methodSpecBuilder.addStatement(codeBuilder.build());
        }
        methodSpecBuilder.build();

        // 创建类 MainActivity_ViewBinding
        TypeSpec bindClass = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpecBuilder.build())
                .build();

        try {
            // 生成文件
            JavaFile javaFile = JavaFile.builder(packageName, bindClass).build();
            //　将文件写出
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
