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
package org.olat.modules.coach.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipRow extends UserPropertiesRow {
	private final ResourceReservation resourceReservation;
	private final String title;
	private final String extRef;
	private final Date begin;
	private final Date end;
	private final String type;
	private final Date confirmationUntil;

	public PendingMembershipRow(Long identityKey, String title, String extRef,
								Date begin, Date end, String type, Date confirmationUntil, 
								ResourceReservation resourceReservation, 
								List<UserPropertyHandler> userPropertyHandlers,
								String[] identityProps, Locale locale) {
		super(identityKey, extRef, userPropertyHandlers, identityProps, locale);
		this.title = title;
		this.extRef = extRef;
		this.type = type;
		this.confirmationUntil = confirmationUntil;
		this.resourceReservation = resourceReservation;
		this.begin = begin;
		this.end = end;
	}

	public ResourceReservation getResourceReservation() {
		return resourceReservation;
	}

	public String getTitle() {
		return title;
	}

	public String getExtRef() {
		return extRef;
	}

	public Date getBegin() {
		return begin;
	}

	public Date getEnd() {
		return end;
	}

	public String getType() {
		return type;
	}

	public Date getConfirmationUntil() {
		return confirmationUntil;
	}
}
