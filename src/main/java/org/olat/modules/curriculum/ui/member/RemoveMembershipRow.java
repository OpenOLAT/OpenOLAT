/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RemoveMembershipRow extends UserPropertiesRow {
	
	private final Identity identity;
	private final List<ResourceReservation> reservations = new ArrayList<>(2);
	private final List<CurriculumElementMembership> memberships = new ArrayList<>();

	private MemberDetailsController detailsCtrl;
	
	public RemoveMembershipRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public List<ResourceReservation> getReservations() {
		return reservations;
	}
	
	public void addReservation(ResourceReservation reservation) {
		reservations.add(reservation);
	}
	
	public List<CurriculumElementMembership> getMemberships() {
		return memberships;
	}
	
	public void addMembership(CurriculumElementMembership membership) {
		memberships.add(membership);
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
