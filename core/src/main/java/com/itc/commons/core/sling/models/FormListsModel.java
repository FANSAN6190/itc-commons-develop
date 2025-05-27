package com.itc.commons.core.sling.models;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FormListsModel {
    @ValueMapValue
    private String checkboxDescription;

    @ValueMapValue
    private String formFields;
    @ValueMapValue
    private String answerLabel;

    @ValueMapValue
    private String nameLabel;
    @ValueMapValue
    private String namePlaceholder;
    @ValueMapValue
    private String nameIsRequired;
    @ValueMapValue
    private String nameErrorLabel;
    @ValueMapValue
    private String nameEmptyLabel;

    @ValueMapValue
    private String emailLabel;
    @ValueMapValue
    private String emailPlaceholder;
    @ValueMapValue
    private String emailIsRequired;
    @ValueMapValue
    private String emailErrorLabel;
    @ValueMapValue
    private String emailEmptyLabel;

    @ValueMapValue
    private String mobileLabel;
    @ValueMapValue
    private String mobilePlaceholder;
    @ValueMapValue
    private String mobileIsRequired;
    @ValueMapValue
    private String mobileErrorLabel;
    @ValueMapValue
    private String mobileEmptyLabel;

    @ValueMapValue
    private String genericInputLabel;
    @ValueMapValue
    private String genericInputPlaceholder;
    @ValueMapValue
    private String genericInputValue;
    @ValueMapValue
    private String genericInputIsRequired;
    @ValueMapValue
    private String genericInputErrorLabel;
    @ValueMapValue
    private String genericInputEmptyLabel;

    public String getFormFields() {
        return formFields;
    }

    public String getNameLabel() {
        return nameLabel;
    }

    public String getEmailLabel() {
        return emailLabel;
    }

    public String getMobileLabel() {
        return mobileLabel;
    }

    public String getAnswerLabel() {
        return answerLabel;
    }

    public String getGenericInputLabel() {
        return genericInputLabel;
    }
    public String getCheckboxDescription() {
        return checkboxDescription;
    }

}

