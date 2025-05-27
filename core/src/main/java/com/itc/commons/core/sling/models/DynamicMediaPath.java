package com.itc.commons.core.sling.models;

import static com.day.cq.dam.commons.util.DamUtil.resolveToAsset;

import com.day.cq.dam.api.s7dam.utils.PublishUtils;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class DynamicMediaPath {

  @Self
  SlingHttpServletRequest request;

  @RequestAttribute
  private String assetPath;

  @OSGiService
  private PublishUtils publishUtils;

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMediaPath.class);

  private static final String SLASH = "/";
  private static final String CONTENT_DAM = "/content/dam";
  private static final String SCENE7_TYPE = "dam:scene7Type";
  private static final String IS_IMAGE = "is/image";
  private static final String IS_CONTENT = "is/content";
  private static final String VIDEO = "Video";
  private static final String IMAGE = "Image";
  private static final String FORMAT = "fmt";
  private static final String DOT_PNG = ".png";
  private static final String WEBP_ALPHA = "webp-alpha";
  private static final String DOT_WEBP = ".webp";
  private static final String MASTER_VIDEO = "MasterVideo";
  private static final String ANIMATED_GIF = "AnimatedGif";

  public String getDmAssetPath() {
    return Optional.ofNullable(request.getResourceResolver())
      .filter(data -> isDamPath(assetPath))
      .map(this::getDynamicMediaImageUrl)
      .filter(path -> !StringUtils.startsWith(path, CONTENT_DAM))
      .map(s7Uri -> appendQueryParams(s7Uri, assetPath))
      .orElse(assetPath);
  }

  public String getDynamicMediaImageUrl(ResourceResolver resolver) {
    try {
      Resource assetResource = resolver.getResource(assetPath);
      if (Objects.nonNull(assetResource) && Objects.nonNull(publishUtils)) {
        String assetType = resolveToAsset(assetResource).getMetadataValue(SCENE7_TYPE);
        String[] dmArray = publishUtils.externalizeImageDeliveryAsset(assetResource);
        if (ArrayUtils.isNotEmpty(dmArray) && dmArray.length >= 2 && Arrays.stream(dmArray)
          .noneMatch(element -> element.equals(assetPath))) {
          return joinPath(assetType, dmArray);
        }
      }
    } catch (RepositoryException rEx) {
      LOGGER.error("Error getting dynamic media URL for asset {}", rEx.getMessage());
    }
    return assetPath;
  }

  public boolean isDamPath(String path) {
    return Optional.ofNullable(path)
      .filter(str -> StringUtils.isNoneBlank(str) && StringUtils.startsWith(str, CONTENT_DAM))
      .map(StringUtils::isNoneBlank).orElse(Boolean.FALSE);
  }

  private String appendQueryParams(String s7Uri, String damPath) {
    if (StringUtils.isNoneBlank(s7Uri, damPath)) {
      try {
        URIBuilder uriBuilder = new URIBuilder(s7Uri);
        if (StringUtils.endsWithIgnoreCase(damPath, DOT_PNG) || StringUtils.endsWithIgnoreCase(damPath, DOT_WEBP)){
          uriBuilder.addParameter(FORMAT, WEBP_ALPHA);
        }
        return uriBuilder.build().toString();
      } catch (URISyntaxException e) {
        LOGGER.error("Unable to append params to S7 URL: {}. returning as is", s7Uri);
        return s7Uri;
      }
    }
    return s7Uri;
  }

  private String joinPath(String assetType, String[] array) {
    String imageOrVideo = StringUtils.EMPTY;
    if (StringUtils.equals(assetType, IMAGE)) {
      imageOrVideo = IS_IMAGE;
    } else if (StringUtils.equals(assetType, VIDEO) || StringUtils.equals(assetType, MASTER_VIDEO) || StringUtils.equals(assetType, ANIMATED_GIF)) {
      imageOrVideo = IS_CONTENT;
    }
    return Arrays.stream(ArrayUtils.insert(1, array, imageOrVideo))
      .reduce("", (acc, next) -> StringUtils
        .stripStart(StringUtils.stripEnd(acc, SLASH) + SLASH + StringUtils.stripStart(next, SLASH), SLASH));
  }

}
