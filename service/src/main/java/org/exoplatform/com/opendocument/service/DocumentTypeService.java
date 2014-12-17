package org.exoplatform.com.opendocument.service;

import org.exoplatform.com.opendocument.service.enties.DocumentType;

/**
 * Created by The eXo Platform SEA
 * Author : eXoPlatform
 * toannh@exoplatform.com
 * On 12/12/14
 * Provider all methods of document-type in open document with desktop applications.
 */
public interface DocumentTypeService {
  /**
   * Get documentType of extension string
   * @param mimeType
   * @return DocumentType
   */
  public DocumentType getDocumentType(String mimeType);
}
