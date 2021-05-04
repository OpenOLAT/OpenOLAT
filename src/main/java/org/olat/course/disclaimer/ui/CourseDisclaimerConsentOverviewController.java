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
package org.olat.course.disclaimer.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.course.disclaimer.manager.CourseDisclaimerManagerImpl;
import org.olat.course.disclaimer.ui.CourseDisclaimerConsentTableModel.ConsentCols;
import org.olat.course.member.MemberListController;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 17 Mar 2020<br>
 * @author Alexander Boeckle
 */
public class CourseDisclaimerConsentOverviewController extends FormBasicController {

	private static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;

	public static final String TABLE_ACTION_HOME = "tbl_home";
	public static final String TABLE_ACTION_CONTACT = "tbl_contact";

	private final AtomicInteger counter = new AtomicInteger();
	private final boolean canRevokeConsents;

	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private UserInfoMainController visitingCardCtrl;
	private ContactFormController contactCtrl;

	private FlexiTableElement tableEl;
	private CourseDisclaimerConsentTableModel tableModel;

	private List<UserPropertyHandler> userPropertyHandlers;

	private RepositoryEntry repositoryEntry;
	private CloseableModalController cmc;
	private CourseDisclaimerUpdateConfirmController revokeConfirmController;
	private CourseDisclaimerUpdateConfirmController removeConfirmController;
	private FormLink revokeBtn;
	private FormLink removeBtn;

	private CourseConfig courseConfig;

	private TooledStackedPanel toolbarPanel;

	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CourseDisclaimerManagerImpl disclaimerManager;
	@Autowired
	private BaseSecurity securityManager;

	public CourseDisclaimerConsentOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry repositoryEntry, TooledStackedPanel stackedPanel, boolean canRevokeConsents) {
		super(ureq, wControl, "consent_list");

		setTranslator(Util.createPackageTranslator(AbstractMemberListController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		this.repositoryEntry = repositoryEntry;
		this.courseConfig = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId()).getCourseConfig();
		this.toolbarPanel = stackedPanel;
		this.canRevokeConsents = canRevokeConsents;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose here
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null)
				continue;

			FlexiColumnModel col;
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID, userPropertyHandler);
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, TABLE_ACTION_HOME, true, propName,
						new StaticFlexiCellRenderer(TABLE_ACTION_HOME, new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible,
						userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, ConsentCols.consent));

		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(ConsentCols.tools.i18nHeaderKey(), ConsentCols.tools.ordinal());
		toolsCol.setExportable(false);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);


		tableModel = new CourseDisclaimerConsentTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "consents", tableModel, 25, false, getTranslator(),
				formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings(!courseConfig.isDisclaimerEnabled() ? "error.no.consent.found.no.disclaimer" : "error.no.consent.found", null, "o_icon_disclaimer");
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "course_disclaimer_consents_table-v2");

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);

		revokeBtn = uifactory.addFormLink("consents.selected.revoke", formLayout, Link.BUTTON);
		buttonLayout.add(revokeBtn);

		if (canRevokeConsents) {
			removeBtn = uifactory.addFormLink("consents.selected.delete", formLayout, Link.BUTTON);
			buttonLayout.add(removeBtn);
		}
	}

	public void loadModel() {
		List<CourseDisclaimerConsent> consents = disclaimerManager.getConsents(repositoryEntry);
		List<CourseDisclaimerConsenstPropertiesRow> rows = new ArrayList<>();

		for (CourseDisclaimerConsent consent : consents) {
			CourseDisclaimerConsenstPropertiesRow row = new CourseDisclaimerConsenstPropertiesRow(consent.getIdentity(), userPropertyHandlers, getLocale(), consent.getConsentDate());
			forgeLinks(row);
			rows.add(row);
		}

		if (rows.isEmpty()) {
			revokeBtn.setVisible(false);
			if (removeBtn != null && canRevokeConsents) {
				removeBtn.setVisible(false);
			}
			tableEl.setSearchEnabled(false);
		} else {
			revokeBtn.setVisible(true);
			if (removeBtn != null && canRevokeConsents) {
				removeBtn.setVisible(true);
			}
			tableEl.setSearchEnabled(true);
		}

		tableModel.setObjects(rows);
		tableModel.setBackupList(rows);
		tableEl.reset(true, true, true);
	}

	protected void forgeLinks(CourseDisclaimerConsenstPropertiesRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == revokeBtn) {
			activateRevokeConfirmDialog(ureq, Long.valueOf(tableEl.getMultiSelectedIndex().size()));
		} else if (source == removeBtn) {
			activateRemoveConfirmDialog(ureq, Long.valueOf(tableEl.getMultiSelectedIndex().size()));
		} else if (source == tableEl) {
			if(event instanceof FlexiTableSearchEvent) {
				if (event.getCommand().equals(FlexiTableSearchEvent.QUICK_SEARCH)) {
					FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
					String searchString = ftse.getSearch();
					tableModel.search(searchString, ureq);
					tableEl.setEmptyTableSettings("error.no.consent.found.filter", null, "o_icon_disclaimer");
				} else if (event.getCommand().equals(FlexiTableSearchEvent.RESET.getCommand())) {
					tableModel.resetSearch();
				}

				tableEl.reset();
				tableEl.reloadData();
			} else if (event instanceof SelectionEvent) {
				SelectionEvent selectionEvent = (SelectionEvent) event;
				if (event.getCommand().equals(TABLE_ACTION_HOME)) {
					doOpenVisitingCard(ureq, tableModel.getObject(selectionEvent.getIndex()));
				} 
			} 
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				CourseDisclaimerConsenstPropertiesRow row = (CourseDisclaimerConsenstPropertiesRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
			}
		} else if (revokeConfirmController == source) {
			if (event.equals(FormEvent.DONE_EVENT)) {
				revokeConsents();
				cmc.deactivate();
			} else if (event.equals(FormEvent.CANCELLED_EVENT)) {
				cmc.deactivate();
			}
			cleanUp();
		} else if (removeConfirmController == source) {
			if (event.equals(FormEvent.DONE_EVENT)) {
				removeConsents();
				cmc.deactivate();
			} else if (event.equals(FormEvent.CANCELLED_EVENT)) {
				cmc.deactivate();
			}
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}

	private void revokeConsents() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		if (selections.isEmpty()) {
			showWarning("consents.table.empty");
		}

		List<Long> identityKeys = new ArrayList<>(selections.size());
		for (Integer selection : selections) {
			CourseDisclaimerConsenstPropertiesRow row = tableModel.getObject(selection.intValue());
			identityKeys.add(row.getIdentityKey());
		}

		disclaimerManager.revokeConsents(repositoryEntry, identityKeys);
		getWindowControl().setInfo(translate("consents.update.revoke"));
		loadModel();
	}

	private void removeConsents() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		if (selections.isEmpty()) {
			showWarning("consents.table.empty");
		}

		List<Long> identityKeys = new ArrayList<>(selections.size());
		for (Integer selection : selections) {
			CourseDisclaimerConsenstPropertiesRow row = tableModel.getObject(selection.intValue());
			identityKeys.add(row.getIdentityKey());
		}

		disclaimerManager.removeConsents(repositoryEntry, identityKeys);
		getWindowControl().setInfo(translate("consents.update.remove"));
		loadModel();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(revokeConfirmController);
		removeAsListenerAndDispose(removeConfirmController);

		cmc = null;
		revokeConfirmController = null;
		removeConfirmController = null;
	}

	private void activateRevokeConfirmDialog(UserRequest ureq, Long numOfConsents) {
		if (tableEl.getMultiSelectedIndex().size() < 1) {
			getWindowControl().setWarning(translate("warning.table.selection"));
		} else {
			revokeConfirmController = new CourseDisclaimerUpdateConfirmController(ureq, getWindowControl(), false, numOfConsents);
			listenTo(revokeConfirmController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), revokeConfirmController.getInitialComponent(), true, translate("dialog.confirm.revoke.title"), true);
			cmc.activate();
		}
	}

	private void activateRemoveConfirmDialog(UserRequest ureq, Long numOfConsents) {
		if (tableEl.getMultiSelectedIndex().size() < 1) {
			getWindowControl().setWarning(translate("warning.table.selection"));
		} else {
			removeConfirmController = new CourseDisclaimerUpdateConfirmController(ureq, getWindowControl(), true, numOfConsents);
			listenTo(removeConfirmController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), removeConfirmController.getInitialComponent(), true, translate("dialog.confirm.remove.title"), true);
			cmc.activate();
		}	
	}

	private void doOpenTools(UserRequest ureq, CourseDisclaimerConsenstPropertiesRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	protected void doOpenVisitingCard(UserRequest ureq, CourseDisclaimerConsenstPropertiesRow member) {
		removeAsListenerAndDispose(visitingCardCtrl);
		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
		visitingCardCtrl = new UserInfoMainController(ureq, getWindowControl(), choosenIdentity, false, false);
		listenTo(visitingCardCtrl);

		String fullname = userManager.getUserDisplayName(choosenIdentity);
		toolbarPanel.pushController(fullname, visitingCardCtrl);
	}

	protected void doOpenContact(UserRequest ureq, CourseDisclaimerConsenstPropertiesRow member) {
		removeAsListenerAndDispose(contactCtrl);

		Identity choosenIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
		String fullname = userManager.getUserDisplayName(choosenIdentity);

		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList emailList = new ContactList(fullname);
		emailList.add(choosenIdentity);
		cmsg.addEmailTo(emailList);

		OLATResourceable ores = OresHelper.createOLATResourceableType("Contact");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactCtrl = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactCtrl);

		toolbarPanel.pushController(fullname, contactCtrl);
	}

	private class ToolsController extends BasicController {

		private final CourseDisclaimerConsenstPropertiesRow row;

		private final VelocityContainer mainVC;

		public ToolsController(UserRequest ureq, WindowControl wControl, CourseDisclaimerConsenstPropertiesRow row) {
			super(ureq, wControl);
			this.row = row;

			setTranslator(Util.createPackageTranslator(AbstractMemberListController.class, getLocale(), getTranslator()));

			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			//links
			addLink("home", TABLE_ACTION_HOME, "o_icon o_icon_home", links);
			addLink("contact", TABLE_ACTION_CONTACT, "o_icon o_icon_mail", links);

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}

		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if(TABLE_ACTION_HOME.equals(cmd)) {
					doOpenVisitingCard(ureq, row);
				} else if(TABLE_ACTION_CONTACT.equals(cmd)) {
					doOpenContact(ureq, row);
				}
			}
		}
	}
}
