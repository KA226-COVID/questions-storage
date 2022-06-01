package com.juezlti.repository.models.yapexil.exclusionStrategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class FieldsExclusionStrategy implements ExclusionStrategy {
    private String[] fieldsProhibitedMetada;
    private final Class<?> excludedThisClass;

    public FieldsExclusionStrategy(String[] fieldsProhibitedMetada) {
        this.fieldsProhibitedMetada = fieldsProhibitedMetada;
        this.excludedThisClass = ExclusionStrategy.class;
    }

    public boolean shouldSkipClass(Class<?> clazz) {
        return excludedThisClass.equals(clazz);
    }

    public void setFieldsProhibitedMetada(String[] fieldsProhibitedMetada){
        this.fieldsProhibitedMetada = fieldsProhibitedMetada;
    }

    public boolean shouldSkipField(FieldAttributes f) {
        boolean skipField = false;
            for(String field :fieldsProhibitedMetada) {
                if(f.getName() == field){
                    skipField = true;
                }
            }
        return skipField;
    }
}