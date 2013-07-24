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
*/

package org.olat.repository.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.NewControllerFactory;
import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.IdentityShort;
import org.olat.catalog.ui.CatalogEntryAddController;
import org.olat.catalog.ui.RepoEntryCategoriesTableController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.run.RunMainController;
import org.olat.repository.DisplayCourseInfoForm;
import org.olat.repository.DisplayInfoForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryIconRenderer;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.CourseHandler;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;


/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RepositoryDetailsController extends BasicController implements GenericEventListener, Activateable2 {
	OLog log = Tracing.createLoggerFor(this.getClass());

	private static final String ACTION_CLOSE = "cmd.close";
	private static final String ACTION_DOWNLOAD = "dl";
	private static final String ACTION_DOWNLOAD_BACKWARD_COMPAT = "dlcompat";
	private static final String ACTION_LAUNCH = "lch";
	private static final String ACTION_COPY = "cp";
	private static final String ACTION_BOOKMARK = "bm";
	private static final String ACTION_EDIT = "edt";
	private static final String ACTION_DETAILSEDIT = "dtedt";
	private static final String ACTION_ADD_CATALOG = "add.cat";
	private static final String ACTION_DELETE = "del";
	private static final String ACTION_CLOSE_RESSOURCE = "close.ressource";
	private static final String ACTION_MEMBERS = "members";
	
	private static final String ACTION_ORDERS = "orders";
	private static final String ACTION_EDITDESC = "chdesc";
	private static final String ACTION_EDITPROP = "chprop";

	private static final String TOOL_BOOKMARK = "b";
	private static final String TOOL_COPY = "c";
	private static final String TOOL_DOWNLOAD = "d";
	private static final String TOOL_DOWNLOAD_BACKWARD_COMPAT = "dcompat";
	private static final String TOOL_EDIT = "e";
	private static final String TOOL_CATALOG = "cat";
	private static final String TOOL_CHDESC = "chd";
	private static final String TOOL_CHPROP = "chp";
	private static final String TOOL_LAUNCH = "l";
	private static final String TOOL_CLOSE_RESSOURCE = "cr";
	
	private VelocityContainer main;
	private Link downloadButton;
	private Link backLink;
	private Link launchButton;
	private Link loginLink;
	
	private GroupController groupController;
	private RepositoryMembersController membersEditController;
	private OrdersAdminController ordersController;
	private ToolController detailsToolC;
	private RepositoryCopyController copyController;
	private RepositoryEditPropertiesController repositoryEditPropertiesController;
	private RepositoryEditDescriptionController repositoryEditDescriptionController;
	private RepoEntryCategoriesTableController repoEntryCategoriesTableController;
	private CloseableModalController closeableModalController;
	private DialogBoxController deleteDialogController;
	private Controller catalogAdddController;
	private Controller detailsForm;

	private RepositoryEntry repositoryEntry;
	private boolean isOwner;
	private boolean isAuthor;
	private boolean isOlatAdmin;
	private boolean isGuestOnly = false;
	private boolean jumpfromcourse = false;
	private boolean corrupted;
	public static final String ACTIVATE_EDITOR = "activateEditor";
	
	private DisplayCourseInfoForm courseInfoForm;
	private DisplayInfoForm displayInfoForm;
	
	private LockResult lockResult;
	private WizardCloseResourceController wc;
	private CloseableModalController cmc;
	
	//different instances for "copy" and "settings change", since it is important to know what triggered the CLOSE_MODAL_EVENT
	private CloseableModalController copyCloseableModalController;
	private CloseableModalController settingsCloseableModalController;
	//fxdiff FXOLAT-128: back/resume function
	public static final Event LAUNCHED_EVENT = new Event("lr-launched");
	
	private final BaseSecurity securityManager;
	private final UserManager userManager;
	private final MarkManager markManager;

	/**
	 * Controller displaying details of a given repository entry.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param mainPanel
	 */
	public RepositoryDetailsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		//sets velocity root and translator to RepositoryManager package 
		setBasePackage(RepositoryManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		
		if (log.isDebug()){
			log.debug("Constructing ReposityMainController using velocity root " + velocity_root);
		}
		// main component layed out in panel
		main = createVelocityContainer("details");
		
		downloadButton = LinkFactory.createButton("details.download", main, this);
		downloadButton.setElementCssClass("o_sel_repo_download_button");
		LinkFactory.markDownloadLink(downloadButton);
		launchButton = LinkFactory.createButton("details.launch", main, this);
		launchButton.setElementCssClass("o_sel_repo_launch_button");
		
		backLink = LinkFactory.createLinkBack(main, this);
		backLink.setElementCssClass("o_sel_repo_back_button");
		loginLink = LinkFactory.createLink("repo.login", main, this);
		loginLink.setElementCssClass("o_sel_repo_login_button");
		
		putInitialPanel(main);
	}

	/**
	 * @param ureq
	 */
	private void checkSecurity(UserRequest ureq) {
		if (ureq.getUserSession().getRoles().isOLATAdmin()) {
			isOwner = true;
			isAuthor = true;
			isOlatAdmin = true;
		} else {
			// load repositoryEntry again because the hibenate object is 'detached'.Otherwise you become an exception when you check owner-group.
			boolean isInstitutionalResourceManager = RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(repositoryEntry, ureq.getIdentity());
			repositoryEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry);
			isOwner = BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(ureq.getIdentity(), Constants.PERMISSION_ACCESS,
					repositoryEntry.getOwnerGroup())
					| isInstitutionalResourceManager;
			isAuthor = ureq.getUserSession().getRoles().isAuthor() | isInstitutionalResourceManager;
			isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
			isOlatAdmin = false;
		}
	}

	/**
	 * @param ureq
	 */
	private void updateRepositoryEntryView(UserRequest ureq) {

		main.contextPut("isOwner", new Boolean(isOwner));
		main.contextPut("isAuthor", new Boolean(isAuthor));
		main.contextPut("isOlatAdmin", new Boolean(isOlatAdmin));
		main.contextPut("launchableTyp", new Boolean(checkIsRepositoryEntryTypeLaunchable()));
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
		main.contextPut("extlink", url);
		
		String displayName = getDisplayName(ureq.getLocale());
		main.contextPut("title", displayName);

		boolean isLaunchable = checkIsRepositoryEntryLaunchable(ureq);
		launchButton.setEnabled(isLaunchable);
		launchButton.setTextReasonForDisabling(translate("launch.noaccess"));
		downloadButton.setEnabled(repositoryEntry.getCanDownload());
		downloadButton.setTextReasonForDisabling(translate("disabledexportreason"));
		
		RepositoryEntryLifecycle cycle = repositoryEntry.getLifecycle();
		if(cycle != null) {
			Formatter format = Formatter.getInstance(getLocale());
			main.contextPut("lfStart", format.formatDateAndTime(cycle.getValidFrom()));
			main.contextPut("lfEnd", format.formatDateAndTime(cycle.getValidTo()));
			if(!cycle.isPrivateCycle()) {
				String label = cycle.getLabel();
				String softKey = cycle.getSoftKey();
				main.contextPut("lfLabel", label);
				main.contextPut("lfSoftKey", softKey);
			} else {
				main.contextPut("lfLabel", null);
				main.contextPut("lfSoftKey", null);
			}
		}
		
		if (repositoryEntry.getDescription() != null) {
			main.contextPut("description", Formatter.formatLatexFormulas(repositoryEntry.getDescription()));
		}
		VFSLeaf image = RepositoryManager.getInstance().getImage(repositoryEntry);
		if (image != null) {
			// display only within 600x300 - everything else looks ugly
			ImageComponent ic = new ImageComponent("image");
			ic.setMediaResource(new VFSMediaResource(image));
			ic.setMaxWithAndHeightToFitWithin(600, 300);
			main.contextPut("hasImage", Boolean.TRUE);
			main.put("image", ic);
		} else {
			main.contextPut("hasImage", Boolean.FALSE);
		}

		main.contextPut("id", repositoryEntry.getResourceableId());
		main.contextPut("ores_id", repositoryEntry.getOlatResource().getResourceableId());
		main.contextPut("softkey", repositoryEntry.getSoftkey());
		
		boolean managed = StringHelper.containsNonWhitespace(repositoryEntry.getExternalId())
				|| StringHelper.containsNonWhitespace(repositoryEntry.getExternalRef())
				|| repositoryEntry.getManagedFlags().length > 0;
		
		if(managed) {
			main.contextPut("externalId",
					repositoryEntry.getExternalId() == null ? "" : repositoryEntry.getExternalId());
			main.contextPut("externalRef",
					repositoryEntry.getExternalRef() == null ? "" : repositoryEntry.getExternalRef());
		}
		
		//add the list of owners
		List<IdentityShort> authors = securityManager.getIdentitiesShortOfSecurityGroups(Collections.singletonList(repositoryEntry.getOwnerGroup()), 0, -1);
		List<String> authorLinkNames = new ArrayList<String>(authors.size());
		int counter = 0;
		for(IdentityShort author:authors) {
			String authorName = userManager.getUserDisplayName(author);
			Link authorLink = LinkFactory.createLink("author_" + counter++, main, this);
			authorLink.setCustomDisplayText(authorName);
			authorLink.setUserObject(author);
			authorLinkNames.add(authorLink.getComponentName());
		}
		main.contextPut("authorlinknames", authorLinkNames);
		
		//add the initial author
		String initialAuthorName = repositoryEntry.getInitialAuthor();
		List<IdentityShort> initialAuthors = securityManager.findShortIdentitiesByName(Collections.singletonList(initialAuthorName));
		if(!initialAuthors.isEmpty()) {
			String authorName = userManager.getUserDisplayName(initialAuthors.get(0));
			Link authorLink = LinkFactory.createLink("author_" + counter++, main, this);
			authorLink.setCustomDisplayText(authorName);
			authorLink.setUserObject(initialAuthors.get(0));
			main.contextPut("initialauthorlinkename", authorLink.getComponentName());
		}
		
		main.contextPut("userlang", I18nManager.getInstance().getLocaleKey(ureq.getLocale()));
		main.contextPut("isGuestAllowed", (repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS ? Boolean.TRUE	: Boolean.FALSE));
		main.contextPut("isGuest", Boolean.valueOf(ureq.getUserSession().getRoles().isGuestOnly()));

		String typeName = repositoryEntry.getOlatResource().getResourceableTypeName();
		StringBuilder typeDisplayText = new StringBuilder(100);
		if (typeName != null) { // add image and typename code
			RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer(ureq.getLocale());
			typeDisplayText.append("<span class=\"b_with_small_icon_left ");
			typeDisplayText.append(reir.getIconCssClass(repositoryEntry));
			typeDisplayText.append("\">");
			String tName = ControllerFactory.translateResourceableTypeName(typeName, getLocale());
			typeDisplayText.append(tName);
			if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
				PackageTranslator pT = new PackageTranslator(RepositoryEntryStatus.class.getPackage().getName(), ureq.getLocale());
				typeDisplayText.append(" " + "(" + pT.translate("title.prefix.closed") + ")");
			}
			typeDisplayText.append("</span>");
		} else {
			typeDisplayText.append(translate("cif.type.na"));
		}
		main.contextPut("type", typeDisplayText.toString());
		VelocityContainer infopanelVC = createVelocityContainer("infopanel");
		// show how many users are currently using this resource
		String numUsers;
		OLATResourceable ores = repositoryEntry.getOlatResource();
		int cnt = 0;
		OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource().getResourceableId());
		if (ores != null) cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
		numUsers = String.valueOf(cnt);
		infopanelVC.contextPut("numUsers", numUsers);
	
		removeAsListenerAndDispose(displayInfoForm);
		displayInfoForm = new DisplayInfoForm(ureq, getWindowControl(), repositoryEntry);
		listenTo(displayInfoForm);
		main.put("displayform", displayInfoForm.getInitialComponent());
		
		infopanelVC.contextPut("isAuthor", Boolean.valueOf(isAuthor));
		infopanelVC.contextPut("isOwner", Boolean.valueOf(isOwner));
		// init handler details
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		infopanelVC.contextPut("iscourse", new Boolean(handler instanceof CourseHandler));
		main.contextPut("iscourse", new Boolean(handler instanceof CourseHandler));
		//brasato:: review why such a type check was necessary
		
		setCorrupted(false);
		if (handler instanceof CourseHandler) {
			try {
				removeAsListenerAndDispose(courseInfoForm);
				courseInfoForm = new DisplayCourseInfoForm(ureq, getWindowControl(), CourseFactory.loadCourse(repositoryEntry.getOlatResource()));
				listenTo(courseInfoForm);
				infopanelVC.put("CourseInfoForm", courseInfoForm.getInitialComponent());
			} catch(CorruptedCourseException e) {
				log.error("", e);
				setCorrupted(true);
			}
		}
		
		if(managed) {
			infopanelVC.contextPut("managedflags", repositoryEntry.getManagedFlagsString());
		}
		
		removeAsListenerAndDispose(detailsForm);
		detailsForm = handler.createDetailsForm(ureq, getWindowControl(), repositoryEntry.getOlatResource());
		if (detailsForm != null) { // push handler specific details view
			listenTo(detailsForm);
			infopanelVC.contextPut("hasHandlerDetails", Boolean.valueOf("true"));
			infopanelVC.put("handlerDetails", detailsForm.getInitialComponent());
		} else {
			infopanelVC.contextRemove("hasHandlerDetails");
		}
		// init reference usage
		// Where is it in use
		String referenceDetails = ReferenceManager.getInstance().getReferencesToSummary(repositoryEntry.getOlatResource(), ureq.getLocale());
		if (referenceDetails != null) {
			infopanelVC.contextPut("referenceDetails", referenceDetails);
		} else {
			infopanelVC.contextRemove("referenceDetails");
		}

		// Number of launches
		String numLaunches;
		if (repositoryEntry.getCanLaunch()) {
			numLaunches = String.valueOf(repositoryEntry.getLaunchCounter());
		} else {
			numLaunches = translate("cif.canLaunch.na");
		}
		infopanelVC.contextPut("numLaunches", numLaunches);

		// Number of downloads
		String numDownloads;
		if (repositoryEntry.getCanDownload()) {
			numDownloads = String.valueOf(repositoryEntry.getDownloadCounter());
		} else {
			numDownloads = translate("cif.canDownload.na");
		}

		infopanelVC.contextPut("numDownloads", numDownloads);

		if (repositoryEntry.getLastUsage() != null) {
			infopanelVC.contextPut("lastUsage", repositoryEntry.getLastUsage());
		} else {
			infopanelVC.contextPut("lastUsage", translate("cif.lastUsage.na"));
		}

		main.put(infopanelVC.getComponentName(), infopanelVC);

		removeAsListenerAndDispose(groupController);
		groupController = new GroupController(ureq, getWindowControl(), false, true, false, false, true, false, repositoryEntry.getOwnerGroup());
		listenTo(groupController);
		
		main.put("ownertable", groupController.getInitialComponent());
	}
	
	public void setCorrupted(boolean corrupted) {
		this.corrupted = corrupted;
		downloadButton.setEnabled(!corrupted);
		launchButton.setEnabled(!corrupted);
		main.contextPut("corrupted", new Boolean(corrupted));
	}

	/**
	 * @param ureq
	 * @param newToolController
	 */
	private void updateView(UserRequest ureq) {
		checkSecurity(ureq);
		updateRepositoryEntryView(ureq);
		updateDetailsToolC(ureq);
		updateCategoriesTableC(ureq);
	}

	
	private void updateCategoriesTableC(UserRequest ureq) {		
		// load category links
		removeAsListenerAndDispose(repoEntryCategoriesTableController);
		repoEntryCategoriesTableController = new RepoEntryCategoriesTableController(ureq, getWindowControl(), this.repositoryEntry, (isOlatAdmin || isOwner));
		listenTo(repoEntryCategoriesTableController);
		main.put("repoEntryCategoriesTable", repoEntryCategoriesTableController.getInitialComponent());
	}
	/**
	 * @param newToolController
	 */
	private void updateDetailsToolC(UserRequest ureq) {
		boolean isNewController = false;
		if (detailsToolC == null) {
			detailsToolC = ToolFactory.createToolController(getWindowControl());
			listenTo(detailsToolC);
			isNewController = true;
		}
		// init handler details
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (isNewController) {
			detailsToolC.addHeader(translate("tools.details.header"));
			detailsToolC.addLink(ACTION_LAUNCH, translate("details.launch"), TOOL_LAUNCH, null, "o_sel_repo_launch", false);
		}
		detailsToolC.setEnabled(TOOL_LAUNCH, checkIsRepositoryEntryLaunchable(ureq) && !corrupted);
		if (!isGuestOnly) {
			if (isNewController) {
				//mark as download link
				detailsToolC.addLink(ACTION_DOWNLOAD, translate("details.download"), TOOL_DOWNLOAD, null, "o_sel_repo_download", true);
				detailsToolC.addLink(ACTION_DOWNLOAD_BACKWARD_COMPAT, translate("details.download.compatible"), TOOL_DOWNLOAD_BACKWARD_COMPAT,
						null, "o_sel_repo_download_backward", true);
				//bookmark
				boolean marked = markManager.isMarked(repositoryEntry, getIdentity(), null);
				String css = marked ? "b_mark_set" : "b_mark_not_set";
				detailsToolC.addLink(ACTION_BOOKMARK, translate("details.bookmark"), TOOL_BOOKMARK, css);
			}
			boolean canDownload = repositoryEntry.getCanDownload() && handler.supportsDownload(repositoryEntry);
			// disable download for courses if not author or owner
			if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())
				&& !(isOwner || isAuthor)) canDownload = false;
			// always enable download for owners
			if (isOwner && handler.supportsDownload(repositoryEntry)) {
				canDownload = true;
			}
			detailsToolC.setEnabled(TOOL_DOWNLOAD, canDownload && !corrupted);
			detailsToolC.setEnabled(TOOL_DOWNLOAD_BACKWARD_COMPAT, canDownload && !corrupted
					&& "CourseModule".equals(repositoryEntry.getOlatResource().getResourceableTypeName()));
			detailsToolC.setEnabled(TOOL_BOOKMARK, !corrupted);
		}
		//fxdiff VCRP-1 : moved some things around here to split large toolbox into smaller pieces
		if (isNewController)
			detailsToolC.addLink(ACTION_CLOSE, translate("details.close"), null, "b_toolbox_close");

		if (isAuthor || isOwner) {
			if (isNewController) {
				detailsToolC.addHeader(translate("edit"));
			}
			boolean canCopy = repositoryEntry.getCanCopy();
			if (isOwner) {
				if (isNewController) {
					if(isAuthor) {
						detailsToolC.addLink(ACTION_EDIT, translate("details.openeditor"), TOOL_EDIT, null, "o_sel_repo_open_editor", false);
						detailsToolC.addLink(ACTION_EDITDESC, translate("details.chdesc"), TOOL_CHDESC, null, "o_sel_repo_edit_descritpion", false);
						detailsToolC.addLink(ACTION_EDITPROP, translate("details.chprop"), TOOL_CHPROP, null, "o_sel_repor_edit_properties", false);
					}
					detailsToolC.addLink(ACTION_ADD_CATALOG, translate("details.catadd"), TOOL_CATALOG, null, "o_sel_repo_add_to_catalog", false);
					
					detailsToolC.addHeader(translate("table.action"));

					boolean closeManaged = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.close);
					if ((OresHelper.isOfType(repositoryEntry.getOlatResource(), CourseModule.class))
							&& !closeManaged
							&& (!RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed())) {
						detailsToolC.addLink(ACTION_CLOSE_RESSOURCE, translate("details.close.ressoure"), TOOL_CLOSE_RESSOURCE, null, "o_sel_repo_close_resource", false);
						if(corrupted) {
							detailsToolC.setEnabled(TOOL_CLOSE_RESSOURCE, false);
						}
					}
				}
				// update catalog link
				boolean addCatalogEnabled = !corrupted &&
						(repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS ||
						repositoryEntry.isMembersOnly());
				detailsToolC.setEnabled(TOOL_CATALOG, addCatalogEnabled);
			}
			if (isNewController) {
				if(isAuthor) {
					detailsToolC.addLink(ACTION_COPY, translate("details.copy"), TOOL_COPY, null, "o_sel_repo_copy", false);
				}
			}
			if (isOwner) {
				if (isNewController) {
					detailsToolC.addLink(ACTION_DELETE, translate("details.delete"), null, null, "o_sel_repo_delete", false);
					detailsToolC.addHeader(translate("details.members"));
					detailsToolC.addLink(ACTION_MEMBERS, translate("details.members"), null, null, "o_sel_repo_members", false);
					detailsToolC.addLink(ACTION_ORDERS, translate("details.orders"), null, null, "o_sel_repo_booking", false);
				}
				// enable
				if(isAuthor) {
					boolean editManaged = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.editcontent);
					detailsToolC.setEnabled(TOOL_EDIT, handler.supportsEdit(repositoryEntry) && !corrupted && !editManaged);
					detailsToolC.setEnabled(TOOL_CHDESC, !corrupted);
					detailsToolC.setEnabled(TOOL_CHPROP, !corrupted);
				}
				
				canCopy = true;
			}
			if(isAuthor) {
				detailsToolC.setEnabled(TOOL_COPY, canCopy && !corrupted);
			}
		}
	}

	/**
	 * Sets a repository entry for this details controller. Returns a
	 * corresponding tools controller
	 * 
	 * @param entry
	 * @param ureq
	 * @return A tool controller representing available tools for the given entry.
	 */
	public ToolController setEntry(RepositoryEntry entry, UserRequest ureq, boolean jumpfromcourse) {
		this.corrupted = false;//reset the flag
		this.jumpfromcourse = jumpfromcourse;
		if (repositoryEntry != null) {
			// The controller has already a repository-entry => do de-register it
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, repositoryEntry);
	  }
		repositoryEntry = entry;
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), repositoryEntry);
		checkSecurity(ureq);
		removeAsListenerAndDispose(detailsToolC);
		detailsToolC = null; // force recreation of tool controller
		updateView(ureq);
		return detailsToolC;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		String cmd = event.getCommand();

		if (source == main) {
			if (cmd.equals(ACTION_DETAILSEDIT)) {
				// detailsForm.setDisplayOnly(false);
				main.contextPut("enableEdit", Boolean.valueOf(false)); // disable edit
			} else if (cmd.equals(ACTION_CLOSE)) { // close details
				doCloseDetailView(ureq);
			} else if (cmd.equals(ACTION_LAUNCH)) { // launch resource

			}
		} else if (source instanceof Link) {
			Link sourceLink = (Link)source;
			if (sourceLink == backLink){
				doCloseDetailView(ureq);
				return;
			} else if (sourceLink == downloadButton){
				doDownload(ureq, repositoryEntry, false);
			} else if (sourceLink == launchButton){
				doLaunch(ureq, repositoryEntry);
			} else if (sourceLink == loginLink){
				DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
			} else if (sourceLink.getUserObject() instanceof IdentityShort) {
				IdentityShort author = (IdentityShort)sourceLink.getUserObject();
				String businessPath = "[Identity:" + author.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("settings".equals(type)) {
			if(isAuthor && isOwner) {
				removeAsListenerAndDispose(repositoryEditPropertiesController);
				repositoryEditPropertiesController = new RepositoryEditPropertiesController(ureq, getWindowControl(), repositoryEntry, false);
				listenTo(repositoryEditPropertiesController);
				String title = translate("properties.for", new String[]{ repositoryEntry.getDisplayname() });
				doEditSettings(ureq, repositoryEditPropertiesController, title);
				
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				repositoryEditPropertiesController.activate(ureq, subEntries, entry.getTransientState());
			}
		}
	}

	private void doCloseDetailView(UserRequest ureq) {
		// REVIEW:pb:note:handles jumps from Catalog and Course
		if (jumpfromcourse && repositoryEntry.getCanLaunch()) {
			doLaunch(ureq, repositoryEntry);
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	void deleteRepositoryEntry(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		if (RepositoryManager.getInstance().deleteRepositoryEntryWithAllData( ureq, wControl, repositoryEntry ) ) {
			fireEvent(ureq, new EntryChangedEvent(entry, EntryChangedEvent.DELETED));
			showInfo("info.entry.deleted");
		} else {
			showInfo("info.could.not.delete.entry");
		}
	}
	
	/**
	 * Get displayname of a repository entry. If repository entry a course 
	 * and is this course closed then add a prefix to the title.
	 */
	private String getDisplayName(Locale locale) {
		// load repositoryEntry again because the hibernate object is 'detached'. 
		// Otherwise you become an exception when you check owner-group.
		repositoryEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry);
		String displayName = repositoryEntry.getDisplayname();
		if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
			PackageTranslator pT = new PackageTranslator(RepositoryEntryStatus.class.getPackage().getName(), locale);
			displayName = "[" + pT.translate("title.prefix.closed") + "] ".concat(displayName);
		}
		
		return displayName;
	}

	/**
	 * Also used by RepositoryMainController
	 * 
	 * @param ureq
	 */
	boolean doLaunch(UserRequest ureq, RepositoryEntry re) {
		RepositoryHandler typeToLaunch = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
		if (typeToLaunch == null){
			StringBuilder sb = new StringBuilder(translate("error.launch"));
			sb.append(": No launcher for repository entry: ");
			sb.append(re.getKey());
			throw new OLATRuntimeException(RepositoryDetailsController.class,sb.toString(), null);
		}
		if (RepositoryManager.getInstance().lookupRepositoryEntry(re.getKey()) == null) {
			showInfo("info.entry.deleted");
			return false;
		}
		
		try {
			String businessPath = "[RepositoryEntry:" + re.getKey() + "]";
			boolean ok = NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			if(ok) {
				fireEvent(ureq, LAUNCHED_EVENT);
			}
			return ok;
		} catch (CorruptedCourseException e) {
			logError("Corrupted course: " + re, e);
			return false;
		}
	}

	private boolean checkIsRepositoryEntryLaunchable(UserRequest ureq) {
		RepositoryHandler type = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		RepositoryManager rm = RepositoryManager.getInstance();

		if (rm.isAllowedToLaunch(ureq, repositoryEntry) || (type.supportsLaunch(repositoryEntry) && ureq.getUserSession().getRoles().isOLATAdmin())) {
			return true;
		}
		return false;
	}

	private boolean checkIsRepositoryEntryTypeLaunchable() {
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		return handler.supportsLaunch(repositoryEntry);
	}

	/**
	 * Activates the closeableModalController with the input controller's component, if not null
	 * @param ureq
	 * @param contentController
	 */
	private void doEditSettings(UserRequest ureq, Controller contentController, String title) {
	  if (!isAuthor) throw new OLATSecurityException("Trying to edit properties , but user is not author: user = " + ureq.getIdentity());
	 
	  Component component = contentController.getInitialComponent();
	  
	  if(component!=null) {
	  	removeAsListenerAndDispose(settingsCloseableModalController);
	    settingsCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"),
			  contentController.getInitialComponent(), true, title);
	    listenTo(settingsCloseableModalController);
	    
	    settingsCloseableModalController.activate();
	  }
	  return;
	}

	/**
	 * Also used by RepositoryMainController
	 * 
	 * @param ureq
	 */
	void doDownload(UserRequest ureq, RepositoryEntry re, boolean backwardsCompatible) {
		RepositoryHandler typeToDownload = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);

		if (typeToDownload == null){
			StringBuilder sb = new StringBuilder(translate("error.download"));
			sb.append(": No download handler for repository entry: ");
			sb.append(re.getKey());
			throw new OLATRuntimeException(RepositoryDetailsController.class, sb.toString(), null);
		}
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(re.getOlatResource());
		if (ores == null) {
			showError("error.download");
			return;
		}		
		boolean isAlreadyLocked = typeToDownload.isLocked(ores);
		try {			
		  lockResult = typeToDownload.acquireLock(ores, ureq.getIdentity());
		  if(lockResult==null || (lockResult!=null && lockResult.isSuccess() && !isAlreadyLocked)) {
		    MediaResource mr = typeToDownload.getAsMediaResource(ores, backwardsCompatible);
		    if(mr!=null) {
		      RepositoryManager.getInstance().incrementDownloadCounter(re);
		      ureq.getDispatchResult().setResultingMediaResource(mr);
		    } else {
			    showError("error.export");
			    fireEvent(ureq, Event.FAILED_EVENT);			
		    }
		  } else if(lockResult!=null && lockResult.isSuccess() && isAlreadyLocked) {
		  	String fullName = userManager.getUserDisplayName(lockResult.getOwner());
		  	showInfo("warning.course.alreadylocked.bySameUser", fullName);
		  	lockResult = null; //invalid lock, it was already locked
		  } else {
		  	String fullName = userManager.getUserDisplayName(lockResult.getOwner());
		  	showInfo("warning.course.alreadylocked", fullName);
		  }
		}
		finally {	
			if((lockResult!=null && lockResult.isSuccess() && !isAlreadyLocked)) {
			  typeToDownload.releaseLock(lockResult);		
			  lockResult = null;
			}
		}
	}
	
	/**
	 * If lock successfully aquired start copy, else show warning.
	 * @param ureq
	 */
	private void doCopy(UserRequest ureq) {
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource());
		boolean isAlreadyLocked = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).isLocked(ores);
		lockResult = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).acquireLock(ores, ureq.getIdentity());
		if(lockResult==null || (lockResult!=null && lockResult.isSuccess()) && !isAlreadyLocked) {
			removeAsListenerAndDispose(copyController);
		  copyController = new RepositoryCopyController(ureq, getWindowControl(), repositoryEntry);
		  listenTo(copyController);
		  
		  String title = translate("details.copy");
		  removeAsListenerAndDispose(copyCloseableModalController);
		  copyCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"), copyController.getInitialComponent(),
		  		true, title);
		  listenTo(copyCloseableModalController);
		  
		  copyCloseableModalController.activate();				  
		} else if (lockResult!=null && lockResult.isSuccess() && isAlreadyLocked) {
			showWarning("warning.course.alreadylocked.bySameUser");
			lockResult = null;
		}	else {	
			String fullName = userManager.getUserDisplayName(lockResult.getOwner());
		  showWarning("warning.course.alreadylocked", fullName);
	  }
	}
	

	/**
	 * Also used by RepositoryMainController
	 * 
	 * @param ureq
	 */
	void doEdit(UserRequest ureq) {
		if (!isOwner) throw new OLATSecurityException("Trying to launch editor, but not allowed: user = " + ureq.getIdentity());
		doEdit(ureq, repositoryEntry);
	}
		
	public static void doEdit(UserRequest ureq, RepositoryEntry re) {
		RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
		if (!typeToEdit.supportsEdit(re)){
			throw new AssertException("Trying to edit repository entry which has no assoiciated editor: "+ typeToEdit);
		}

		OLATResourceable ores = re.getOlatResource();
		
		//was brasato:: DTabs dts = getWindowControl().getDTabs();
		DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			//fxdiff BAKS-7 Resume function
			dt = dts.createDTab(ores, re, re.getDisplayname());
			if (dt == null){
				//null means DTabs are full -> warning is shown
				return;
			}
			//user activity logger is set by course factory
			Controller editorController = typeToEdit.createEditorController(ores, ureq, dt.getWindowControl());
			if(editorController == null){
				//editor could not be created -> warning is shown
				return;
			}
			dt.setController(editorController);
			dts.addDTab(ureq, dt);
		}
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(RepositoryDetailsController.ACTIVATE_EDITOR);
		dts.activate(ureq, dt, entries);
	}


	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	private void doAddCatalog(UserRequest ureq) {
		removeAsListenerAndDispose(catalogAdddController);
		removeAsListenerAndDispose(closeableModalController);
		
		catalogAdddController = new CatalogEntryAddController(ureq, getWindowControl(),
				repositoryEntry, true, false);
		listenTo(catalogAdddController);
		closeableModalController = new CloseableModalController(getWindowControl(), "close",
				catalogAdddController.getInitialComponent(), true, translate("details.catadd"));
		listenTo(closeableModalController);
		closeableModalController.activate();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (repositoryEntry != null) {
			repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey());
		}
		String cmd = event.getCommand();
		if (source == ordersController) {
			//
		} else if (source == detailsToolC) {
			if (cmd.equals(ACTION_DOWNLOAD)) { // download
				doDownload(ureq, repositoryEntry, false);
				return;
			} else if (cmd.equals(ACTION_DOWNLOAD_BACKWARD_COMPAT)) {
				doDownload(ureq, repositoryEntry, true);
			} else if (cmd.equals(ACTION_LAUNCH)) { // launch resource
				doLaunch(ureq, repositoryEntry);
				return;
			} else if (cmd.equals(ACTION_EDIT)) { // start editor
				doEdit(ureq);
				return;
			} else if (cmd.equals(ACTION_EDITDESC)) { // change description
				removeAsListenerAndDispose(repositoryEditDescriptionController);
				repositoryEditDescriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), repositoryEntry, false);
				listenTo(repositoryEditDescriptionController);
				String title = translate("properties.for", new String[]{ repositoryEntry.getDisplayname() });
				doEditSettings(ureq, repositoryEditDescriptionController, title);
				return;
			} else if (cmd.equals(ACTION_ADD_CATALOG)) { // start add to catalog workflow
				doAddCatalog(ureq);
				return;
			} else if (cmd.equals(ACTION_EDITPROP)) { // change properties
				removeAsListenerAndDispose(repositoryEditPropertiesController);
				repositoryEditPropertiesController = new RepositoryEditPropertiesController(ureq, getWindowControl(), repositoryEntry, false);
				listenTo(repositoryEditPropertiesController);
				String title = translate("properties.for", new String[]{ repositoryEntry.getDisplayname() });
				doEditSettings(ureq, repositoryEditPropertiesController, title);
				return;
			} else if (cmd.equals(ACTION_CLOSE)) {
				doCloseDetailView(ureq);
				return;
			} else if (cmd.equals(ACTION_BOOKMARK)) {
				boolean marked = markManager.isMarked(repositoryEntry, getIdentity(), null);
				if(marked) {
					markManager.removeMark(repositoryEntry, getIdentity(), null);
				} else {
					String businessPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
					markManager.setMark(repositoryEntry, getIdentity(), null, businessPath);
				}
				String css = marked ? "b_mark_not_set" : "b_mark_set";
				detailsToolC.setCssClass(TOOL_BOOKMARK, css);
			} else if (cmd.equals(ACTION_COPY)) { // copy
				if (!isAuthor) throw new OLATSecurityException("Trying to copy, but user is not author: user = " + ureq.getIdentity());
				doCopy(ureq);
				return;
			} else if (cmd.equals(ACTION_MEMBERS)) { // membership
				if (!isOwner) throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + ureq.getIdentity());
				removeAsListenerAndDispose(cmc);
				removeAsListenerAndDispose(membersEditController);
				
				membersEditController = new RepositoryMembersController(ureq, getWindowControl(), repositoryEntry);
				listenTo(membersEditController);
				CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"),
						membersEditController.getInitialComponent(), true, translate("details.members"));
				listenTo(cmc);
				cmc.activate();
				return;
			} else if (cmd.equals(ACTION_ORDERS)) {
				doOrders(ureq);
				return;
			} else if (cmd.equals(ACTION_CLOSE_RESSOURCE)) {
				doCloseResource(ureq);
				return;
			} else if (cmd.equals(ACTION_DELETE)) { // delete
				if (!isOwner) throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
				doDelete(ureq);
				return;
			}
		} else if (source == wc) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				
			} else if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				
				removeAsListenerAndDispose(detailsToolC);
				detailsToolC = null; // force recreation of tool controller
				updateView(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == copyController) {				
			RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);		
			lockResult = null;
			copyCloseableModalController.deactivate();
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT); // go back to overview on success
				fireEvent(ureq, new EntryChangedEvent(copyController.getNewEntry(), EntryChangedEvent.ADDED)); // go
				// back to overview on success
			} else if (event == Event.FAILED_EVENT) { // copy failed, go back to
				// overview
				fireEvent(ureq, Event.DONE_EVENT); // go back to overview on failure
			} else if (event instanceof EntryChangedEvent) {
				fireEvent(ureq, event);
			}
			removeAsListenerAndDispose(copyController);
			copyController = null;
		} else if (source == repositoryEditDescriptionController) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				// RepositoryEntry changed
				String displayname = repositoryEditDescriptionController.getRepositoryEntry().getDisplayname();
				String description = repositoryEditDescriptionController.getRepositoryEntry().getDescription();
				RepositoryEntryLifecycle cycle = repositoryEditDescriptionController.getRepositoryEntry().getLifecycle();
				repositoryEntry = RepositoryManager.getInstance().setDescriptionAndName(repositoryEntry, displayname, description, cycle);
				// do not close upon save/upload image closeableModalController.deactivate();
				updateView(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(repositoryEditDescriptionController);
				repositoryEntry = repositoryEditDescriptionController.getRepositoryEntry();
			}
			settingsCloseableModalController.deactivate();
		} else if (source == repositoryEditPropertiesController) {
			if (event == Event.CHANGED_EVENT || event.getCommand().equals("courseChanged")) {
				// RepositoryEntry changed
				this.repositoryEntry = repositoryEditPropertiesController.getRepositoryEntry();
				updateView(ureq);
				RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
				boolean canDownload = repositoryEntry.getCanDownload() && handler.supportsDownload(repositoryEntry);
				detailsToolC.setEnabled(TOOL_DOWNLOAD, canDownload);
				if (checkIsRepositoryEntryTypeLaunchable()) {
					detailsToolC.setEnabled(TOOL_LAUNCH, checkIsRepositoryEntryLaunchable(ureq));
				}				
				if(event.getCommand().equals("courseChanged")) {
					removeAsListenerAndDispose(repositoryEditPropertiesController);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				settingsCloseableModalController.deactivate();
				removeAsListenerAndDispose(repositoryEditPropertiesController);
			} else if(event == Event.DONE_EVENT) {
				removeAsListenerAndDispose(repositoryEditPropertiesController);
				repositoryEditPropertiesController = null;
			}
		} else if (source == deleteDialogController){
			if (DialogBoxUIFactory.isYesEvent(event)){
				deleteRepositoryEntry(ureq, getWindowControl(), this.repositoryEntry);
			}	
		} else if (source == settingsCloseableModalController) {
			if (event == CloseableModalController.CLOSE_MODAL_EVENT) {				
				updateView(ureq);				
				//check if commit or not the course conf changes				
				if(repositoryEditPropertiesController!=null) {
					boolean configsChanged = repositoryEditPropertiesController.checkIfCourseConfigChanged(ureq);
					if(!configsChanged) {
						removeAsListenerAndDispose(repositoryEditPropertiesController);
				    repositoryEditPropertiesController = null;
					}
				}
			}
		} else if (source == copyCloseableModalController) {
			if (event == CloseableModalController.CLOSE_MODAL_EVENT) {				
				updateView(ureq);						
				if(copyController!=null) {
					//copyController's modal dialog was closed, that is cancel copy
					RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);		
					lockResult = null;
					removeAsListenerAndDispose(copyController);
					copyController = null;
				}
			}
		}	else if (source == catalogAdddController) {
			// finish modal dialog and reload categories list controller
			closeableModalController.deactivate();
			updateCategoriesTableC(ureq);
		}
	}
	
	private void doOrders(UserRequest ureq) {
		removeAsListenerAndDispose(ordersController);

		ordersController = new OrdersAdminController(ureq, getWindowControl(), repositoryEntry.getOlatResource());
		listenTo(ordersController);

		removeAsListenerAndDispose(cmc);
		CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), ordersController.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
	}

	/**
	 * @param ureq
	 */
	private void doCloseResource(UserRequest ureq) {
		RepositoryHandler repoHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);

		removeAsListenerAndDispose(wc);
		wc = repoHandler.createCloseResourceController(ureq, getWindowControl(), repositoryEntry);
		listenTo(wc);
		
		wc.startWorkflow();
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), wc.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq) {
		//show how many users are currently using this resource

		String dialogTitle = translate("del.header", repositoryEntry.getDisplayname());
		OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource().getResourceableId());
		int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
		
		String dialogText = translate(corrupted ? "del.confirm.corrupted" : "del.confirm", String.valueOf(cnt));
		deleteDialogController = activateYesNoDialog(ureq, dialogTitle, dialogText, deleteDialogController);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if(lockResult!=null) {
			//the lock it is assumed to be released after export/copy operation, but release it anyway in case it failed to release
			if (repositoryEntry != null) {
			  RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);
			}  
			lockResult = null;
		}
		if (repositoryEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, repositoryEntry);
		}
		if(copyCloseableModalController!=null) {
			copyCloseableModalController.dispose();
			copyCloseableModalController = null;
		}
		if(settingsCloseableModalController!=null) {
			settingsCloseableModalController.dispose();
			settingsCloseableModalController = null;
		}		
	}

	public void event(Event event) {
		if (event instanceof EntryChangedEvent) {
			repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey());
		}
	}

	/**
	 *
	 * @return
	 */
	public ToolController getDetailsToolController() {
		return this.detailsToolC;
	}

}