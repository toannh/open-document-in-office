package org.exoplatform.com.opendocument.service.rest;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.cms.documents.DocumentTypeService;
import org.exoplatform.services.cms.documents.impl.DocumentType;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          toannh@exoplatform.com
 * Dec 09, 2014
 * Provider all rest methods of Open Document feature.
 */
public class OpenInOfficeConnector extends org.exoplatform.wcm.connector.collaboration.OpenInOfficeConnector{
  private final int CACHED_TIME = 60*24*30*12;

  private static final String SOC_PROP_PARAMS_NODE        = "soc:params";
  private static final String SOC_WORKSPACE               = "social";
  private static final String SOC_PROP_CONTENT_LINK       = "contenLink";
  private static final String SOC_PROP_REPOSITORY         = "repository";
  private static final String SOC_PROP_WORKSPACE          = "workspace";

  private static final String VERSION_MIXIN ="mix:versionable";

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

    String ws = session.getWorkspace().getName();
    String nodePath = node.getPath();
    if(nodeSocParams.hasProperty(SOC_PROP_WORKSPACE)){
      ws = nodeSocParams.getProperty(SOC_PROP_WORKSPACE).getString();
    }

    if(nodeSocParams.hasProperty(SOC_PROP_CONTENT_LINK)){
      nodePath = nodeSocParams.getProperty(SOC_PROP_CONTENT_LINK).getString();
    }

    String _ws = nodePath.split("/")[0];
    String _repo = nodePath.split("/")[1];

    String filePath = StringUtils.replace(nodePath, _repo+"/"+_ws, "");//nodePath.replace(_ws+_repo, "");
    String absolutePath = httpServletRequest.getScheme()+ "://" + httpServletRequest.getServerName() + ":"
            +httpServletRequest.getServerPort() + "/"
            + WCMCoreUtils.getRestContextName()+ "/private/jcr/" + nodePath;

    //return & stored on cached
    JSONObject rs = new JSONObject();
    rs.put("absolutePath", absolutePath);
    rs.put("workspace", ws);
    rs.put("filePath", filePath);
    builder = Response.ok(rs.toString(), MediaType.APPLICATION_JSON);
    builder.tag(etag);
    builder.cacheControl(cc);
    return builder.build();
  }
}
