package com.itc.commons.core.services;

public interface RecaptchaService {

  boolean verifyRecaptcha(String token);

  String getRecaptchaSiteKey();

  String getRecaptchaScriptUrl();
}
