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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroGroupRoles;
import org.olat.modules.vitero.model.ViteroStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroUserToGroupController extends BasicController {
	
	private final ViteroBooking booking;

	private final BusinessGroup group;
	private final OLATResourceable ores;
	
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	
	@Autowired
	private ViteroManager viteroManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public ViteroUserToGroupController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores, ViteroBooking booking, boolean readOnly) {
		super(ureq, wControl);
		
		this.ores = ores;
		this.group = group;
		this.booking = booking;
		
		mainVC = createVelocityContainer("user_admin");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("users.empty"), null, "o_icon_user");
		
		Translator trans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans, true);
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.firstName.ordinal(), UserConstants.FIRSTNAME, getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.lastName.ordinal(), UserConstants.LASTNAME, getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.email.ordinal(), UserConstants.EMAIL, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("user.role", Col.role.ordinal(),null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new RoleCellRenderer(getTranslator())){

			@Override
			public int compareTo(int rowa, int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
				
				String r1 = null;
				if(a instanceof GroupRole) {
					r1 = ((GroupRole)a).name();
				} else if(a instanceof String) {
					r1 = ("owner".equals(a) || "coach".equals(a)) ? GroupRole.teamleader.name() : (String)a;
				}
				
				String r2 = null;
				if(b instanceof GroupRole) {
					r2 = ((GroupRole)b).name();
				} else if(b instanceof String) {
					r2 = ("owner".equals(b) || "coach".equals(b)) ? GroupRole.teamleader.name() : (String)b;
				}

				return super.compareString(r1, r2);
			}
		});
		
		if(!readOnly) {
			tableCtr.addColumnDescriptor(new SignColumnDescriptor("signin", Col.sign.ordinal(), getLocale(), getTranslator()));
			tableCtr.addMultiSelectAction("signin", "signin");
			tableCtr.addMultiSelectAction("signout", "signout");
		}
		tableCtr.addMultiSelectAction("reload", "reload");
		tableCtr.setMultiSelect(true);

		loadModel();
		mainVC.put("userTable", tableCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private ColumnDescriptor getColumnDescriptor(int pos, String attrName, Locale locale) {
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getAllUserPropertyHandlers();
		for(UserPropertyHandler handler:userPropertyHandlers) {
			if(handler.getName().equals(attrName)) {
				return handler.getColumnDescriptor(pos, null, locale);
			}
		}
		return null;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				Identity identity = (Identity)tableCtr.getTableDataModel().getObject(row);
				if("signin".equals(e.getActionId())) {
					signIn(Collections.singletonList(identity));
				} else if("signout".equals(e.getActionId())) {
					signOut(Collections.singletonList(identity));
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent e = (TableMultiSelectEvent)event;
				List<Identity> identities = new ArrayList<>();
				for (int i = e.getSelection().nextSetBit(0); i >= 0; i = e.getSelection().nextSetBit(i + 1)) {
					Identity identity = (Identity)tableCtr.getTableDataModel().getObject(i);
					identities.add(identity);
				}
				if("signin".equals(e.getAction())) {
					signIn(identities);
				} else if("signout".equals(e.getAction())) {
					signOut(identities);
				} else if("reload".equals(e.getAction())) {
					loadModel();
				}
			}
			
		}
		super.event(ureq, source, event);
	}
	
	private void signIn(List<Identity> identities) {
		try {
			ResourceMembers members = ((UserToGroupDataModel)tableCtr.getTableDataModel()).getMembers();
			
			for(Identity identity:identities) {
				boolean upgrade = members.getCoaches().contains(identity) || members.getOwners().contains(identity);
				GroupRole role = upgrade ? GroupRole.teamleader : null;
				ViteroStatus status = viteroManager.addToRoom(booking, identity, role);
				if(status.isOk()) {
					showInfo("signin.ok");
				} else {
					showInfo("signin.nok");
					break;
				}
			}
			
			loadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	private void signOut(List<Identity> identities) {
		try {
			for(Identity identity:identities) {
				ViteroStatus status = viteroManager.removeFromRoom(booking, identity);
				if(status.isOk()) {
					showInfo("signout.ok");
				} else {
					showInfo("signout.nok");
					break;
				}
			}
			loadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	private void loadModel() {
		try {
			ViteroGroupRoles groupRoles = viteroManager.getGroupRoles(booking.getGroupId());
			
			ResourceMembers members = getIdentitiesInResource(groupRoles);
			tableCtr.setTableDataModel(new UserToGroupDataModel(members, groupRoles));
			
			int numOfFreePlaces = booking.getRoomSize() - groupRoles.size();
			mainVC.contextPut("freePlaces", new String[]{Integer.toString(numOfFreePlaces)});
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	private ResourceMembers getIdentitiesInResource(ViteroGroupRoles groupRoles) {
		Set<Identity> owners = new HashSet<>();
		Set<Identity> coaches = new HashSet<>();
		Set<Identity> participants = new HashSet<>();
		Set<Identity> selfParticipants = new HashSet<>();
		
		if(group != null) {
			owners.addAll(businessGroupService.getMembers(group, GroupRoles.coach.name()));
			participants.addAll(businessGroupService.getMembers(group, GroupRoles.participant.name()));
		} else {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(ores, false);
			List<Identity> repoOwners = repositoryService.getMembers(repoEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			owners.addAll(repoOwners);
			List<Identity> repoParticipants = repositoryService.getMembers(repoEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
			participants.addAll(repoParticipants);
			List<Identity> repoCoaches = repositoryService.getMembers(repoEntry, RepositoryEntryRelationType.all, GroupRoles.coach.name());
			coaches.addAll(repoCoaches);
		}
		
		//add all self signed participants
		if(booking.isAutoSignIn()) {
			List<String> emailsOfParticipants = new ArrayList<>(groupRoles.getEmailsOfParticipants());
			//remove owners, coaches and already participating users
			for(Identity owner:owners) {
				emailsOfParticipants.remove(owner.getUser().getProperty(UserConstants.EMAIL, null));
			}
			for(Identity coach:coaches) {
				emailsOfParticipants.remove(coach.getUser().getProperty(UserConstants.EMAIL, null));
			}
			for(Identity participant:participants) {
				emailsOfParticipants.remove(participant.getUser().getProperty(UserConstants.EMAIL, null));
			}

			if(!emailsOfParticipants.isEmpty()) {
				List<Identity> selfSignedParticipants =  UserManager.getInstance().findIdentitiesByEmail(emailsOfParticipants);
				selfParticipants.addAll(selfSignedParticipants);
			}
		}
		return new ResourceMembers(owners, coaches, participants, selfParticipants);
	}
	
	public class ResourceMembers {
		private final List<Identity> owners = new ArrayList<>();
		private final List<Identity> coaches = new ArrayList<>();
		private final List<Identity> participants = new ArrayList<>();
		private final List<Identity> selfParticipants = new ArrayList<>();
		
		public ResourceMembers() {
			//
		}
		
		public ResourceMembers(Collection<Identity> owners, Collection<Identity> coaches, Collection<Identity> participants,
				Collection<Identity> selfParticipants) {
			this.owners.addAll(owners);
			this.coaches.addAll(coaches);
			this.participants.addAll(participants);
			this.selfParticipants.addAll(selfParticipants);
			
			//remove duplicates
			this.coaches.removeAll(owners);
			
			this.participants.removeAll(owners);
			this.participants.removeAll(coaches);
			
			this.selfParticipants.removeAll(owners);
			this.selfParticipants.removeAll(coaches);
			this.selfParticipants.removeAll(participants);
		}

		public List<Identity> getOwners() {
			return owners;
		}

		public List<Identity> getCoaches() {
			return coaches;
		}

		public List<Identity> getParticipants() {
			return participants;
		}

		public List<Identity> getSelfParticipants() {
			return selfParticipants;
		}
	}
	
	public class UserToGroupDataModel implements TableDataModel<Identity> {
		
		private ResourceMembers members;
		private List<Identity> identities;
		private final ViteroGroupRoles groupRoles;
		
		public UserToGroupDataModel(ResourceMembers members, ViteroGroupRoles groupRoles) {
			this.members = members;
			this.groupRoles = groupRoles;
			
			identities = new ArrayList<>();
			identities.addAll(members.getOwners());
			identities.addAll(members.getCoaches());
			identities.addAll(members.getParticipants());
			identities.addAll(members.getSelfParticipants());
		}

		public ResourceMembers getMembers() {
			return members;
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return identities == null ? 0 : identities.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Identity identity = getObject(row);
			switch(Col.values()[col]) {
				case firstName: return identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
				case lastName: return identity.getUser().getProperty(UserConstants.LASTNAME, null);
				case email: return identity.getUser().getProperty(UserConstants.EMAIL, null);
				case role: {
					String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
					GroupRole role = groupRoles.getEmailsToRole().get(email);
					if(role != null) {
						return role;
					}
					if(members == null) return null;
					if(members.owners.contains(identity)) {
						return "owner";
					}
					if(members.coaches.contains(identity)) {
						return "coach";
					}
					return null;
				}
				case sign: {
					String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
					if(groupRoles.getEmailsOfParticipants().contains(email)) {
						return Sign.signout;
					}
					return Sign.signin;
				}
			}
			return null;
		}

		@Override
		public Identity getObject(int row) {
			return identities.get(row);
		}

		@Override
		public void setObjects(List<Identity> objects) {
			this.identities = objects;
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new UserToGroupDataModel(new ResourceMembers(), groupRoles);
		}
	}
	
	public enum Col {
		firstName,
		lastName,
		email,
		role,
		sign,
	}
}