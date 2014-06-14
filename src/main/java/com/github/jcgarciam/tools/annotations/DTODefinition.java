package com.github.jcgarciam.tools.annotations;

/**
 *
 **/
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface DTODefinition {
    public String DTOName();
    public boolean generateGettersAndSetters() default true;
    public DTODefinitionProperties[] properties() default {};

    public @interface DTODefinitionProperties{
        public String propertyType();
        public String propertyName();
    }
}
