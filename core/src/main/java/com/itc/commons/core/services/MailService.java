package com.itc.commons.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface MailService {

  void sendEmail(String groupName, ResourceResolver resourceResolver, String message, String subject) throws MessagingException, UnsupportedEncodingException;
}
