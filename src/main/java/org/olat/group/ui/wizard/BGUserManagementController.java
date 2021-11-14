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
package org.olat.group.ui.wizard;

import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.MembershipModification;
import org.olat.group.ui.main.BGRoleCellRenderer;
import org.olat.group.ui.main.BusinessGroupMembershipComparator;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGUserManagementController extends BasicController {

	protected static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();

	protected static final String COMMAND_REMOVEUSER = "removesubjectofgroup";
	protected static final String COMMAND_VCARD = "show.vcard";
	protected static final String COMMAND_SELECTUSER = "select.user";
	protected static final BusinessGroupMembershipComparator MEMBERSHIP_COMPARATOR = new BusinessGroupMembershipComparator();
	
	private VelocityContainer mainVC;
	private Link addOwner, addParticipant, addToWaitingList;
	private Link okLink, cancelLink;
	private TableController usersCtrl;
	private UserSearchController addCtrl;
	private CloseableModalController cmc;
	private BGUserManagementGroupTableDataModel userTableModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private final List<BusinessGroup> groups;
	private final BusinessGroupService businessGroupService;

	public BGUserManagementController(UserRequest ureq, WindowControl wControl, List<BusinessGroup> groups) {
		super(ureq, wControl);
		this.groups = groups;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
		
		mainVC = createVelocityContainer("users");
		
		addOwner = LinkFactory.createButton("users.addowner", mainVC, this);
		addParticipant = LinkFactory.createButton("users.addparticipant", mainVC, this);
		addToWaitingList = LinkFactory.createButton("users.addwaiting", mainVC, this);

		Translator userTrans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());;
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("resources.nomembers"), null, "o_icon_group");
		tableConfig.setTableEmptyNextPrimaryAction(translate("users.addparticipant"), "o_icon_add");
		
		usersCtrl = new TableController(tableConfig, ureq, getWindowControl(), userTrans);
		listenTo(usersCtrl);

		userTableModel = new BGUserManagementGroupTableDataModel(getLocale(), userPropertyHandlers);
		CustomCellRenderer statusRenderer = new BGUserStatusCellRenderer();
		usersCtrl.addColumnDescriptor(new CustomRenderColumnDescriptor("table.status", 0, null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, statusRenderer));
		CustomCellRenderer roleRenderer = new BGRoleCellRenderer(getLocale());
		usersCtrl.addColumnDescriptor(new CustomRenderColumnDescriptor("table.role", 1, null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			usersCtrl.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i+2, null, getLocale()));
		}
		usersCtrl.addMultiSelectAction("remove", COMMAND_REMOVEUSER);
		usersCtrl.setMultiSelect(true);
		usersCtrl.setTableDataModel(userTableModel);
		
		mainVC.put("users", usersCtrl.getInitialComponent());
		okLink = LinkFactory.createButton("ok", mainVC, this);
		cancelLink = LinkFactory.createButton("cancel", mainVC, this);
		putInitialPanel(mainVC);

		loadModel();
	}
	
	private void loadModel() {
		List<Identity> owners = businessGroupService.getMembers(groups, GroupRoles.coach.name());
		List<Identity> participants = businessGroupService.getMembers(groups, GroupRoles.participant.name());
		List<Identity> waitingList = businessGroupService.getMembers(groups, GroupRoles.waiting.name());;
		userTableModel.setMembers(owners, participants, waitingList);
		usersCtrl.modelChanged();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanupPopup();
		} else if (source == addCtrl) {
			List<Identity> identitiesToAdd = extractIdentities(event);
			GroupRoles type = (GroupRoles)addCtrl.getUserObject();
			switch(type) {
				case coach: userTableModel.addCoaches(identitiesToAdd); break;
				case participant: userTableModel.addParticipants(identitiesToAdd); break;
				case waiting: userTableModel.addToWaitingList(identitiesToAdd); break;
				default: break;
			}
			usersCtrl.modelChanged();
			cmc.deactivate();
			cleanupPopup();
		} else if (source == usersCtrl) {
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(COMMAND_REMOVEUSER)) {
					List<Identity> toRemove = userTableModel.getObjects(tmse.getSelection());
					removeIdentities(toRemove);
				}
			} else if (event.equals(TableController.EVENT_EMPTY_TABLE_NEXT_PRIMARY_ACTION)) {
				addMembership(ureq, GroupRoles.participant);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == addOwner) {
			addMembership(ureq, GroupRoles.coach);
		} else if (source == addParticipant) {
			addMembership(ureq, GroupRoles.participant);
		} else if (source == addToWaitingList) {
			addMembership(ureq, GroupRoles.waiting);
		} else if (source == okLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == cancelLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	private void addMembership(UserRequest ureq, GroupRoles type) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(addCtrl);
		
		addCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);	
		addCtrl.setUserObject(type);
		listenTo(addCtrl);
		String title;
		switch(type) {
			case coach: title = translate("users.addowner"); break;
			case participant: title = translate("users.addparticipant"); break;
			case waiting: title = translate("users.addwaiting"); break;
			default: title = "";
		}
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	public List<BusinessGroup> getGroups() {
		return groups;
	}

	public MembershipModification getModifications() {
		MembershipModification mod = new MembershipModification();
		mod.setAddOwners(userTableModel.getAddCoachesIdentities());
		mod.setAddParticipants(userTableModel.getAddParticipantIdentities());
		mod.setAddToWaitingList(userTableModel.getAddToWaitingList());
		mod.setRemovedIdentities(userTableModel.getRemovedIdentities());
		return mod;
	}
	
	private void removeIdentities(List<Identity> toRemove) {
		userTableModel.remove(toRemove);
		usersCtrl.modelChanged();
	}
	
	private List<Identity> extractIdentities(Event event) {
		if(event instanceof SingleIdentityChosenEvent) {
			SingleIdentityChosenEvent chosenEvent = (SingleIdentityChosenEvent)event;
			Identity identity = chosenEvent.getChosenIdentity();
			return Collections.singletonList(identity);
		}
		if (event instanceof MultiIdentityChosenEvent) {
			MultiIdentityChosenEvent chosenEvent = (MultiIdentityChosenEvent)event;
			List<Identity> identities = chosenEvent.getChosenIdentities();
			return identities;
		}
		return Collections.emptyList();
	}
	
	private void cleanupPopup() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(addCtrl);
		cmc = null;
		addCtrl = null;
	}


}
