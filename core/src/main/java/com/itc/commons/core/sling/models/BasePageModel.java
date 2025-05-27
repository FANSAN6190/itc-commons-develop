package com.itc.commons.core.sling.models;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.Page;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for managing the favicon of this site within the content management system.
 * This model is designed for handling property associated with the base page, such as the Mimetype of the
 * favicon.
 */
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BasePageModel {

  /**
   * Current Page
   */
  @ScriptVariable
  private Page currentPage;

  @ValueMapValue(name = "image/fileReference")
  private String thumbnailImage;

  /**
   * Resource Resolver
   */
  @SlingObject
  private ResourceResolver resourceResolver;

  /**
   * Constant for "faviconFileReference"
   */
  public static final String FAVICON_FILE_REFERENCE = "faviconFileReference";

  private String faviconMimeType;

  /**
   * Initializes the model post-construction.
   * This method fetches the property inherited from parent page and retrieve its value.
   */
  @PostConstruct
  protected void init() {
    String pathFormat = StringUtils.EMPTY;
    InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(currentPage.getContentResource());
    String inheritedCustomProperty = inheritanceValueMap.getInherited(FAVICON_FILE_REFERENCE, String.class);
    if(Objects.nonNull(inheritedCustomProperty)) {
      Resource assetResource = resourceResolver.getResource(inheritedCustomProperty);
      if(Objects.nonNull(assetResource)) {
        Asset asset = assetResource.adaptTo(Asset.class);
        if(Objects.nonNull(asset)) {
          pathFormat = asset.getMetadataValue(DamConstants.DC_FORMAT);
        }
      }
    }
    this.faviconMimeType = pathFormat;
  }

  /**
   * Retrieves the format of the icon associated with the site.
   * @return The MimeType of the favicon.
   */
  public String getFaviconMimeType() {
    return faviconMimeType;
  }

  /**
   * Gets thumbnail image.
   *
   * @return the thumbnail image
   */
  public String getThumbnailImage() {
    return thumbnailImage;
  }

  /**
   * @return the brands Name
   * */
  public String getBrandName() {
    String path = currentPage.getPath();
    String[] parts = StringUtils.split(path, '/');

    String brandName = "";
    if (parts.length >= 3) {
      brandName = parts[1];
    }
    return brandName;
  }

}
