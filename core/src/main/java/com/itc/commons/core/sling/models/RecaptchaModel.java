package com.itc.commons.core.sling.models;

import com.itc.commons.core.services.RecaptchaService;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

/**
 * Sling Model for getting recaptcha Script URL and site Key.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RecaptchaModel {

  @OSGiService
  private RecaptchaService recaptchaService;

  /**
   * Gets recaptcha script URL.
   *
   * @return the recaptcha script URL.
   */
  public String getRecaptchaScriptURL() {
    return recaptchaService.getRecaptchaScriptUrl();
  }

  /**
   * Gets recaptcha site key.
   *
   * @return the recaptcha site key
   */
  public String getRecaptchaSiteKey() {
    return recaptchaService.getRecaptchaSiteKey();
  }
}