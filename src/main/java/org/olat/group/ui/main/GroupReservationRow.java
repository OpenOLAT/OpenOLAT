/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.group.ui.main;

import org.olat.core.gui.components.link.Link;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * Initial date: 2026-03-11<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GroupReservationRow {

	private final ResourceReservation reservation;
	private final String displayName;
	private final String description;
	private final boolean detailsAvailable;

	private Link detailsLink;
	private Link acceptLink;
	private Link declineLink;

	public GroupReservationRow(ResourceReservation reservation, String displayName, String description,
			boolean detailsAvailable) {
		this.reservation = reservation;
		this.displayName = displayName;
		this.description = description;
		this.detailsAvailable = detailsAvailable;
	}

	public ResourceReservation getReservation() {
		return reservation;
	}

	public Long getKey() {
		return reservation.getKey();
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public boolean isDetailsAvailable() {
		return detailsAvailable;
	}

	public Link getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(Link detailsLink) {
		this.detailsLink = detailsLink;
	}

	public String getDetailsLinkName() {
		return detailsLink != null ? detailsLink.getComponentName() : null;
	}

	public Link getAcceptLink() {
		return acceptLink;
	}

	public void setAcceptLink(Link acceptLink) {
		this.acceptLink = acceptLink;
	}

	public String getAcceptLinkName() {
		return acceptLink != null ? acceptLink.getComponentName() : null;
	}

	public Link getDeclineLink() {
		return declineLink;
	}

	public void setDeclineLink(Link declineLink) {
		this.declineLink = declineLink;
	}

	public String getDeclineLinkName() {
		return declineLink != null ? declineLink.getComponentName() : null;
	}
}
