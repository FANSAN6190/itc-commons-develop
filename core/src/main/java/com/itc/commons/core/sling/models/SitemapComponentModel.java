package com.itc.commons.core.sling.models;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.itc.commons.core.constants.ApplicationConstants.CHILDREN;
import static com.itc.commons.core.constants.ApplicationConstants.LINKTEXT;
import static com.itc.commons.core.constants.ApplicationConstants.LINKURL;
import static com.itc.commons.core.constants.ApplicationConstants.STATIC;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.itc.commons.core.beans.ListItems;
import com.itc.commons.core.beans.PageItems;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model representing a component for generating a sitemap, providing a list of child pages
 * or static links based on configuration.
 *
 */
@Model(adaptables = {Resource.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SitemapComponentModel {

  @ValueMapValue
  private String parentPage;

  @ValueMapValue
  private Long childDepth;

  @ValueMapValue
  private String listFrom;

  @SlingObject
  private ResourceResolver resourceResolver;

  @ChildResource
  private Resource staticItem;

  private final List<PageItems> childPages = new ArrayList<>();



  private List<ListItems> staticListItem;

  /**
   * Post-construction method to initialize the model based on the configured parameters.
   */

  @PostConstruct
  protected void init() {
    if (STATIC.equals(listFrom)) {
      pagesForStatic();
    } else if (Objects.nonNull(parentPage)) {
      Resource parentPageResource = resourceResolver.getResource(parentPage);
      if (Objects.nonNull(parentPageResource) && CHILDREN.equals(listFrom)) {
        buildChildPages(parentPageResource.getChild(JCR_CONTENT), childDepth, childPages);
      }
    }
  }
  /**
   * Recursively builds a list of child pages up to the specified depth.
   *
   * @param resource      The starting resource.
   * @param currentDepth  The current depth in the hierarchy.
   * @param itemsList     The list to populate with child page items.
   */

  private void buildChildPages(Resource resource, Long currentDepth, List<PageItems> itemsList) {
    if (resource != null && currentDepth > 0) {
      PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
      if (Objects.nonNull(pageManager)) {
        Page currentPage = pageManager.getContainingPage(resource);
        if (Objects.nonNull(currentPage)) {
          for (Iterator<Page> childPageIterator = currentPage.listChildren(); childPageIterator.hasNext(); ) {
            Page childPage = childPageIterator.next();
            List<PageItems> childNodes = new ArrayList<>();
            buildChildPages(childPage.getContentResource(), currentDepth - 1, childNodes);
            if(!childPage.isHideInNav()) {
              PageItems pageItems = new PageItems();
              pageItems.setTitle(childPage.getTitle());
              pageItems.setLink(childPage.getPath());
              pageItems.setChildren(childNodes);
              itemsList.add(pageItems);
            }
          }
        }
      }
    }
  }
  /**
   * Retrieves static pages and populates the list of static links.
   */
  private void pagesForStatic() {
    if(Objects.nonNull(staticItem)){
      staticListItem = new ArrayList<>();
      staticItem.listChildren().forEachRemaining(item -> {
        ValueMap valueMap = item.getValueMap();
        String linkText = valueMap.get(LINKTEXT, StringUtils.EMPTY);
        String linkUrl = valueMap.get(LINKURL, StringUtils.EMPTY);
        ListItems listItems = new ListItems();
        listItems.setText(linkText);
        listItems.setIconLink(linkUrl);
        staticListItem.add(listItems);
      });
    }

  }
  /**
   * Gets the list of child pages.
   *
   * @return List of child pages.
   */
  public List<PageItems> getChildPages() {
    return new ArrayList<>(childPages);
  }
  /**
   * Gets the parent page path.
   *
   * @return The parent page path.
   */
  public String getParentPage() {
    return parentPage;
  }
  /**
   * Gets the depth of child pages to include.
   *
   * @return The depth of child pages.
   */
  public Long getChildDepth() {
    return childDepth;
  }
  /**
   * Gets the list of static links.
   *
   * @return List of static links.
   */
  public List<ListItems> getStaticListItem() {
    if(Objects.isNull(staticItem)){
     return new ArrayList<>();
    } else {
      return new ArrayList<>(staticListItem);
    }
  }
}



