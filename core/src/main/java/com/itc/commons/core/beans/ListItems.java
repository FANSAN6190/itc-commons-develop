package com.itc.commons.core.beans;

import java.util.Objects;

/**
 * Represents an item with a text description and an associated icon. This class is commonly used to store and manage
 * data elements that contain both textual and iconographic information.
 */
public class ListItems {

  private String text;
  private String iconLink;
  private String iconName;

  /**
   * Sets the text description for this item.
   *
   * @param text The text description to be set.
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Sets the icon link or identifier for this item.
   *
   * @param iconLink The icon link or identifier to be set.
   */
  public void setIconLink(String iconLink) {
    this.iconLink = iconLink;
  }
  /**
   * Sets the icon name for this item.
   *
   * @param iconName The icon name to be set.
   */
  public void setIconName(String iconName){
    this.iconName = iconName;
  }

  /**
   * Retrieves the text description of this item.
   *
   * @return The text description.
   */
  public String getText() {
    return text;
  }

  /**
   * Retrieves the link or identifier of the icon associated with this item.
   *
   * @return The icon link or identifier.
   */
  public String getIconLink() {
    return iconLink;
  }

  /**
   * Retrieves the name or identifier of the icon associated with this item.
   *
   * @return The icon name or identifier.
   */
  public String getIconName() {
    return iconName;
  }

  /**
   * @return boolean value based on equality of two objects.
   */
  @Override
  public boolean equals(Object o) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    ListItems listItems = (ListItems) o;
    if ( !Objects.equals(text, listItems.text) ) {
      return false;
    }
    return Objects.equals(iconLink, listItems.iconLink);
  }

  /**
   * @return the hash code
   */
  @Override
  public int hashCode() {
    int result = text != null ? text.hashCode() : 0;
    result = 31 * result + (iconLink != null ? iconLink.hashCode() : 0);
    return result;
  }
}
