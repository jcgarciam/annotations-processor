package com.github.jcgarciam.tools.processors;

import com.github.jcgarciam.tools.annotations.JaxRsEndpointExporter;
import com.github.jcgarciam.tools.metadata.ClassField;
import com.github.jcgarciam.tools.metadata.ClassMetadataCollector;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 *
 **/
@SupportedAnnotationTypes("com.github.jcgarciam.tools.annotations.JaxRsEndpointExporter")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JaxRsEndpointGeneratorProcessor extends AbstractProcessor {
    public JaxRsEndpointGeneratorProcessor() {
        super();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || annotations.size() == 0) {
            return false;
        }

        Filer filer          = processingEnv.getFiler();
        String newLine        = System.getProperty("line.separator");

        for (Element element : roundEnv.getElementsAnnotatedWith(annotations.iterator().next())) {
            if (element.getKind() == ElementKind.CLASS) {
                //debug("DTOGenerator for class " + element.getSimpleName());

                JaxRsEndpointExporter annot = element.getAnnotation(JaxRsEndpointExporter.class);
                generateUsinClassMetadataCollector(filer, newLine, element, annot);

            }
        }
        return true;
    }

    private void generateUsinClassMetadataCollector(final Filer theFiler, final String theNewLine, final Element theElement, final JaxRsEndpointExporter theAnnot) {
        String nameSuffix = theAnnot.suffix();
        ClassMetadataCollector metadataCollector = new ClassMetadataCollector(theElement,
                                                                              processingEnv,
                                                                              new HashSet<String>());

        String targetPackageName = theAnnot.packageName();
        if("".equals(targetPackageName) || targetPackageName == null){
            targetPackageName = metadataCollector.getPackageClassName();
        }
        String sourceClassName = metadataCollector.getClassName();
        String targetClassName = sourceClassName+nameSuffix;
        //TODO:Utilizar template FreeMarker
        try {

            JavaFileObject jfo = theFiler.createSourceFile(targetPackageName+".Base"+targetClassName, theElement) ;
            Writer w = jfo.openWriter();
            w.write("package "+targetPackageName+ ";"+theNewLine);
            w.write(theNewLine);
            w.write("import java.io.Serializable;");
            w.write(theNewLine);
            w.write("import "+metadataCollector.getPackageClassName()+"."+sourceClassName+";"+theNewLine);
            w.write("import com.wasab.web.dto."+metadataCollector.getClassName()+"DTO;"+theNewLine);
            w.write("import com.wasab.web.dto.builder.ListObjectBuilder;"+theNewLine);

            w.write(theNewLine);

            w.write("import javax.inject.Inject;"+theNewLine);
            w.write("import javax.persistence.EntityManager;"+theNewLine);
            w.write("import javax.persistence.PersistenceContext;"+theNewLine);
            w.write("import javax.ws.rs.*;"+theNewLine);
            w.write("import javax.ws.rs.core.Response;"+theNewLine);
            w.write("import java.util.List;"+theNewLine);

            w.write(theNewLine);
            w.write("import static javax.ws.rs.core.MediaType.APPLICATION_JSON;"+theNewLine);

            w.write(theNewLine);
            w.write("//JAX-RS Endpoint for "+metadataCollector.getPackageClassName()+"."+sourceClassName+theNewLine);
            w.write("//At: " + GregorianCalendar.getInstance().getTime().toString() + theNewLine);
            w.write("public abstract class Base" + targetClassName + " {" + theNewLine);

            w.write(theNewLine);
            w.write("\t@PersistenceContext"+theNewLine);
            w.write("\tprotected EntityManager entityManager;"+theNewLine);

            w.write(theNewLine);
            w.write("\t@Inject" +theNewLine+
                    "\tprotected ListObjectBuilder listObjectBuilder;");

            w.write(theNewLine);
            w.write(theNewLine);

            w.write("    protected Object fromJPAToDTO("+sourceClassName+" obj){\n" +
                    "        return new "+sourceClassName+"DTO(obj);\n" +
                    "    }\n");
            w.write(theNewLine);
            w.write("    @SuppressWarnings(\"unchecked\")\n" +
                    "    protected List fromJPAListToDTOList(List objList){\n" +
                    "        return listObjectBuilder.build("+sourceClassName+"DTO.class, "+sourceClassName+".class, objList);\n" +
                    "    }\n");
            w.write(theNewLine);
            w.write("    @GET\n" +
                            "    @Path(\"/{id}\")\n" +
                            "    public Response getById(@PathParam(\"id\") int id) {\n" +
                            "        "+sourceClassName+" found = entityManager.find("+sourceClassName+".class, id);\n" +
                            "        if(found == null) {\n" +
                            "            return Response.status(Response.Status.NOT_FOUND).entity(\"Entity not found.\").build();\n" +
                            "        }\n" +
                            "        return Response.ok(fromJPAToDTO(found)).build();\n" +
                            "    }"+theNewLine);

            w.write(theNewLine);

            w.write("    @GET\n" +
                            "    @Path(\"list\")\n" +
                            "    public Response list() {\n" +
                            "        final List<"+sourceClassName+"> lst = entityManager.createNamedQuery(\""+sourceClassName+".findAll\").getResultList();\n" +
                            "        return Response.ok(fromJPAListToDTOList(lst) ).build();\n" +
                            "    }"+theNewLine);

            w.write(theNewLine);
            w.write("    @POST\n" +
                            "    @Path(\"create\")\n" +
                            "    public Response create("+sourceClassName+" param){\n" +
                            "        param = entityManager.merge(param);\n" +
                            "        return Response.ok(fromJPAToDTO(param)).build();\n" +
                            "    }"+theNewLine);

            w.write(theNewLine);

            w.write("    protected void updateMergeFields("+sourceClassName+" persisted, "+sourceClassName+" httpParam){\n");
            for (ClassField field : metadataCollector) {
                w.write("\t\tif(httpParam.get"+capitalize(field.getPropertyName())+"() != null){\n");
                w.write("\t\t\tpersisted.set"+capitalize(field.getPropertyName())+ "(httpParam.get"+capitalize(field.getPropertyName())+"());\n");
                w.write("\t\t}\n");
            }
            w.write("    }");
            w.write(theNewLine);
            w.write("    @POST\n" +
                            "    @Path(\"update\")\n" +
                            "    public Response update("+sourceClassName+" param){\n" +
                            "        "+sourceClassName+" persisted = entityManager.find("+sourceClassName+".class, param.getId());\n" +
                            "        updateMergeFields(persisted, param);\n" +
                            "        entityManager.merge(persisted);\n" +
                            "        return Response.ok(fromJPAToDTO(persisted)).build();\n" +
                            "    }"+theNewLine);

            w.write(theNewLine);

            w.write("    @POST\n" +
                            "    @Path(\"delete/{id}\")\n" +
                            "    public Response delete(@PathParam(\"id\")int id){\n" +
                            "        "+sourceClassName+" apps = entityManager.find("+sourceClassName+".class, id);\n" +
                            "        entityManager.remove(apps);\n" +
                            "        return Response.ok(fromJPAToDTO(apps)).build();\n" +
                            "    }"+theNewLine);

            w.write(theNewLine);

            w.write("}"+theNewLine);
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
}
