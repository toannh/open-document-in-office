package org.exoplatform.com.opendocument.service.enties;

import java.util.List;

/**
 * Created by The eXo Platform SEA
 * Author : eXoPlatform
 * toannh@exoplatform.com
 * On 12/12/14
 * DocumentType of Open document in Office or on Desktop Applications
 */
public class DocumentType {
  private List<String> mimeTypes;
  private String resourceBundleKey;
  private String iconClass;

  public DocumentType(){}

  public DocumentType(List<String> mimeTypes, String resourceBundleKey, String iconClass) {
    this.mimeTypes = mimeTypes;
    this.resourceBundleKey = resourceBundleKey;
    this.iconClass = iconClass;
  }

  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  public String getResourceBundleKey() {
    return resourceBundleKey;
  }

  public String getIconClass() {
    return iconClass;
  }
}
