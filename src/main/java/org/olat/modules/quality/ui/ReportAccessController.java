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
package org.olat.modules.quality.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.EmailTrigger;
import org.olat.modules.quality.QualityReportAccess.Type;
import org.olat.modules.quality.QualityReportAccessReference;
import org.olat.modules.quality.QualityReportAccessRightProvider;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ReportAccessDataModel.ReportAccessCols;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ReportAccessController extends FormBasicController {
	
	static final int USER_PROPS_OFFSET = 500;
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private static final String[] ONLINE_KEYS = new String[] { "enabled" };
	private static final String[] ONLINE_VALUES = new String[] { "" };
	
	private ReportAccessDataModel accessDataModel;
	private FlexiTableElement accessTableEl;
	private final String[] emailTriggerKeys;
	private final String[] emailTriggerValues;
	private FormLayoutContainer membersLayout;
	private ReportMemberTableModel membersTableModel;
	private FlexiTableElement membersTableEl;
	private FormLink addMemberButton;
	private FormLink removeMemberButton;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmRemoveCtrl;
	
	private QualityReportAccessReference reference;
	private QualityReportAccessSearchParams searchParams;
	private String topicIdentityName;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private List<QualityReportAccess> reportAccesses;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;

	protected ReportAccessController(UserRequest ureq, WindowControl windowControl, QualityReportAccessReference reference) {
		super(ureq, windowControl, "report_access");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.reference = reference;
		this.searchParams = new QualityReportAccessSearchParams();
		this.searchParams.setReference(reference);
		
		if (reference.isDataCollectionRef()) {
			QualityDataCollection dataCollection = qualityService.loadDataCollectionByKey(reference.getDataCollectionRef());
			if (dataCollection != null && dataCollection.getTopicIdentity() != null) {
				topicIdentityName = userManager.getUserDisplayName(dataCollection.getTopicIdentity());
			}
		}
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		KeyValues emailTriggerKV = getEmailTriggerKV();
		this.emailTriggerKeys = emailTriggerKV.keys();
		this.emailTriggerValues = emailTriggerKV.values();
	}
	
	protected abstract boolean canEditReportAccessOnline();
	
	protected abstract boolean canEditReportAccessEmail();
	
	protected abstract boolean canEditReportMembers();

	private KeyValues getEmailTriggerKV() {
		EmailTrigger[] emailTriggers = QualityReportAccess.EmailTrigger.values();
		KeyValues kv = new KeyValues();
		for (int i = 0; i < emailTriggers.length; i++) {
			EmailTrigger emailTrigger = emailTriggers[i];
			String key = emailTrigger.name();
			String value = translate("report.access.email.trigger." + emailTrigger.name());
			kv.add(entry(key, value));
		}
		return kv;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initAccessTable(ureq);
		initMembersTable(ureq);
	}

	protected void initAccessTable(UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.online));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportAccessCols.emailTrigger));
		
		accessDataModel = new ReportAccessDataModel(columnsModel, getTranslator());
		
		if (accessTableEl != null) flc.remove(accessTableEl);
		accessTableEl = uifactory.addTableElement(getWindowControl(), "reportaccess", accessDataModel, 25, true, getTranslator(), flc);
		accessTableEl.setAndLoadPersistedPreferences(ureq, "quality-report-access");
		accessTableEl.setEmptyTableMessageKey("report.access.empty.table");
		accessTableEl.setNumOfRowsEnabled(false);
		accessTableEl.setCustomizeColumns(false);
		loadAccessDataModel();
	}

	private void loadAccessDataModel() {
		reportAccesses = qualityService.loadReportAccesses(searchParams);
		List<ReportAccessRow> rows = createRows();
		accessDataModel.setObjects(rows);
		accessTableEl.reset(true, true, true);
	}

	private List<ReportAccessRow> createRows() {
		List<ReportAccessRow> rows = new ArrayList<>();

		rows.add(createRow(translate("report.access.name.participants.all"), Type.Participants, null));
		rows.add(createRow(translate("report.access.name.participants.done"), Type.Participants, EvaluationFormParticipationStatus.done.name()));
		rows.add(createRow(translate("report.access.name.repo.owner"), Type.GroupRoles, GroupRoles.owner.name()));
		rows.add(createRow(translate("report.access.name.repo.coach"), Type.GroupRoles, GroupRoles.coach.name()));
		rows.add(createRow(translate("report.access.name.repo.lrm"), Type.LearnResourceManager, null));
		
		String topicIdentityRowName = StringHelper.containsNonWhitespace(topicIdentityName)
				? translate("report.access.name.topic.identity.name", new String[] {topicIdentityName})
				: translate("report.access.name.topic.identity");
		rows.add(createRow(topicIdentityRowName, Type.TopicIdentity, null));
		
		if (securityModule.isRelationRoleEnabled()) {
			List<RelationRole> roles = identityRelationshipService.getRolesByRight(QualityReportAccessRightProvider.RELATION_RIGHT);
			if (!roles.isEmpty()) {
				String relationRoleIdentityName = StringHelper.containsNonWhitespace(topicIdentityName)
						? topicIdentityName
						: "\"" + translate("report.access.name.topic.identity") + "\"";
				for (RelationRole role : roles) {
					String roleDescription = RelationRolesAndRightsUIFactory.getTranslatedRoleDescription(role, getLocale());
					String roleName = roleDescription + " " + relationRoleIdentityName;
					rows.add(createRow(roleName, Type.RelationRole, role.getKey().toString()));
				}
			}
		}

		rows.add(createRow(translate("report.access.name.members"), Type.ReportMember, null));
		return rows;
	}
	
	private ReportAccessRow createRow(String name, Type type, String role) {
		ReportAccessRow row = new ReportAccessRow(name, type, role);
		QualityReportAccess access = getCachedReportAccess(type, role);
		MultipleSelectionElement onlineEl = createOnlineCheckbox(row, access);
		row.setOnlineEl(onlineEl);
		SingleSelection emailTriggerEl = createEmailTriggerEl(row, access);
		row.setEmailTriggerEl(emailTriggerEl);
		return row;
	}

	private MultipleSelectionElement createOnlineCheckbox(ReportAccessRow row, QualityReportAccess access) {
		String name =  "online-" + CodeHelper.getRAMUniqueID();
		MultipleSelectionElement onlineEl = uifactory.addCheckboxesHorizontal(name, null, flc, ONLINE_KEYS, ONLINE_VALUES);
		onlineEl.setUserObject(row);
		onlineEl.addActionListener(FormEvent.ONCHANGE);
		onlineEl.setAjaxOnly(true);
		onlineEl.select(ONLINE_KEYS[0], access != null && access.isOnline());
		onlineEl.setEnabled(canEditReportAccessOnline());
		return onlineEl;
	}

	private SingleSelection createEmailTriggerEl(ReportAccessRow row, QualityReportAccess access) {
		String name =  "email-" + CodeHelper.getRAMUniqueID();
		SingleSelection emailTriggerEl = uifactory.addDropdownSingleselect(name, null, flc, emailTriggerKeys, emailTriggerValues);
		emailTriggerEl.setUserObject(row);
		emailTriggerEl.addActionListener(FormEvent.ONCHANGE);
		String accessKey = access != null? access.getEmailTrigger().name(): null;
		if (Arrays.asList(emailTriggerKeys).contains(accessKey)) {
			emailTriggerEl.select(accessKey, true);
		}
		emailTriggerEl.setEnabled(canEditReportAccessEmail());
		return emailTriggerEl;
	}
	
	
	private void initMembersTable(UserRequest ureq) {
		if (membersLayout != null) flc.remove(membersLayout);
		membersLayout = FormLayoutContainer.createVerticalFormLayout("members", getTranslator());
		membersLayout.setRootForm(mainForm);
		membersLayout.setFormTitle(translate("report.member.title"));
		flc.add("members", membersLayout);
		
		
		if (canEditReportMembers()) {
			FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
			membersLayout.add("topButtons", topButtons);
			topButtons.setRootForm(mainForm);
			topButtons.setElementCssClass("o_button_group_right");
			addMemberButton = uifactory.addFormLink("report.member.add", topButtons, Link.BUTTON);
			addMemberButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		membersTableModel = new ReportMemberTableModel(columnsModel, getLocale()); 
		membersTableEl = uifactory.addTableElement(getWindowControl(), "memberstable", membersTableModel, 20, false, getTranslator(), membersLayout);
		membersTableEl.setAndLoadPersistedPreferences(ureq, "quality-report-members-v2");
		membersTableEl.setEmptyTableMessageKey("report.member.empty.table");
		membersTableEl.setSelectAllEnable(true);
		membersTableEl.setMultiSelect(true);
		
		if (canEditReportMembers()) {
			FormLayoutContainer bottomButtons = FormLayoutContainer.createButtonLayout("bottomButtons", getTranslator());
			membersLayout.add("buttomButtons", bottomButtons);
			bottomButtons.setElementCssClass("o_button_group");
			removeMemberButton = uifactory.addFormLink("report.member.remove", bottomButtons, Link.BUTTON);
		}
		
		loadMembersModel(false);
	}

	private void loadMembersModel(boolean reset) {
		List<Identity> members = qualityService.loadReportMembers(reference);
		List<UserPropertiesRow> rows = new ArrayList<>(members.size());
		for(Identity member:members) {
			rows.add(new UserPropertiesRow(member, userPropertyHandlers, getLocale()));
		}
		membersTableModel.setObjects(rows);
		membersTableEl.reset(reset, reset, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement onlineEl = (MultipleSelectionElement) source;
			doEnableOnline(onlineEl);
		} else if (source instanceof SingleSelection) {
			SingleSelection emailTriggerEl = (SingleSelection)source;
			doSetEmailTrigger(emailTriggerEl);
		} else if (addMemberButton == source) {
			doSearchMember(ureq);
		} else if (removeMemberButton == source) {
			doConfirmRemoveAllMembers(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<UserPropertiesRow> rows = (List<UserPropertiesRow>)confirmRemoveCtrl.getUserObject();
				doRemoveMember(rows);
			}
		} else if (userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddMember(toAdd);
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddMember(multiEvent.getChosenIdentities());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		userSearchCtrl = null;
		cmc = null;
	}

	private void doEnableOnline(MultipleSelectionElement onlineEl) {
		boolean enable = onlineEl.isAtLeastSelected(1);
		ReportAccessRow row = (ReportAccessRow) onlineEl.getUserObject();
		QualityReportAccess reportAccess = getOrCreateReportAccess(row);
		reportAccess.setOnline(enable);
		updateReportAccess(reportAccess);
	}

	private void doSetEmailTrigger(SingleSelection emailTriggerEl) {
		String selectedKey = emailTriggerEl.isOneSelected()? emailTriggerEl.getSelectedKey(): null;
		EmailTrigger emailTrigger = QualityReportAccess.EmailTrigger.valueOf(selectedKey);
		ReportAccessRow row = (ReportAccessRow) emailTriggerEl.getUserObject();
		QualityReportAccess reportAccess = getOrCreateReportAccess(row);
		reportAccess.setEmailTrigger(emailTrigger);
		updateReportAccess(reportAccess);
	}

	private QualityReportAccess getOrCreateReportAccess(ReportAccessRow row) {
		QualityReportAccess reportAccess = getCachedReportAccess(row.getType(), row.getRole());
		if (reportAccess == null) {
			reportAccess = qualityService.createReportAccess(reference, row.getType(), row.getRole());
		}
		return reportAccess;
	}

	private QualityReportAccess getCachedReportAccess(Type type, String role) {
		for (QualityReportAccess access : reportAccesses) {
			if (type.equals(access.getType()) && equalsEmpty(role, access.getRole())) {
				return access;
			}
		}
		return null;
	}

	private void updateReportAccess(QualityReportAccess reportAccess) {
		reportAccess = qualityService.updateReportAccess(reportAccess);
		updateCache(reportAccess);
	}
	
	private void updateCache(QualityReportAccess reportAccess) {
		reportAccesses.removeIf(a -> reportAccess.getType().equals(a.getType()) && equalsEmpty(reportAccess.getRole(), a.getRole()));
		reportAccesses.add(reportAccess);
	}
	
	private boolean equalsEmpty(String s1, String s2) {
		if (!StringHelper.containsNonWhitespace(s1) && !StringHelper.containsNonWhitespace(s2)) {
			return true;
		}
		
		if (StringHelper.containsNonWhitespace(s1) && StringHelper.containsNonWhitespace(s2)) {
			return s1.equals(s2);
		}
		
		return false;
	}
	
	private void doSearchMember(UserRequest ureq) {
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		listenTo(userSearchCtrl);
		
		String title = translate("report.member.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMember(List<Identity> identities) {
		for (Identity identity : identities) {
			qualityService.addReportMember(reference, identity);
		}
		QualityReportAccess reportAccess = qualityService.loadMembersReportAccess(reference);
		updateCache(reportAccess);
		loadMembersModel(true);
	}
	
	private void doConfirmRemoveAllMembers(UserRequest ureq) {
		Set<Integer> selectedRows = membersTableEl.getMultiSelectedIndex();
		if (selectedRows.isEmpty()) {
			showWarning("report.member.warning.atleastone");
		} else {
			List<UserPropertiesRow> rows = new ArrayList<>(selectedRows.size());
			for (Integer selectedRow:selectedRows) {
				rows.add(membersTableModel.getObject(selectedRow.intValue()));
			}
			String title = translate("report.member.remove.confirm.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("report.member.remove.confirm.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}

	private void doRemoveMember(List<UserPropertiesRow> rows) {
		for (UserPropertiesRow row : rows) {
			IdentityRefImpl identityRef = new IdentityRefImpl(row.getIdentityKey());
			qualityService.removeReportMember(reference, identityRef);
		}
		QualityReportAccess reportAccess = qualityService.loadMembersReportAccess(reference);
		updateCache(reportAccess);
		loadMembersModel(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
