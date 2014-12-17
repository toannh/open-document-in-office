package org.exoplatform.com.opendocument.service.impl;

import org.exoplatform.com.opendocument.service.DocumentTypeService;
import org.exoplatform.com.opendocument.service.enties.DocumentType;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.picocontainer.Startable;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by The eXo Platform SEA
 * Author : eXoPlatform
 * toannh@exoplatform.com
 * On 12/12/14
 * Implementation class get DocumentType with mime-type input
 */
public class DocumentTypeServiceImpl implements DocumentTypeService, Startable{

  private InitParams          params_;

  private static final String OPEN_DESKTOP_PROVIDER_REGEX="^exo.remote-edit\\.([a-z]+)$";
  private static final String OPEN_PROVIDER_RESOURCEBUNDLE_SUFFIX = ".label";
  private static final String OPEN_PROVIDER_STYLE_SUFFIX = ".ico";
  private final String OPEN_DOCUMENT_ON_DESKTOP_ICO = "uiIcon16x16FileDefault";
  private final String OPEN_DOCUMENT_IN_DESKTOP_RESOURCE_KEY = "OpenDocumentInOffice.label.exo.remote-edit.desktop";

  private void init() {
    //load desktop application from system property to init-params
    Properties properties = System.getProperties();
    Enumeration keys = properties.keys();
    ObjectParameter _objectParameter=null;
    while (keys.hasMoreElements()){
      String key = (String)keys.nextElement();
      if(key.matches(OPEN_DESKTOP_PROVIDER_REGEX)) {
        String[] _mimetypes = properties.getProperty(key) != null ? properties.getProperty(key).split(",") : null;
        String _resourceBundle = properties.getProperty(key + OPEN_PROVIDER_RESOURCEBUNDLE_SUFFIX);
        String _ico = properties.getProperty(key + OPEN_PROVIDER_STYLE_SUFFIX);

        _objectParameter = new ObjectParameter();
        _objectParameter.setName(key);
        _objectParameter.setObject(new DocumentType(Arrays.asList(_mimetypes), _resourceBundle, _ico));
        params_.addParam(_objectParameter);
      }
    }
  }

  public DocumentTypeServiceImpl(InitParams initParams) {
    params_ = initParams;
  }

  @Override
  public DocumentType getDocumentType(String mimeType) {
    for(DocumentType documentType: params_.getObjectParamValues(DocumentType.class)){
      if(documentType.getMimeTypes().contains(mimeType)){
        return documentType;
      }
    }
    return new DocumentType(Arrays.asList(new String[]{mimeType}), OPEN_DOCUMENT_IN_DESKTOP_RESOURCE_KEY, OPEN_DOCUMENT_ON_DESKTOP_ICO);
  }

  @Override
  public void start() {
    init();
  }

  @Override
  public void stop() {

  }
}
