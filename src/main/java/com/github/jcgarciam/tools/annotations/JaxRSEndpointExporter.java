package com.github.jcgarciam.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 **/
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface JaxRsEndpointExporter {
    public String packageNameEndpoint();
    public String suffix() default "Endpoint";
}
