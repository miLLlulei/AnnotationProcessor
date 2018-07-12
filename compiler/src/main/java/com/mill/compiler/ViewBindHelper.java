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

public class ViewBindHelper {


    public static void parseBindView(Element element, Map<TypeElement, List<CodeBlock.Builder>> codeBuilderMap) {
        //获取最里面的节点
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String typeMirror = element.asType().toString();

        //注解的值
        int annotationValue = element.getAnnotation(BindView.class).value();
        String name = element.getSimpleName().toString();

        //创建代码块
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", name);//$L是占位符，会把后面的name参数拼接大$L所在的地方
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
        //获取最里面的节点
        String packageName = enclosingElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        String className = enclosingElement.getSimpleName().toString();

        TypeName type = TypeName.get(enclosingElement.asType());//此元素定义的类型
        if (type instanceof ParameterizedTypeName) {
            type = ((ParameterizedTypeName) type).rawType;
        }

        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");

        // 创建main方法
        MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, "target", Modifier.FINAL)
                .addParameter(ClassName.get("android.view", "View"), "source", Modifier.FINAL);
        for (CodeBlock.Builder codeBuilder : codeList) {
            methodSpecBuilder.addStatement(codeBuilder.build());
        }
        methodSpecBuilder.build();

        // 创建类
        TypeSpec bindClass = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpecBuilder.build())
                .build();

        try {
            // 生成文件
            JavaFile javaFile = JavaFile.builder(packageName, bindClass)
                    .build();
            //　将文件写出
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
