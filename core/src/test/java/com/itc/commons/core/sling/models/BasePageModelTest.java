package com.itc.commons.core.sling.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The BasePageModel Test
 */
@ExtendWith(AemContextExtension.class)
class BasePageModelTest {

  /**
   * AEM Context
   */
  private final AemContext aemContext = new AemContext(ResourceResolverType.JCR_MOCK);

  /**
   * Creating BasePageModel object.
   */
  private BasePageModel basePageModel;

  /**
   * The setup method
   */
  @BeforeEach
  void setUp() {
    aemContext.addModelsForClasses(BasePageModel.class);
    aemContext.load().json("/com/itc/commons/core/sling/models/BasePageModelTest.json", "/content/candyman");
    aemContext.load().json("/com/itc/commons/core/sling/models/BasePageModelFavicon.json", "/content/dam/candyman");
    aemContext.request().setResource(aemContext.currentResource("/content/candyman/home-page/jcr:content"));
    aemContext.request().setAttribute("currentPage", aemContext.pageManager().getPage("/content/candyman/home-page"));
    basePageModel = aemContext.request().adaptTo(BasePageModel.class);
  }

  /**
   * The testGetFaviconMimeType method for fetching Favicon Mime Type.
   */
  @Test
  void testGetFaviconMimeType() {
    String iconMimetype = "image/png";
    assertEquals(iconMimetype, basePageModel.getFaviconMimeType());
  }

  /**
   * Test get thumbnail image.
   */
  @Test
  void testGetThumbnailImage() {
    String thumbnailImage = "/content/dam/thumbnailImage.png";
    assertEquals(thumbnailImage, basePageModel.getThumbnailImage());
  }
}
