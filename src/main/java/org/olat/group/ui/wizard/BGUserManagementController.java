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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
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
	
	private VelocityContainer mainVC;
	private Link addOwner, addParticipant;
	private Link okLink, cancelLink;
	private TableController usersCtrl;
	private UserSearchController addOwnerCtrl;
	private UserSearchController addParticipantCtrl;
	private CloseableModalController cmc;
	private BGUserManagementGroupTableDataModel userTableModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private final List<BusinessGroup> groups;
	private final BaseSecurity securityManager;
	private final List<Identity> addedOwners = new ArrayList<Identity>();
	private final List<Identity> addedParticipants = new ArrayList<Identity>();
	private final List<Identity> removedIdentities = new ArrayList<Identity>();

	public BGUserManagementController(UserRequest ureq, WindowControl wControl, List<BusinessGroup> groups) {
		super(ureq, wControl);
		this.groups = groups;
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
		
		mainVC = createVelocityContainer("users");
		
		addOwner = LinkFactory.createButton("users.addparticipant", mainVC, this);
		addParticipant = LinkFactory.createButton("users.addowner", mainVC, this);

		Translator userTrans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());;
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("resources.noresources"));
		usersCtrl = new TableController(tableConfig, ureq, getWindowControl(), userTrans);
		listenTo(usersCtrl);

		userTableModel = new BGUserManagementGroupTableDataModel(Collections.<Identity>emptyList(), getLocale(), userPropertyHandlers);
		initGroupTable();
		usersCtrl.setTableDataModel(userTableModel);
		
		mainVC.put("users", usersCtrl.getInitialComponent());
		
		

		okLink = LinkFactory.createButton("ok", mainVC, this);
		cancelLink = LinkFactory.createButton("cancel", mainVC, this);
		
		
		putInitialPanel(mainVC);

		loadModel();
	}
	
	protected void initGroupTable() {			
		// first the login name
		DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.user.login", 0, COMMAND_VCARD, getLocale());
		cd0.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		
		usersCtrl.addColumnDescriptor(cd0);

		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			usersCtrl.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i+1, null, getLocale()));
		}

		usersCtrl.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_SELECTUSER, "table.subject.action", translate("action.general")));
		usersCtrl.addMultiSelectAction("remove", COMMAND_REMOVEUSER);
		usersCtrl.setMultiSelect(true);
	}
	
	private void loadModel() {
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		
		for(BusinessGroup group:groups) {
			if(group.getOwnerGroup() != null) {
				secGroups.add(group.getOwnerGroup());
			}
			if(group.getPartipiciantGroup() != null) {
				secGroups.add(group.getPartipiciantGroup());
			}
			if(group.getWaitingGroup() != null) {
				secGroups.add(group.getWaitingGroup());
			}
		}
		
		List<Identity> identities = securityManager.getIdentitiesOfSecurityGroups(secGroups);
		userTableModel.setObjects(identities);
		usersCtrl.modelChanged();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanupPopup();
		} else if (source == addOwnerCtrl || source == addParticipantCtrl) {
			List<Identity> identitiesToAdd = extractIdentities(event);
			if (source == addOwnerCtrl) {
				addedParticipants.addAll(identitiesToAdd);
				userTableModel.getObjects().addAll(identitiesToAdd);
			} else if(source == addParticipantCtrl) {
				addedParticipants.addAll(identitiesToAdd);
				userTableModel.getObjects().addAll(identitiesToAdd);
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
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == addOwner) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(addOwnerCtrl);
			
			addOwnerCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);			
			listenTo(addOwnerCtrl);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addOwnerCtrl.getInitialComponent(), true, translate("users.addowner"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == addParticipant) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(addParticipantCtrl);
			
			addParticipantCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);			
			listenTo(addParticipantCtrl);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addParticipantCtrl.getInitialComponent(), true, translate("users.addowner"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == okLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == cancelLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	private void removeIdentities(List<Identity> toRemove) {
		userTableModel.getObjects().removeAll(toRemove);
		addedOwners.removeAll(toRemove);
		addedParticipants.removeAll(toRemove);
		removedIdentities.addAll(toRemove);
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
		removeAsListenerAndDispose(addOwnerCtrl);
		removeAsListenerAndDispose(addParticipantCtrl);
		cmc = null;
		addOwnerCtrl = null;
		addParticipantCtrl = null;
	}


}
