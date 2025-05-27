package com.itc.commons.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itc.commons.core.services.RecaptchaService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = RecaptchaService.class, immediate = true)
public class RecaptchaServiceImpl implements RecaptchaService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaServiceImpl.class);

  private String recaptchaSiteKey;
  private String recaptchaSecretKey;
  private int timeout;
  private String apiUrl;
  private String scriptUrl;

  @Activate
  @Modified
  protected void activate(RecaptchaConfig config) {
    this.apiUrl = config.apiUrl();
    this.scriptUrl = config.scriptUrl();
    this.timeout = config.timeout();
    this.recaptchaSiteKey = config.siteKey();
    this.recaptchaSecretKey = config.secretKey();
  }

  @Override
  public boolean verifyRecaptcha(String token) {
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout)
      .setSocketTimeout(timeout).build();
    CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    HttpPost httpPost = new HttpPost(apiUrl);
    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(new BasicNameValuePair("secret", this.recaptchaSecretKey));
    nameValuePairs.add(new BasicNameValuePair("response", token));
    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
    try {
      CloseableHttpResponse httpResponse = client.execute(httpPost);
      String response = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
      httpResponse.close();
      client.close();
      LOGGER.info("Recaptcha response {}", response);
      JsonElement jsonElement = new Gson().fromJson(response, JsonElement.class);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      return jsonObject.get("success").getAsBoolean();
    } catch (IOException e) {
      LOGGER.error("Unable to execute connection or parse response to JSON: {}", e.getMessage(), e);
    }
    return false;
  }

  @Override
  public String getRecaptchaSiteKey() {
    return recaptchaSiteKey;
  }

  @Override
  public String getRecaptchaScriptUrl() {
    return scriptUrl;
  }

  @ObjectClassDefinition(name = "ITC Google Recaptcha Configuration")
  public @interface RecaptchaConfig {

    /**
     * Verify url string.
     *
     * @return the string
     */
    @AttributeDefinition(
      name = "Recaptcha API URL",
      description = "The Google Recaptcha API Url to validate token"
    )
    String apiUrl();

    /**
     * Script url string.
     *
     * @return the string
     */
    @AttributeDefinition(
      name = "Recaptcha Script URL",
      description = "The Google Recaptcha Script Url to get token"
    )
    String scriptUrl();

    /**
     * The Site Key String of recaptcha.
     *
     * @return the site key name
     */
    @AttributeDefinition(
      name = "Site Key",
      description = "Site Key of the Google recaptcha"
    )
    String siteKey();

    /**
     * Secret key string of recaptcha.
     *
     * @return the secret key
     */
    @AttributeDefinition(
      name = "Secret Key",
      description = "Secret Key of the Google recaptcha"
    )
    String secretKey();

    /**
     * Timeout string.
     *
     * @return the int
     */
    @AttributeDefinition(
      name = "Connection Timeout",
      description = "Connection Timeout in millisecond"
    )
    int timeout();

  }
}
