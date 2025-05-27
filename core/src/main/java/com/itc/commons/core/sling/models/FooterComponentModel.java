package com.itc.commons.core.sling.models;

import com.itc.commons.core.beans.ListItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for managing the Footer Component within a content management system. This model is designed to handle
 * properties and child resources of the Footer Component, including various links, text, logos, and social media
 * information.
 */
@Model(adaptables = {Resource.class},
  defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FooterComponentModel {

  @ValueMapValue
  private String logoReference;
  @ValueMapValue
  private String portalText;
  @ValueMapValue
  private String portalTextLink;
  @ValueMapValue
  private String foodiesOnly;
  @ValueMapValue
  private String foodiesLink;
  @ValueMapValue
  private String esiteText;
  @ValueMapValue
  private String esiteLink;
  @ValueMapValue
  private String copyrightText;
  @ValueMapValue
  private String logoLink;
  @ValueMapValue
  private String logoAltText;

  @ValueMapValue
  private String secondaryLogoReference;
  @ValueMapValue
  private String secondaryLogoLink;
  @ValueMapValue
  private String secondaryLogoAltText;
  @ValueMapValue
  private String desktopImageReference;
  @ValueMapValue
  private String mobileImageReference;
  @ValueMapValue
  private String chosenColorPrimary;
  @ValueMapValue
  private String chosenColorSecondary;
  @ChildResource
  private Resource footerLogoField;
  private List<ListItems> socialMediaLogoList;
  /**
   * The constant FOOTER_MENU.
   */
  public static final String FOOTER_MENU = "footerMenu";
  /**
   * The constant FOOTER_MENU_LINK.
   */
  public static final String FOOTER_MENU_LINK = "footerMenuLink";
  /**
   * The constant SOCIAL_MEDIA_LOGO.
   */
  public static final String SOCIAL_MEDIA_LOGO = "socialMediaLogo";
  /**
   * The constant SOCIAL_MEDIA_LINK.
   */
  public static final String SOCIAL_MEDIA_LINK = "socialMediaLink";
  /**
   * The constant SOCIAL_MEDIA_NAME.
   */
  public static final String SOCIAL_MEDIA_NAME = "socialMediaName";

  /**
   * Initializes the model post-construction. Populates the lists for footer data and social media logos based on the
   * child resources.
   */
  @PostConstruct
  protected void init() {
    if (Objects.nonNull(footerLogoField)) {
      socialMediaLogoList = new ArrayList<>();
      footerLogoField.listChildren().forEachRemaining(item -> {
        ValueMap valueMap = item.getValueMap();
        String footerLogo = valueMap.get(SOCIAL_MEDIA_LOGO, StringUtils.EMPTY);
        String footerLink = valueMap.get(SOCIAL_MEDIA_LINK, StringUtils.EMPTY);
        String logoName = valueMap.get(SOCIAL_MEDIA_NAME, StringUtils.EMPTY);
        ListItems socialMediaItems = new ListItems();
        socialMediaItems.setText(footerLogo);
        socialMediaItems.setIconLink(footerLink);
        socialMediaItems.setIconName(logoName);
        socialMediaLogoList.add(socialMediaItems);
      });
    }
  }

  /**
   * Gets the list of social media logo items.
   *
   * @return A list of ListItems representing the social media logos.
   */
  public List<ListItems> getFooterLogoList() {
    return new ArrayList<>(socialMediaLogoList);
  }

  /**
   * Gets the portal text for the footer.
   *
   * @return The portal text.
   */
  public String getPortalText() {
    return portalText;
  }

  /**
   * Gets the portal text link for the footer.
   *
   * @return The portal text link.
   */
  public String getPortalTextLink() {
    return portalTextLink;
  }

  /**
   * Gets the 'Foodies Only' text for the footer.
   *
   * @return The 'Foodies Only' text.
   */
  public String getFoodiesOnly() {
    return foodiesOnly;
  }

  /**
   * Gets the link associated with the 'Foodies Only' text in the footer.
   *
   * @return The 'Foodies Only' link.
   */
  public String getFoodiesLink() {
    return foodiesLink;
  }

  /**
   * Gets the e-site text for the footer.
   *
   * @return The e-site text.
   */
  public String getEsiteText() {
    return esiteText;
  }

  /**
   * Gets the e-site link for the footer.
   *
   * @return The e-site link.
   */
  public String getEsiteLink() {
    return esiteLink;
  }

  /**
   * Gets the copyright text for the footer.
   *
   * @return The copyright text.
   */
  public String getCopyrightText() {
    return copyrightText;
  }

  /**
   * Gets logo link.
   *
   * @return the logo link
   */
  public String getLogoLink() {
    return logoLink;
  }

  /**
   * Gets logo alt text.
   *
   * @return the logo alt text
   */
  public String getLogoAltText() {
    return logoAltText;
  }

  /**
   * Gets logo reference.
   *
   * @return the logo reference
   */
  public String getLogoReference() {
    return logoReference;
  }

  /**
   * Gets Desktop Background Image reference.
   *
   * @return the background image reference for desktop.
   */
  public String getDesktopImageReference(){
    return desktopImageReference;
  }

  /**
   * Gets Mobile Background Image reference.
   *
   * @return the background image reference for mobile.
   */
  public String getMobileImageReference(){
    return mobileImageReference;
  }

  /**
   * Gets Primary Background Color.
   *
   * @return the primary background color.
   */
  public String getChosenColorPrimary(){
    return chosenColorPrimary;
  }

  /**
   * Gets Secondary Background Color.
   *
   * @return the secondary background color.
   */
  public String getChosenColorSecondary(){
    return chosenColorSecondary;
  }

  /**
   * Gets secondary logo reference.
   *
   * @return the secondary logo reference
   */
  public String getSecondaryLogoReference() {
    return secondaryLogoReference;
  }

  /**
   * Gets secondary logo link.
   *
   * @return the secondary logo link
   */
  public String getSecondaryLogoLink() {
    return secondaryLogoLink;
  }

  /**
   * Gets secondary logo alt text.
   *
   * @return the secondary logo alt text
   */
  public String getSecondaryLogoAltText() {
    return secondaryLogoAltText;
  }
}
