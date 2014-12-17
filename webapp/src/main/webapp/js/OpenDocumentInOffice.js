(function(gj, ecmWebdav) {
  var OpenDocumentInOffice = function() {}

  OpenDocumentInOffice.prototype.openDocument = function(absolutePath, workspace, filePath){
    var documentManager = eXo.ecm.ECMWebDav.WebDAV.Client.DocManager;
    var openStatus = false;
    if (documentManager.IsMicrosoftOfficeAvailable() && documentManager.IsMicrosoftOfficeDocument(absolutePath)) {
      if (!('ActiveXObject' in window)) absolutePath += '\0';
      openStatus = documentManager.MicrosoftOfficeEditDocument(absolutePath);
    } else {
      openStatus = documentManager.JavaEditDocument(absolutePath, null, "/ecmexplorer/applet/ITHitMountOpenDocument.jar");
    }

    //create version when successfully open
    if(openStatus){
      eXo.ecm.OpenDocumentInOffice.checkout(workspace, filePath);
    }
  }

  /*
   * Checkout a versioned document when open successfully with desktop application to edit.
   */
  OpenDocumentInOffice.prototype.checkout = function(workspace, filePath){
    gj.ajax({
      url: "/portal/rest/office/checkout?filePath=" + filePath+"&workspace="+workspace,
      dataType: "text",
      type: "GET",
      async: true
    })
        .success(function (data) {
          console.log("checkout status "+!data);
        });
  };

  /*Lock item */
  OpenDocumentInOffice.prototype.lockItem = function(filePath){

  } //end lock function

  /**
   * Update OpenDocument button's label
   * objId is workspace_name ':' node_path of file
   * activityId is id of activity in home page
   * rightClick update button when right click (context-menu)
   */
  OpenDocumentInOffice.prototype.updateLabel = function(objId, activityId, rightClick){

    eXo.ecm.ECMWebDav.WebDAV.Client.DocManager.ShowMicrosoftOfficeWarning();
    var currentDocumentObj = {};
    gj.ajax({
      url: "/portal/rest/office/updateDocumentTitle?objId=" + objId+"&lang="+eXo.env.portal.language,
      dataType: "text",
      type: "GET",
      async: false
      //	timeout:1000 * 10
    })
        .success(function (data) {
          data = gj.parseJSON(data);
          var elClass = "uiIconEcmsOpenDocument";
          var isRightClick="";
          if(activityId != null && activityId != "undefined" && activityId != "") elClass +="_"+activityId;
          if(rightClick) isRightClick="#ECMContextMenu";
          var openDocument = gj(isRightClick+" ."+elClass).parent();
          var html = "<i class=\"uiIcon16x16FileDefault uiIcon16x16nt_file "+data.ico+" "+elClass+"\"></i>\n"+data.title;
          openDocument.html(html);
          gj(".detailContainer").find('.openDocument').html(data.title);
          currentDocumentObj = '{"title":"'+data.title+'", "ico": "'+data.ico+'"}';
        });

    setCookie("_currentDocument", currentDocumentObj, 1);
  }

  /**
   * Close all popup
   */

  OpenDocumentInOffice.prototype.closePopup = function(){
    console.log("close all popup");
  }

  OpenDocumentInOffice.prototype.openDocument_doClick = function(){
    gj("#uiActionsBarContainer .uiIconEcmsOpenDocument").parent().click();
  }

  /**
   * Set value to browser's cookie
   */
  function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
  }

  /**
   *get cookie by key
   */
  function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0)==' ') c = c.substring(1);
      if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
    }
    return "";
  }

  var bindContextMenu = function(){

    // bind RightClick
    if(gj("#UITreeExplorer li").data("events") === undefined){
      gj("#UITreeExplorer li").bind("contextmenu",function(e){
        var _data = gj(e.target).html();
        eXo.ecm.OpenDocumentInOffice.updateLabel(_data, null, true);
      });
    }
    if(gj("#UITreeExplorer li").find(".nodeName").data("events") === undefined){
      gj("#UITreeExplorer li").find(".nodeName").bind("click",function(e){
        var _defaultTitle = gj(e.target).html();
        eXo.ecm.OpenDocumentInOffice.updateLabel(_defaultTitle);
        updateActionBarLabel();
      });
    }
    /*
     gj("#UIJCRExplorer").bind("DOMSubtreeModified", function() {
     var _btnTitle = gj("#UIActionBar li").find(".uiIconEcmsOpenDocument").html();
     if(_btnTitle!=null && "OpenDocument" != _btnTitle.trim()){
     //console.log("update 2: "+_btnTitle);
     }else{
     //	console.log("update 2");
     }
     });
     */

    // bind Working area
    if(gj("#UIDocumentInfo .uiListGrid  div:first-child").data("events") === undefined){
      gj("#UIDocumentInfo .uiListGrid  div:first-child").bind("contextmenu",function(e){
        var _data = gj(e.target).closest(".rowView").find(".nodeName").html();
        eXo.ecm.OpenDocumentInOffice.updateLabel(_data);
      });
    }
  }

  var bindASActionBar = function(){
    gj("#UIActivitiesLoader .uiContentActivity").each(function(){

      var activityId        = gj(this).attr("id").replace("activityContainer", "");
      var activityActionBar = gj(this).find(".actionBar .pull-left");
      var linkTitle         = gj(this).find(".linkTitle").html().trim();

      //add OpenDocument button
      var openDocumentButton = gj(activityActionBar).find(".uiIconEcmsOpenDocument");
      if(openDocumentButton.length === 0){
        var html  = "<i class=\"uiIconEcmsOpenDocument_"+activityId+"\" </i>\n Open...";
        var documentLink = "#";

        gj.ajax({
          url: "/portal/rest/office/getActivity?activityId=" + activityId+"&lang="+eXo.env.portal.language,
          dataType: "text",
          type: "GET",
          async: false
        })
            .success(function (data) {
              data = gj.parseJSON(data);
              documentLink = data.filePath;
            });

        gj(activityActionBar).prepend("<li><a href=\"javascript:eXo.ecm.OpenDocumentInOffice.openDocument('"+documentLink+"')\">"+html+"</a></li>");

        console.log("update Label: as "+activityId);
        eXo.ecm.OpenDocumentInOffice.updateLabel(linkTitle, activityId);
      }

      //remove last btn
      gj(activityActionBar).find(".uiIconEdit").parents().eq(1).remove();


    });

  }


  var updateActionBarLabel = function(){
    var _btnTitle = gj("#UIActionBar").find(".uiIconEcmsOpenDocument").parent().html();
    var _currentDocumentObj = getCookie("_currentDocument");

    if(_currentDocumentObj!=null){
      var _currentDocument = gj.parseJSON(_currentDocumentObj);
      var html = "<i class=\"uiIcon16x16FileDefault ";
      html+= "uiIcon16x16nt_file ";
      html+= _currentDocument.ico + "\"></i>\n";
      html+= _currentDocument.title
      if("<i class=\"uiIconEcmsOpenDocument uiIconEcmsLightGray\"></i> OpenDocument" === _btnTitle){
        //eXo.ecm.OpenDocumentInOffice.updateLabel(getCookie("_currentDocument"));
        console.log("updated");
      }else{
        console.log(_btnTitle);
      }
    }
  }

  gj(window).load(function(){
    //var _currentDocument = getCookie("_currentDocument");
    //if(_currentDocument!=null && _currentDocument!="undefined" && _currentDocument!="")
    //  eXo.ecm.OpenDocumentInOffice.updateLabel(_currentDocument);
  });

  gj(document).ready(function(){
    bindASActionBar();

    setInterval(function(){
      bindContextMenu();

    }, 1000);
  });

  gj.ajaxSetup({	});

  eXo.ecm.OpenDocumentInOffice = new OpenDocumentInOffice();
  return {
    OpenDocumentInOffice : eXo.ecm.OpenDocumentInOffice
  };

})(gj, ecmWebdav);