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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.NewControllerFactory;
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
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.olat.repository.ui.author.CatalogSettingsController;
import org.olat.repository.ui.author.ConfirmDeleteSoftlyController;
import org.olat.repository.ui.author.CopyRepositoryEntryController;
import org.olat.repository.ui.author.RepositoryEditDescriptionController;
import org.olat.repository.ui.author.RepositoryMembersController;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
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
	private CatalogSettingsController catalogCtlr;
	private CopyRepositoryEntryController copyCtrl;
	private ConfirmDeleteSoftlyController confirmDeleteCtrl;
	protected AuthoringEditAccessController accessCtrl;
	private RepositoryEntryDetailsController detailsCtrl;
	private RepositoryMembersController membersEditController;
	protected RepositoryEditDescriptionController descriptionCtrl;
	
	private Dropdown tools;
	private Dropdown settings;
	protected Link editLink, membersLink, ordersLink,
				 editDescriptionLink, accessLink, catalogLink,
				 detailsLink, bookmarkLink,
				 copyLink, downloadLink, deleteLink;
	
	protected final boolean isOlatAdmin;
	protected final boolean isGuestOnly;
	protected final boolean isAuthor;
	
	protected RepositoryEntrySecurity reSecurity;
	protected final Roles roles;

	protected final boolean showInfos;
	protected final boolean allowBookmark;
	
	protected boolean corrupted;
	protected boolean overrideReadOnly = false;
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
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator, boolean allowBookmark, boolean showInfos) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable
				.wrapBusinessPath(OresHelper.createOLATResourceableType("RepositorySite")));
		
		//! check corrupted
		corrupted = isCorrupted(re);
		assessmentLock = isAssessmentLock(ureq, re, reSecurity);

		UserSession session = ureq.getUserSession();
		Object wcard = session.removeEntry("override_readonly_" + re.getKey());
		if(Boolean.TRUE.equals(wcard)) {
			overrideReadOnly = true;
		}
		
		this.re = re;
		this.showInfos = showInfos;
		this.allowBookmark = allowBookmark;
		this.runtimeControllerCreator = runtimeControllerCreator;
		organisations = repositoryService.getOrganisationReferences(re);
		
		if(assessmentLock) {
			TransientAssessmentMode mode = session.getLockMode();
			assessmentMode = assessmentModeMgr.getAssessmentModeById(mode.getModeKey());
		}
		
		if(session != null &&  session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
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

		roles = session.getRoles();
		isOlatAdmin = roles.isOLATAdmin();
		isGuestOnly = roles.isGuestOnly();
		isAuthor = roles.isAuthor();
		this.reSecurity = reSecurity;

		// set up the components
		toolbarPanel = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root (course) level
		toolbarPanel.setShowCloseLink(!assessmentLock, !assessmentLock);
		toolbarPanel.getBackLink().setEnabled(!assessmentLock);
		putInitialPanel(toolbarPanel);
		doRun(ureq, reSecurity);
		loadRights(reSecurity);
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
		return lock != null && !reSec.isOwner() && !ureq.getUserSession().getRoles().isOLATAdmin()
				&& lock.getResourceableId().equals(resource.getResourceableId())
				&& lock.getResourceableTypeName().equals(resource.getResourceableTypeName());
	}
	
	/**
	 * If override, need to set isOwner and isEntryAdmin
	 */
	protected void loadRights(RepositoryEntrySecurity security) {
		this.reSecurity = security;
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
		tools = new Dropdown("toolbox.tools", "toolbox.tools", false, getTranslator());
		tools.setElementCssClass("o_sel_repository_tools");
		tools.setIconCSS("o_icon o_icon_tools");
		
		String resourceType = re.getOlatResource().getResourceableTypeName();
		String name = NewControllerFactory.translateResourceableTypeName(resourceType, getLocale());
		settings = new Dropdown("settings", "toolbox.settings", false, getTranslator());
		settings.setTranslatedLabel(name);
		settings.setElementCssClass("o_sel_course_settings");
		settings.setIconCSS("o_icon o_icon_actions");
		
		initToolbar(tools, settings);
		
		if(tools.size() > 0) {
			toolbarPanel.addTool(tools, Align.left, true);
		}
		if(settings.size() > 0) {
			toolbarPanel.addTool(settings, Align.left, true);
		}
	}
	
	protected void initToolbar(Dropdown toolsDropdown, Dropdown settingsDropdown) {
		initRuntimeTools(toolsDropdown);
		initSettingsTools(settingsDropdown);
		initEditionTools(settingsDropdown);
		initDeleteTools(settingsDropdown, true);

		detailsLink = LinkFactory.createToolLink("details", translate("details.header"), this, "o_sel_repo_details");
		detailsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
		detailsLink.setElementCssClass("o_sel_author_details");
		detailsLink.setVisible(showInfos);
		toolbarPanel.addTool(detailsLink);
		
		boolean marked = markManager.isMarked(re, getIdentity(), null);
		String css = marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON;
		bookmarkLink = LinkFactory.createToolLink("bookmark", translate("details.bookmark.label"), this, css);
		bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		bookmarkLink.setVisible(allowBookmark);
		toolbarPanel.addTool(bookmarkLink, Align.right);
	}
	
	protected void initRuntimeTools(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			//tools
			if(handler.supportsEdit(re.getOlatResource()) == EditionSupport.yes) {
				boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
				editLink = LinkFactory.createToolLink("edit.cmd", translate("details.openeditor"), this, "o_sel_repository_editor");
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
				editLink.setEnabled(!managed);
				toolsDropdown.addComponent(editLink);
			}
			
			membersLink = LinkFactory.createToolLink("members", translate("details.members"), this, "o_sel_repo_members");
			membersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_membersmanagement");
			toolsDropdown.addComponent(membersLink);
			
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = acService.isResourceAccessControled(re.getOlatResource(), null);
			ordersLink.setEnabled(booking);
			toolsDropdown.addComponent(ordersLink);	
		}
	}
	
	protected void initSettingsTools(Dropdown settingsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			//settings
			editDescriptionLink = LinkFactory.createToolLink("settings.cmd", translate("details.chdesc"), this, "o_icon_details");
			editDescriptionLink.setElementCssClass("o_sel_course_settings");
			editDescriptionLink.setEnabled(!corrupted);
			settingsDropdown.addComponent(editDescriptionLink);
			
			accessLink = LinkFactory.createToolLink("access.cmd", translate("tab.accesscontrol"), this, "o_icon_password");
			accessLink.setElementCssClass("o_sel_course_access");
			settingsDropdown.addComponent(accessLink);
			
			catalogLink = LinkFactory.createToolLink("cat", translate("details.categoriesheader"), this, "o_icon_catalog");
			catalogLink.setElementCssClass("o_sel_repo_add_to_catalog");
			catalogLink.setVisible(repositoryModule.isCatalogEnabled());
			settingsDropdown.addComponent(catalogLink);
		}
	}
	
	protected void initEditionTools(Dropdown settingsDropdown) {
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
			if(settingsDropdown.size() > 0) {
				settingsDropdown.addComponent(new Spacer("copy-download"));
			}
			if (canCopy) {
				copyLink = LinkFactory.createToolLink("copy", translate("details.copy"), this, "o_icon o_icon-fw o_icon_copy");
				copyLink.setElementCssClass("o_sel_repo_copy");
				settingsDropdown.addComponent(copyLink);
			}
			if(canDownload) {
				downloadLink = LinkFactory.createToolLink("download", translate("details.download"), this, "o_icon o_icon-fw o_icon_download");
				downloadLink.setElementCssClass("o_sel_repo_download");
				settingsDropdown.addComponent(downloadLink);
			}
		}
	}
	
	protected void initDeleteTools(Dropdown settingsDropdown, boolean needSpacer) {
		if(reSecurity.isEntryAdmin()) {
			boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.delete);
			if(needSpacer && settingsDropdown.size() > 0 && !deleteManaged) {
				settingsDropdown.addComponent(new Spacer("close-delete"));
			}
	
			if(!deleteManaged) {
				String type = translate(handler.getSupportedType());
				String deleteTitle = translate("details.delete.alt", new String[]{ type });
				deleteLink = LinkFactory.createToolLink("delete", deleteTitle, this, "o_icon o_icon-fw o_icon_delete_item");
				deleteLink.setElementCssClass("o_sel_repo_close");
				settingsDropdown.addComponent(deleteLink);
			}
		}
	}
	
	public void setActiveTool(Link tool) {
		if(tools != null) {
			tools.setActiveLink(tool);
		}
		if(settings != null) {
			settings.setActiveLink(tool);
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
		if(entries != null && entries.size() > 0) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Editor".equalsIgnoreCase(type)) {
				if(handler.supportsEdit(re) == EditionSupport.yes
						&& !repositoryManager.createRepositoryEntryStatus(re.getStatusCode()).isClosed()) {
					doEdit(ureq);
				}
			} else if("Catalog".equalsIgnoreCase(type)) {
				doCatalog(ureq);
			} else if("Infos".equalsIgnoreCase(type)) {
				doDetails(ureq);	
			} else if("EditDescription".equalsIgnoreCase(type) || "Settings".equalsIgnoreCase(type)) {
				doEditSettings(ureq);
			} else if("MembersMgmt".equalsIgnoreCase(type)) {
				doMembers(ureq);
			}
		}

		if(runtimeController instanceof Activateable2) {
			((Activateable2)runtimeController).activate(ureq, entries, state);
		}
	}
	
	protected List<ContextEntry> removeRepositoryEntry(List<ContextEntry> entries) {
		if(entries != null && entries.size() > 0) {
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
		} else if(editDescriptionLink == source) {
			doEditSettings(ureq);
		} else if(accessLink == source) {
			doAccess(ureq);
		} else if(catalogLink == source) {
			doCatalog(ureq);
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
		} else if(source == toolbarPanel) {
			if (event == Event.CLOSE_EVENT) {
				doClose(ureq);
			} else if(event instanceof PopEvent) {
				setActiveTool(null);
			}
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
		} else if(accessCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				refreshRepositoryEntry(accessCtrl.getEntry());
				if(ordersLink != null) {
					boolean booking = acService.isResourceAccessControled(re.getOlatResource(), null);
					ordersLink.setVisible(!corrupted && booking);
				}
			} else if(event == Event.CLOSE_EVENT) {
				doClose(ureq);
			}
		} else if(descriptionCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				RepositoryEntry entry = descriptionCtrl.getRepositoryEntry();
				refreshRepositoryEntry(entry);
				handler.onDescriptionChanged(entry);
				// update name of root bread crumb and opened tabs in top nav in case the title has been modified
				if (toolbarPanel.getBreadCrumbs().size() > 0) {					
					String newTitle = entry.getDisplayname();
					String oldTitle = toolbarPanel.getBreadCrumbs().get(0).getCustomDisplayText();
					if (!newTitle.equals(oldTitle)) {						
						// 1: update breadcrumb in toolbar
						toolbarPanel.getBreadCrumbs().get(0).setCustomDisplayText(newTitle);
						// 2: update dynamic tab in topnav
						OLATResourceable reOres = OresHelper.clone(entry);
						getWindowControl().getWindowBackOffice().getWindow().getDTabs().updateDTabTitle(reOres, newTitle);
					}
				}
			} else if(event == Event.CLOSE_EVENT) {
				doClose(ureq);
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
		} else if(copyCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				RepositoryEntryRef copy = copyCtrl.getCopiedEntry();
				String businessPath = "[RepositoryEntry:" + copy.getKey() + "][EditDescription:0]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				
				EntryChangedEvent e = new EntryChangedEvent(getRepositoryEntry(), getIdentity(), Change.added, "runtime");
				ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			}
			cleanUp();
		}
	}
	
	protected RepositoryEntryRuntimeController popToRoot(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		return this;
	}
	
	protected void cleanUp() {		
		removeAsListenerAndDispose(membersEditController);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(accessController);
		removeAsListenerAndDispose(descriptionCtrl);
		removeAsListenerAndDispose(catalogCtlr);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(editorCtrl);
		removeAsListenerAndDispose(ordersCtlr);
		removeAsListenerAndDispose(copyCtrl);
		removeAsListenerAndDispose(cmc);
		
		membersEditController = null;
		confirmDeleteCtrl = null;
		accessController = null;
		descriptionCtrl = null;
		catalogCtlr = null;
		detailsCtrl = null;
		editorCtrl = null;
		ordersCtlr = null;
		copyCtrl = null;
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
	
	/**
	 * Open the editor for all repository entry metadata, access control...
	 * @param ureq
	 */
	protected void doAccess(UserRequest ureq) {
		WindowControl bwControl = getSubWindowControl("Access");
		RepositoryEntry refreshedEntry = loadRepositoryEntry();
		AuthoringEditAccessController ctrl = new AuthoringEditAccessController(ureq, addToHistory(ureq, bwControl), refreshedEntry);
		listenTo(ctrl);
		accessCtrl = pushController(ureq, translate("tab.accesscontrol"), ctrl);
		setActiveTool(accessLink);
		currentToolCtr = accessCtrl;
	}
	
	protected void doPostSuccessfullAccess(UserRequest ureq) {
		reSecurity = repositoryManager.isAllowed(ureq, getRepositoryEntry());
		launchContent(ureq, reSecurity);
		initToolbar();
		cleanUp();
	}
	
	protected final void doClose(UserRequest ureq) {
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
	
	/**
	 * Open the editor for all repository entry metadata, access control...
	 * @param ureq
	 */
	protected void doEditSettings(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return;
		
		WindowControl bwControl = getSubWindowControl("Settings");
		RepositoryEntry refreshedEntry = loadRepositoryEntry();
		RepositoryEditDescriptionController ctrl
			= new RepositoryEditDescriptionController(ureq, addToHistory(ureq, bwControl), refreshedEntry);
		listenTo(ctrl);
		descriptionCtrl = pushController(ureq, translate("settings.editor"), ctrl);
		currentToolCtr = descriptionCtrl;
		setActiveTool(editDescriptionLink);
	}
	
	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	protected void doCatalog(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return;
		
		popToRoot(ureq).cleanUp();

		WindowControl bwControl = getSubWindowControl("Catalog");
		catalogCtlr = new CatalogSettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, re);
		listenTo(catalogCtlr);
		catalogCtlr.initToolbar();
		currentToolCtr = catalogCtlr;
		setActiveTool(catalogLink);
	}
	
	protected Activateable2 doMembers(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return null;

		WindowControl bwControl = getSubWindowControl("MembersMgmt");
		RepositoryMembersController ctrl = new RepositoryMembersController(ureq, addToHistory(ureq, bwControl), toolbarPanel ,re);
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
	
	private void doRun(UserRequest ureq, RepositoryEntrySecurity security) {
		if(ureq.getUserSession().getRoles().isOLATAdmin()) {
			launchContent(ureq, security);
		} else {
			// guest are allowed to see resource with BARG 
			if(re.getAccess() == RepositoryEntry.ACC_USERS_GUESTS && ureq.getUserSession().getRoles().isGuestOnly()) {
				launchContent(ureq, security);
			} else {
				AccessResult acResult = acService.isAccessible(re, getIdentity(), security.isMember(), false);
				if(acResult.isAccessible()) {
					launchContent(ureq, security);
				} else if (re != null
						&& !re.getRepositoryEntryStatus().isUnpublished()
						&& !re.getRepositoryEntryStatus().isClosed()
						&& acResult.getAvailableMethods().size() > 0) {
					//try auto booking
					ACResultAndSecurity autoResult = tryAutoBooking(ureq, acResult, security);
					acResult = autoResult.getAcResult();
					security = autoResult.getSecurity();
					if(acResult.isAccessible()) {
						launchContent(ureq, security);
					} else {
						accessController = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods());
						listenTo(accessController);
						toolbarPanel.rootController(re.getDisplayname(), accessController);
					}
				} else {
					Controller ctrl = new AccessRefusedController(ureq, getWindowControl());
					listenTo(ctrl);
					toolbarPanel.rootController(re.getDisplayname(), ctrl);
				}
			}
		}
	}
	
	private ACResultAndSecurity tryAutoBooking(UserRequest ureq, AccessResult acResult, RepositoryEntrySecurity security) {
		if(acResult.getAvailableMethods().size() == 1) {
			OfferAccess offerAccess = acResult.getAvailableMethods().get(0);
			if(offerAccess.getOffer().isAutoBooking() && !offerAccess.getMethod().isNeedUserInteraction()) {
				acResult = acService.accessResource(getIdentity(), offerAccess, null);
				 if(acResult.isAccessible()) {
					 security = repositoryManager.isAllowed(ureq, re);
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
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(copyCtrl);

		copyCtrl = new CopyRepositoryEntryController(ureq, getWindowControl(), re);
		listenTo(copyCtrl);
		
		String title = translate("details.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copyCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
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
				MediaResource mr = handler.getAsMediaResource(ores, false);
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
	
	protected void launchContent(UserRequest ureq, RepositoryEntrySecurity security) {
		if(corrupted) {
			runtimeController = new CorruptedCourseController(ureq, getWindowControl());
			listenTo(runtimeController);
			toolbarPanel.rootController(re.getDisplayname(), runtimeController);
		} else if(security.canLaunch()) {
			removeAsListenerAndDispose(runtimeController);
			runtimeController = runtimeControllerCreator.create(ureq, getWindowControl(), toolbarPanel, re, reSecurity, assessmentMode);
			listenTo(runtimeController);
			toolbarPanel.rootController(re.getDisplayname(), runtimeController);
			if(!launchDateUpdated.getAndSet(true)) {
				userCourseInfoMgr.updateUserCourseInformations(re.getOlatResource(), getIdentity());
			}
		} else {
			runtimeController = new AccessRefusedController(ureq, getWindowControl());
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