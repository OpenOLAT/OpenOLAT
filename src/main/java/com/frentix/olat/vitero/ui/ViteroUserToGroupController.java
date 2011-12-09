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
package com.frentix.olat.vitero.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
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
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.manager.VmsNotAvailableException;
import com.frentix.olat.vitero.model.GroupRole;
import com.frentix.olat.vitero.model.ViteroBooking;
import com.frentix.olat.vitero.model.ViteroGroupRoles;

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
	private final ViteroManager viteroManager;
	private final RepositoryManager repositoryManager;
	private final BaseSecurity securityManager;
	private CourseGroupManager courseGroupManager;
	private final BusinessGroup group;
	private final OLATResourceable ores;
	
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	
	public ViteroUserToGroupController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores, ViteroBooking booking) {
		super(ureq, wControl);
		
		this.ores = ores;
		this.group = group;
		this.booking = booking;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		repositoryManager = RepositoryManager.getInstance();
		securityManager = BaseSecurityManager.getInstance();
		
		mainVC = createVelocityContainer("user_admin");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("vc.table.empty"));
		
		Translator trans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans, true);
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.firstName.ordinal(), UserConstants.FIRSTNAME, ureq.getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.lastName.ordinal(), UserConstants.LASTNAME, ureq.getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.email.ordinal(), UserConstants.EMAIL, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("user.role", Col.role.ordinal(),null, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new RoleCellRenderer(getTranslator())));
		tableCtr.addColumnDescriptor(new SignColumnDescriptor("signin", Col.sign.ordinal(), ureq.getLocale(), getTranslator()));
		
		tableCtr.addMultiSelectAction("signin", "signin");
		tableCtr.addMultiSelectAction("signout", "signout");
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
					signIn(ureq, Collections.singletonList(identity));
				} else if("signout".equals(e.getActionId())) {
					signOut(ureq, Collections.singletonList(identity));
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent e = (TableMultiSelectEvent)event;
				List<Identity> identities = new ArrayList<Identity>();
				for (int i = e.getSelection().nextSetBit(0); i >= 0; i = e.getSelection().nextSetBit(i + 1)) {
					Identity identity = (Identity)tableCtr.getTableDataModel().getObject(i);
					identities.add(identity);
				}
				if("signin".equals(e.getAction())) {
					signIn(ureq, identities);
				} else if("signout".equals(e.getAction())) {
					signOut(ureq, identities);
				} else if("reload".equals(e.getAction())) {
					loadModel();
				}
			}
			
		}
		super.event(ureq, source, event);
	}
	
	private void signIn(UserRequest ureq, List<Identity> identities) {
		try {
			ResourceMembers members = ((UserToGroupDataModel)tableCtr.getTableDataModel()).getMembers();
			
			for(Identity identity:identities) {
				boolean upgrade = members.getCoaches().contains(identity) || members.getOwners().contains(identity);
				GroupRole role = upgrade ? GroupRole.teamleader : null;
				if(viteroManager.addToRoom(booking, identity, role)) {
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
	
	private void signOut(UserRequest ureq, List<Identity> identities) {
		try {
			for(Identity identity:identities) {
				if(viteroManager.removeFromRoom(booking, identity)) {
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
			
			ResourceMembers members = getIdentitiesInResource();
			tableCtr.setTableDataModel(new UserToGroupDataModel(members, groupRoles));
			
			int numOfFreePlaces = booking.getRoomSize() - groupRoles.size();
			mainVC.contextPut("freePlaces", new String[]{Integer.toString(numOfFreePlaces)});
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	private ResourceMembers getIdentitiesInResource() {
		Set<Identity> owners = new HashSet<Identity>();
		Set<Identity> coaches = new HashSet<Identity>();
		Set<Identity> participants = new HashSet<Identity>();
		
		if(group != null) {
			owners.addAll(securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup()));
			participants.addAll(securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup()));
		} else {
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(ores, false);
			if ("CourseModule".equals(ores.getResourceableTypeName())) {
				if(courseGroupManager == null) {
					ICourse course = CourseFactory.loadCourse(ores);
					courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
				}
				coaches.addAll(courseGroupManager.getCoachesFromArea(null));
				coaches.addAll(courseGroupManager.getCoachesFromLearningGroup(null));
				participants.addAll(courseGroupManager.getParticipantsFromArea(null));
				participants.addAll(courseGroupManager.getParticipantsFromLearningGroup(null));
			}
			
			List<Identity> repoOwners = securityManager.getIdentitiesOfSecurityGroup(repoEntry.getOwnerGroup());
			owners.addAll(repoOwners);
			
			if(repoEntry.getParticipantGroup() != null) {
				List<Identity> repoParticipants = securityManager.getIdentitiesOfSecurityGroup(repoEntry.getParticipantGroup());
				participants.addAll(repoParticipants);
			}
			if(repoEntry.getTutorGroup() != null) {
				List<Identity> repoTutors = securityManager.getIdentitiesOfSecurityGroup(repoEntry.getTutorGroup());
				coaches.addAll(repoTutors);
			}
		}
		return new ResourceMembers(owners, coaches, participants);
	}
	
	public class ResourceMembers {
		private final List<Identity> owners = new ArrayList<Identity>();
		private final List<Identity> coaches = new ArrayList<Identity>();
		private final List<Identity> participants = new ArrayList<Identity>();
		
		public ResourceMembers() {
			//
		}
		
		public ResourceMembers(Collection<Identity> owners, Collection<Identity> coaches, Collection<Identity> participants) {
			this.owners.addAll(owners);
			this.coaches.addAll(coaches);
			this.participants.addAll(participants);
			
			//remove duplicates
			coaches.removeAll(owners);
			participants.removeAll(owners);
			participants.removeAll(coaches);
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
	}
	
	public class UserToGroupDataModel implements TableDataModel {
		
		private ResourceMembers members;
		private List<Identity> identities;
		private final ViteroGroupRoles groupRoles;
		
		public UserToGroupDataModel(ResourceMembers members, ViteroGroupRoles groupRoles) {
			this.members = members;
			this.groupRoles = groupRoles;
			
			identities = new ArrayList<Identity>();
			identities.addAll(members.getOwners());
			identities.addAll(members.getCoaches());
			identities.addAll(members.getParticipants());
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
		public void setObjects(List objects) {
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