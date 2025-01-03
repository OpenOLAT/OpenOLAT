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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.ui.component.CurriculumRolesComparator;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRow extends UserPropertiesRow {
	
	private Date registration;
	private Identity identity;
	private List<ResourceReservation> reservations;
	private final GroupMembershipInheritance inheritanceMode;

	private String onlineStatus;
	private FormLink toolsLink;
	private FormLink chatLink;
	private MemberDetailsController detailsCtrl;
	
	private final EnumMap<CurriculumRoles, AtomicInteger> numOfRoles = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles, AtomicInteger> numOfReservations = new EnumMap<>(CurriculumRoles.class);
	
	
	public MemberRow(Identity member, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member, userPropertyHandlers, locale);
		inheritanceMode = GroupMembershipInheritance.none;
		this.identity = member;
	}
	
	public MemberRow(CurriculumMember member, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member.getIdentity(), userPropertyHandlers, locale);
		inheritanceMode = member.getInheritanceMode();
		registration = member.getCreationDate();
		this.identity = member.getIdentity();
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public GroupMembershipInheritance getInheritanceMode() {
		return inheritanceMode;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	
	public Date getRegistration() {
		return registration;
	}
	
	public List<CurriculumRoles> getRoles() {
		List<CurriculumRoles> roles = new ArrayList<>(numOfRoles.keySet());
		if(roles.size() > 1) {
			Collections.sort(roles, new CurriculumRolesComparator());
		}
		return roles;
	}
	
	public int getNumOfRole(CurriculumRoles role) {
		AtomicInteger numOf = numOfRoles.get(role);
		return numOf == null ? 0 : numOf.get();
	}
	
	public void addRole(CurriculumRoles roleToAdd) {
		numOfRoles.computeIfAbsent(roleToAdd, r -> new AtomicInteger(0)).incrementAndGet();
	}
	
	public int getNumOfReservations() {
		int numOf = 0;
		for(Map.Entry<CurriculumRoles,AtomicInteger> entry:numOfReservations.entrySet()) {
			numOf += entry.getValue().get();
		}
		return numOf;
	}
	
	public int getNumOfReservations(CurriculumRoles role) {
		AtomicInteger numOf = numOfReservations.get(role);
		return numOf == null ? 0 : numOf.get();
	}
	
	public List<ResourceReservation> getReservations() {
		return reservations == null ? List.of() : reservations;
	}
	
	public void addReservation(CurriculumRoles roleToAdd, ResourceReservation reservation) {
		numOfReservations.computeIfAbsent(roleToAdd, r -> new AtomicInteger(0))
			.incrementAndGet();
		if(reservations == null) {
			reservations = new ArrayList<>();
		}
		reservations.add(reservation);
		
		if(registration == null || registration.after(reservation.getCreationDate())) {
			registration = reservation.getCreationDate();
		}
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public FormLink getChatLink() {
		return chatLink;
	}

	public void setChatLink(FormLink chatLink) {
		this.chatLink = chatLink;
	}
	
	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public MemberDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(MemberDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
