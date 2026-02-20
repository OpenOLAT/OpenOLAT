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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryAdminConfigurationController extends FormBasicController {

	private static final String NOTIFICATION_REPOSITORY_STATUS_CHANGED = "notification.repository.status.changed";
	private static final String[] keys = {"on"};
	private static final String[] leaveKeys = {
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
	};

	private FormLink enableAllSubscribersLink;
	private FormLink disableAllSubscribersLink;
	private SingleSelection leaveEl;
	private MultipleSelectionElement ratingEl;
	private MultipleSelectionElement membershipEl;
	private MultipleSelectionElement commentEl;
	private MultipleSelectionElement taxonomyEl;
	private MultipleSelectionElement notificationEl;
	private MultipleSelectionElement myCourseInPreparationEl;
	private FlexiTableElement tableEl;
	private RoleDataModel dataModel;
	
	private int counter = 0;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private NotificationsManager notificationsManager;
	
	public RepositoryAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createDefaultFormLayout("search", getTranslator());
		searchCont.setFormContextHelp("manual_admin/administration/Modules_Repository/");
		searchCont.setFormTitle(translate("repository.admin.title"));
		formLayout.add(searchCont);
		searchCont.setRootForm(mainForm);

		boolean inPreparationEnabled = repositoryModule.isMyCoursesInPreparationEnabled();
		String[] values = new String[] { translate("on") };
		myCourseInPreparationEl = uifactory.addCheckboxesHorizontal("my.course.in.preparation.enabled", searchCont, keys, values);
		myCourseInPreparationEl.addActionListener(FormEvent.ONCHANGE);
		myCourseInPreparationEl.select(keys[0], inPreparationEnabled);
		
		boolean commentEnabled = repositoryModule.isCommentEnabled();
		commentEl = uifactory.addCheckboxesHorizontal("my.course.comment.enabled", searchCont, keys, values);
		commentEl.addActionListener(FormEvent.ONCHANGE);
		commentEl.select(keys[0], commentEnabled);
		
		boolean ratingEnabled = repositoryModule.isRatingEnabled();
		ratingEl = uifactory.addCheckboxesHorizontal("my.course.rating.enabled", searchCont, keys, values);
		ratingEl.addActionListener(FormEvent.ONCHANGE);
		ratingEl.select(keys[0], ratingEnabled);

		boolean requestMembershipEnabled = repositoryModule.isRequestMembershipEnabled();
		membershipEl = uifactory.addCheckboxesHorizontal("rentry.request.membership", searchCont, keys, values);
		membershipEl.addActionListener(FormEvent.ONCHANGE);
		membershipEl.select(keys[0], requestMembershipEnabled);
		
		SelectionValues taxonomySV = new SelectionValues();
		taxonomyService.getTaxonomyList().forEach(
				taxonomy -> taxonomySV.add(entry(
						taxonomy.getKey().toString(), 
						taxonomy.getDisplayName())));
		taxonomyEl = uifactory.addCheckboxesVertical("selected.taxonomy.tree", searchCont, taxonomySV.keys(), taxonomySV.values(), 1);
		repositoryModule.getTaxonomyRefs().forEach(taxonomy -> taxonomyEl.select(taxonomy.getKey().toString(), true));
		taxonomyEl.addActionListener(FormEvent.ONCHANGE);
		
		// Leave
		FormLayoutContainer leaveCont = FormLayoutContainer.createDefaultFormLayout("leave", getTranslator());
		leaveCont.setFormTitle(translate("repository.admin.leave.title"));
		formLayout.add(leaveCont);
		leaveCont.setRootForm(mainForm);
		
		String[] leaveValues = new String[] {
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		leaveEl = uifactory.addDropdownSingleselect("leave.courses", "repository.admin.leave.label", leaveCont, leaveKeys, leaveValues, null);
		leaveEl.addActionListener(FormEvent.ONCHANGE);
		RepositoryEntryAllowToLeaveOptions leaveOption = repositoryModule.getAllowToLeaveDefaultOption();
		if(leaveOption != null) {
			leaveEl.select(leaveOption.name(), true);
		} else {
			leaveEl.select(RepositoryEntryAllowToLeaveOptions.atAnyTime.name(), true);
		}

		FormLayoutContainer notificationCont = FormLayoutContainer.createDefaultFormLayout("notification", getTranslator());
		notificationCont.setFormTitle(translate("repository.admin.notification.title"));
		notificationCont.setFormInfo(translate("repository.admin.notification.desc"));
		formLayout.add(notificationCont);
		notificationCont.setRootForm(mainForm);

		String[] notiKeys = new String[]{
				NOTIFICATION_REPOSITORY_STATUS_CHANGED
		};

		String[] notiValues = new String[]{
				translate("repository.admin.notification")
		};

		boolean statusChangedNotificationEnabled = repositoryModule.isRepoStatusChangedNotificationEnabledDefault();
		notificationEl = uifactory.addCheckboxesVertical("repository.admin.notification.label", notificationCont, notiKeys, notiValues, 1);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		notificationEl.select(NOTIFICATION_REPOSITORY_STATUS_CHANGED, statusChangedNotificationEnabled);

		// TODO Darstellung inaktiver abos

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		notificationCont.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addStaticTextElement("maintButtonLabel", getTranslator().translate("repository.admin.subscribers"), buttonsCont);
		enableAllSubscribersLink = uifactory.addFormLink("repository.admin.enable.all.subscribers", buttonsCont, Link.BUTTON);
		disableAllSubscribersLink = uifactory.addFormLink("repository.admin.disable.all.subscribers", buttonsCont, Link.BUTTON);
		
		// Default role order
		FormLayoutContainer rolesCont = FormLayoutContainer.createVerticalFormLayout("roles", getTranslator());
		rolesCont.setFormTitle(translate("repository.admin.default.roles.title"));
		rolesCont.setFormInfo(translate("repository.admin.default.roles.info"));
		formLayout.add(rolesCont);
		rolesCont.setRootForm(mainForm);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoleCols.position));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RoleCols.priority));
		DefaultFlexiColumnModel roleColumn = new DefaultFlexiColumnModel(RoleCols.role);
		roleColumn.setColumnCssClass("o_cell_stretch");
		columnsModel.addFlexiColumnModel(roleColumn);
		
		dataModel = new RoleDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		loadRoleModel();
	}
	
	private void loadRoleModel() {
		List<RoleRow> rows = new ArrayList<>(3);
		for (Role role : repositoryModule.getDefaultRoles()) {
			RoleRow row = new RoleRow(role, translate(role.getI18nKey()));
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + counter++, UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(row);
			row.setUpDown(upDown);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
		updateRolesUI();
	}
	
	private void updateRolesUI() {
		for (int i = 0; i < dataModel.getObjects().size(); i++) {
			RoleRow roleRow = dataModel.getObjects().get(i);
			roleRow.setPriority(Integer.valueOf(i + 1));
			roleRow.getUpDown().setTopmost(i == 0);
			roleRow.getUpDown().setLowermost(i == 2);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent && source instanceof UpDown) {
			UpDownEvent ude = (UpDownEvent) event;
			UpDown upDown = (UpDown)source;
			Object userObject = upDown.getUserObject();
			if (userObject instanceof RoleRow row) {
				doMoveRole(row, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(myCourseInPreparationEl == source) {
			boolean on = myCourseInPreparationEl.isAtLeastSelected(1);
			repositoryModule.setMyCoursesInPreparationEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(commentEl == source) {
			boolean on = !commentEl.getSelectedKeys().isEmpty();
			repositoryModule.setCommentEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(ratingEl == source) {
			boolean on = !ratingEl.getSelectedKeys().isEmpty();
			repositoryModule.setRatingEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(membershipEl == source) {
			boolean on = !membershipEl.getSelectedKeys().isEmpty();
			repositoryModule.setRequestMembershipEnabled(on);
			getWindowControl().setInfo("saved");
		} else if (notificationEl == source) {
			repositoryModule.setRepoStatusChangedNotificationEnabledDefault(notificationEl.isKeySelected(NOTIFICATION_REPOSITORY_STATUS_CHANGED));
			getWindowControl().setInfo("saved");
		} else if (taxonomyEl == source) {
			List<TaxonomyRef> taxonomyRefs = taxonomyEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(TaxonomyRefImpl::new).
					collect(Collectors.toList());
			repositoryModule.setTaxonomyRefs(taxonomyRefs);
			getWindowControl().setInfo("saved");
		} else if(leaveEl == source) {
			String selectedOption = leaveEl.getSelectedKey();
			RepositoryEntryAllowToLeaveOptions option = RepositoryEntryAllowToLeaveOptions.valueOf(selectedOption);
			repositoryModule.setAllowToLeaveDefaultOption(option);
			getWindowControl().setInfo("saved");
		} else if (enableAllSubscribersLink == source
				|| disableAllSubscribersLink == source) {
			SubscriptionContext subContext = repositoryService.getSubscriptionContext();
			PublisherData publisherData = repositoryService.getPublisherData();
			Publisher publisher = notificationsManager.getOrCreatePublisher(subContext, publisherData);
			notificationsManager.updateAllSubscribers(publisher, enableAllSubscribersLink == source);
			getWindowControl().setInfo("saved");
		}
	}
	
	private void doMoveRole(RoleRow row, Direction direction) {
		List<RoleRow> rows = new ArrayList<>(dataModel.getObjects());
		
		int swapIndex = Direction.UP == direction? row.getPriority() - 2: row.getPriority();
		Collections.swap(rows, row.getPriority() - 1, swapIndex);
		
		List<Role> defaultRoles = rows.stream().map(RoleRow::getRole).toList();
		repositoryModule.setDefaultRoles(defaultRoles);
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
		updateRolesUI();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private final static class RoleDataModel extends DefaultFlexiTableDataModel<RoleRow> {
		
		private static final RoleCols[] COLS = RoleCols.values();
		
		public RoleDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			RoleRow rolwRow = getObject(row);
			return switch(COLS[col]) {
			case priority -> rolwRow.getPriority();
			case role -> rolwRow.getTranslatedRole();
			case position -> rolwRow.getUpDown();
			default -> null;
			};
		}
	}
	
	private enum RoleCols implements FlexiColumnDef {
		priority("repository.admin.default.roles.priority"),
		role("repository.admin.default.roles.role"),
		position("repository.admin.default.roles.position");
		
		private final String i18nKey;
		
		private RoleCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
	
	final static class RoleRow {
		
		private final Role role;
		private final String translatedRole;
		private Integer priority;
		private UpDown upDown;
		
		public RoleRow(Role role, String translatedRole) {
			this.role = role;
			this.translatedRole = translatedRole;
		}

		public Role getRole() {
			return role;
		}
		
		public String getTranslatedRole() {
			return translatedRole;
		}
		
		public Integer getPriority() {
			return priority;
		}
		
		public void setPriority(Integer priority) {
			this.priority = priority;
		}
		
		public UpDown getUpDown() {
			return upDown;
		}
		
		public void setUpDown(UpDown upDown) {
			this.upDown = upDown;
		}
		
	}
		
}