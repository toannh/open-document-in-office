package org.exoplatform.com.opendocument.service.rest;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.com.opendocument.service.DocumentTypeService;
import org.exoplatform.com.opendocument.service.enties.DocumentType;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          toannh@exoplatform.com
 * Dec 09, 2014
 * Provider all rest methods of Open Document feature.
 */
@Path("/office/")
public class OpenInOfficeConnector implements ResourceContainer {

  private final String OPEN_DOCUMENT_ON_DESKTOP_ICO = "uiIcon16x16FileDefault";
  private final String CONNECTOR_BUNDLE_LOCATION = "locale.open-document.OpenDocumentInOffice";
  private final String OPEN_DOCUMENT_IN_DESKTOP_RESOURCE_KEY = "OpenDocumentInOffice.label.exo.remote-edit.desktop";
  private final String OPEN_DOCUMENT_IN_DESKTOP_APP_RESOURCE_KEY="OpenDocumentInOffice.label.exo.remote-edit.desktop-app";
  private final String OPEN_DOCUMENT_DEFAULT_TITLE="Open";

  private final int CACHED_TIME = 60*24*30*12;

  public static final String SOC_PROP_PARAMS_NODE        = "soc:params";
  public static final String SOC_WORKSPACE               = "social";
  public static final String SOC_PROP_CONTENT_LINK       = "contenLink";
  public static final String SOC_PROP_REPOSITORY         = "repository";
  public static final String SOC_PROP_WORKSPACE          = "workspace";


  /**
   * Return a JsonObject's current file to update display titles
   * @param request
   * @param objId
   * @return
   * @throws Exception
   */
  @GET
  @Path("/updateDocumentTitle")
  public Response updateDocumentTitle(
          @Context Request request,
          @QueryParam("objId") String objId,
          @QueryParam("lang") String language) throws Exception {

    //find from cached
    String extension = objId.substring(objId.lastIndexOf(".") + 1, objId.length());
    EntityTag etag = new EntityTag(Integer.toString((extension+"_"+language).hashCode()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    if(builder!=null) return builder.build();

    //query form configuration values params
    ResourceBundleService resourceBundleService = WCMCoreUtils.getService(ResourceBundleService.class);
    DocumentTypeService documentTypeService = WCMCoreUtils.getService(DocumentTypeService.class);

    CacheControl cc = new CacheControl();
    cc.setMaxAge(CACHED_TIME);

    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(CONNECTOR_BUNDLE_LOCATION, new Locale(language));
    String title = resourceBundle!=null?resourceBundle.getString(OPEN_DOCUMENT_IN_DESKTOP_RESOURCE_KEY):OPEN_DOCUMENT_DEFAULT_TITLE;
    String ico = OPEN_DOCUMENT_ON_DESKTOP_ICO;

    DocumentType documentType = documentTypeService.getDocumentType(extension);

    if(documentType !=null && resourceBundle !=null ){
      try {
        if(!StringUtils.isEmpty(resourceBundle.getString(documentType.getResourceBundleKey())))
         title = resourceBundle.getString(documentType.getResourceBundleKey());
      }catch(Exception ex){
        title = resourceBundle.getString(OPEN_DOCUMENT_IN_DESKTOP_APP_RESOURCE_KEY)+" "+ documentType.getResourceBundleKey();
      }
      if(!StringUtils.isEmpty(documentType.getIconClass())) ico=documentType.getIconClass();
    }

    //return & stored on cached
    JSONObject rs = new JSONObject();
    rs.put("ico", ico);
    rs.put("title", title);

    builder = Response.ok(rs.toString(), MediaType.APPLICATION_JSON);
    builder.tag(etag);
    builder.cacheControl(cc);
    return builder.build();
  }

  /**
   * Return an Activity by id
   * @param request
   * @param activityId
   * @return an Activity
   * @throws Exception
   */
  @GET
  @Path("/getActivity")
  public Response getActivity(
          @Context Request request,
          @Context HttpServletRequest httpServletRequest,
          @QueryParam("activityId") String activityId) throws Exception {

    //find from cached
    EntityTag etag = new EntityTag(Integer.toString((activityId).hashCode()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    if(builder!=null) return builder.build();

    CacheControl cc = new CacheControl();
    cc.setMaxAge(CACHED_TIME);

    Session session = WCMCoreUtils.getSystemSessionProvider().getSession(SOC_WORKSPACE, WCMCoreUtils.getRepository());
    Node node = session.getNodeByUUID(activityId);
    Node nodeSocParams = node.getNode(SOC_PROP_PARAMS_NODE);

    String nodePath = node.getPath();

    if(nodeSocParams.hasProperty(SOC_PROP_CONTENT_LINK)){
      nodePath = nodeSocParams.getProperty(SOC_PROP_CONTENT_LINK).getString();
    }

    // build full node path
    String filePath = httpServletRequest.getScheme()+ "://" + httpServletRequest.getServerName() + ":"
            +httpServletRequest.getServerPort() + "/"
            + WCMCoreUtils.getRestContextName()+ "/private/jcr/" + nodePath;

    //return & stored on cached
    JSONObject rs = new JSONObject();
    rs.put("filePath", filePath);
    builder = Response.ok(rs.toString(), MediaType.APPLICATION_JSON);
    builder.tag(etag);
    builder.cacheControl(cc);
    return builder.build();
  }

  /**
   * Return a JsonObject's check a version when file has been opened successfully by desktop application
   * @param request
   * @param filePath
   * @return
   * @throws Exception
   */
  @GET
  @Path("/checkout")
  public Response checkout(@Context Request request,
                          @QueryParam("filePath") String filePath,
                          @QueryParam("workspace") String workspace
  ) throws Exception {
    Session session = WCMCoreUtils.getSystemSessionProvider().getSession(workspace, WCMCoreUtils.getRepository());
    Node node = (Node)session.getItem(filePath);
    if(!node.isCheckedOut()) node.checkout();
    return Response.ok(String.valueOf(node.isCheckedOut()), MediaType.TEXT_PLAIN).build();
  }
}
