package com.itc.commons.core.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a page item with a title, link, and a list of child page items.
 * This class is designed to be used for building hierarchical structures of pages.
 */

public class PageItems {
  /**
   * The title of the page item.
   */
  private String title;

  /**
   * The link or URL associated with the page item.
   */
  private String link;

  /**
   * The list of child page items.
   */
  private List<PageItems> children;



  /**
   * Sets the title of the page item.
   *
   * @param title The title of the page item.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the link or URL associated with the page item.
   *
   * @param link The link or URL associated with the page item.
   */
  // Setter for pageUrl
  public void setLink(String link) {
    this.link = link;
  }
  /**
   * Sets the list of child page items.
   *
   * @param children The list of child page items.
   */
  // Setter for children
  public void setChildren(List<PageItems> children) {
    this.children = new ArrayList<>(children);
  }
  /**
   * Gets the link or URL associated with the page item.
   *
   * @return The link or URL associated with the page item.
   */
  public String getLink() {
    return link;
  }

  /**
   * Gets the title of the page item.
   *
   * @return The title of the page item.
   */

  public String getTitle() {
    return title;
  }
  /**
   * Gets the list of child page items.
   *
   * @return The list of child page items.
   */
  public List<PageItems> getChildren() {
    return new ArrayList<>(children);
  }
  /**
   *
   * @param o
   *
   * @return boolean value based on equality of two objects.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageItems pageItems = (PageItems) o;
    if (!Objects.equals(title, pageItems.title)) {
      return false;
    }
    if (!Objects.equals(link, pageItems.link)) {
      return false;
    }
    return Objects.equals(children, pageItems.children);
  }
  /**
   * @return the hash code
   */
  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + (link != null ? link.hashCode() : 0);
    result = 31 * result + (children != null ? children.hashCode() : 0);
    return result;
  }

  /**
   * Returns a string representation of the object.
   *
   * @return A string representation of the object.
   */
  @Override
  public String toString() {
    return "PageItems{" +
      "title='" + title + '\'' +
      ", link='" + link + '\'' +
      ", children=" + children +
      '}';
  }
}
