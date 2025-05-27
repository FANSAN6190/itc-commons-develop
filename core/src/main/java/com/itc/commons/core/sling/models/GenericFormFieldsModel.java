package com.itc.commons.core.sling.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.*;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GenericFormFieldsModel {
    @ValueMapValue
    private String formTitle;


    @ValueMapValue
    private String brandName;

    @ValueMapValue
    private String campaignName;

    @ChildResource(name = "formLists")
    private List<FormListsModel> formLists;

    private List<String> fieldLabels;

    @PostConstruct
    protected void init() {
        fieldLabels = new ArrayList<>();

        if (formLists != null) {
            for (FormListsModel formField : formLists) {
                if (formField.getNameLabel() != null) {
                    fieldLabels.add(formField.getNameLabel());
                }
                if (formField.getEmailLabel() != null) {
                    fieldLabels.add(formField.getEmailLabel());
                }
                if (formField.getAnswerLabel() != null) {
                    fieldLabels.add(formField.getAnswerLabel());
                }
                if (formField.getMobileLabel() != null) {
                    fieldLabels.add(formField.getMobileLabel());
                }
                if (formField.getGenericInputLabel() != null) {
                    fieldLabels.add(formField.getGenericInputLabel());
                }
            }
        }
        System.out.println("Final Field Labels: " + fieldLabels);
    }

    public String getBrandName() {
        return brandName;
    }
    public String getFormTitle() {
        return formTitle;
    }

    public String getCampaignName() {
        return campaignName + "details.xlsx";
    }

    public List<FormListsModel> getFormLists() {
        return new ArrayList<>(formLists);
    }

    public List<String> getFieldLabels() {
        return fieldLabels;
    }
}
