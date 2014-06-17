/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.modules.singlepage;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.ExternalSiteEvent;
import org.olat.core.gui.components.htmlsite.HtmlStaticPageComponent;
import org.olat.core.gui.components.htmlsite.NewInlineUriEvent;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSManager;

/**
 * Description:<BR>
 * Wrapper controller that shows local html pages from the given folder / filename
 * <P/>
 * Initial Date:  Dec 16, 2004
 *
 * EVENTS: to listening controllers:
 * - OlatCmdEvent (which has to be accepted by calling accept() on the event) 
 * - NewInlineUriEvent if the user changed the page by clicking on a link
 *
 * @author gnaegi 
 */
public class SinglePageController extends BasicController implements CloneableController {

	private OLog log = Tracing.createLoggerFor(this.getClass());
	
	private static final String GOTO_NID = "GOTO_NID: ";
	
	private static final String COMMAND_EDIT = "command.edit";
	
	private HtmlStaticPageComponent cpc;
	private VelocityContainer myContent;
	
	
	// mapper for the external site
	private String amapPath;
	private DeliveryOptions deliveryOptions;
	private IFrameDisplayController idc;
	
	private String g_curURI;
	
	// save constructor args to remember if we open a site in a new window
	private String g_fileName;
	private boolean g_inIframe;
	private boolean g_allowRelativeLinks;
	private VFSContainer g_rootContainer;
	
	private VFSContainer g_new_rootContainer;
	
	private Controller htmlEditorController;
	private Link editLink;
	private CustomLinkTreeModel customLinkTreeModel;
	private CloseableModalController cmc;

	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param inIframe
	 * @param rootContainer
	 * @param fileName
	 * @param currentUri
	 * @param allowRelativeLinks
	 * @param showHomeLink
	 */
	public SinglePageController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName, String currentUri,
			boolean allowRelativeLinks) {
		//default behavior is to show the home link in a single page
		this(ureq, wControl, rootContainer, fileName, currentUri, allowRelativeLinks, null, null);
	}

	 /**
	  * Constructor for the generic single page controller.
	  * 
	  * displays the html page (or any file if in iframe mode) and, if not on the first page and not in iframe mode, 
	  * offers a button to return to the start page. 
	  * (useful for a "home" button)
 	  * <p>
	  * You can call the allowPageEditing after this construtor to allow users to edit the page
	  * 
	  * @param folderPath The course folder which contains the single page html file
	  * @param inIframe if true, the contents are rendered within an iframe
	  * @param fileName the relative filePath in the material folder starting with a slash, e.g. /welcome.html or /docu/info.html
	  * @param rootContainer the root from which to resolve the files (like "the htdocs directory")
	  * @param currentUri if not null, the start page is set to this uri (instead of the fileName arg). relative to the -corrected- rootcontainer if !allowRelativeLinks
	  * 
	  * @param allowRelativeLinks if true, an initial uri of /folder/a.html allows navigating till "/", if false, only 
	  * navigating in /folder/ and subfolders of this folder is allowed
	  * @param showHomeLink true enables the home link icon and link which is added to the SP, false removes icon and link.
	  * 
	  * 
	  */
	public SinglePageController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName, String currentUri,
			boolean allowRelativeLinks, OLATResourceable contextResourcable, DeliveryOptions config) {
		super(ureq, wControl);
		
		Panel mainP = new Panel("iframemain");
		myContent = createVelocityContainer("index");
		
		// remember values in case of later cloning
		// g_fileName : initial file name given (no root correction), e.g. bla.html or f/g/blu.html
		// always use non-iframe mode for screenreaders
		this.deliveryOptions = config;
		this.g_inIframe = !getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader();
		this.g_allowRelativeLinks = allowRelativeLinks;
		this.g_fileName = fileName;
		this.g_rootContainer = rootContainer;
		boolean jumpIn = false;
		
		// strip beginning slash
		String startURI = ( (fileName.charAt(0) == '/')? fileName.substring(1) : fileName);

		// jump (e.g. from search) to the path if the business-launch-path says so.
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me
			log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable ores = ce.getOLATResourceable();
			log.debug("OLATResourceable=" + ores);
			String typeName = ores.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if  (path.length() > 0) {
			  log.debug("direct navigation to container-path=" + path);
			  jumpIn = true;
			  currentUri = path;
			  startURI = path;
			}
		}
		
		// adjust root folder if security does not allow using ../.. etc.
		if (!allowRelativeLinks && !jumpIn) {
			// start uri is filename without relative path.
			// the relative path of the file is added to the vfs rootcontainer
			int sla = startURI.lastIndexOf('/');
			if (sla != -1) {
				String root = startURI.substring(0,sla);
				startURI = startURI.substring(sla+1);
				VFSContainer newroot = (VFSContainer)rootContainer.resolve(root);
				this.g_new_rootContainer = newroot;
			} else {
				this.g_new_rootContainer = rootContainer;				
			}
		} else {
			this.g_new_rootContainer = rootContainer;
		}
		setCurURI(startURI);
		
		// startURI and g_new_rootContainer set
		// g_curURI   : the current uri (relative to the (evt. corrected) rootcontainer)
		// g_new_rootContainer : the given rootcontainer or adjusted in case when relativelinks are not allowed		
		
		// Display in iframe when
		// a) configured as to be displayed in iframe and not in braille mode
		// b) page is a direct jump in (unclear why not in this case, code was like that)
		// c) when page type can not be inline rendered (e.g. when page is a pdf file)
		if (g_inIframe || jumpIn || !HtmlStaticPageComponent.isFileTypeSupported(startURI)) {
			idc = new IFrameDisplayController(ureq, getWindowControl(), g_new_rootContainer, contextResourcable, deliveryOptions);
			listenTo(idc);
			
			idc.setCurrentURI(startURI);
			myContent.put("content", idc.getInitialComponent());
		} else {	
			// in inline mode
			// create single page root file now and start component for display dispathing
			cpc = new HtmlStaticPageComponent("content", g_new_rootContainer);
			cpc.addListener(this);
			myContent.put("content", cpc);

			if (currentUri != null) {
				if (currentUri.charAt(0) == '/') {
					//strip beginning slash
					currentUri = currentUri.substring(1);
				}
				setCurURI(currentUri);
				cpc.setCurrentURI(currentUri);
			} else {
				// no bookmarked uri given
				setCurURI(startURI);
				cpc.setCurrentURI(startURI);
			}			
		}	
					
		mainP.setContent(myContent);
		//setInitialComponent(mainP);
		putInitialPanel(mainP);
	}

	/**
	 * When you call this method the edit mode will be enabled. By default no edit
	 * is possible, you have to call this method after construction time explicitly
	 */
	public void allowPageEditing() {
		editLink = LinkFactory.createCustomLink(COMMAND_EDIT, COMMAND_EDIT, "", Link.NONTRANSLATED, myContent, this);
		editLink.setCustomEnabledLinkCSS("b_content_edit");
		editLink.setTooltip(translate(COMMAND_EDIT));
	}
	
	public void setAllowDownload(boolean allow) {
		if (idc != null) {
			// can be null because the boolean "inIframe" in the constructor does not
			// always use the iframe. When in braille mode the system renders the page inline in any case.
			idc.setAllowDownload(allow);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == idc) {
			if (event instanceof OlatCmdEvent) {
				//TODO:gs legacy code???
				//FIXME:fj:b move to other place (whole class) since single page controller could be used generically
		    OlatCmdEvent oce = (OlatCmdEvent) event;
		    String nodeId = oce.getSubcommand();
		    ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_BROWSE_GOTO_NODE, getClass(),
		    		CoreLoggingResourceable.wrapSpUri(GOTO_NID+nodeId));
				// refire to listening controllers
				fireEvent(ureq, event);
			} else if (event instanceof NewIframeUriEvent) {
				NewIframeUriEvent iframeEvent = (NewIframeUriEvent) event;
				String newUri = iframeEvent.getNewUri();
				setCurURI(newUri);
				
				// log this uri change
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.NODE_SINGLEPAGE_GET_FILE, getClass(),
						CoreLoggingResourceable.wrapSpUri(newUri));

			}
		} else if (source == htmlEditorController) {
			
			cmc.deactivate();
			
			if (g_inIframe) {
				idc.setCurrentURI(g_curURI);
			} else {	
				cpc.setCurrentURI(g_curURI);
			}	
			
		} else if (source == cpc) {
			if (event instanceof OlatCmdEvent) {
		    OlatCmdEvent oce = (OlatCmdEvent) event;
		    String nodeId = oce.getSubcommand();
		    ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_BROWSE_GOTO_NODE, getClass(),
		    		CoreLoggingResourceable.wrapSpUri(GOTO_NID+nodeId));
				// refire to listening controllers
				fireEvent(ureq, event);
			}
			else if (event instanceof NewInlineUriEvent) {
				// adapt path if needed and refire to listening controllers
				String opath = ((NewInlineUriEvent)event).getNewUri();
				setCurURI(opath);
				fireEvent(ureq, event);
				
		    NewInlineUriEvent iue = (NewInlineUriEvent) event;
		    String newUri = iue.getNewUri();
		    ThreadLocalUserActivityLogger.log(CourseLoggingAction.NODE_SINGLEPAGE_GET_FILE, getClass(),
		    		CoreLoggingResourceable.wrapSpUri(newUri));
			}
			else if (event instanceof ExternalSiteEvent) {
				ExternalSiteEvent ese = (ExternalSiteEvent)event;
				String startUri = ese.getStartUri();
				final VFSContainer finalRootContainer = g_new_rootContainer;
				
				if (amapPath == null) {
					Mapper mapper = new VFSContainerMapper(finalRootContainer);
					// Register mapper as cacheable
					String mapperID = VFSManager.getRealPath(finalRootContainer);
					if (mapperID == null) {
						// Can't cache mapper, no cacheable context available
						amapPath  = registerMapper(ureq, mapper);
					} else {
						// Add classname to the file path to remove conflicts with other
						// usages of the same file path
						mapperID = this.getClass().getSimpleName() + ":" + mapperID;
						amapPath  = registerCacheableMapper(ureq, mapperID, mapper);				
					}
				}
				ese.setResultingMediaResource(new RedirectMediaResource(amapPath+"/"+startUri));
				ese.accept();
			}
			
		} 
	}
	
	/** 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink) {
			if (event.getCommand().equals(COMMAND_EDIT)) {
				if (g_curURI == null || g_new_rootContainer == null || g_new_rootContainer.resolve(g_curURI) == null) {
					showError("error.pagenotfound");
					return;
				}
				
				removeAsListenerAndDispose(htmlEditorController);
				if (customLinkTreeModel == null) {
					htmlEditorController = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), g_new_rootContainer, g_curURI, true, true);
				} else {
					htmlEditorController = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), g_new_rootContainer,
							g_curURI, true, customLinkTreeModel);
				}
				listenTo(htmlEditorController);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), htmlEditorController.getInitialComponent());
				listenTo(cmc);
				
				cmc.activate();
			}
		}
	}
	
	private void setCurURI(String uri) {
		this.g_curURI = uri;
	}
	
	
	/** 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// NOTE: do not deregister this mapper here: the url pointing to this mapper is opened in a new browser window
		// and the user will expect to be able to browse beyond the lifetime of this originating controller here.
		//if (mapper != null) {mr.deregister(mapper);}
	}

	/**
	 * Set the internal link tree model that should be used in the HTML editor to
	 * display links
	 * 
	 * @param customLinkTreeModel
	 */
	public void setInternalLinkTreeModel(CustomLinkTreeModel customLinkTreeModel) {
		this.customLinkTreeModel = customLinkTreeModel;
	}

	/**
	 * @see org.olat.core.gui.control.generic.clone.CloneableController#cloneController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller cloneController(UserRequest ureq, WindowControl control) {
		return new SinglePageController(ureq, control, g_rootContainer, g_fileName, g_curURI, g_allowRelativeLinks, null, deliveryOptions);
	}

	/**
	 * Set a scale factor to enlarge / shrink the entire page. This is handy when
	 * a preview of a page should be displayed.
	 * E.g: 2 will scale the page by factor 2, 0.5 will shrink the page by factor 2
	 * 
	 * @param scaleFactor > 0 or 1: don't scale; < 1: shrink page; > 1 enlarge page. 
	 * @param displayHeight > 0: size to fit; > 0: fixed size in pixel
	 * @param hideOverflow true: don't show scroll-bars; false: default behavior with scroll-bars
	 */
	public void setScaleFactorAndHeight(float scaleFactor, int displayHeight, boolean hideOverflow) {
		String cssRule = "";
		// add rule for scaling
		if (scaleFactor > 0 && scaleFactor != 1) {
			String formattedScaleFactor = Formatter.roundToString(scaleFactor, 2);
			cssRule = "zoom: " + formattedScaleFactor + "; -moz-transform: scale(" + formattedScaleFactor + ");";								
		}
		// add rule to set fix height
		if (displayHeight > 0) {
			if (idc != null)  {
				idc.setHeightPX(displayHeight);
			} else {
				// add to rule for html component, so such feature available
				cssRule += "height: " + displayHeight + "px;";
			}
		}
		// add overflow rule
		if (hideOverflow) {
			cssRule += "overflow: hidden;";
		}
		// cleanup
		if (cssRule.length() == 0) {
			cssRule = null;
		}

		// apply css rule to iframe controller or html component
		if (idc != null) {
			if (cssRule == null) {
				idc.setCustomHeaderContent(null);
			} else {
				idc.setCustomHeaderContent("<style type='text/css'>body {" + cssRule + "}</style>");								
			}
		} else {
			cpc.setWrapperCssStyle(cssRule);
		}
		
	}
}
