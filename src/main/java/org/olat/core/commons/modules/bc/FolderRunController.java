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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.modules.bc.commands.CmdCreateFile;
import org.olat.core.commons.modules.bc.commands.CmdCreateFolder;
import org.olat.core.commons.modules.bc.commands.CmdDelete;
import org.olat.core.commons.modules.bc.commands.CmdEditContent;
import org.olat.core.commons.modules.bc.commands.CmdEditQuota;
import org.olat.core.commons.modules.bc.commands.CmdMoveCopy;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandFactory;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.search.ui.SearchController;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory.DisplayOption;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.servlets.WebDAVManager;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * The FolderRunController offers a full-fledged folder component that can be
 * used to navigate and manage a VFS based file/folder structure. There are some
 * options to configure the webDAV link visibility, file filters and a custom
 * link tree model that is used in the HTML editor when editing a file.
 * 
 * @author Felix Jost, Florian Gnägi
 */
public class FolderRunController extends BasicController implements Activateable {

	private OLog log = Tracing.createLoggerFor(this.getClass());
	
	public static final String ACTION_PRE = ".action";
	public static final String FORM_ACTION = "action";

	private VelocityContainer folderContainer;

	private SelectionTree selTree;	
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csController;
	
	private SearchController searchC;
	private FolderComponent folderComponent;
	private Controller folderCommandController;
	private FolderCommand folderCommand;
	private CloseableModalController cmc;
	private Link editQuotaButton;

	/**
	 * Constructor for a folder controller without filter and custom link model for editor
	 * @param rootContainer
	 * @param displayWebDAVLink
	 * @param ureq
	 * @param wControl
	 */
	public FolderRunController(VFSContainer rootContainer, boolean displayWebDAVLink, UserRequest ureq, WindowControl wControl) { 
		this(rootContainer, displayWebDAVLink, false, ureq, wControl, null, null);
	}
	
	/**
	 * Constructor for a folder controller without filter and custom link model for editor.
	 * @param rootContainer
	 * @param displayWebDAVLink
	 * @param ureq
	 * @param wControl
	 */
	public FolderRunController(VFSContainer rootContainer, boolean displayWebDAVLink, boolean displaySearch, UserRequest ureq, WindowControl wControl) { 
		this(rootContainer, displayWebDAVLink, displaySearch, ureq, wControl, null, null);
	}

	/**
	 * Constructor for a folder controller with an optional file filter and an
	 * optional custom link model for editor. Use this one if you don't wan't to
	 * display all files in the file browser or if you want to use a custom link
	 * tree model in the editor.
	 * 
	 * @param rootContainer
	 *            The folder base. User can not navigate out of this container.
	 * @param displayWebDAVLink
	 *            true: show the webDAV link; false: hide the webDAV link
	 * @param displaySearch
	 *            true: display the search field; false: omit the search field.
	 *            Note: for guest users the search is always omitted.
	 * @param ureq
	 *            The user request object
	 * @param wControl
	 *            The window control object
	 * @param filter
	 *            A file filter or NULL to not use a filter
	 * @param customLinkTreeModel
	 *            A custom link tree model used in the HTML editor or NULL to
	 *            not use this feature.
	 */
	public FolderRunController(VFSContainer rootContainer,
			boolean displayWebDAVLink, boolean displaySearch, UserRequest ureq,
			WindowControl wControl, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel) {

		super(ureq, wControl);
		
		folderContainer = this.createVelocityContainer("run");
		editQuotaButton = LinkFactory.createButtonSmall("editQuota", folderContainer, this);
		
		BusinessControl bc = getWindowControl().getBusinessControl();
		// --- subscription ---
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(rootContainer);
		if (secCallback != null) {
			subsContext = secCallback.getSubscriptionContext();
			// if null, then no subscription is desired
			if (subsContext != null && (rootContainer instanceof OlatRelPathImpl)) {
				String businessPath = wControl.getBusinessControl().getAsString();
				String data = ((OlatRelPathImpl)rootContainer).getRelPath();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(FolderModule.class), data, businessPath);
				csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
				folderContainer.put("subscription", csController.getInitialComponent());
			}
		}
		
		if(!ureq.getUserSession().getRoles().isGuestOnly() && displaySearch) {
		  SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		  searchC = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
		  listenTo(searchC); // register for auto-dispose
		  folderContainer.put("searchcomp", searchC.getInitialComponent());
		}
		
		
		folderComponent = new FolderComponent(ureq, "foldercomp", rootContainer, filter, customLinkTreeModel);
		folderComponent.addListener(this);
		folderContainer.put("foldercomp", folderComponent);
		if (WebDAVManager.getInstance().isEnabled() && displayWebDAVLink)
			folderContainer.contextPut("webdavlink", FolderManager.getWebDAVLink());

		selTree = new SelectionTree("seltree", getTranslator());
		selTree.addListener(this);
		folderContainer.put("seltree", selTree);

		// jump to either the forum or the folder if the business-launch-path says so.
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me						
			if (log.isDebug()) log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable ores = ce.getOLATResourceable();			
			if (log.isDebug()) log.debug("OLATResourceable=" + ores);
			String typeName = ores.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if(path.endsWith(":0")) {
				path = path.substring(0, path.length() - 2);
			}
			activate(ureq, path);
		}
		    
		enableDisableQuota(ureq);		
		putInitialPanel(folderContainer);
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == folderCommandController) {			
			if (event == FolderCommand.FOLDERCOMMAND_FINISHED) {
				if ( ! folderCommand.runsModal()) {
					cmc.deactivate();
				}
				folderComponent.updateChildren();
        //TODO: LD check status and show error if STATUS_FAILED - check why this desn't work!!!
				/*if(folderCommandController instanceof FolderCommand) {
					int status = ((FolderCommand)folderCommandController).getStatus();
					if(status == FolderCommandStatus.STATUS_FAILED) {						
						showError("folder.command.failed");
						return;
					}
				}*/
				
				// do logging
				if (source instanceof CmdCreateFile) {
					ThreadLocalUserActivityLogger
							.log(
									FolderLoggingAction.FILE_CREATE,
									getClass(),
									CoreLoggingResourceable
											.wrapBCFile(folderComponent
													.getCurrentContainerPath()
													+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator:"")
													+ ((CmdCreateFile) source).getFileName()));
				} else if (source instanceof CmdCreateFolder) {
					ThreadLocalUserActivityLogger
					.log(
							FolderLoggingAction.FOLDER_CREATE,
							getClass(),
							CoreLoggingResourceable
									.wrapBCFile(folderComponent
											.getCurrentContainerPath()
											+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator:"")
											+ ((CmdCreateFolder) source).getFolderName()));
				} else if (source instanceof CmdEditContent) {
					ThreadLocalUserActivityLogger
					.log(
							FolderLoggingAction.FILE_EDIT,
							getClass(),
							CoreLoggingResourceable
									.wrapBCFile(folderComponent
											.getCurrentContainerPath()
											+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator:"")
											+ ((CmdEditContent) source).getFileName()));
				} else if (source instanceof CmdDelete) {
					Iterator<String> it = ((CmdDelete) source).getFileSelection().getFiles().iterator();
					while(it.hasNext()) {
						String aFileName = it.next();
						ThreadLocalUserActivityLogger
								.log(
										FolderLoggingAction.FILE_DELETE,
										getClass(),
										CoreLoggingResourceable
												.wrapBCFile(folderComponent
														.getCurrentContainerPath()
														+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator: "")
														+ aFileName));
					}
				} else if (source instanceof CmdEditQuota) {
					ThreadLocalUserActivityLogger.log(FolderLoggingAction.EDIT_QUOTA, getClass());
				} else if (source instanceof CmdMoveCopy) {
					ILoggingAction loggingAction = ((CmdMoveCopy)source).isMoved()?FolderLoggingAction.FILE_MOVED:FolderLoggingAction.FILE_COPIED;
					String target = ((CmdMoveCopy)source).getTarget();
					Iterator<String> it = ((CmdMoveCopy) source).getFileSelection().getFiles().iterator();
					while(it.hasNext()) {
						String aFileName = it.next();
						ThreadLocalUserActivityLogger
								.log(
										loggingAction,
										getClass(),
										CoreLoggingResourceable
												.wrapBCFile(folderComponent
														.getCurrentContainerPath()
														+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator: "")
														+ aFileName),
										CoreLoggingResourceable
												.wrapBCFile(target));
					}
				}
				
				removeAsListenerAndDispose(folderCommandController);
				folderCommandController = null;
				removeAsListenerAndDispose(cmc);
				cmc = null;
				fireEvent(ureq, event);	
			} else if (event instanceof FolderEvent) {
				enableDisableQuota(ureq);
				fireEvent(ureq, event);				
			}
		} else if (source == cmc) {
			// close event from modal dialog, cleanup upload controller
			removeAsListenerAndDispose(folderCommandController);		
			folderCommandController = null;
			removeAsListenerAndDispose(cmc);
			cmc = null;
		}
	}

	/**
	 * @seec org.olat removeAsListenerAndDispose(folderCommandController);
	 *      folderCommandController = null; removeAsListenerAndDispose(cmc); cmc =
	 *      null; .UserRequest, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == folderComponent || source == folderContainer || source == editQuotaButton) {
			// we catch events from both folderComponent and folderContainer
			// and process them through the generic folder command implementations
			String cmd = event.getCommand();
			if (cmd.equals(FORM_ACTION)) cmd = getFormAction(ureq);
			
			folderCommand = FolderCommandFactory.getInstance().getCommand(cmd, ureq, getWindowControl());
			if (folderCommand != null) {
				folderCommandController = folderCommand.execute(folderComponent, ureq, getWindowControl(), getTranslator());
				if (folderCommandController != null) {
					// activate command's controller
					this.listenTo(folderCommandController);
					if ( ! folderCommand.runsModal()) {
						cmc = new CloseableModalController(getWindowControl(), translate("close"), folderCommandController.getInitialComponent());
						cmc.activate();						
						listenTo(cmc);
					}
				} else {
					// update view after unzip
					if (cmd.equals(FolderCommandFactory.COMMAND_UNZIP)) {
						if(folderCommand.getStatus()==FolderCommandStatus.STATUS_INVALID_NAME) {
							showError("zip.name.notvalid");
						}
						// update view, but not when serving a resource, then nothing has to
						// be updated here (and specially nothing has to be marked as dirty)
						else if ( ! cmd.equals(FolderCommandFactory.COMMAND_SERV)) {
							folderComponent.updateChildren();
						}
					}//TODO review 
				}
				
				if(FolderCommandStatus.STATUS_FAILED == folderCommand.getStatus()) {
					//failed, reload the children to see if a file has disappeared
					folderComponent.updateChildren();
				}	
			}
			enableDisableQuota(ureq);
		}
	}

	private void enableDisableQuota(UserRequest ureq) {
		if (!ureq.getUserSession().getRoles().isOLATAdmin()) {
			if (!ureq.getUserSession().getRoles().isInstitutionalResourceManager()) {
				folderContainer.contextPut("editQuota", Boolean.FALSE);
				return;
			}
		}

		Quota q = VFSManager.isTopLevelQuotaContainer(folderComponent.getCurrentContainer());
		folderContainer.contextPut("editQuota", (q == null)? Boolean.FALSE : Boolean.TRUE);
	}
	
	/**
	 * Special treatment of forms with multiple submit actions.
	 * @param ureq
	 * @return The action triggered by the user.
	 */
	private String getFormAction(UserRequest ureq) {
		Enumeration params = ureq.getHttpReq().getParameterNames();
		while (params.hasMoreElements()) {
			String key = (String) params.nextElement();
			if (key.startsWith(ACTION_PRE)) {
				return key.substring(ACTION_PRE.length());
			}
		}
		return null;
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {		
    //folderCommandController is registerd with listenTo and gets disposed in BasicController
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String path) {
		if (path != null && path.length() > 0) {
			// Check if there is something after path= e.g. '/test1/test2/readme.txt'
			if (path.lastIndexOf("/") > 0) {
				// ok there is file e.g. /readme.txt => navigate only to folder =>
				// remove file name
				String dirPath = path.substring(0, path.lastIndexOf("/"));
				if (!path.equals("")) {
					if (log.isDebug()) log.debug("direct navigation to container-path=" + dirPath);
					folderComponent.setCurrentContainerPath(dirPath);
				}
			}
			VFSItem vfsItem = folderComponent.getRootContainer().resolve(path.endsWith("/") ? path.substring(0, path.length()-1) : path);
			if (vfsItem != null && !(vfsItem instanceof VFSContainer)) {
				// could be a file - create the mapper - otherwise don't create one if it's a directory
				
				// Create a mapper to deliver the auto-download of the file. We have to
				// create a dedicated mapper here
				// and can not reuse the standard briefcase way of file delivering, some
				// very old fancy code
				// Mapper is cleaned up automatically by basic controller
				String baseUrl = registerMapper(new Mapper() {
					public MediaResource handle(String relPath, HttpServletRequest request) {
						VFSLeaf vfsfile = (VFSLeaf) folderComponent.getRootContainer().resolve(relPath);
						if (vfsfile == null) {
							return new NotFoundMediaResource(relPath);
						} else {
							return new VFSMediaResource(vfsfile);
						}
					}
				});
				// Trigger auto-download
				DisplayOrDownloadComponent dordc = new DisplayOrDownloadComponent("downloadcomp",baseUrl + path);
				folderContainer.put("autoDownloadComp", dordc);
			}
		}
	}
	

}
