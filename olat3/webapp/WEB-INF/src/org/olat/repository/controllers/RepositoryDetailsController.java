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
 * <p>
 */

package org.olat.repository.controllers;

import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.bookmark.AddAndEditBookmarkController;
import org.olat.bookmark.BookmarkManager;
import org.olat.catalog.ui.CatalogAjaxAddController;
import org.olat.catalog.ui.CatalogEntryAddController;
import org.olat.catalog.ui.RepoEntryCategoriesTableController;
import org.olat.core.commons.persistence.DBFactory;
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
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.run.RunMainController;
import org.olat.repository.DisplayCourseInfoForm;
import org.olat.repository.DisplayInfoForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryIconRenderer;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.CourseHandler;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RepositoryDetailsController extends BasicController implements GenericEventListener {
	OLog log = Tracing.createLoggerFor(this.getClass());

	private static final String ACTION_CLOSE = "cmd.close";
	private static final String ACTION_DOWNLOAD = "dl";
	private static final String ACTION_LAUNCH = "lch";
	private static final String ACTION_COPY = "cp";
	private static final String ACTION_BOOKMARK = "bm";
	private static final String ACTION_EDIT = "edt";
	private static final String ACTION_DETAILSEDIT = "dtedt";
	private static final String ACTION_ADD_CATALOG = "add.cat";
	private static final String ACTION_DELETE = "del";
	private static final String ACTION_CLOSE_RESSOURCE = "close.ressource";
	private static final String ACTION_GROUPS = "grp";
	private static final String ACTION_EDITDESC = "chdesc";
	private static final String ACTION_EDITPROP = "chprop";

	private static final String TOOL_BOOKMARK = "b";
	private static final String TOOL_COPY = "c";
	private static final String TOOL_DOWNLOAD = "d";
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
	
	private GroupController groupController, groupEditController;
	private SecurityGroup ownerGroup;
	private AddAndEditBookmarkController bookmarkController;
	private ToolController detailsToolC = null;
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
	public static final String ACTIVATE_EDITOR = "activateEditor";
	public static final String ACTIVATE_RUN = "activateRun";
	
	private DisplayCourseInfoForm courseInfoForm;
	private DisplayInfoForm displayInfoForm;
	
	private LockResult lockResult;
	private WizardCloseResourceController wc;
	private CloseableModalController cmc;
	
	//different instances for "copy" and "settings change", since it is important to know what triggered the CLOSE_MODAL_EVENT
	private CloseableModalController copyCloseableModalController;
	private CloseableModalController settingsCloseableModalController;

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
		if (log.isDebug()){
			log.debug("Constructing ReposityMainController using velocity root " + velocity_root);
		}
		// main component layed out in panel
		main = createVelocityContainer("details");
		
		downloadButton = LinkFactory.createButton("details.download", main, this);
		LinkFactory.markDownloadLink(downloadButton);
		launchButton = LinkFactory.createButton("details.launch", main, this);
		
		backLink = LinkFactory.createLinkBack(main, this);
		loginLink = LinkFactory.createLink("repo.login", main, this);
		
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
		
		if (repositoryEntry.getDescription() != null) {
			main.contextPut("description", Formatter.formatLatexFormulas(repositoryEntry.getDescription()));
		}
		ImageComponent ic = RepositoryEntryImageController.getImageComponentForRepositoryEntry("image", repositoryEntry);

		if (ic != null) {
			// display only within 600x300 - everything else looks ugly
			ic.setMaxWithAndHeightToFitWithin(600, 300);
			main.contextPut("hasImage", Boolean.TRUE);
			main.put("image", ic);
		} else {
			main.contextPut("hasImage", Boolean.FALSE);
		}

		main.contextPut("id", repositoryEntry.getResourceableId());
		main.contextPut("ores_id", repositoryEntry.getOlatResource().getResourceableId());
		main.contextPut("initialauthor", repositoryEntry.getInitialAuthor());
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
		
		if (handler instanceof CourseHandler) {
			removeAsListenerAndDispose(courseInfoForm);
			courseInfoForm = new DisplayCourseInfoForm(ureq, getWindowControl(), CourseFactory.loadCourse(repositoryEntry.getOlatResource()));
			listenTo(courseInfoForm);
			infopanelVC.put("CourseInfoForm", courseInfoForm.getInitialComponent());
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
		groupController = new GroupController(ureq, getWindowControl(), false, true, false, repositoryEntry.getOwnerGroup());
		listenTo(groupController);
		
		main.put("ownertable", groupController.getInitialComponent());

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
			detailsToolC.addLink(ACTION_LAUNCH, translate("details.launch"), TOOL_LAUNCH, null);
		}
		detailsToolC.setEnabled(TOOL_LAUNCH, checkIsRepositoryEntryLaunchable(ureq));
		if (!isGuestOnly) {
			if (isNewController) {
				//mark as download link
				detailsToolC.addLink(ACTION_DOWNLOAD, translate("details.download"), TOOL_DOWNLOAD, null, true);
				detailsToolC.addLink(ACTION_BOOKMARK, translate("details.bookmark"), TOOL_BOOKMARK, null);
			}
			boolean canDownload = repositoryEntry.getCanDownload() && handler.supportsDownload(repositoryEntry);
			// disable download for courses if not author or owner
			if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())
				&& !(isOwner || isAuthor)) canDownload = false;
			// always enable download for owners
			if (isOwner && handler.supportsDownload(repositoryEntry)) canDownload = true;
			detailsToolC.setEnabled(TOOL_DOWNLOAD, canDownload);
			boolean canBookmark = true;
			if (BookmarkManager.getInstance().isResourceableBookmarked(ureq.getIdentity(), repositoryEntry) || !repositoryEntry.getCanLaunch())
				canBookmark = false;
			detailsToolC.setEnabled(TOOL_BOOKMARK, canBookmark);
		}
		if (isAuthor || isOwner) {
			boolean canCopy = repositoryEntry.getCanCopy();
			if (isOwner) {
				if (isNewController) {
					detailsToolC.addLink(ACTION_EDIT, translate("details.openeditor"), TOOL_EDIT, null);
					detailsToolC.addLink(ACTION_EDITDESC, translate("details.chdesc"), TOOL_CHDESC, null);
					detailsToolC.addLink(ACTION_EDITPROP, translate("details.chprop"), TOOL_CHPROP, null);
					detailsToolC.addLink(ACTION_ADD_CATALOG, translate("details.catadd"), TOOL_CATALOG, null);
					if ((OresHelper.isOfType(repositoryEntry.getOlatResource(), CourseModule.class)) && (!RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed())) {
						detailsToolC.addLink(ACTION_CLOSE_RESSOURCE, translate("details.close.ressoure"), TOOL_CLOSE_RESSOURCE, null);
					}
				}
				// update catalog link
				detailsToolC.setEnabled(TOOL_CATALOG, (repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS));
			}
			if (isNewController)
				detailsToolC.addLink(ACTION_COPY, translate("details.copy"), TOOL_COPY, null);
			if (isOwner) {
				if (isNewController) {
					detailsToolC.addLink(ACTION_DELETE, translate("details.delete"));
					detailsToolC.addLink(ACTION_GROUPS, translate("details.groups"));
				}
				// enable
				detailsToolC.setEnabled(TOOL_EDIT, handler.supportsEdit(repositoryEntry));
				detailsToolC.setEnabled(TOOL_CHDESC, true);
				detailsToolC.setEnabled(TOOL_CHPROP, true);
				canCopy = true;
			}
			detailsToolC.setEnabled(TOOL_COPY, canCopy);
		}
		if (isNewController)
			detailsToolC.addLink(ACTION_CLOSE, translate("details.close"), null, "b_toolbox_close");
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
				// button
				return;
			} else if (cmd.equals(ACTION_CLOSE)) { // close details
				doCloseDetailView(ureq);
				return;
			} else if (cmd.equals(ACTION_LAUNCH)) { // launch resource

			}
		} else if (source == backLink){
			doCloseDetailView(ureq);
			return;
		} else if (source == downloadButton){
			doDownload(ureq);
		} else if (source == launchButton){
			doLaunch(ureq);
		} else if (source == loginLink){
			DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
		}
	}

	private void doCloseDetailView(UserRequest ureq) {
		// REVIEW:pb:note:handles jumps from Catalog and Course
		if (jumpfromcourse && repositoryEntry.getCanLaunch()) {
			doLaunch(ureq);
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
	void doLaunch(UserRequest ureq) {
		RepositoryHandler typeToLaunch = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (typeToLaunch == null){
			StringBuilder sb = new StringBuilder(translate("error.launch"));
			sb.append(": No launcher for repository entry: ");
			sb.append(repositoryEntry.getKey());
			throw new OLATRuntimeException(RepositoryDetailsController.class,sb.toString(), null);
		}
		if (RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey()) == null) {
			showInfo("info.entry.deleted");
			return;
		}
		
		RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntry);
		OLATResourceable ores = repositoryEntry.getOlatResource();
		String displayName = getDisplayName(ureq.getLocale());

		//was brasato:: DTabs dts = getWindowControl().getDTabs();
		DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, displayName);
			if (dt == null) return;
			
			// build up the context path
			OLATResourceable businessOres = repositoryEntry;
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, dt.getWindowControl());
			
			Controller ctrl = typeToLaunch.createLaunchController(ores, null, ureq, bwControl);
			// if resource is an image, PDF or eq. (e.g. served by resulting media request), no controller is returned.
			// FIXME:fj:test this
			if (ctrl == null) return;
			dt.setController(ctrl);
			dts.addDTab(dt);
		}
		dts.activate(ureq, dt, RepositoryDetailsController.ACTIVATE_RUN);	
		/**
		 * close detail page after resource is closed
		 * DONE_EVENT will be catched by RepositoryMainController
		 */ 
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private boolean checkIsRepositoryEntryLaunchable(UserRequest ureq) {
		RepositoryHandler type = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		RepositoryManager rm = RepositoryManager.getInstance();
		
		if (rm.isAllowedToLaunch(ureq, repositoryEntry) || (type.supportsLaunch(repositoryEntry) && ureq.getUserSession().getRoles().isOLATAdmin())) {
			return true;
		}// else if(!rm.isAllowedToLaunch(ureq, repositoryEntry)){
			return false;
		//}
		// OLAT-5571: noticed while reviewing OLAT-5571: below code was never called due to if statement above
/*		if (type instanceof CourseHandler) {
			// course
			ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
			CourseNode rootNode = course.getRunStructure().getRootNode();
			UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course
					.getCourseEnvironment());
			NodeEvaluation nodeEval = rootNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());
			boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(nodeEval);
			if (!mayAccessWholeTreeUp || !nodeEval.isVisible()) {
				String explan = rootNode.getNoAccessExplanation();
				String sExplan = (explan == null ? translate("launch.noaccess") : Formatter.formatLatexFormulas(explan));
				main.contextPut("disabledlaunchreason", sExplan);
				return false;
			}
		}
		return repositoryEntry.getCanLaunch(); */
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
	private void doEditSettings(UserRequest ureq, Controller contentController) {
	  if (!isAuthor) throw new OLATSecurityException("Trying to edit properties , but user is not author: user = " + ureq.getIdentity());
	 
	  Component component = contentController.getInitialComponent();
	  
	  if(component!=null) {
	  	removeAsListenerAndDispose(settingsCloseableModalController);
	    settingsCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"),
			  contentController.getInitialComponent());
	    listenTo(settingsCloseableModalController);
	    
	    settingsCloseableModalController.activate();
	  }
	  return;
	}
	
	private void doAddBookmark(Controller contentController) {
		removeAsListenerAndDispose(closeableModalController);		
		closeableModalController = new CloseableModalController(getWindowControl(), translate("close"),
				contentController.getInitialComponent());
		listenTo(closeableModalController);
		
		closeableModalController.activate();
		return;
	}

	/**
	 * Also used by RepositoryMainController
	 * 
	 * @param ureq
	 */
	void doDownload(UserRequest ureq) {
		RepositoryHandler typeToDownload = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);

		if (typeToDownload == null){
			StringBuilder sb = new StringBuilder(translate("error.download"));
			sb.append(": No download handler for repository entry: ");
			sb.append(repositoryEntry.getKey());
			throw new OLATRuntimeException(RepositoryDetailsController.class, sb.toString(), null);
		}
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource());
		if (ores == null) {
			showError("error.download");
			return;
		}		
		boolean isAlreadyLocked = typeToDownload.isLocked(ores);
		try {			
		  lockResult = typeToDownload.acquireLock(ores, ureq.getIdentity());
		  if(lockResult==null || (lockResult!=null && lockResult.isSuccess() && !isAlreadyLocked)) {
		    MediaResource mr = typeToDownload.getAsMediaResource(ores);
		    if(mr!=null) {
		      RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntry);
		      ureq.getDispatchResult().setResultingMediaResource(mr);
		    } else {
			    showError("error.export");
			    fireEvent(ureq, Event.FAILED_EVENT);			
		    }
		  } else if(lockResult!=null && lockResult.isSuccess() && isAlreadyLocked) {
		  	showInfo("warning.course.alreadylocked.bySameUser", lockResult.getOwner().getName());
		  	lockResult = null; //invalid lock, it was already locked
		  } else {
		  	showInfo("warning.course.alreadylocked", lockResult.getOwner().getName());
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
		  
		  removeAsListenerAndDispose(copyCloseableModalController);
		  copyCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"), copyController.getInitialComponent());
		  listenTo(copyCloseableModalController);
		  
		  copyCloseableModalController.activate();				  
		} else if (lockResult!=null && lockResult.isSuccess() && isAlreadyLocked) {
			showWarning("warning.course.alreadylocked.bySameUser");
			lockResult = null;
		}	else {			  
		  showWarning("warning.course.alreadylocked", lockResult.getOwner().getName());
	  }
	}
	

	/**
	 * Also used by RepositoryMainController
	 * 
	 * @param ureq
	 */
	void doEdit(UserRequest ureq) {
		if (!isOwner) throw new OLATSecurityException("Trying to launch editor, but not allowed: user = " + ureq.getIdentity());
		RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (!typeToEdit.supportsEdit(repositoryEntry)){
			throw new AssertException("Trying to edit repository entry which has no assoiciated editor: "+ typeToEdit);
		}
				

		OLATResourceable ores = repositoryEntry.getOlatResource();
		
		//was brasato:: DTabs dts = getWindowControl().getDTabs();
		DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, repositoryEntry.getDisplayname());
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
			dts.addDTab(dt);
		}
		dts.activate(ureq, dt, RepositoryDetailsController.ACTIVATE_EDITOR);
	}


	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	private void doAddCatalog(UserRequest ureq) {
		removeAsListenerAndDispose(catalogAdddController);
		boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		if (ajax) {
			// fancy ajax tree
			catalogAdddController = new CatalogAjaxAddController(ureq, getWindowControl(), repositoryEntry);
		} else {
			// old-school selection tree
			catalogAdddController = new CatalogEntryAddController(ureq, getWindowControl(), repositoryEntry);
		}

		
		
		listenTo(catalogAdddController);
		removeAsListenerAndDispose(closeableModalController);
		closeableModalController = new CloseableModalController(getWindowControl(), "close", catalogAdddController.getInitialComponent());
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
		if (source == groupEditController) {
			if(event instanceof IdentitiesAddEvent ) { //FIXME:chg: Move into seperate RepositoryOwnerGroupController like BusinessGroupEditController ?
				IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
				RepositoryManager rm = RepositoryManager.getInstance();
				//add to group and also adds identities really added to the event.
				//this is then later used by the GroupController to determine if the 
				//model should be updated or not.
				rm.addOwners(ureq.getIdentity(),identitiesAddedEvent,repositoryEntry);
			} else if (event instanceof IdentitiesRemoveEvent) {
				IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
				RepositoryManager rm = RepositoryManager.getInstance();
        rm.removeOwners(ureq.getIdentity(),identitiesRemoveEvent.getRemovedIdentities(), repositoryEntry);
			}
			updateView(ureq);
		} else if (source == detailsToolC) {
			if (cmd.equals(ACTION_DOWNLOAD)) { // download
				doDownload(ureq);
				return;
			} else if (cmd.equals(ACTION_LAUNCH)) { // launch resource
				doLaunch(ureq);
				return;
			} else if (cmd.equals(ACTION_EDIT)) { // start editor
				doEdit(ureq);
				return;
			} else if (cmd.equals(ACTION_EDITDESC)) { // change description
				removeAsListenerAndDispose(repositoryEditDescriptionController);
				repositoryEditDescriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), repositoryEntry, false);
				listenTo(repositoryEditDescriptionController);
				doEditSettings(ureq, repositoryEditDescriptionController);
				return;
			} else if (cmd.equals(ACTION_ADD_CATALOG)) { // start add to catalog workflow
				doAddCatalog(ureq);
				return;
			} else if (cmd.equals(ACTION_EDITPROP)) { // change properties
				removeAsListenerAndDispose(repositoryEditPropertiesController);
				repositoryEditPropertiesController = new RepositoryEditPropertiesController(ureq, getWindowControl(), repositoryEntry, false);
				listenTo(repositoryEditPropertiesController);
				doEditSettings(ureq, repositoryEditPropertiesController);
				return;
			} else if (cmd.equals(ACTION_CLOSE)) {
				doCloseDetailView(ureq);
				return;
			} else if (cmd.equals(ACTION_BOOKMARK)) {
				removeAsListenerAndDispose(bookmarkController);
				bookmarkController = new AddAndEditBookmarkController(ureq, getWindowControl(), repositoryEntry.getDisplayname(), "",
						repositoryEntry, repositoryEntry.getOlatResource().getResourceableTypeName());
				listenTo(bookmarkController);
				
				doAddBookmark(bookmarkController);
				return;
			} else if (cmd.equals(ACTION_COPY)) { // copy
				if (!isAuthor) throw new OLATSecurityException("Trying to copy, but user is not author: user = " + ureq.getIdentity());
				doCopy(ureq);
				return;
			} else if (cmd.equals(ACTION_GROUPS)) { // edit authors group
				if (!isOwner) throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + ureq.getIdentity());
				ownerGroup = repositoryEntry.getOwnerGroup();
				
				removeAsListenerAndDispose(groupEditController);
				groupEditController = new GroupController(ureq, getWindowControl(), true, true, false, ownerGroup);
				listenTo(groupEditController);
				
				VelocityContainer groupContainer = createVelocityContainer("groups");  
				groupContainer.put("groupcomp", groupEditController.getInitialComponent());
				
				removeAsListenerAndDispose(cmc);
				CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), groupContainer);
				listenTo(cmc);
				
				cmc.activate();
				return;
			} else if (cmd.equals(ACTION_CLOSE_RESSOURCE)) {
				doCloseResource(ureq);
				return;
			} else if (cmd.equals(ACTION_DELETE)) { // delete
				if (!isOwner) throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
				// show how many users are currently using this resource
				OLATResourceable ores = repositoryEntry.getOlatResource();
				
				String dialogTitle = translate("del.header", repositoryEntry.getDisplayname());
				OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource().getResourceableId());
				int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
				
				String dialogText = translate("del.confirm", String.valueOf(cnt));
				deleteDialogController = activateYesNoDialog(ureq, dialogTitle, dialogText, deleteDialogController);
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
		} else if (source == bookmarkController) {
			closeableModalController.deactivate();
			if (event.equals(Event.DONE_EVENT)) { // bookmark added... remove tool
				if (detailsToolC != null) {
					detailsToolC.setEnabled(TOOL_BOOKMARK, false);
				}
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
			if (event == Event.CHANGED_EVENT) {
				// RepositoryEntry changed
				// setEntry(repositoryEditDescriptionController.getRepositoryEntry(), ureq);
				this.repositoryEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEditDescriptionController.getRepositoryEntry()); // need a reload from hibernate because create a new cp load a repository-entry (OLAT-5631) TODO: 7.1 Refactor in method getRepositoryEntry()
				this.repositoryEntry.setDisplayname(repositoryEditDescriptionController.getRepositoryEntry().getDisplayname());
				this.repositoryEntry.setDescription(repositoryEditDescriptionController.getRepositoryEntry().getDescription());
				RepositoryManager.getInstance().updateRepositoryEntry(this.repositoryEntry);
				// do not close upon save/upload image closeableModalController.deactivate();
				updateView(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				settingsCloseableModalController.deactivate();
				removeAsListenerAndDispose(repositoryEditDescriptionController);
				this.repositoryEntry = repositoryEditDescriptionController.getRepositoryEntry();
			}
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