package com.mill.compiler;

import com.google.auto.service.AutoService;
import com.mill.annotation.BindView;
import com.mill.annotation.HelloAnnotation;
import com.mill.annotation.OnClick;
import com.squareup.javapoet.CodeBlock;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class HelloProcessor extends AbstractProcessor {
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler(); // for creating file
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (TypeElement element : annotations) {
            //编译时，生成 java 类
            if (element.getQualifiedName().toString().equals(HelloAnnotation.class.getCanonicalName())) {
                ClassCreateHelper.buildClass(filer);
            }
        }

        // Process each @BindView element.
        Map<TypeElement, List<CodeBlock.Builder>> builderMap = findAndParseTargets(env);
        for (TypeElement typeElement : builderMap.keySet()) {
            List<CodeBlock.Builder> codeList = builderMap.get(typeElement);
            ViewBindHelper.writeBindView(typeElement, codeList, filer);
        }
        return true;
    }

    private Map<TypeElement, List<CodeBlock.Builder>> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, List<CodeBlock.Builder>> builderMap = new HashMap<>();

        for (Element element : env.getElementsAnnotatedWith(BindView.class)) {
            ViewBindHelper.parseBindView(element, builderMap);
        }

        for (Element element : env.getElementsAnnotatedWith(OnClick.class)) {
            ViewBindHelper.parseListenerView(element, builderMap);
        }
        return builderMap;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(HelloAnnotation.class.getCanonicalName());
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
