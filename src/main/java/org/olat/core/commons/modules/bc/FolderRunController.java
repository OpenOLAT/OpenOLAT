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

package org.olat.core.commons.modules.bc;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.modules.bc.commands.CmdCreateFile;
import org.olat.core.commons.modules.bc.commands.CmdCreateFolder;
import org.olat.core.commons.modules.bc.commands.CmdDelete;
import org.olat.core.commons.modules.bc.commands.CmdEditQuota;
import org.olat.core.commons.modules.bc.commands.CmdMoveCopy;
import org.olat.core.commons.modules.bc.commands.CmdOpenContent;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandFactory;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.ScrollTopCommand;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The FolderRunController offers a full-fledged folder component that can be
 * used to navigate and manage a VFS based file/folder structure. There are some
 * options to configure the webDAV link visibility, file filters and a custom
 * link tree model that is used in the HTML editor when editing a file.
 * 
 * @author Felix Jost, Florian Gnägi
 */
public class FolderRunController extends BasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(FolderRunController.class);
	
	public static final String ACTION_PRE = ".action";
	public static final String FORM_ACTION = "action";

	private VelocityContainer folderContainer;

	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csController;
	
	private SearchInputController searchC;
	private FolderComponent folderComponent;
	private Controller folderCommandController;
	private FolderCommand folderCommand;
	private CloseableModalController cmc;
	private Link editQuotaButton;
	
	private final Mail canMail;
	
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private QuotaManager quotaManager;

	/**
	 * Default Constructor, results in showing users personal folder, used by Spring
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public FolderRunController(UserRequest ureq, WindowControl wControl) {
		this(new BriefcaseWebDAVMergeSource(ureq.getIdentity(), ureq.getUserSession().getRoles(), UserManager.getInstance().getUserDisplayName(ureq.getIdentity())),
				true, true, Mail.publicOnly, ureq, wControl, null, null, null);
		//set the resource URL to match the indexer ones
		setResourceURL("[Identity:" + ureq.getIdentity().getKey() + "][userfolder:0]");
	}
 	 	 	 	
	/**
	 * Constructor for a folder controller without filter and custom link model for editor
	 * @param rootContainer
	 * @param displayWebDAVLink
	 *            true: show the webDAV link; false: hide the webDAV link
	 * @param ureq
	 * @param wControl
	 */
	public FolderRunController(VFSContainer rootContainer, boolean displayWebDAVLink, UserRequest ureq, WindowControl wControl) { 
		this(rootContainer, displayWebDAVLink, false, false, ureq, wControl, null, null);
	}
	
	/**
	 * Constructor for a folder controller without filter and custom link model for editor.
	 * @param rootContainer
	 * @param displayWebDAVLink
	 *            true: show the webDAV link; false: hide the webDAV link
	 * @param displaySearch
	 *            true: display the search field; false: omit the search field.
	 *            Note: for guest users the search is always omitted.
	 * @param canMail
	 * 			  true: allow sending document / link to document via email to other users
	 *            false: don't use mail feature
	 * @param ureq
	 * @param wControl
	 */
	public FolderRunController(VFSContainer rootContainer, boolean displayWebDAVLink, boolean displaySearch, boolean canMail, UserRequest ureq, WindowControl wControl) { 
		this(rootContainer, displayWebDAVLink, displaySearch, canMail, ureq, wControl, null, null);
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
	 * @param canMail
	 * 			  true: allow sending document / link to document via email to other users
	 *            false: don't use mail feature
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
			boolean displayWebDAVLink, boolean displaySearch, boolean canMail, UserRequest ureq,
			WindowControl wControl, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel) {
		this(rootContainer, displayWebDAVLink, displaySearch, Mail.valueOf(canMail), ureq, wControl, filter, customLinkTreeModel, null);
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
	 * @param canMail
	 * 			  true: allow sending document / link to document via email to other users
	 *            false: don't use mail feature
	 * @param ureq
	 *            The user request object
	 * @param wControl
	 *            The window control object
	 * @param filter
	 *            A file filter or NULL to not use a filter
	 * @param customLinkTreeModel
	 *            A custom link tree model used in the HTML editor or NULL to
	 *            not use this feature.
	 * @param externContainerForCopy
	 *            A container to copy files from
	 */
	public FolderRunController(VFSContainer rootContainer,
			boolean displayWebDAVLink, boolean displaySearch, Mail canMail, UserRequest ureq,
			WindowControl wControl, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel, VFSContainer externContainerForCopy) {
		this(rootContainer, displayWebDAVLink, displaySearch, canMail, false, ureq, wControl, filter, customLinkTreeModel, externContainerForCopy);
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
	 * @param canMail
	 * 			  true: allow sending document / link to document via email to other users
	 *            false: don't use mail feature
	 * @param isCourseFolder
	 * 			  true: the FolderRunController is used for display a courseFolder and show contextHelp
	 *            false: the FolderRunController is used for other occasions
	 * @param ureq
	 *            The user request object
	 * @param wControl
	 *            The window control object
	 * @param filter
	 *            A file filter or NULL to not use a filter
	 * @param customLinkTreeModel
	 *            A custom link tree model used in the HTML editor or NULL to
	 *            not use this feature.
	 * @param externContainerForCopy
	 *            A container to copy files from
	 */
	public FolderRunController(VFSContainer rootContainer,
			boolean displayWebDAVLink, boolean displaySearch, Mail canMail, boolean isCourseFolder, UserRequest ureq,
			WindowControl wControl, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel, VFSContainer externContainerForCopy) {

		super(ureq, wControl);
		
		this.canMail = canMail;
		folderContainer = createVelocityContainer("run");
		editQuotaButton = LinkFactory.createButtonSmall("editQuota", folderContainer, this);

		folderContainer.contextPut("showCourseFolderHelp", isCourseFolder);
		BusinessControl bc = getWindowControl().getBusinessControl();
		// --- subscription ---
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(rootContainer);
		if (secCallback != null) {
			subsContext = secCallback.getSubscriptionContext();
			// if null, then no subscription is desired
			String data = rootContainer.getRelPath();
			if (subsContext != null && data != null) {
				String businessPath = wControl.getBusinessControl().getAsString();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(FolderModule.class), data, businessPath);
				csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
				folderContainer.put("subscription", csController.getInitialComponent());
			}
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		if(displaySearch && searchModule.isSearchAllowed(roles)) {
		  SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		  searchC = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
		  listenTo(searchC); // register for auto-dispose
		  folderContainer.put("searchcomp", searchC.getInitialComponent());
		}
		
		
		boolean isGuest = roles.isGuestOnly();
		folderComponent = new FolderComponent(ureq, "foldercomp", rootContainer, filter, customLinkTreeModel, externContainerForCopy);
		folderComponent.setCanMail(!isGuest && canMail == Mail.always); // guests can never send mail
		folderComponent.addListener(this);
		folderContainer.put("foldercomp", folderComponent);
		if (displayWebDAVLink && !isGuest) {
			WebDAVModule webDAVModule = CoreSpringFactory.getImpl(WebDAVModule.class);
			if (webDAVModule.isEnabled() && webDAVModule.isLinkEnabled() && displayWebDAVLink) {
				folderContainer.contextPut("webdavhttp", FolderManager.getWebDAVHttp());
				folderContainer.contextPut("webdavhttps", FolderManager.getWebDAVHttps());
			}
		}

		// jump to either the forum or the folder if the business-launch-path says so.
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me						
			if (log.isDebugEnabled()) log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable ores = ce.getOLATResourceable();			
			if (log.isDebugEnabled()) log.debug("OLATResourceable=" + ores);
			String typeName = ores.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if(path.endsWith(":0")) {
				path = path.substring(0, path.length() - 2);
			}
			activatePath(ureq, path);
		}
		    
		enableDisableQuota(ureq);		
		putInitialPanel(folderContainer);
	}
	
	/**
	 * Remove the subscription panel but let the subscription context active
	 */
	public void disableSubscriptionController() {
		if(csController != null) {
			folderContainer.remove(csController.getInitialComponent());
		}
	}
	
	public void setResourceURL(String resourceUrl) {
		if(searchC != null) {
			searchC.setResourceUrl(resourceUrl);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == folderCommandController) {			
			if (event == FolderCommand.FOLDERCOMMAND_FINISHED) {
				if (!folderCommand.runsModal() && cmc != null) {
					cmc.deactivate();
				}
				folderComponent.updateChildren();
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
				} else if (source instanceof CmdOpenContent) {
					ThreadLocalUserActivityLogger
					.log(
							FolderLoggingAction.FILE_EDIT,
							getClass(),
							CoreLoggingResourceable
									.wrapBCFile(folderComponent
											.getCurrentContainerPath()
											+ ((folderComponent.getCurrentContainerPath().length() > 1) ? File.separator:"")
											+ ((CmdOpenContent) source).getFileName()));
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

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == folderComponent || source == folderContainer || source == editQuotaButton) {
			// we catch events from both folderComponent and folderContainer
			// and process them through the generic folder command implementations
			String cmd = event.getCommand();
			if (cmd.equals(FORM_ACTION)) {
				cmd = getFormAction(ureq);
			}
			
			folderCommand = FolderCommandFactory.getInstance().getCommand(cmd, ureq, getWindowControl());
			if (folderCommand != null) {
				Controller commandController = folderCommand.execute(folderComponent, ureq, getWindowControl(), getTranslator());
				if (commandController != null) {
					folderCommandController = commandController;
					// activate command's controller
					listenTo(folderCommandController);
					if (!folderCommand.runsModal()) {
						String title = folderCommand.getModalTitle();
						cmc = new CloseableModalController(getWindowControl(), translate("close"),
								folderCommandController.getInitialComponent(), true, title);
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
					}
				}
				
				if(FolderCommandStatus.STATUS_FAILED == folderCommand.getStatus()) {
					//failed, reload the children to see if a file has disappeared
					folderComponent.updateChildren();
				}	
			}
			
			if(FolderCommandFactory.COMMAND_BROWSE.equals(cmd)) {
				updatePathResource(ureq);
				getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
			}
			enableDisableQuota(ureq);
		}
	}
	
	private void updatePathResource(UserRequest ureq) {
		final String containerPath = folderComponent.getCurrentContainerPath();
		final String path = "path=" + containerPath;
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(path);
		addToHistory(ureq, ores, null);
		if(canMail == Mail.publicOnly) {
			folderComponent.setCanMail(containerPath.startsWith("/public"));
		}
	}

	private void enableDisableQuota(UserRequest ureq) {
		//prevent a timing condition if the user logout while a thumbnail is generated
		UserSession usess = ureq.getUserSession();
		if (usess == null || usess.getRoles() == null) {
			return;
		} 
		
		Boolean newEditQuota = Boolean.FALSE;
		if (quotaManager.hasMinimalRolesToEditquota(usess.getRoles())) {
			// Only sys admins or institutonal resource managers can have the quota button
			Quota q = VFSManager.isTopLevelQuotaContainer(folderComponent.getCurrentContainer());
			if(q != null) {
				newEditQuota = quotaManager.hasQuotaEditRights(ureq.getIdentity(), usess.getRoles(), q);
			}
		}

		Boolean currentEditQuota = (Boolean) folderContainer.contextGet("editQuota");
		// Update the container only if a new value is available or no value is set to 
		// not make the component dirty after asynchronous thumbnail loading
		if (currentEditQuota == null || !currentEditQuota.equals(newEditQuota)) {
			folderContainer.contextPut("editQuota", newEditQuota);			
		}
	}
	
	/**
	 * Special treatment of forms with multiple submit actions.
	 * @param ureq
	 * @return The action triggered by the user.
	 */
	private String getFormAction(UserRequest ureq) {
		Enumeration<String> params = ureq.getHttpReq().getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			if (key.startsWith(ACTION_PRE)) {
				return key.substring(ACTION_PRE.length());
			} else if("multi_action_identifier".equals(key)) {
				String actionKey = ureq.getParameter("multi_action_identifier");
				if (actionKey.startsWith(ACTION_PRE)) {
					return actionKey.substring(ACTION_PRE.length());
				}
			}
		}
		return null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		if("DocEditor".equals(entries.get(entries.size() - 1).getOLATResourceable().getResourceableTypeName())
				&& folderCommandController != null) {
			// do nothing
		} else {
			String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
			VFSItem vfsItem = folderComponent.getRootContainer().resolve(path);
			if (vfsItem instanceof VFSContainer) {
				folderComponent.setCurrentContainerPath(path);
				updatePathResource(ureq);
			} else {
				activatePath(ureq, path);
			}
		}
	}
	
	public String getCurrentContainerPath() {
		if(folderComponent != null) {
			return folderComponent.getCurrentContainerPath();
		}
		return null;
	}

	public void activatePath(UserRequest ureq, String path) {
		if(folderCommandController != null) {
			removeAsListenerAndDispose(folderCommandController);
			folderCommandController = null;
		}
		if (path != null && path.length() > 0) {
			VFSItem vfsItem = folderComponent.getRootContainer().resolve(path.endsWith("/") ? path.substring(0, path.length()-1) : path);
			if (vfsItem instanceof VFSLeaf) {
				// could be a file - create the mapper - otherwise don't create one if it's a directory
				
				// Create a mapper to deliver the auto-download of the file. We have to
				// create a dedicated mapper here
				// and can not reuse the standard briefcase way of file delivering, some
				// very old fancy code
				// Mapper is cleaned up automatically by basic controller
				String baseUrl = registerMapper(ureq, new FolderMapper(folderComponent.getRootContainer()));
				// Trigger auto-download
				DisplayOrDownloadComponent dordc = new DisplayOrDownloadComponent("downloadcomp", baseUrl + path);
				folderContainer.put("autoDownloadComp", dordc);
				
				if (path.lastIndexOf('/') > 0) {
					String dirPath = path.substring(0, path.lastIndexOf('/'));
					if (StringHelper.containsNonWhitespace(dirPath)) {
						folderComponent.setCurrentContainerPath(dirPath);
					}
				}
			} else if(vfsItem instanceof VFSContainer) {
				if (StringHelper.containsNonWhitespace(path)) {
					folderComponent.setCurrentContainerPath(path);
				}
			}
			
			updatePathResource(ureq);
		}
	}
	
	public enum Mail {
		always,
		never,
		publicOnly;
		
		public static final Mail valueOf(boolean val) {
			return val ? always : never;
		}
	}
}