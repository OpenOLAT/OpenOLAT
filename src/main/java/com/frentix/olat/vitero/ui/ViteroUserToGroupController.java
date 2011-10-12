/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.manager.VmsNotAvailableException;
import com.frentix.olat.vitero.model.GroupRole;
import com.frentix.olat.vitero.model.ViteroBooking;

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
	
	private TableController tableCtlr;
	
	public ViteroUserToGroupController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores, ViteroBooking booking) {
		super(ureq, wControl);
		
		this.ores = ores;
		this.group = group;
		this.booking = booking;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		repositoryManager = RepositoryManager.getInstance();
		securityManager = BaseSecurityManager.getInstance();

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("vc.table.empty"));
		
		removeAsListenerAndDispose(tableCtlr);
		tableCtlr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtlr);
		
		tableCtlr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", Col.firstName.ordinal(), null, ureq.getLocale()));
		tableCtlr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", Col.lastName.ordinal(), null, ureq.getLocale()));
		tableCtlr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", Col.email.ordinal(), null, ureq.getLocale()));
		tableCtlr.addColumnDescriptor(new SignColumnDescriptor("vc.table.end", Col.sign.ordinal(), ureq.getLocale(), getTranslator()));
		
		loadModel();
		
		putInitialPanel(tableCtlr.getInitialComponent());
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
		if(source == tableCtlr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				Identity identity = (Identity)tableCtlr.getTableDataModel().getObject(row);
				if("signin".equals(e.getActionId())) {
					signIn(ureq, identity);
					loadModel();
				} else if("signout".equals(e.getActionId())) {
					signOut(ureq, identity);
					loadModel();
				}
			}
			
		}
		super.event(ureq, source, event);
	}
	
	private void signIn(UserRequest ureq, Identity identity) {
		try {
			ResourceMembers members = ((UserToGroupDataModel)tableCtlr.getTableDataModel()).getMembers();
			boolean upgrade = members.getCoaches().contains(identity) || members.getOwners().contains(identity);
			GroupRole role = upgrade ? GroupRole.teamleader : null;
			if(viteroManager.addToRoom(booking, identity, role)) {
				showInfo("signin.ok");
			} else {
				showInfo("signin.nok");
			}
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	private void signOut(UserRequest ureq, Identity identity) {
		try {
			if(viteroManager.removeFromRoom(booking, identity)) {
				showInfo("signout.ok");
			} else {
				showInfo("signout.nok");
			}
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	private void loadModel() {
		try {
			List<Identity> identitiesInGroup = viteroManager.getIdentitiesInBooking(booking);
			ResourceMembers members = getIdentitiesInResource();
			tableCtlr.setTableDataModel(new UserToGroupDataModel(members, identitiesInGroup));
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
		private final List<Identity> identitiesInGroup;
		
		public UserToGroupDataModel(ResourceMembers members, List<Identity> identitiesInGroup) {
			this.members = members;
			this.identitiesInGroup = identitiesInGroup;
			
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
			return 4;
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
				case sign: {
					if(identitiesInGroup.contains(identity)) {
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
			return new UserToGroupDataModel(new ResourceMembers(), identitiesInGroup);
		}
	}
	
	public enum Col {
		firstName,
		lastName,
		email,
		sign,
	}
}