package com.github.jcgarciam.tools.metadata;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 **/
public class ClassMetadataCollector implements Iterable<ClassField> {
    private String className;
    private String packageClassName;
    private List<ClassField> fields;
    public ClassMetadataCollector(Element element, ProcessingEnvironment processingEnvironment, Set<String> excludes) {
        fields = new ArrayList<>();
        className = element.getSimpleName().toString();
        PackageElement sourcePackageElement = (PackageElement) element.getEnclosingElement();
        packageClassName = sourcePackageElement.getQualifiedName().toString();
        List<VariableElement> variableElements = ElementFilter.fieldsIn(element.getEnclosedElements());
        for (VariableElement variableElement : variableElements) {
            String fieldName = variableElement.getSimpleName().toString();
            if(excludes.contains(fieldName)) {
                continue;
            }
            Element innerElement = processingEnvironment.getTypeUtils().asElement(variableElement.asType());
            String fieldClass = "";
            if (innerElement == null) { // Primitive type
                PrimitiveType primitiveType = (PrimitiveType) variableElement.asType();
                fieldClass = primitiveType.toString();//typeUtils.boxedClass(primitiveType).getQualifiedName().toString();
            } else {
                if (innerElement instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) innerElement;
                    fieldClass = typeElement.getQualifiedName().toString();
                    /*
                    TypeElement collectionType = elementUtils.getTypeElement("java.util.Collection");
                    if (typeUtils.isAssignable(typeElement.asType(), collectionType.asType())) {
                        TypeVariable typeMirror = (TypeVariable)((DeclaredType)typeElement.asType()).getTypeArguments().get(0);
                        TypeParameterElement typeParameterElement = (TypeParameterElement) typeUtils.asElement(typeMirror);
                        // I am stuck here. I don't know how to get the
                        // full qualified class name of the generic type of
                        // property 'roles' when the code processes the User
                        // class as above. What I want to retrieve is the
                        // 'my.package.Role' value
                    }
                    */
                }
            }
            fields.add(new ClassField(fieldClass, fieldName));
        }
    }

    @Override
    public Iterator<ClassField> iterator() {
        return fields.iterator();
    }

    public String getClassName() {
        return className;
    }

    public String getPackageClassName() {
        return packageClassName;
    }
}
