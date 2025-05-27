package com.itc.commons.core.sling.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itc.commons.core.beans.ListItems;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The FooterComponentModelTest
 */
@ExtendWith(AemContextExtension.class)
class FooterComponentModelTest {

  /**
   * The Aem Context
   */
  private final AemContext ctx = new AemContext(ResourceResolverType.JCR_MOCK);

  /**
   * The FooterComponentModel object.
   */
  private FooterComponentModel footerComponentModel;

  /**
   * The expectedSocialMediaLogoList
   */
  private List<ListItems> expectedSocialMediaLogoList = new ArrayList<>();

  /**
   * The setUp
   */
  @BeforeEach
  void setUp() {
    ctx.addModelsForClasses(FooterComponentModel.class);
    ctx.load().json("/com/itc/commons/core/sling/models/FooterComponentModelTest.json", "/content/itcshared");
    footerComponentModel = Objects.requireNonNull(ctx.currentResource("/content/itcshared/footer"))
      .adaptTo(FooterComponentModel.class);
  }

  /**
   * The testGetFooterLogoList to test getFooterLogoList Method
   */
  @Test
  void testGetFooterLogoList() {
    ListItems socialMediaLogoItem1 = new ListItems();
    socialMediaLogoItem1.setText("/content/dam/itcshared/damAsset/path3620@2x.png");
    socialMediaLogoItem1.setIconLink("https://www.facebook.com/itcportal/");
    ListItems socialMediaLogoItem2 = new ListItems();
    socialMediaLogoItem2.setText("/content/dam/itcshared/damAsset/path3621@2x.png");
    socialMediaLogoItem2.setIconLink("https://twitter.com/i/flow/login?redirect_after_login=%2FITCCorpCom");
    ListItems socialMediaLogoItem3 = new ListItems();
    socialMediaLogoItem3.setText("/content/dam/itcshared/damAsset/instagram_icon.png");
    socialMediaLogoItem3.setIconLink("https://www.instagram.com/itc_limited/");
    ListItems socialMediaLogoItem4 = new ListItems();
    socialMediaLogoItem4.setText("/content/dam/itcshared/en/damAsset/path3622@2x.png");
    socialMediaLogoItem4.setIconLink("https://www.youtube.com/channel/UCA6kL19PkfpLSu57zIyWHrg");
    ListItems socialMediaLogoItem5 = new ListItems();
    socialMediaLogoItem5.setText("/content/dam/itcshared/damAsset/whatsapp-icon.png");
    socialMediaLogoItem5.setIconLink("https://www.itcportal.com/contact-us.aspx");
    expectedSocialMediaLogoList.add(socialMediaLogoItem1);
    expectedSocialMediaLogoList.add(socialMediaLogoItem2);
    expectedSocialMediaLogoList.add(socialMediaLogoItem3);
    expectedSocialMediaLogoList.add(socialMediaLogoItem4);
    expectedSocialMediaLogoList.add(socialMediaLogoItem5);
    assertEquals(expectedSocialMediaLogoList, footerComponentModel.getFooterLogoList());
  }

  /**
   * The testGetPortalText to test getPortalText Method
   */
  @Test
  void testGetPortalText() {
    String expectedPortalText = "ITC Portal";
    assertEquals(expectedPortalText, footerComponentModel.getPortalText());
  }

  /**
   * The testGetPortalTextLink to test getPortalTextLink Method
   */
  @Test
  void testGetPortalTextLink() {
    String expectedPortalTextLink = "https://www.itcportal.com/";
    assertEquals(expectedPortalTextLink, footerComponentModel.getPortalTextLink());
  }

  /**
   * The testGetFoodiesOnly to test getFoodiesOnly Method
   */
  @Test
  void testGetFoodiesOnly() {
    String expectedFoodiesOnly = "ITC Foodiesonly";
    assertEquals(expectedFoodiesOnly, footerComponentModel.getFoodiesOnly());
  }

  /**
   * The testGetFoodiesLink to test getFoodiesLink Method
   */
  @Test
  void testGetFoodiesLink() {
    String expectedFoodiesLink = "https://www.itcportal.com/businesses/fmcg/foods.aspx";
    assertEquals(expectedFoodiesLink, footerComponentModel.getFoodiesLink());
  }

  /**
   * The testGetEsiteText to test getEsiteText Method
   */
  @Test
  void testGetEsiteText() {
    String expectedEsiteText = "ITC eStore";
    assertEquals(expectedEsiteText, footerComponentModel.getEsiteText());
  }

  /**
   * The testGetEsiteLink to test getEsiteLink Method
   */
  @Test
  void testGetEsiteLink() {
    String expectedEsiteLink = "https://www.itcstore.in/brand/itcshared";
    assertEquals(expectedEsiteLink, footerComponentModel.getEsiteLink());
  }

  /**
   * The testGetEsiteText to test getEsiteText Method
   */
  @Test
  void testgetCopyrightText() {
    String expectedCopyrightText = "Copyright © 2024";
    assertEquals(expectedCopyrightText, footerComponentModel.getCopyrightText());
  }

  /**
   * The testGetLogoLink to test getLogoLink Method
   */
  @Test
  void testGetLogoLink() {
    String expectedLogoLink = "/content/itcshared/language-masters/en/home-page";
    assertEquals(expectedLogoLink, footerComponentModel.getLogoLink());
  }

  /**
   * The testGetLogo to test getLogo Method
   */
  @Test
  void testGetLogo() {
    String expectedLogo = "/content/dam/itcshared/damAsset/itcshared Logo.png";
    assertEquals(expectedLogo, footerComponentModel.getLogoReference());
  }

  /**
   * The testGetLogoAltText to test getLogoAltText Method
   */
  @Test
  void testGetLogoAltText() {
    String expectedLogoAltText = "ITC Logo";
    assertEquals(expectedLogoAltText, footerComponentModel.getLogoAltText());
  }
}
