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
package org.olat.course.noderight.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRight.EditMode;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.ui.NodeRightGrantDataModel.GrantCols;
import org.olat.course.noderight.ui.NodeRightGrantDataModel.NodeRightGrantRow;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightsController extends FormBasicController {

	private static final String CMD_SHOW_ADVANCED = "show.advanced";
	private static final String CMD_HIDE_ADVANCED = "hide.advanced";
	private static final String CMD_ADD_ROLES = "add.roles";
	private static final String CMD_ADD_IDENTITIES = "add.identities";
	private static final String CMD_ADD_GROUPS = "add.groups";
	private static final String CMD_DELETE = "delete";
	private static final String EL_NAME_START = "nr_start_";
	private static final String EL_NAME_END = "nr_end_";
	private static final NodeRightGrantRowComparator ROWS_COMPARATOR = new NodeRightGrantRowComparator();
	
	private CloseableModalController cmc;
	private DialogBoxController confirmRegularCrtl;
	private AddRolesController addRolesCtrl;
	private AddGroupsController addGroupsCtrl;
	private StepsMainRunController wizard;

	private final CourseGroupManager courseGroupManager;
	// Keep the types because the order is used for display.
	private final List<NodeRightType> types;
	private final ModuleConfiguration moduleConfigs;
	private final Map<String, NodeRightWrapper> identifierToWrapper = new HashMap<>();
	private final String contextHelp;
	private final boolean groupsAvailable;
	private int counter = 0;
	
	@Autowired
	private NodeRightService nodeRightService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	

	public NodeRightsController(UserRequest ureq, WindowControl wControl, CourseGroupManager courseGroupManager,
			List<NodeRightType> types, ModuleConfiguration moduleConfigs, String contextHelp) {
		super(ureq, wControl);
		this.courseGroupManager = courseGroupManager;
		this.types = types;
		this.moduleConfigs = moduleConfigs;
		this.contextHelp = contextHelp;
		this.groupsAvailable = courseGroupManager.hasBusinessGroups();
		
		for (NodeRightType type : types) {
			NodeRightWrapper wrapper = new NodeRightWrapper();
			identifierToWrapper.put(type.getIdentifier(), wrapper);
			wrapper.setType(type);
			
			NodeRight nodeRight = nodeRightService.getRight(moduleConfigs, type);
			wrapper.setNodeRight(nodeRight);
		}
		
		initForm(ureq);
	}
	
	/**
	 * Set the visibility of a type.
	 *
	 * @param type
	 * @param visible
	 */
	public void setVisible(NodeRightType type, boolean visible) {
		NodeRightWrapper wrapper = identifierToWrapper.get(type.getIdentifier());
		if (wrapper != null && wrapper.getContainer() != null) {
			wrapper.getContainer().setVisible(visible);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title");
		if (StringHelper.containsNonWhitespace(contextHelp)) {
			setFormContextHelp(contextHelp);
		}
		
		for (NodeRightType type : types) {
			FormLayoutContainer cont = FormLayoutContainer.createCustomFormLayout("nr_cont_" + counter++, getTranslator(), velocity_root + "/node_right.html");
			String label = type.getTranslatorBaseClass() != null
					? Util.createPackageTranslator(type.getTranslatorBaseClass(), getLocale()).translate(type.getI18nKey())
					: type.getIdentifier();
			cont.setLabel(label, null, false);
			cont.setRootForm(mainForm);
			formLayout.add(cont);
			
			NodeRightWrapper wrapper = identifierToWrapper.get(type.getIdentifier());
			wrapper.setContainer(cont);
			
			initContainer(wrapper);
		}
	}

	private void initContainer(NodeRightWrapper wrapper) {
		FormLayoutContainer cont = wrapper.getContainer();
		
		Boolean advanced = EditMode.advanced == wrapper.getNodeRight().getEditMode()? Boolean.TRUE: Boolean.FALSE;
		cont.contextPut("advanced", advanced);
		
		if (wrapper.getType().isCssClassEnabled()) {
			String typeCssClass = "o_nr_role_" + wrapper.getType().getIdentifier().replace(".", "_");
			cont.contextPut("typeCssClass", typeCssClass);
		}
		
		FormLink showAdvandesLink = uifactory.addFormLink("show.advanced", CMD_SHOW_ADVANCED, "off", null, cont, Link.LINK);
		showAdvandesLink.setCustomEnabledLinkCSS("o_button_toggle");
		showAdvandesLink.setIconLeftCSS("o_icon o_icon_toggle");
		showAdvandesLink.setUserObject(wrapper);
		
		FormLink hideAdvancedLink = uifactory.addFormLink("hide.advanced", CMD_HIDE_ADVANCED, "on", null, cont, Link.LINK);
		hideAdvancedLink.setCustomEnabledLinkCSS("o_button_toggle o_on");
		hideAdvancedLink.setIconRightCSS("o_icon o_icon_toggle");
		hideAdvancedLink.setUserObject(wrapper);
		
		initRegular(wrapper);
		initAdvanced(wrapper);
	}

	private void initRegular(NodeRightWrapper wrapper) {
		FormLayoutContainer cont = wrapper.getContainer();
		
		Collection<NodeRightRole> roles = wrapper.getType().getRoles();
		KeyValues rolesKV = new KeyValues();
		addRole(rolesKV, roles, NodeRightRole.owner);
		addRole(rolesKV, roles, NodeRightRole.coach);
		addRole(rolesKV, roles, NodeRightRole.participant);
		addRole(rolesKV, roles, NodeRightRole.guest);
		String name = "nr_role_" + counter++;
		MultipleSelectionElement rolesEl = uifactory.addCheckboxesVertical(name, "roles", cont, rolesKV.keys(), rolesKV.values(), null, 1);
		rolesEl.addActionListener(FormEvent.ONCHANGE);
		rolesEl.setUserObject(wrapper);
		wrapper.setRolesEl(rolesEl);
		cont.contextPut("rolesName", name);
		
		for (NodeRightGrant grant : wrapper.getNodeRight().getGrants()) {
			NodeRightRole role = grant.getRole();
			if (role != null && rolesEl.getKeys().contains(role.name())) {
				rolesEl.select(role.name(), true);
			}
		}
	}
	
	private void addRole(KeyValues rolesKV, Collection<NodeRightRole> roles, NodeRightRole role) {
		if (roles.contains(role)) {
			rolesKV.add(KeyValues.entry(role.name(), translateRole(role)));
		}
	}
	
	private String translateRole(NodeRightRole role) {
		return translate("role." + role.name().toLowerCase());
	}

	private void initAdvanced(NodeRightWrapper wrapper) {
		FormLayoutContainer cont = wrapper.getContainer();
		
		FormLayoutContainer topButtonCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		topButtonCont.setElementCssClass("o_button_group o_button_group_right");
		cont.add(topButtonCont);
		
		FormLink addRolesLink = uifactory.addFormLink("add.roles", CMD_ADD_ROLES, "add.roles", topButtonCont, Link.BUTTON);
		addRolesLink.setUserObject(wrapper);
		wrapper.setAddRolesLink(addRolesLink);

		FormLink addIdentitiesLink = uifactory.addFormLink("add.identities", CMD_ADD_IDENTITIES, "add.identities", topButtonCont, Link.BUTTON);
		addIdentitiesLink.setUserObject(wrapper);

		if (groupsAvailable) {
			FormLink addGroupsLink = uifactory.addFormLink("add.groups", CMD_ADD_GROUPS, "add.groups", topButtonCont, Link.BUTTON);
			addGroupsLink.setUserObject(wrapper);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GrantCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GrantCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GrantCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GrantCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CMD_DELETE, translate(GrantCols.delete.i18nHeaderKey()), CMD_DELETE));
		
		NodeRightGrantDataModel dataModel = new NodeRightGrantDataModel(columnsModel, getLocale());
		wrapper.setDataModel(dataModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), cont);
		tableEl.setUserObject(wrapper);
		wrapper.setTableEl(tableEl);
		refreshTable(wrapper);
	}
	
	private void refreshTable(NodeRightWrapper wrapper) {
		loadModel(wrapper);
		updateUI(wrapper);
	}
	
	private void loadModel(NodeRightWrapper wrapper) {
		Collection<NodeRightRole> roles = wrapper.getType().getRoles();
		Collection<NodeRightGrant> grants = wrapper.getNodeRight().getGrants();
		List<NodeRightGrantRow> rows = new ArrayList<>(grants.size());
			
		for (NodeRightGrant grant : grants) {
			NodeRightGrantRow row = new NodeRightGrantRow(grant);
			row.setWrapper(wrapper);
			String name = null;
			
			if (grant.getIdentityRef() != null) {
				name = userManager.getUserDisplayName(grant.getIdentityRef());
				row.setType(translate("grant.type.identity"));
			} else if (grant.getRole() != null && roles.contains(grant.getRole())) {
				name = translateRole(grant.getRole());
				row.setType(translate("grant.type.role"));
			} else if (grant.getBusinessGroupRef() != null) {
				BusinessGroup group = businessGroupService.loadBusinessGroup(grant.getBusinessGroupRef().getKey());
				if (group != null) {
					name = group.getName();
					row.setType(translate("grant.type.group"));
				}
			}
			
			if (!StringHelper.containsNonWhitespace(name)) {
				continue;
			}
			row.setName(name);

			DateChooser startEl = uifactory.addDateChooser(EL_NAME_START + counter++, null, grant.getStart(), wrapper.getContainer());
			startEl.setDateChooserTimeEnabled(true);
			startEl.addActionListener(FormEvent.ONCHANGE);
			startEl.setUserObject(row);
			row.setStartEl(startEl);
			DateChooser endEl = uifactory.addDateChooser(EL_NAME_END + counter++, null, grant.getEnd(), wrapper.getContainer());
			endEl.setDateChooserTimeEnabled(true);
			endEl.addActionListener(FormEvent.ONCHANGE);
			endEl.setUserObject(row);
			row.setEndEl(endEl);

			rows.add(row);
		}
		
		rows.sort(ROWS_COMPARATOR);
		wrapper.getDataModel().setObjects(rows);
		wrapper.getTableEl().reset(true, true, true);
	}
	
	private void updateUI(NodeRightWrapper wrapper) {
		List<NodeRightRole> selectedRoles = wrapper.getNodeRight().getGrants().stream()
				.map(NodeRightGrant::getRole)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		MultipleSelectionElement rolesEl = wrapper.getRolesEl();
		for (String key : rolesEl.getKeys()) {
			boolean granted = selectedRoles.contains(NodeRightRole.valueOf(key));
			rolesEl.select(key, granted);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_SHOW_ADVANCED.equals(cmd)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				doSetMode(ureq, wrapper, EditMode.advanced);
			} else if (CMD_HIDE_ADVANCED.equals(cmd)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				doConfirmRegularMode(ureq, wrapper);
			} else if (CMD_ADD_ROLES.equals(cmd)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				doAddRoles(ureq, wrapper);
			} else if (CMD_ADD_IDENTITIES.equals(cmd)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				doAddIdentities(ureq, wrapper);
			} else if (CMD_ADD_GROUPS.equals(cmd)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				doAddGroups(ureq, wrapper);
			}
		} else if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement rolesEl = (MultipleSelectionElement)source;
			NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
			doSetRoles(ureq, wrapper, rolesEl);
		} else if (source instanceof DateChooser) {
			DateChooser dateChooser = (DateChooser)source;
			String name = dateChooser.getName();
			if (name.startsWith(EL_NAME_START)) {
				NodeRightGrantRow row = (NodeRightGrantRow)source.getUserObject();
				row.getGrant().setStart(dateChooser.getDate());
				NodeRight nodeRight = row.getWrapper().getNodeRight();
				doSave(ureq, nodeRight);
			} else if (name.startsWith(EL_NAME_END)) {
				NodeRightGrantRow row = (NodeRightGrantRow)source.getUserObject();
				row.getGrant().setEnd(dateChooser.getDate());
				NodeRight nodeRight = row.getWrapper().getNodeRight();
				doSave(ureq, nodeRight);
			}
		} else if (event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			if(CMD_DELETE.equals(se.getCommand())) {
				NodeRightWrapper wrapper = (NodeRightWrapper)source.getUserObject();
				NodeRightGrantRow row = wrapper.getDataModel().getObject(se.getIndex());
				NodeRightGrant grant = row.getGrant();
				doDeleteGrant(ureq, wrapper, grant);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmRegularCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				NodeRightWrapper wrapper = (NodeRightWrapper)confirmRegularCrtl.getUserObject();
				doSetMode(ureq, wrapper, EditMode.regular);
			}
		} else if (addRolesCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAddRoles(ureq, addRolesCtrl.getNodeRight(), addRolesCtrl.getRoles(), addRolesCtrl.getStart(), addRolesCtrl.getEnd());
			}
			cmc.deactivate();
			cleanUp();
		} else if (addGroupsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAddGroups(ureq, addGroupsCtrl.getNodeRight(), addGroupsCtrl.getGroupKeys(), addGroupsCtrl.getStart(), addGroupsCtrl.getEnd());
			}
			cmc.deactivate();
			cleanUp();
		} else if (wizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addGroupsCtrl);
		removeAsListenerAndDispose(addRolesCtrl);
		removeAsListenerAndDispose(wizard);
		removeAsListenerAndDispose(cmc);
		addGroupsCtrl = null;
		addRolesCtrl = null;
		wizard = null;
		cmc = null;
	}
	
	private void doConfirmRegularMode(UserRequest ureq, NodeRightWrapper wrapper) {
		String title = translate("confirm.regular.title");
		String text = translate("confirm.regular.text");
		confirmRegularCrtl = activateYesNoDialog(ureq, title, text, confirmRegularCrtl);
		confirmRegularCrtl.setUserObject(wrapper);
	}

	private void doSetMode(UserRequest ureq, NodeRightWrapper wrapper, EditMode mode) {
		NodeRight nodeRight = wrapper.getNodeRight();
		nodeRightService.setEditMode(nodeRight, mode);
		Boolean advanced = EditMode.advanced == mode? Boolean.TRUE: Boolean.FALSE;
		wrapper.getContainer().contextPut("advanced", advanced);
		doSave(ureq, nodeRight);
		refreshTable(wrapper);
	}

	private void doSetRoles(UserRequest ureq, NodeRightWrapper wrapper, MultipleSelectionElement rolesEl) {
		List<NodeRightRole> roles = rolesEl.getSelectedKeys().stream()
				.map(NodeRightRole::valueOf)
				.collect(Collectors.toList());
		NodeRight nodeRight = wrapper.getNodeRight();
		nodeRightService.setRoleGrants(nodeRight, roles);
		doSave(ureq, nodeRight);
	}
	
	private void doAddRoles(UserRequest ureq, NodeRightWrapper wrapper) {
		guardModalController(addRolesCtrl);
		
		addRolesCtrl = new AddRolesController(ureq, getWindowControl(), wrapper.getType(), wrapper.getNodeRight());
		listenTo(addRolesCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addRolesCtrl.getInitialComponent(), true,
				translate("add.roles"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddRoles(UserRequest ureq, NodeRight nodeRight, List<NodeRightRole> roles, Date start, Date end) {
		List<NodeRightGrant> grants = new ArrayList<>(roles.size());
		for (NodeRightRole role : roles) {
			NodeRightGrant grant = nodeRightService.createGrant(role);
			grant.setStart(start);
			grant.setEnd(end);
			grants.add(grant);
		}
		nodeRightService.addGrants(nodeRight, grants);
		doSave(ureq, nodeRight);
		refreshTable(identifierToWrapper.get(nodeRight.getTypeIdentifier()));
	}

	private void doAddIdentities(UserRequest ureq, NodeRightWrapper wrapper) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new AddIdentities_1_SelectStep(ureq, wrapper),
				addSelectedIdentities(), null, translate("add.identities"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}
	
	private StepRunnerCallback addSelectedIdentities() {
		return (uureq, wControl, runContext) -> {
			AddIdentitiesContext context = (AddIdentitiesContext) runContext.get("context");
			NodeRightWrapper wrapper = context.getWrapper();
			NodeRight nodeRight = wrapper.getNodeRight();
			List<Identity> identities = context.getIdentities();
			Date start = context.getStart();
			Date end = context.getEnd();
			
			List<NodeRightGrant> grants = new ArrayList<>(identities.size());
			for (Identity identity : identities) {
				NodeRightGrant grant = nodeRightService.createGrant(identity);
				grant.setStart(start);
				grant.setEnd(end);
				grants.add(grant);
			}
			nodeRightService.addGrants(nodeRight, grants);
			doSave(uureq, nodeRight);
			refreshTable(wrapper);
			
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void doAddGroups(UserRequest ureq, NodeRightWrapper wrapper) {
		guardModalController(addGroupsCtrl);
		
		addGroupsCtrl = new AddGroupsController(ureq, getWindowControl(), courseGroupManager, wrapper.getNodeRight());
		listenTo(addGroupsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addGroupsCtrl.getInitialComponent(), true,
				translate("add.groups"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddGroups(UserRequest ureq, NodeRight nodeRight, Collection<Long> groupKeys, Date start, Date end) {
		List<NodeRightGrant> grants = new ArrayList<>(groupKeys.size());
		for (Long groupKey : groupKeys) {
			NodeRightGrant grant = nodeRightService.createGrant((BusinessGroupRef)(() -> groupKey));
			grant.setStart(start);
			grant.setEnd(end);
			grants.add(grant);
		}
		nodeRightService.addGrants(nodeRight, grants);
		doSave(ureq, nodeRight);
		refreshTable(identifierToWrapper.get(nodeRight.getTypeIdentifier()));
	}

	private void doDeleteGrant(UserRequest ureq, NodeRightWrapper wrapper, NodeRightGrant grant) {
		NodeRight nodeRight = wrapper.getNodeRight();
		nodeRightService.removeGrant(nodeRight, grant);
		doSave(ureq, nodeRight);
		refreshTable(wrapper);
	}

	private void doSave(UserRequest ureq, NodeRight nodeRight) {
		nodeRightService.setRight(moduleConfigs, nodeRight);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
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
