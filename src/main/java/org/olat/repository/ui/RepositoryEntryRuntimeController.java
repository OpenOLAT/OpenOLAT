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

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.olat.repository.ui.author.CatalogSettingsController;
import org.olat.repository.ui.author.RepositoryEditDescriptionController;
import org.olat.repository.ui.author.RepositoryMembersController;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.AccessListController;
import org.olat.resource.accesscontrol.ui.AccessRefusedController;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRuntimeController extends MainLayoutBasicController implements Activateable2 {

	private Controller runtimeController;
	private final TooledStackedPanel toolbarPanel;
	private final RuntimeControllerCreator runtimeControllerCreator;
	
	protected Controller editorCtrl;
	private Controller accessController;
	private OrdersAdminController ordersCtlr;
	private CatalogSettingsController catalogCtlr;
	private AuthoringEditAccessController accessCtrl;
	private RepositoryEntryDetailsController detailsCtrl;
	private RepositoryMembersController membersEditController;
	private RepositoryEditDescriptionController descriptionCtrl;
	
	private Dropdown tools;
	private Dropdown settings;
	private Link editLink, membersLink, ordersLink,
				 editDescriptionLink, accessLink, catalogLink,
				 detailsLink, bookmarkLink;
	
	protected final boolean isOlatAdmin;
	protected final boolean isInstitutionalResourceManager;
	protected final boolean isOwner;
	protected final boolean isAuthor;
	protected final boolean isGuestOnly;

	private boolean corrupted;
	private RepositoryEntry re;
	private final RepositoryHandler handler;
	
	private String launchedFromBusinessPath;
	
	@Autowired
	private ACService acService;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;

	public RepositoryEntryRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		//! check corrupted
		
		UserSession session = ureq.getUserSession();
		if(session != null &&  session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
			// Set previous business path as back link for this course - brings user back to place from which he launched the course
			List<HistoryPoint> stack = session.getHistoryStack();
			HistoryPoint point = stack.get(stack.size() - 2);
			launchedFromBusinessPath = point.getBusinessPath();
		}
		
		this.re = re;
		this.runtimeControllerCreator = runtimeControllerCreator;
		handler = handlerFactory.getRepositoryHandler(re);
		
		Identity identity = getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		isOlatAdmin = roles.isOLATAdmin();
		isInstitutionalResourceManager = !roles.isGuestOnly()
					&& RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(identity, roles, re);
		isOwner = isOlatAdmin || repositoryService.hasRole(ureq.getIdentity(), re, GroupRoles.owner.name())
					| isInstitutionalResourceManager;
		isAuthor = isOlatAdmin || roles.isAuthor() | isInstitutionalResourceManager;
		isGuestOnly = roles.isGuestOnly();

		// set up the components
		toolbarPanel = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root (course) level
		toolbarPanel.setShowCloseLink(true, true);
		putInitialPanel(toolbarPanel);
		doRun(ureq);
		initToolbar();
	}
	
	protected RepositoryEntry getRepositoryEntry() {
		return re;
	}
	
	protected Controller getRuntimeController() {
		return runtimeController;
	}

	private void initToolbar() {
		tools = new Dropdown("toolbox.tools", "toolbox.tools", false, getTranslator());
		tools.setElementCssClass("o_sel_repository_tools");
		tools.setIconCSS("o_icon o_icon_tools");
		
		settings = new Dropdown("settings", "details.chprop", false, getTranslator());
		settings.setElementCssClass("o_sel_course_settings");
		settings.setIconCSS("o_icon o_icon_customize");
		
		initToolbar(tools, settings);
	}
	
	protected void initToolbar(Dropdown toolsDropdown, Dropdown settingsDropdown) {
		
		boolean managed = false;
		
		if (isOwner || isInstitutionalResourceManager || isOlatAdmin) {
			//tools
			if(handler.supportsEdit(re) == EditionSupport.yes) {
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
			ordersLink.setEnabled(!corrupted && booking);
			toolsDropdown.addComponent(ordersLink);
			
			//settings
			editDescriptionLink = LinkFactory.createToolLink("settings.cmd", translate("details.chprop"), this, "o_icon_settings");
			editDescriptionLink.setElementCssClass("o_sel_course_settings");
			editDescriptionLink.setEnabled(!managed);
			settingsDropdown.addComponent(editDescriptionLink);
			
			accessLink = LinkFactory.createToolLink("access.cmd", translate("tab.accesscontrol"), this, "o_icon_password");
			accessLink.setElementCssClass("o_sel_course_access");
			accessLink.setEnabled(!managed);
			settingsDropdown.addComponent(accessLink);
			
			catalogLink = LinkFactory.createToolLink("cat", translate("details.categoriesheader"), this, "o_icon_catalog");
			catalogLink.setElementCssClass("o_sel_repo_add_to_catalog");
			catalogLink.setEnabled(!managed);
			settingsDropdown.addComponent(catalogLink);
		}
		
		if(toolsDropdown.size() > 0) {
			toolbarPanel.addTool(toolsDropdown, Align.left, true);
		}
		if(settingsDropdown.size() > 0) {
			toolbarPanel.addTool(settingsDropdown, Align.left, true);
		}
		
		detailsLink = LinkFactory.createToolLink("details", translate("details.header"), this, "o_sel_repo_details");
		detailsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_details");
		detailsLink.setElementCssClass("o_sel_author_details");
		toolbarPanel.addTool(detailsLink);
		
		
		boolean marked = markManager.isMarked(re, getIdentity(), null);
		String css = marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON;
		bookmarkLink = LinkFactory.createToolLink("bookmark", translate("details.bookmark.label"), this, css);
		bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		toolbarPanel.addTool(bookmarkLink, Align.right);
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
		if(entries != null && entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Editor".equals(type)) {
				
			}
		}

		if(runtimeController instanceof Activateable2) {
			((Activateable2)runtimeController).activate(ureq, entries, state);
		}
	}

	@Override
	protected void doDispose() {
		if(runtimeController != null && !runtimeController.isDisposed()) {
			runtimeController.dispose();
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
			boolean marked = doMark();
			String css = "o_icon " + (marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
			bookmarkLink.setIconLeftCSS(css);
			bookmarkLink.setTitle( translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		} else if(source == toolbarPanel) {
			if (event == Event.CLOSE_EVENT) {
				doClose(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				launchContent(ureq);
				cleanUp();
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		}
	}
	
	protected RepositoryEntryRuntimeController popToRoot(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		return this;
	}
	
	protected RepositoryEntryRuntimeController cleanUp() {
		removeAsListenerAndDispose(membersEditController);
		removeAsListenerAndDispose(accessController);
		removeAsListenerAndDispose(descriptionCtrl);
		removeAsListenerAndDispose(catalogCtlr);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(editorCtrl);
		removeAsListenerAndDispose(ordersCtlr);
		membersEditController = null;
		accessController = null;
		descriptionCtrl = null;
		catalogCtlr = null;
		detailsCtrl = null;
		editorCtrl = null;
		ordersCtlr = null;
		return this;
	}
	
	/**
	 * Pop to root, clean up, and push
	 * @param ureq
	 * @param name
	 * @param controller
	 */
	protected void pushController(UserRequest ureq, String name, Controller controller) {
		popToRoot(ureq).cleanUp();
		toolbarPanel.pushController(name, controller);
	}
	
	/**
	 * Open the editor for all repository entry metadata, access control...
	 * @param ureq
	 */
	protected void doAccess(UserRequest ureq) {
		popToRoot(ureq).cleanUp();
		accessCtrl = new AuthoringEditAccessController(ureq, getWindowControl(), re);
		listenTo(accessCtrl);
		toolbarPanel.pushController(translate("tab.accesscontrol"), accessCtrl);
	}
	
	protected void doClose(UserRequest ureq) {
		// Navigate beyond the stack, our own layout has been popped - close this tab
		DTabs tabs = getWindowControl().getWindowBackOffice().getWindow().getDTabs();
		if (tabs != null) {
			DTab tab = tabs.getDTab(re.getOlatResource());
			if (tab != null) {
				tabs.removeDTab(ureq, tab);						
			}
		}
		// Now try to go back to place that is attacked to (optional) root back business path
		if (StringHelper.containsNonWhitespace(launchedFromBusinessPath)) {
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(launchedFromBusinessPath);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			try {
				//make the resume secure. If something fail, don't generate a red screen
				NewControllerFactory.getInstance().launch(ureq, bwControl);
			} catch (Exception e) {
				logError("Error while resuming with root leve back business path::" + launchedFromBusinessPath, e);
			}
		}
	}
	
	protected void doEdit(UserRequest ureq) {
		popToRoot(ureq).cleanUp();
		editorCtrl = handler.createEditorController(re, ureq, getWindowControl(), toolbarPanel);
		listenTo(editorCtrl);
		toolbarPanel.pushController(translate("resource.editor"), editorCtrl);
	}
	
	protected void doDetails(UserRequest ureq) {
		popToRoot(ureq).cleanUp();
		detailsCtrl = new  RepositoryEntryDetailsController(ureq, getWindowControl(), re);
		listenTo(detailsCtrl);
		toolbarPanel.pushController(translate("details.header"), detailsCtrl);
	}
	
	/**
	 * Open the editor for all repository entry metadata, access control...
	 * @param ureq
	 */
	protected void doEditSettings(UserRequest ureq) {
		popToRoot(ureq).cleanUp();
		descriptionCtrl = new RepositoryEditDescriptionController(ureq, getWindowControl(), re, false);
		listenTo(descriptionCtrl);
		toolbarPanel.pushController(translate("settings.editor"), descriptionCtrl);
	}
	
	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	protected void doCatalog(UserRequest ureq) {
		popToRoot(ureq).cleanUp();
		catalogCtlr = new CatalogSettingsController(ureq, getWindowControl(), toolbarPanel, re);
		listenTo(catalogCtlr);
		catalogCtlr.initToolbar();
	}
	
	protected void doMembers(UserRequest ureq) {
		if (!isOwner) {
			throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + getIdentity());
		}
		popToRoot(ureq).cleanUp();
		membersEditController = new RepositoryMembersController(ureq, getWindowControl(), re);
		listenTo(membersEditController);
		toolbarPanel.pushController(translate("details.members"), membersEditController);
	}
	
	protected void doOrders(UserRequest ureq) {
		if (!isOwner) {
			throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + getIdentity());
		}

		popToRoot(ureq).cleanUp();
		ordersCtlr = new OrdersAdminController(ureq, getWindowControl(), re.getOlatResource());
		listenTo(ordersCtlr);
		toolbarPanel.pushController(translate("details.orders"), ordersCtlr);
	}
	
	private void doRun(UserRequest ureq) {
		if(ureq.getUserSession().getRoles().isOLATAdmin()) {
			launchContent(ureq);
		} else {
			// guest are allowed to see resource with BARG 
			if(re.getAccess() == RepositoryEntry.ACC_USERS_GUESTS && ureq.getUserSession().getRoles().isGuestOnly()) {
				launchContent(ureq);
			} else {
				AccessResult acResult = acService.isAccessible(re, getIdentity(), false);
				if(acResult.isAccessible()) {
					launchContent(ureq);
				} else if (re != null && acResult.getAvailableMethods().size() > 0) {
					accessController = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods());
					listenTo(accessController);
					toolbarPanel.rootController(re.getDisplayname(), accessController);
				} else {
					Controller ctrl = new AccessRefusedController(ureq, getWindowControl());
					listenTo(ctrl);
					toolbarPanel.rootController(re.getDisplayname(), ctrl);
				}
			}
		}
	}
	
	protected boolean doMark() {
		OLATResourceable item = OresHelper.clone(re);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
	
	private void launchContent(UserRequest ureq) {
		runtimeController = runtimeControllerCreator.create(ureq, getWindowControl(), re);
		toolbarPanel.rootController(re.getDisplayname(), runtimeController);
	}
	
	public interface RuntimeControllerCreator {
		
		public Controller create(UserRequest ureq, WindowControl wControl, RepositoryEntry entry);
		
	}
}