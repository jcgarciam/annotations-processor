package com.github.jcgarciam.tools.metadata;

/**
 *
 **/
public class ClassField {
    private String propertyType;
    private String propertyName;

    public ClassField(final String propertyType, final String propertyName) {
        this.propertyType = propertyType;
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(final String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(final String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Field{");
        sb.append("propertyType='").append(propertyType).append('\'');
        sb.append(", propertyName='").append(propertyName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
