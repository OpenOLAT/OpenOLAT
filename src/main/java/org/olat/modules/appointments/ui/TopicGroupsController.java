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
package org.olat.modules.appointments.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicGroupsController extends FormBasicController {
	
	private static final String usageIdentifyer = UserRestrictionTableModel.class.getCanonicalName();
	
	private static final String[] ON_KEYS = new String[] { "on" };

	private FormLink backLink;
	private MultipleSelectionElement courseEl;
	private MultipleSelectionElement businessGroupEl;
	private MultipleSelectionElement curriculumEl;
	private UserRestrictionTableModel usersTableModel;
	private FlexiTableElement usersTableEl;
	private FormLink addUserButton;
	private FormLink removeUserButton;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmRemoveCtrl;

	private Topic topic;
	private final RepositoryEntry entry;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private Map<String, Group> keyToBussinesGroups;
	private Map<String, Group> keyToCurriculumElementGroup;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	public TopicGroupsController(UserRequest ureq, WindowControl wControl, TopicRef topicRef) {
		super(ureq, wControl, "topic_groups");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		topic = appointmentsService.getTopic(topicRef);
		entry = topic.getEntry();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("title", topic.getTitle());
		
		// Back
		FormLayoutContainer backButtons = FormLayoutContainer.createButtonLayout("backButtons", getTranslator());
		backButtons.setRootForm(mainForm);
		formLayout.add("backButtons", backButtons);
		backButtons.setElementCssClass("o_button_group o_button_group_left");
		
		backLink = uifactory.addFormLink("backLink", "back", "back", "", backButtons, Link.LINK_BACK);
		backLink.setElementCssClass("o_back");
		
		// Groups
		FormLayoutContainer groupsCont = FormLayoutContainer.createDefaultFormLayout("groups", getTranslator());
		groupsCont.setFormTitle(translate("groups.title"));
		groupsCont.setRootForm(mainForm);
		formLayout.add("groups", groupsCont);
		
		List<Group> groups = appointmentsService.getGroupRestrictions(topic);
		
		courseEl = uifactory.addCheckboxesVertical("groups.course", groupsCont, ON_KEYS,
				TranslatorHelper.translateAll(getTranslator(), ON_KEYS), 1);
		courseEl.addActionListener(FormEvent.ONCHANGE);
		Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
		if (groups.contains(entryBaseGroup)) {
			courseEl.select(courseEl.getKey(0), true);
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1, BusinessGroupOrder.nameAsc);
		if (!businessGroups.isEmpty()) {
			SelectionValues businessGroupKV = new SelectionValues();
			keyToBussinesGroups = new HashMap<>();
			businessGroups.forEach(bg -> {
				String key = bg.getBaseGroup().getKey().toString();
				businessGroupKV.add(entry(key, bg.getName()));
				keyToBussinesGroups.put(key, bg.getBaseGroup());
			});
			businessGroupEl = uifactory.addCheckboxesVertical("groups.business.groups", groupsCont,
					businessGroupKV.keys(), businessGroupKV.values(), 2);
			businessGroupEl.addActionListener(FormEvent.ONCHANGE);
			Set<String> keys = businessGroupEl.getKeys();
			for (Group group : groups) {
				String key = group.getKey().toString();
				if (keys.contains(key)) {
					businessGroupEl.select(key, true);
				}
			}
		}
		
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		if (!elements.isEmpty()) {
			SelectionValues curriculumKV = new SelectionValues();
			keyToCurriculumElementGroup = new HashMap<>();
			elements.forEach(curEle -> {
				String key = curEle.getGroup().getKey().toString();
				curriculumKV.add(entry(key, curEle.getDisplayName()));
				keyToCurriculumElementGroup.put(key, curEle.getGroup());
			});
			curriculumEl = uifactory.addCheckboxesVertical("groups.curriculum", groupsCont, curriculumKV.keys(),
					curriculumKV.values(), 2);
			curriculumEl.addActionListener(FormEvent.ONCHANGE);
			Set<String> keys = curriculumEl.getKeys();
			for (Group group : groups) {
				String key = group.getKey().toString();
				if (keys.contains(key)) {
					curriculumEl.select(key, true);
				}
			}
		}
		
		initUsersTable(ureq);
	}
	
	private void initUsersTable(UserRequest ureq) {
		FormLayoutContainer usersLayout = FormLayoutContainer.createVerticalFormLayout("users", getTranslator());
		usersLayout.setRootForm(mainForm);
		usersLayout.setFormTitle(translate("groups.users.title"));
		flc.add("users", usersLayout);
		
		
		FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
		usersLayout.add("topButtons", topButtons);
		topButtons.setRootForm(mainForm);
		topButtons.setElementCssClass("o_button_group_right");
		addUserButton = uifactory.addFormLink("groups.users.add", topButtons, Link.BUTTON);
		addUserButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = UserRestrictionTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		usersTableModel = new UserRestrictionTableModel(columnsModel, getLocale()); 
		usersTableEl = uifactory.addTableElement(getWindowControl(), "users", usersTableModel, 20, false, getTranslator(), usersLayout);
		usersTableEl.setAndLoadPersistedPreferences(ureq, "topic.groups.users.v2");
		usersTableEl.setEmptyTableMessageKey("groups.users.empty.table");
		usersTableEl.setSelectAllEnable(true);
		usersTableEl.setMultiSelect(true);
		
		FormLayoutContainer bottomButtons = FormLayoutContainer.createButtonLayout("bottomButtons", getTranslator());
		usersLayout.add("buttomButtons", bottomButtons);
		bottomButtons.setElementCssClass("o_button_group");
		removeUserButton = uifactory.addFormLink("groups.users.remove", bottomButtons, Link.BUTTON);
		
		loadUsersModel();
	}

	private void loadUsersModel() {
		List<Identity> users = appointmentsService.getUserRestrictions(topic);
		List<UserPropertiesRow> rows = new ArrayList<>(users.size());
		for(Identity member:users) {
			rows.add(new UserPropertiesRow(member, userPropertyHandlers, getLocale()));
		}
		usersTableModel.setObjects(rows);
		usersTableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == courseEl) {
			doUpdateGroups();
		} else if (source == businessGroupEl) {
			doUpdateGroups();
		} else if (source == curriculumEl) {
			doUpdateGroups();
		} else if (addUserButton == source) {
			doSearchUser(ureq);
		} else if (removeUserButton == source) {
			doConfirmRemoveAllUsers(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<UserPropertiesRow> rows = (List<UserPropertiesRow>)confirmRemoveCtrl.getUserObject();
				doRemoveUsers(rows);
			}
		} else if (userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddUser(toAdd);
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddUser(multiEvent.getChosenIdentities());
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

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doUpdateGroups() {
		List<Group> groups = new ArrayList<>();
		if (courseEl.isAtLeastSelected(1)) {
			Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
			groups.add(entryBaseGroup);
		}
		if (keyToBussinesGroups != null) {
			for (String key : businessGroupEl.getSelectedKeys()) {
				Group group = keyToBussinesGroups.get(key);
				groups.add(group);
			}
		}
		if (keyToCurriculumElementGroup != null) {
			for (String key : curriculumEl.getSelectedKeys()) {
				Group group = keyToCurriculumElementGroup.get(key);
				groups.add(group);
			}
		}
		topic = appointmentsService.getTopic(topic);
		if (topic.getGroup() != null) {
			groups.add(topic.getGroup());
		}
		appointmentsService.restrictTopic(topic, groups);
	}
	
	private void doSearchUser(UserRequest ureq) {
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		listenTo(userSearchCtrl);
		
		String title = translate("groups.users.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddUser(List<Identity> identities) {
		for (Identity identity : identities) {
			appointmentsService.addTopicRestriction(topic, identity);
		}
		doUpdateGroups();
		loadUsersModel();
	}
	
	private void doConfirmRemoveAllUsers(UserRequest ureq) {
		Set<Integer> selectedRows = usersTableEl.getMultiSelectedIndex();
		if (selectedRows.isEmpty()) {
			showWarning("error.user.remove.atleastone");
		} else {
			List<UserPropertiesRow> rows = new ArrayList<>(selectedRows.size());
			for (Integer selectedRow:selectedRows) {
				rows.add(usersTableModel.getObject(selectedRow.intValue()));
			}
			String title = translate("groups.users.remove.confirm.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("groups.users.remove.confirm.text"), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}

	private void doRemoveUsers(List<UserPropertiesRow> rows) {
		for (UserPropertiesRow row : rows) {
			IdentityRefImpl identityRef = new IdentityRefImpl(row.getIdentityKey());
			appointmentsService.removeTopicRestriction(topic, identityRef);
		}
		doUpdateGroups();
		loadUsersModel();
	}

	@Override
	protected void doDispose() {
		//
	}

}
