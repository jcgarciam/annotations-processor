package com.github.jcgarciam.tools.processors;

import com.github.jcgarciam.tools.annotations.DTODefinition;
import com.github.jcgarciam.tools.annotations.DTOGenerator;
import com.github.jcgarciam.tools.metadata.ClassField;
import com.github.jcgarciam.tools.metadata.ClassMetadataCollector;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 *
 **/
@SupportedAnnotationTypes("com.github.jcgarciam.tools.annotations.DTOGenerator")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DTOAnnotationProcessor extends AbstractProcessor {
    public DTOAnnotationProcessor() {
        super();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        if (roundEnv.processingOver() || annotations.size() == 0) {
            return false;
        }

        Filer  filer          = processingEnv.getFiler();
        String newLine        = System.getProperty("line.separator");

        for (Element element : roundEnv.getElementsAnnotatedWith(annotations.iterator().next())) {
            if (element.getKind() == ElementKind.CLASS) {
                debug("DTOGenerator for class " + element.getSimpleName());

                DTOGenerator annot = element.getAnnotation(DTOGenerator.class);
                if(annot.describe() == null || annot.describe().length == 0) {
                    generateUsinClassMetadataCollector(filer, newLine, element, annot);
                }else{
                    generateUsingDescribeMetadata(filer, newLine, element, annot);
                }
            }
        }
        return true;
    }

    private void generateUsingDescribeMetadata(final Filer filer, final String newLine, final Element element, final DTOGenerator annot) {
        String targetPackageName = annot.packageName();
        PackageElement sourcePackageElement = (PackageElement) element.getEnclosingElement();
        String elementPackageClassName = sourcePackageElement.getQualifiedName().toString();

        for(DTODefinition def : annot.describe()){
            String sourceClassName = def.DTOName();
            String targetClassName = sourceClassName+annot.suffix();

            //TODO:Utilizar template FreeMarker
            try {
                JavaFileObject jfo = filer.createSourceFile(targetPackageName + "." + targetClassName, element);
                Writer w = jfo.openWriter();
                w.write("package " + targetPackageName + ";" + newLine);
                w.write(newLine);
                w.write("import java.io.Serializable;");
                w.write(newLine);
                w.write(newLine);
                w.write("//DTO Generated from " + elementPackageClassName+"."+element.getSimpleName());
                w.write(newLine);
                w.write("//At: " + GregorianCalendar.getInstance().getTime().toString() + newLine);
                w.write("public class " + targetClassName + " implements Serializable {" + newLine);

                //private fields
                for (DTODefinition.DTODefinitionProperties prop: def.properties()) {
                    String fieldClass = prop.propertyType();
                    String fieldName  = prop.propertyName();
                    if(def.generateGettersAndSetters()) {
                        w.write("\tprivate " + fieldClass + " " + fieldName + ";" + newLine);
                    }else{
                        w.write("\tpublic " + fieldClass + " " + capitalize(fieldName) + ";" + newLine);
                    }
                }

                //getters and setters
                if(def.generateGettersAndSetters()) {
                    w.write(newLine);
                    for (DTODefinition.DTODefinitionProperties prop : def.properties()) {
                        String fieldClass = prop.propertyType();
                        String fieldName = prop.propertyName();
                        w.write("\tpublic " + fieldClass + " get" + capitalize(fieldName) + "(){" + newLine);
                        w.write("\t\treturn this." + fieldName + ";" + newLine);
                        w.write("\t}" + newLine);
                        w.write("\tpublic void set" + capitalize(fieldName) + "(" + fieldClass + " value){" + newLine);
                        w.write("\t\tthis." + fieldName + " = value;" + newLine);
                        w.write("\t}" + newLine);
                        w.write(newLine);
                    }
                }

                w.write("}" + newLine);
                w.flush();
                w.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void generateUsinClassMetadataCollector(final Filer filer, final String newLine, final Element element, final DTOGenerator annot) {
        String nameSuffix = annot.suffix();
        HashSet<String> excludes = new HashSet<>(Arrays.asList(annot.excludes()));
        ClassMetadataCollector metadataCollector = new ClassMetadataCollector(element,
                                                                              processingEnv,
                                                                              excludes);

        String targetPackageName = annot.packageName();
        if("".equals(targetPackageName) || targetPackageName == null){
            targetPackageName = metadataCollector.getPackageClassName();
        }
        String sourceClassName = metadataCollector.getClassName();
        String targetClassName = sourceClassName+nameSuffix;

        //TODO:Utilizar template FreeMarker
        try {

            JavaFileObject jfo = filer.createSourceFile(targetPackageName+"."+targetClassName, element) ;
            Writer w = jfo.openWriter();
            w.write("package "+targetPackageName+ ";"+newLine);
            w.write(newLine);
            w.write("import java.io.Serializable;");
            w.write(newLine);
            w.write("import "+metadataCollector.getPackageClassName()+"."+sourceClassName+";");
            w.write(newLine);
            w.write(newLine);
            w.write("//DTO for "+metadataCollector.getPackageClassName()+"."+sourceClassName+newLine);
            w.write("//At: " + GregorianCalendar.getInstance().getTime().toString() + newLine);
            w.write("public class " + targetClassName + " implements Serializable {" + newLine);

            //private fields
            for (ClassField field : metadataCollector) {
                String fieldClass = field.getPropertyType();
                String fieldName  = field.getPropertyName();
                w.write("\tprivate " + fieldClass + " " + fieldName + ";" + newLine);
            }

            w.write(newLine);
            //constructor
            w.write("\tpublic "+targetClassName+"(){"+newLine);
            w.write("\t}"+newLine);
            w.write(newLine);
            w.write("\tpublic "+targetClassName+"("+sourceClassName+" theSource){"+newLine);
            for (ClassField field : metadataCollector) {
                String capitalizedFieldName = capitalize(field.getPropertyName());

                w.write("\t\tthis.set"+capitalize(capitalizedFieldName)+"(theSource.get"+capitalizedFieldName+"());"+newLine);
            }
            w.write("\t}"+newLine);
            //end of constructor

            //getters and setters
            w.write(newLine);
            for (ClassField field : metadataCollector) {
                String fieldClass = field.getPropertyType();
                String fieldName  = field.getPropertyName();
                w.write("\tpublic "+fieldClass+ " get" + capitalize(fieldName )+ "(){"+newLine);
                w.write("\t\treturn this."+fieldName+";"+newLine);
                w.write("\t}"+newLine);
                w.write("\tpublic void set" + capitalize(fieldName )+ "("+fieldClass+" value){"+newLine);
                w.write("\t\tthis."+fieldName + " = value;"+newLine);
                w.write("\t}"+newLine);
                w.write(newLine);
            }

            w.write("}"+newLine);
            w.flush();
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String str){
        char[] c_str = str.toCharArray();
        c_str[0] = Character.toTitleCase(c_str[0]);
        return String.valueOf(c_str);
    }

    private void debug(String message){
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

}
