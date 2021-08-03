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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

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
public class SinglePageController extends BasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(SinglePageController.class);
	
	private static final String GOTO_NID = "GOTO_NID: ";
	private static final String COMMAND_EDIT = "command.edit";

	private Link editLink;
	private final StackedPanel mainPanel;
	private Controller htmlEditorController;
	private final IFrameDisplayController idc;
	private final VelocityContainer myContent;
	private CustomLinkTreeModel customLinkTreeModel;
	private CustomLinkTreeModel toolLinkTreeModel;

	private final DeliveryOptions deliveryOptions;
	
	private String g_curURI;
	private VFSContainer g_new_rootContainer;
	private Long courseRepoKey;
	
	public SinglePageController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName,
			boolean allowRelativeLinks) {
		//default behavior is to show the home link in a single page
		this(ureq, wControl, rootContainer, fileName, allowRelativeLinks, null, null, null, false, null);
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
	  */
	public SinglePageController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName,
			boolean allowRelativeLinks, String frameId, OLATResourceable contextResourcable, DeliveryOptions config,
			boolean randomizeMapper) {
		this(ureq, wControl, rootContainer, fileName, allowRelativeLinks, frameId, contextResourcable, config, randomizeMapper, null);
	}
	
	public SinglePageController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName,
			boolean allowRelativeLinks, String frameId, OLATResourceable contextResourcable, DeliveryOptions config,
			boolean randomizeMapper, Long courseRepoKey) {
		super(ureq, wControl);
		
		SimpleStackedPanel mainP = new SimpleStackedPanel("iframemain");
		myContent = createVelocityContainer("index");
		
		this.deliveryOptions = config;
		this.courseRepoKey = courseRepoKey;
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
			  startURI = path;
			}
		}
		
		// adjust root folder if security does not allow using ../.. etc.
		// *** IF YOU CHANGE THIS LOGIC, do also change it in SPCourseNodeIndexer! ***
		if (!allowRelativeLinks && !jumpIn) {
			// start uri is filename without relative path.
			// the relative path of the file is added to the vfs rootcontainer
			int sla = startURI.lastIndexOf('/');
			if (sla != -1) {
				String root = startURI.substring(0,sla);
				startURI = startURI.substring(sla+1);
				VFSContainer newroot = (VFSContainer)rootContainer.resolve(root);
				g_new_rootContainer = newroot;
			} else {
				g_new_rootContainer = rootContainer;				
			}
		} else {
			g_new_rootContainer = rootContainer;
		}
		setCurURI(startURI);
		
		// startURI and g_new_rootContainer set
		// g_curURI   : the current uri (relative to the (evt. corrected) rootcontainer)
		// g_new_rootContainer : the given rootcontainer or adjusted in case when relativelinks are not allowed		
		
		// Display in iframe when
		idc = new IFrameDisplayController(ureq, getWindowControl(), g_new_rootContainer,
				frameId, contextResourcable, deliveryOptions, false, randomizeMapper);
		listenTo(idc);
			
		idc.setCurrentURI(startURI);
		myContent.put("content", idc.getInitialComponent());
		
		mainP.setContent(myContent);
		mainPanel = putInitialPanel(mainP);
	}

	/**
	 * When you call this method the edit mode will be enabled. By default no edit
	 * is possible, you have to call this method after construction time explicitly
	 */
	public void allowPageEditing() {
		editLink = LinkFactory.createButtonXSmall(COMMAND_EDIT, myContent, this);
		editLink.setElementCssClass("o_edit");
		editLink.setIconLeftCSS("o_icon o_icon_edit_file o_icon-lg");
	}
	
	public void setAllowDownload(boolean allow) {
		if (idc != null) {
			// can be null because the boolean "inIframe" in the constructor does not
			// always use the iframe. When in braille mode the system renders the page inline in any case.
			idc.setAllowDownload(allow);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == idc) {
			if (event instanceof OlatCmdEvent) {
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
			idc.setCurrentURI(g_curURI);
			mainPanel.setContent(myContent);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink && event.getCommand().equals(COMMAND_EDIT)) {
			removeAsListenerAndDispose(htmlEditorController);
			if (g_curURI == null || g_new_rootContainer == null || g_new_rootContainer.resolve(g_curURI) == null) {
					showError("error.pagenotfound");
			} else {
				VFSEdusharingProvider edusharingProvider = courseRepoKey != null? new LazyRepositoryEdusharingProvider(courseRepoKey): null;
				if (customLinkTreeModel == null) {
					htmlEditorController = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), g_new_rootContainer, g_curURI, true, true, edusharingProvider);
				} else {
					htmlEditorController = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), g_new_rootContainer,
							g_curURI, true, customLinkTreeModel, toolLinkTreeModel, edusharingProvider);
				}
				listenTo(htmlEditorController);
				mainPanel.setContent(htmlEditorController.getInitialComponent());
			}
		}
	}
	
	private void setCurURI(String uri) {
		this.g_curURI = uri;
	}
	
	@Override
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
	
	public void setToolLinkTreeModel(CustomLinkTreeModel toolLinkTreeModel) {
		this.toolLinkTreeModel = toolLinkTreeModel;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || idc == null) return;
		// delegate to iframe controller
		idc.activate(ureq, entries, state);
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
		if ((scaleFactor > 0 && scaleFactor != 1) || hideOverflow || displayHeight > 0) {
			StringBuilder cssRule = new StringBuilder(128);
			StringBuilder ieCssRule = new StringBuilder(32);
			// add rule for scaling
			if (scaleFactor > 0 && scaleFactor != 1) {
				String formattedScaleFactor = Formatter.roundToString(scaleFactor, 2);
				cssRule.append("transform: scale(").append(formattedScaleFactor).append("); transform-origin: top;")
				       .append("--webkit-transform: scale(").append(formattedScaleFactor).append("); --webkit-transform-origin: top;")
				       .append("--moz-transform: scale(").append(formattedScaleFactor).append("); --moz-transform-origin: top;");
				
				ieCssRule.append("zoom: ").append(formattedScaleFactor).append(";");
			}
			// add rule to set fix height
			if (displayHeight > 0) {
				if (idc != null)  {
					idc.setHeightPX(displayHeight);
				} else {
					// add to rule for html component, so such feature available
					cssRule.append("height: ").append(displayHeight).append("px;");
				}
			}
			// add overflow rule
			if (hideOverflow) {
				cssRule.append("overflow: hidden;");
			}
			
			StringBuilder header = new StringBuilder(256);
			header.append("<style>body {").append(cssRule).append("}</style>");
			header.append("<!--[if lt IE 10]><style>body {").append(ieCssRule).append("}</style><![endif]-->");
			idc.setCustomHeaderContent(header.toString());								
		} else {
			idc.setCustomHeaderContent(null);
		}
	}
}