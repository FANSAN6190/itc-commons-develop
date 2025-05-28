package com.itc.commons.core.services.impl;

import org.apache.sling.api.resource.ResourceResolver;

import javax.mail.MessagingException;

public interface MailService {

  void sendEmail(String groupName, ResourceResolver resourceResolver, String message, String subject, boolean isGroup) throws MessagingException;
}
