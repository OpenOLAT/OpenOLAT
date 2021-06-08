/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.repository.ui.author.ConfirmCloseController;
import org.olat.repository.ui.author.ConfirmDeleteSoftlyController;
import org.olat.repository.ui.author.ConfirmRestoreController;
import org.olat.repository.ui.author.RepositoryMembersController;
import org.olat.repository.ui.author.copy.CopyRepositoryEntryWrapperController;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.AccessListController;
import org.olat.resource.accesscontrol.ui.AccessRefusedController;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRuntimeController extends MainLayoutBasicController implements Activateable2, GenericEventListener {


	private Controller runtimeController;
	protected final TooledStackedPanel toolbarPanel;
	private final RuntimeControllerCreator runtimeControllerCreator;
	
	protected Controller editorCtrl;
	protected Controller currentToolCtr;
	private CloseableModalController cmc;
	protected Controller accessController;
	private OrdersAdminController ordersCtlr;
	private CopyRepositoryEntryWrapperController copyWrapperCtrl;
	private ConfirmCloseController confirmCloseCtrl;
	private ConfirmRestoreController confirmRestoreCtrl;
	private ConfirmDeleteSoftlyController confirmDeleteCtrl;
	private RepositoryEntryDetailsController detailsCtrl;
	private RepositoryMembersController membersEditController;
	protected RepositoryEntrySettingsController settingsCtrl;
	
	private Dropdown tools;
	private Dropdown status;
	protected Link editLink;
	protected Link membersLink;
	protected Link ordersLink;
	protected Link detailsLink;
	protected Link bookmarkLink;
	protected Link copyLink;
	protected Link downloadLink;
	protected Link deleteLink;
	protected Link settingsLink;
	protected Link restoreLink;
	
	private Dropdown rolesDropdown;
	private Link ownerLink;
	private Link administratorLink;
	private Link learningResourceManagerLink;
	private Link coachLink;
	private Link principalLink;
	private Link masterCoachLink;
	private Link participantLink;
	
	private Link preparationLink;
	private Link reviewLink;
	private Link coachPublishLink;
	private Link publishLink;
	private Link closeLink;
	
	protected final boolean isGuestOnly;
	protected final boolean isAuthor;
	
	protected SingleRoleRepositoryEntrySecurity reSecurity;
	protected final Roles roles;

	protected final boolean showDetails;
	protected final boolean allowBookmark;
	
	protected boolean corrupted;
	protected boolean settingsChanged;
	protected boolean overrideReadOnly = false;
	protected final String businessPathEntry;
	private RepositoryEntry re;
	private List<OrganisationRef> organisations;
	private LockResult lockResult;
	private boolean assessmentLock;// by Assessment mode
	private AssessmentMode assessmentMode;
	protected final RepositoryHandler handler;
	private AtomicBoolean launchDateUpdated = new AtomicBoolean(false);
	
	private final EventBus eventBus;
	private HistoryPoint launchedFromPoint;
	
	@Autowired
	protected ACService acService;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected MarkManager markManager;
	@Autowired
	protected RepositoryModule repositoryModule;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private UserCourseInformationsManager userCourseInfoMgr;
	
	public RepositoryEntryRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		this(ureq, wControl, re, reSecurity, runtimeControllerCreator, true, true);
	}

	public RepositoryEntryRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator, boolean allowBookmark, boolean showDetails) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable
				.wrapBusinessPath(OresHelper.createOLATResourceableType("RepositorySite")));
		
		//! check corrupted
		corrupted = isCorrupted(re);
		
		businessPathEntry = "[RepositoryEntry:" + re.getKey() + "]";

		UserSession session = ureq.getUserSession();
		Object wcard = session.removeEntry("override_readonly_" + re.getKey());
		if(Boolean.TRUE.equals(wcard)) {
			overrideReadOnly = true;
		}
		
		roles = session.getRoles();
		isGuestOnly = roles.isGuestOnly();
		isAuthor = reSecurity.isAuthor();
		this.reSecurity = new SingleRoleRepositoryEntrySecurity(reSecurity);
		
		assessmentLock = isAssessmentLock(ureq, re, this.reSecurity);
		
		this.re = re;
		this.showDetails = showDetails;
		this.allowBookmark = allowBookmark;
		this.runtimeControllerCreator = runtimeControllerCreator;
		organisations = repositoryService.getOrganisationReferences(re);
		
		if(assessmentLock) {
			TransientAssessmentMode mode = session.getLockMode();
			assessmentMode = assessmentModeMgr.getAssessmentModeById(mode.getModeKey());
		}
		
		if(session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
			// Set previous business path as back link for this course - brings user back to place from which he launched the course
			List<HistoryPoint> stack = session.getHistoryStack();
			for(int i=stack.size() - 2; i-->0; ) {
				HistoryPoint point = stack.get(stack.size() - 2);
				if(!point.getEntries().isEmpty()) {
					OLATResourceable ores = point.getEntries().get(0).getOLATResourceable();
					if(!OresHelper.equals(re, ores) && !OresHelper.equals(re.getOlatResource(), ores)) {
						launchedFromPoint = point;
						break;
					}
				}
			}
		}
		
		handler = handlerFactory.getRepositoryHandler(re);

		// set up the components
		toolbarPanel = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root (course) level
		
		boolean ltiLaunched = isLtiLaunched(ureq);
		boolean showCloseLink = !assessmentLock && !ltiLaunched;
		toolbarPanel.setShowCloseLink(showCloseLink, showCloseLink);
		toolbarPanel.getBackLink().setEnabled(showCloseLink);
		putInitialPanel(toolbarPanel);
		onSecurityReloaded(ureq);
		doRun(ureq, this.reSecurity);
		initToolbar();
		
		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	protected boolean isCorrupted(RepositoryEntry entry) {
		return entry == null;
	}
	
	protected final boolean isAssessmentLock() {
		return assessmentLock;
	}
	
	private final boolean isAssessmentLock(UserRequest ureq, RepositoryEntry entry, RepositoryEntrySecurity reSec) {
		OLATResource resource = entry.getOlatResource();
		OLATResourceable lock = ureq.getUserSession().getLockResource();
		return lock != null && !reSec.isOwner() && !reSec.isEntryAdmin()
				&& lock.getResourceableId().equals(resource.getResourceableId())
				&& lock.getResourceableTypeName().equals(resource.getResourceableTypeName());
	}
	
	protected boolean isLtiLaunched(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		return usess != null && usess.getSessionInfo() != null && "LTI".equalsIgnoreCase(usess.getSessionInfo().getAuthProvider());
	}
	
	protected final void reloadSecurity(UserRequest ureq) {
		reSecurity.setWrappedSecurity(repositoryManager.isAllowed(ureq, getRepositoryEntry()));
		onSecurityReloaded(ureq);
		initToolbar();
	}
	
	//ureq my be used by sub controller
	protected void onSecurityReloaded(@SuppressWarnings("unused") UserRequest ureq) {
		//
	}
	
	protected RepositoryEntry getRepositoryEntry() {
		return re;
	}
	
	protected RepositoryEntry loadRepositoryEntry() {
		re = repositoryService.loadByKey(re.getKey());
		organisations = repositoryService.getOrganisationReferences(re);
		return re;
	}
	
	protected RepositoryEntry refreshRepositoryEntry(RepositoryEntry refreshedEntry) {
		re = refreshedEntry;
		return re;
	}
	
	protected List<OrganisationRef> getOrganisations() {
		return organisations;
	}
	
	protected OLATResourceable getOlatResourceable() {
		return OresHelper.clone(re.getOlatResource());
	}
	
	protected Controller getRuntimeController() {
		return runtimeController;
	}

	protected final void initToolbar() {
		toolbarPanel.removeAllTools();
		
		tools = new Dropdown("toolbox.tools", "toolbox.tools", false, getTranslator());
		tools.setElementCssClass("o_sel_repository_tools");
		tools.setIconCSS("o_icon o_icon_tools");
		
		initRole();
		initToolbar(tools);
		
		if(tools.size() > 0) {
			toolbarPanel.addTool(tools, Align.left, true);
		}
		
		if (reSecurity.isEntryAdmin()) {
			status = new Dropdown("toolbox.status", "cif.status", false, getTranslator());
			status.setElementCssClass("o_sel_repository_status");
			status.setIconCSS("o_icon o_icon_edit");
			initStatus(status);
			toolbarPanel.addTool(status, Align.left, false);
		}
		
		toolbarPanel.setDirty(true);
	}

	protected void reloadStatus() {
		if(status != null) {
			status.removeAllComponents();
			initStatus(status);
		}
	}

	protected void initStatus(Dropdown statusDropdown) {
		RepositoryEntry entry = getRepositoryEntry();
		RepositoryEntryStatusEnum entryStatus = entry.getEntryStatus();
		statusDropdown.setI18nKey("details.label.status");
		statusDropdown.setElementCssClass("o_repo_tools_status o_with_labeled");
		statusDropdown.setIconCSS("o_icon o_icon_repo_status_".concat(entryStatus.name()));
		statusDropdown.setInnerText(translate(entryStatus.i18nKey()));
		statusDropdown.setInnerCSS("o_labeled o_repo_status_".concat(entryStatus.name()));
		
		if(entryStatus == RepositoryEntryStatusEnum.preparation || entryStatus == RepositoryEntryStatusEnum.review
				|| entryStatus == RepositoryEntryStatusEnum.coachpublished || entryStatus == RepositoryEntryStatusEnum.published
				|| entryStatus == RepositoryEntryStatusEnum.closed) {
			preparationLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.preparation, entryStatus);
			reviewLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.review, entryStatus);
			coachPublishLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.coachpublished, entryStatus);
			publishLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.published, entryStatus);
			if(!RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.close)) {
				closeLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.closed, entryStatus);
			}
		}
	}
	
	protected Link initStatus(Dropdown statusDropdown, RepositoryEntryStatusEnum entryStatus, RepositoryEntryStatusEnum currentStatus) {
		Link statusLink = LinkFactory.createToolLink("status.".concat(entryStatus.name()), translate(entryStatus.i18nKey()), this);
		statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_repo_status_".concat(entryStatus.name()));
		statusLink.setElementCssClass("o_labeled o_repo_status_".concat(entryStatus.name()));
		statusLink.setVisible(entryStatus != currentStatus);
		statusDropdown.addComponent(statusLink);
		return statusLink;
	}
	
	private void initRole() {
		rolesDropdown = new Dropdown("toolbox.roles", "role.switch", false, getTranslator());
		rolesDropdown.setElementCssClass("o_sel_switch_role o_with_labeled");
		rolesDropdown.setIconCSS("o_icon " + reSecurity.getCurrentRole().getIconCssClass());
		rolesDropdown.setInnerText(translate(reSecurity.getCurrentRole().getI18nKey()));
		rolesDropdown.setInnerCSS("o_labeled");
		
		Collection<Role> otherRoles = reSecurity.getOtherRoles();
		if (otherRoles.contains(Role.owner)) {
			ownerLink = LinkFactory.createToolLink("role.owner", translate(Role.owner.getI18nKey()), this);
			ownerLink.setIconLeftCSS("o_icon o_icon-fw " + Role.owner.getIconCssClass());
			ownerLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(ownerLink);
		}
		if (otherRoles.contains(Role.administrator)) {
			administratorLink = LinkFactory.createToolLink("role.administrator", translate(Role.administrator.getI18nKey()), this);
			administratorLink.setIconLeftCSS("o_icon o_icon-fw " + Role.administrator.getIconCssClass());
			administratorLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(administratorLink);
		}
		if (otherRoles.contains(Role.learningResourceManager)) {
			learningResourceManagerLink = LinkFactory.createToolLink("role.learning.resource.manager", translate(Role.learningResourceManager.getI18nKey()), this);
			learningResourceManagerLink.setIconLeftCSS("o_icon o_icon-fw " + Role.learningResourceManager.getIconCssClass());
			learningResourceManagerLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(learningResourceManagerLink);
		}
		if (otherRoles.contains(Role.coach)) {
			coachLink = LinkFactory.createToolLink("role.coach", translate(Role.coach.getI18nKey()), this);
			coachLink.setIconLeftCSS("o_icon o_icon-fw " + Role.coach.getIconCssClass());
			coachLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(coachLink);
		}
		if (otherRoles.contains(Role.principal)) {
			principalLink = LinkFactory.createToolLink("role.principal", translate(Role.principal.getI18nKey()), this);
			principalLink.setIconLeftCSS("o_icon o_icon-fw " + Role.principal.getIconCssClass());
			principalLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(principalLink);
		}
		if (otherRoles.contains(Role.masterCoach)) {
			masterCoachLink = LinkFactory.createToolLink("role.master.coach", translate(Role.masterCoach.getI18nKey()), this);
			masterCoachLink.setIconLeftCSS("o_icon o_icon-fw " + Role.masterCoach.getIconCssClass());
			masterCoachLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(masterCoachLink);
		}
		if (otherRoles.contains(Role.participant)) {
			participantLink = LinkFactory.createToolLink("role.participant", translate(Role.participant.getI18nKey()), this);
			participantLink.setIconLeftCSS("o_icon o_icon-fw " + Role.participant.getIconCssClass());
			participantLink.setElementCssClass("o_labeled o_repo_role");
			rolesDropdown.addComponent(participantLink);
		}
		if (rolesDropdown.size() > 0) {
			toolbarPanel.addTool(rolesDropdown, Align.right);
		}
	}

	protected void initToolbar(Dropdown toolsDropdown) {
		initToolsMenu(toolsDropdown);

		detailsLink = LinkFactory.createToolLink("details", translate("details.header"), this, "o_sel_repo_details");
		detailsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
		detailsLink.setElementCssClass("o_sel_author_details");
		detailsLink.setVisible(showDetails);
		toolbarPanel.addTool(detailsLink);
		
		boolean marked = markManager.isMarked(re, getIdentity(), null);
		String css = marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON;
		bookmarkLink = LinkFactory.createToolLink("bookmark", translate("details.bookmark.label"), this, css);
		bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		bookmarkLink.setVisible(allowBookmark);
		toolbarPanel.addTool(bookmarkLink, Align.right);
	}
	
	protected void initToolsMenu(Dropdown toolsDropdown) {
		toolsDropdown.removeAllComponents();
		
		initToolsMenuSettings(toolsDropdown);
		initToolsMenuEditor(toolsDropdown);
		initToolsMenuRuntime(toolsDropdown);
		initToolsMenuEdition(toolsDropdown);
		initToolsMenuDelete(toolsDropdown);
	}
	
	protected void initToolsMenuSettings(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			settingsLink = LinkFactory.createToolLink("settings", translate("details.settings"), this, "o_sel_repo_settings");
			settingsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Settings:0][Info:0]"));
			settingsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_settings");
			settingsLink.setElementCssClass("o_sel_repo_settings");
			toolsDropdown.addComponent(settingsLink);
			
			membersLink = LinkFactory.createToolLink("members", translate("details.members"), this, "o_sel_repo_members");
			membersLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[MembersMgmt:0]]"));
			membersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_membersmanagement");
			toolsDropdown.addComponent(membersLink);
		}
	}
	
	protected void initToolsMenuEditor(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin() && handler.supportsEdit(re.getOlatResource(), getIdentity(), roles) == EditionSupport.yes) {
			toolsDropdown.addComponent(new Spacer("editors-tools"));
			
			boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
			editLink = LinkFactory.createToolLink("edit.cmd", translate("details.openeditor"), this, "o_sel_repository_editor");
			editLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Editor:0]]"));
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			editLink.setEnabled(!managed);
			toolsDropdown.addComponent(editLink);
		}
	}
	
	protected void initToolsMenuRuntime(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Booking:0]]"));
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = acService.isResourceAccessControled(re.getOlatResource(), null);
			ordersLink.setEnabled(booking);
			toolsDropdown.addComponent(ordersLink);	
		}
	}
	
	protected void initToolsMenuEdition(Dropdown toolsDropdown) {
		boolean copyManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.copy);
		boolean canCopy = (isAuthor || reSecurity.isEntryAdmin()) && (re.getCanCopy() || reSecurity.isEntryAdmin()) && !copyManaged;
		
		boolean canDownload = re.getCanDownload() && handler.supportsDownload();
		// disable download for courses if not author or owner
		if (re.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(reSecurity.isEntryAdmin() || isAuthor)) {
			canDownload = false;
		}
		// always enable download for owners
		if (reSecurity.isEntryAdmin() && handler.supportsDownload()) {
			canDownload = true;
		}
		
		if(canCopy || canDownload) {
			if(toolsDropdown.size() > 0) {
				toolsDropdown.addComponent(new Spacer("copy-download"));
			}
			if (canCopy) {
				copyLink = LinkFactory.createToolLink("copy", translate("details.copy"), this, "o_icon o_icon-fw o_icon_copy");
				copyLink.setElementCssClass("o_sel_repo_copy");
				toolsDropdown.addComponent(copyLink);
			}
			if(canDownload) {
				downloadLink = LinkFactory.createToolLink("download", translate("details.download"), this, "o_icon o_icon-fw o_icon_download");
				downloadLink.setElementCssClass("o_sel_repo_download");
				toolsDropdown.addComponent(downloadLink);
			}
		}
	}
	
	protected void initToolsMenuDelete(Dropdown toolsDropdown) {
		boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.delete);
		if(reSecurity.isEntryAdmin() && !deleteManaged) {
			toolsDropdown.addComponent(new Spacer("close-delete"));
			
			if(re.getEntryStatus() == RepositoryEntryStatusEnum.deleted || re.getEntryStatus() == RepositoryEntryStatusEnum.trash) {
				restoreLink = LinkFactory.createToolLink("restore", translate("details.restore"), this, "o_icon o_icon-fw o_icon_restore");
				restoreLink.setElementCssClass("o_sel_repo_restore");
				toolsDropdown.addComponent(restoreLink);
			} else {
				String type = translate(handler.getSupportedType());
				String deleteTitle = translate("details.delete.alt", new String[]{ type });
				deleteLink = LinkFactory.createToolLink("delete", deleteTitle, this, "o_icon o_icon-fw o_icon_delete_item");
				deleteLink.setElementCssClass("o_sel_repo_close");
				toolsDropdown.addComponent(deleteLink);
			}
		}
	}
	
	public void setActiveTool(Link tool) {
		if(tools != null) {
			tools.setActiveLink(tool);
		}
	}
	
	@Override
	public CustomCSS getCustomCSS() {
		return runtimeController instanceof MainLayoutController ? ((MainLayoutController)runtimeController).getCustomCSS() : null;
	}

	@Override
	public void setCustomCSS(CustomCSS newCustomCSS) {
		if(runtimeController instanceof MainLayoutController) {
			((MainLayoutController)runtimeController).setCustomCSS(newCustomCSS);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Editor".equalsIgnoreCase(type)) {
				if(handler.supportsEdit(re, ureq.getIdentity(), ureq.getUserSession().getRoles()) == EditionSupport.yes
						&& re.getEntryStatus() != RepositoryEntryStatusEnum.closed) {
					doEdit(ureq);
				}
			} else if("MembersMgmt".equalsIgnoreCase(type)) {
				doMembers(ureq);
			} else if("Settings".equalsIgnoreCase(type) || "EditDescription".equalsIgnoreCase(type)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doSettings(ureq, subEntries);
			} else if("Infos".equalsIgnoreCase(type)) {
				doDetails(ureq);	
			}
		}

		if(runtimeController instanceof Activateable2) {
			((Activateable2)runtimeController).activate(ureq, entries, state);
		}
	}
	
	protected void activateSubEntries(UserRequest ureq, Activateable2 ctrl, List<ContextEntry> entries) {
		if(ctrl == null || entries == null) return;
		try {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			ctrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} catch (OLATSecurityException e) {
			//the wrong link to the wrong person
		}
	}
	
	protected List<ContextEntry> removeRepositoryEntry(List<ContextEntry> entries) {
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("RepositoryEntry".equals(type)) {
				if(entries.size() > 1) {
					entries = entries.subList(1, entries.size());
				} else {
					entries = Collections.emptyList();
				}
			}
		}
		return entries;
	}
	
	protected WindowControl getSubWindowControl(String name) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(name, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		return BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
	}

	@Override
	protected void doDispose() {
		if(runtimeController != null && !runtimeController.isDisposed()) {
			runtimeController.dispose();
		}
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
	}

	@Override
	public void event(Event event) {
		//
	}
	
	protected void processClosedUnclosedEvent(UserRequest ureq) {
		loadRepositoryEntry();
		reloadSecurity(ureq);
		toolbarPanel.popUpToRootController(ureq);
	}
	
	protected void processReloadSettingsEvent(ReloadSettingsEvent event) {
		if(event.isChangedTitle()) {
			RepositoryEntry entry = repositoryService.loadByKey(getRepositoryEntry().getKey());
			refreshRepositoryEntry(entry);
			handler.onDescriptionChanged(entry, getIdentity());
			// update name of root bread crumb and opened tabs in top nav in case the title has been modified
			if (!toolbarPanel.getBreadCrumbs().isEmpty()) {					
				String newTitle = entry.getDisplayname();
				String oldTitle = toolbarPanel.getBreadCrumbs().get(0).getCustomDisplayText();
				if (!newTitle.equals(oldTitle)) {						
					// 1: update breadcrumb in toolbar
					toolbarPanel.getBreadCrumbs().get(0).setCustomDisplayText(StringHelper.escapeHtml(newTitle));
					// 2: update dynamic tab in topnav
					OLATResourceable reOres = OresHelper.clone(entry);
					getWindowControl().getWindowBackOffice().getWindow().getDTabs().updateDTabTitle(reOres, newTitle);
				}
			}
		}
		if(event.isChangedToolbar()) {
			RepositoryEntry entry = repositoryService.loadByKey(getRepositoryEntry().getKey());
			refreshRepositoryEntry(entry);
			initToolsMenu(tools);
		}
		if(event.isChangedStatus()) {
			reloadStatus();
		}
		settingsChanged = true;
	}
	
	protected void processEntryChangedEvent(EntryChangedEvent repoEvent) {
		if(repoEvent.isMe(getIdentity()) &&
				(repoEvent.getChange() == Change.addBookmark || repoEvent.getChange() == Change.removeBookmark)) {
			if(bookmarkLink != null) {
				boolean marked = markManager.isMarked(OresHelper.clone(re), getIdentity(), null);
				String css = "o_icon " + (marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
				bookmarkLink.setIconLeftCSS(css);
				bookmarkLink.setTitle( translate(marked ? "details.bookmark.remove" : "details.bookmark"));
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == runtimeController) {
			fireEvent(ureq, event);
		} else if(editLink == source) {
			doEdit(ureq);
		} else if(membersLink == source) {
			doMembers(ureq);
		} else if(settingsLink == source) {
			doSettings(ureq, null);
		} else if(ordersLink == source) {
			doOrders(ureq);
		} else if(detailsLink == source) {
			doDetails(ureq);
		} else if(bookmarkLink == source) {
			boolean marked = doMark(ureq);
			String css = "o_icon " + (marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
			bookmarkLink.setIconLeftCSS(css);
			bookmarkLink.setTitle( translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		} else if(copyLink == source) {
			doCopy(ureq);
		} else if(downloadLink == source) {
			doDownload(ureq);
		} else if(deleteLink == source) {
			doDelete(ureq);
		} else if(restoreLink == source) {
			doConfirmRestore(ureq);
		} else if (ownerLink == source) {
			doSwitchRole(ureq, Role.owner);
		} else if (administratorLink == source) {
			doSwitchRole(ureq, Role.administrator);
		} else if (learningResourceManagerLink == source) {
			doSwitchRole(ureq, Role.learningResourceManager);
		} else if (coachLink == source) {
			doSwitchRole(ureq, Role.coach);
		} else if (principalLink == source) {
			doSwitchRole(ureq, Role.principal);
		} else if (masterCoachLink == source) {
			doSwitchRole(ureq, Role.masterCoach);
		} else if (participantLink == source) {
			doSwitchRole(ureq, Role.participant);
		} else if(preparationLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.preparation);
		} else if(reviewLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.review);
		} else if(coachPublishLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.coachpublished);
		} else if(publishLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.published);
		} else if(closeLink == source) {
			doConfirmCloseResource(ureq);
		} else if(source == toolbarPanel) {
			if (event == Event.CLOSE_EVENT) {
				doClose(ureq);
			} else if(event instanceof PopEvent) {
				setActiveTool(null);
			}
		}
	}
	
	protected void processPopEvent(UserRequest ureq, PopEvent pop) {
		if(pop.getController() == settingsCtrl && settingsChanged) {
			RepositoryEntry entry = repositoryService.loadByKey(getRepositoryEntry().getKey());
			refreshRepositoryEntry(entry);
			reloadSecurity(ureq);
			settingsChanged = false;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				doPostSuccessfullAccess(ureq);
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		} else if(settingsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				//
			} else if(event == Event.CLOSE_EVENT) {
				doClose(ureq);
			} else if(event instanceof ReloadSettingsEvent) {
				processReloadSettingsEvent((ReloadSettingsEvent)event);
			} else if (event == RepositoryEntryLifeCycleChangeController.closedEvent
					|| event == RepositoryEntryLifeCycleChangeController.unclosedEvent) {
				processClosedUnclosedEvent(ureq);
			}
		} else if(detailsCtrl == source) {
			if(event instanceof LeavingEvent) {
				doClose(ureq);
			} else if(event == Event.DONE_EVENT) {
				popToRoot(ureq);
				cleanUp();
				if(getRuntimeController() == null) {
					doRun(ureq, reSecurity);
				}
			}
		} else if(confirmDeleteCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				removeAsListenerAndDispose(confirmDeleteCtrl);
				removeAsListenerAndDispose(cmc);
				confirmDeleteCtrl = null;
				cmc = null;
			} else if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cmc.deactivate();
				
				EntryChangedEvent e = new EntryChangedEvent(getRepositoryEntry(), getIdentity(), Change.deleted, "runtime");
				ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);

				doClose(ureq);
				cleanUp();
			}
		} else if (source == copyWrapperCtrl) {
			cleanUp();
		} else if(confirmRestoreCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				doRestore(ureq);
			}
		} else if(confirmCloseCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				doCloseResource(ureq);
			}
		}
	}
	
	protected RepositoryEntryRuntimeController popToRoot(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		return this;
	}
	
	protected void cleanUp() {		
		removeAsListenerAndDispose(membersEditController);
		removeAsListenerAndDispose(confirmRestoreCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(accessController);
		removeAsListenerAndDispose(confirmCloseCtrl);
		removeAsListenerAndDispose(copyWrapperCtrl);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(editorCtrl);
		removeAsListenerAndDispose(ordersCtlr);
		removeAsListenerAndDispose(cmc);
		
		membersEditController = null;
		confirmRestoreCtrl = null;
		confirmDeleteCtrl = null;
		accessController = null;
		confirmCloseCtrl = null;
		copyWrapperCtrl = null;
		detailsCtrl = null;
		editorCtrl = null;
		ordersCtlr = null;
		cmc = null;
	}
	
	/**
	 * Pop to root, clean up, and push
	 * @param ureq
	 * @param name
	 * @param controller
	 */
	protected <T extends Controller> T pushController(UserRequest ureq, String name, T controller) {
		popToRoot(ureq).cleanUp();
		toolbarPanel.pushController(name, controller);
		if(controller instanceof ToolbarAware) {
			((ToolbarAware)controller).initToolbar();
		}
		return controller;
	}
	
	protected void doPostSuccessfullAccess(UserRequest ureq) {
		reloadSecurity(ureq);
		launchContent(ureq);
		cleanUp();
		initToolbar();
	}
	
	protected final void doChangeStatus(UserRequest ureq, RepositoryEntryStatusEnum updatedStatus) {
		RepositoryEntry entry = getRepositoryEntry();
		RepositoryEntry reloadedEntry = repositoryManager.setStatus(entry, updatedStatus);
		refreshRepositoryEntry(reloadedEntry);
		reloadSecurity(ureq);

		EntryChangedEvent e = new EntryChangedEvent(reloadedEntry, getIdentity(), Change.modifiedAccess, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
	}

	private void doSwitchRole(UserRequest ureq, Role role) {
		reSecurity.setCurrentRole(role);
		onSecurityReloaded(ureq);
		initToolbar();
	}
	
	private void doConfirmCloseResource(UserRequest ureq) {
		if (!reSecurity.isEntryAdmin()) {
			throw new OLATSecurityException("Trying to close, but not allowed: user = " + ureq.getIdentity());
		}

		List<RepositoryEntry> entryToClose = Collections.singletonList(re);
		confirmCloseCtrl = new ConfirmCloseController(ureq, getWindowControl(), entryToClose);
		listenTo(confirmCloseCtrl);
		
		String title = translate("read.only.header", re.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), "close", confirmCloseCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * Remove close and edit tools, if in edit mode, pop-up-to root
	 * @param ureq
	 */
	private void doCloseResource(UserRequest ureq) {
		doChangeStatus(ureq, RepositoryEntryStatusEnum.closed); 
		
		fireEvent(ureq, RepositoryEntryLifeCycleChangeController.closedEvent);
		EntryChangedEvent e = new EntryChangedEvent(re, getIdentity(), Change.closed, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	protected void doClose(UserRequest ureq) {
		// Now try to go back to place that is attacked to (optional) root back business path
		getWindowControl().getWindowBackOffice().getWindow().getDTabs()
			.closeDTab(ureq, re.getOlatResource(), launchedFromPoint);
	}
	
	protected void doEdit(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return;
		
		WindowControl bwControl = getSubWindowControl("Editor");
		Controller ctrl = handler.createEditorController(re, ureq, addToHistory(ureq, bwControl), toolbarPanel);
		if(ctrl != null) {
			listenTo(ctrl);
			editorCtrl = pushController(ureq, translate("resource.editor"), ctrl);
			currentToolCtr = editorCtrl;
			setActiveTool(editLink);
		}
	}
	
	protected void doDetails(UserRequest ureq) {
		WindowControl bwControl = getSubWindowControl("Infos");
		
		RepositoryEntry entry = loadRepositoryEntry();
		RepositoryEntryDetailsController ctrl = new RepositoryEntryDetailsController(ureq, addToHistory(ureq, bwControl), entry, true);
		listenTo(ctrl);
		detailsCtrl = pushController(ureq, translate("details.header"), ctrl);
		currentToolCtr = detailsCtrl;
	}
	
	protected Activateable2 doSettings(UserRequest ureq, List<ContextEntry> entries) {
		if(!reSecurity.isEntryAdmin()) return null;
		
		WindowControl bwControl = getSubWindowControl("Settings");
		RepositoryEntry refreshedEntry = loadRepositoryEntry();
		RepositoryEntrySettingsController ctrl = createSettingsController(ureq, bwControl, refreshedEntry);
			
		listenTo(ctrl);
		settingsCtrl = pushController(ureq, translate("details.settings"), ctrl);
		currentToolCtr = settingsCtrl;
		setActiveTool(settingsLink);
		settingsCtrl.activate(ureq, entries, null);
		return settingsCtrl;
	}
	
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new RepositoryEntrySettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, refreshedEntry);
	}
	
	protected Activateable2 doMembers(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return null;

		WindowControl bwControl = getSubWindowControl("MembersMgmt");
		RepositoryMembersController ctrl = new RepositoryMembersController(ureq, addToHistory(ureq, bwControl), toolbarPanel, re);
		listenTo(ctrl);
		membersEditController = pushController(ureq, translate("details.members"), ctrl);
		currentToolCtr = membersEditController;
		setActiveTool(membersLink);
		return membersEditController;
	}
	
	protected void doOrders(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return;

		WindowControl bwControl = getSubWindowControl("Booking");
		OrdersAdminController ctrl = new OrdersAdminController(ureq, addToHistory(ureq, bwControl), toolbarPanel, re.getOlatResource());
		listenTo(ctrl);
		ordersCtlr = pushController(ureq, translate("details.orders"), ctrl);
		currentToolCtr = ordersCtlr;
		setActiveTool(ordersLink);
	}
	
	private void doRun(UserRequest ureq, SingleRoleRepositoryEntrySecurity srSecurity) {
		RepositoryEntrySecurity security = srSecurity.getWrappedSecurity();
		if(security .isEntryAdmin() || security.isPrincipal() || reSecurity.isMasterCoach()) {
			launchContent(ureq);
		} else {
			// guest are allowed to see resource with BARG
			if(security.canLaunch()) {
				launchContent(ureq);
			} else if(re.isBookable() && canBook()) {
				AccessResult acResult = acService.isAccessible(re, getIdentity(), security.isMember(), false);
				if(acResult.isAccessible()) {
					launchContent(ureq);
				} else if (re != null
						&& !re.getEntryStatus().decommissioned()
						&& !acResult.getAvailableMethods().isEmpty()) {
					//try auto booking
					ACResultAndSecurity autoResult = tryAutoBooking(ureq, acResult, security);
					acResult = autoResult.getAcResult();
					security = autoResult.getSecurity();
					reloadSecurity(ureq);
					if(acResult.isAccessible()) {
						launchContent(ureq);
					} else {
						accessController = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods());
						listenTo(accessController);
						toolbarPanel.rootController(re.getDisplayname(), accessController);
					}
				} else {
					accessRefused(ureq);
				}
			} else {
				accessRefused(ureq);
			}
		}
	}
	
	private boolean canBook() {
		// need to check organization too?
		return !roles.isGuestOnly();
	}
	
	private void accessRefused(UserRequest ureq) {
		Controller ctrl = new AccessRefusedController(ureq, getWindowControl(), re);
		listenTo(ctrl);
		toolbarPanel.rootController(re.getDisplayname(), ctrl);
	}
	
	private ACResultAndSecurity tryAutoBooking(UserRequest ureq, AccessResult acResult, RepositoryEntrySecurity security) {
		if(acResult.getAvailableMethods().size() == 1) {
			OfferAccess offerAccess = acResult.getAvailableMethods().get(0);
			if(offerAccess.getOffer().isAutoBooking() && !offerAccess.getMethod().isNeedUserInteraction()) {
				acResult = acService.accessResource(getIdentity(), offerAccess, null);
				 if(acResult.isAccessible()) {
					 reloadSecurity(ureq);
				 }
			}
		}
		return new ACResultAndSecurity(acResult, security);
	}
	
	protected boolean doMark(UserRequest ureq) {
		OLATResourceable item = OresHelper.clone(re);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			
			EntryChangedEvent e = new EntryChangedEvent(getRepositoryEntry(), getIdentity(), Change.removeBookmark, "runtime");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			EntryChangedEvent e = new EntryChangedEvent(getRepositoryEntry(), getIdentity(), Change.addBookmark, "runtime");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return true;
		}
	}
	
	private void doCopy(UserRequest ureq) {
		removeAsListenerAndDispose(copyWrapperCtrl);

		copyWrapperCtrl = new CopyRepositoryEntryWrapperController(ureq, getWindowControl(), re);
		listenTo(copyWrapperCtrl);
	}
	
	private void doDownload(UserRequest ureq) {
		if (handler == null) {
			StringBuilder sb = new StringBuilder(translate("error.download"));
			sb.append(": No download handler for repository entry: ").append(
					re.getKey());
			showError(sb.toString());
			return;
		}

		RepositoryEntry entry = repositoryService.loadByKey(re.getKey());
		OLATResourceable ores = entry.getOlatResource();
		if (ores == null) {
			showError("error.download");
			return;
		}

		boolean isAlreadyLocked = handler.isLocked(ores);
		try {
			lockResult = handler.acquireLock(ores, ureq.getIdentity());
			if (lockResult == null
					|| (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
				MediaResource mr = handler.getAsMediaResource(ores);
				if (mr != null) {
					repositoryService.incrementDownloadCounter(entry);
					ureq.getDispatchResult().setResultingMediaResource(mr);
				} else {
					showError("error.export");
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			} else if (lockResult != null && lockResult.isSuccess()
					&& isAlreadyLocked) {
				String fullName = userManager.getUserDisplayName(lockResult
						.getOwner());
				showInfo("warning.course.alreadylocked.bySameUser", fullName);
				lockResult = null; // invalid lock, it was already locked
			} else {
				String fullName = userManager.getUserDisplayName(lockResult
						.getOwner());
				showInfo("warning.course.alreadylocked", fullName);
			}
		} finally {
			if ((lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
				handler.releaseLock(lockResult);
				lockResult = null;
			}
		}
	}
	
	private void doDelete(UserRequest ureq) {
		if (!reSecurity.isEntryAdmin()) {
			throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
		}

		List<RepositoryEntry> entryToDelete = Collections.singletonList(getRepositoryEntry());
		confirmDeleteCtrl = new ConfirmDeleteSoftlyController(ureq, getWindowControl(), entryToDelete, false);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("del.header", re.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmRestore(UserRequest ureq) {
		if (!reSecurity.isEntryAdmin()) {
			throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
		}
		removeAsListenerAndDispose(confirmRestoreCtrl);
		removeAsListenerAndDispose(cmc);

		List<RepositoryEntry> entriesToRestore = Collections.singletonList(getRepositoryEntry());
		confirmRestoreCtrl = new ConfirmRestoreController(ureq, getWindowControl(), entriesToRestore);
		listenTo(confirmRestoreCtrl);
		
		String title = translate("tools.restore");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRestoreCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRestore(UserRequest ureq) {
		RepositoryEntry reloadedEntry = loadRepositoryEntry();
		refreshRepositoryEntry(reloadedEntry);
		reloadSecurity(ureq);
		
		EntryChangedEvent e = new EntryChangedEvent(reloadedEntry, getIdentity(), Change.restored, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	protected void launchContent(UserRequest ureq) {
		if(corrupted) {
			runtimeController = new CorruptedCourseController(ureq, getWindowControl());
			listenTo(runtimeController);
			toolbarPanel.rootController(re.getDisplayname(), runtimeController);
		} else if(reSecurity.canLaunch()) {
			removeAsListenerAndDispose(runtimeController);
			runtimeController = runtimeControllerCreator.create(ureq, getWindowControl(), toolbarPanel, re, reSecurity, assessmentMode);
			listenTo(runtimeController);
			toolbarPanel.rootController(re.getDisplayname(), runtimeController);
			if(!launchDateUpdated.getAndSet(true)) {
				userCourseInfoMgr.updateUserCourseInformations(re.getOlatResource(), getIdentity());
			}
		} else {
			runtimeController = new AccessRefusedController(ureq, getWindowControl(), re);
			listenTo(runtimeController);
			toolbarPanel.rootController(re.getDisplayname(), runtimeController);
		}
	}
	
	protected void disposeRuntimeController() {
		if(runtimeController != null) {
			removeAsListenerAndDispose(runtimeController);
			runtimeController = null;
		}
	}

	public interface RuntimeControllerCreator {
		
		public Controller create(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
				RepositoryEntry entry, RepositoryEntrySecurity reSecurity, AssessmentMode mode);
		
	}
	
	public interface ToolbarAware {
		
		public void initToolbar();
		
	}
	
	private static class ACResultAndSecurity {
		
		private final AccessResult acResult;
		private final RepositoryEntrySecurity security;
		
		public ACResultAndSecurity(AccessResult acResult, RepositoryEntrySecurity security) {
			this.acResult = acResult;
			this.security = security;
		}

		public AccessResult getAcResult() {
			return acResult;
		}

		public RepositoryEntrySecurity getSecurity() {
			return security;
		}
	}
}