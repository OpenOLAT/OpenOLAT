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
package org.olat.repository.ui.list;

import org.olat.core.gui.components.link.Link;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * Initial date: 2026-03-09<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OverviewReservationRow {

	private final ResourceReservation reservation;
	private final String displayName;
	private final String externalRef;
	private final String translatedType;
	private final String description;
	private final String thumbnailRelPath;
	private final boolean detailsAvailable;
	private final Long repositoryEntryKey;
	private final Long curriculumElementKey;

	private Link detailsLink;
	private Link acceptLink;
	private Link declineLink;

	public OverviewReservationRow(ResourceReservation reservation, String displayName, String externalRef,
			String translatedType, String description, String thumbnailRelPath, boolean detailsAvailable,
			Long repositoryEntryKey) {
		this(reservation, displayName, externalRef, translatedType, description, thumbnailRelPath, detailsAvailable,
				repositoryEntryKey, null);
	}

	public OverviewReservationRow(ResourceReservation reservation, String displayName, String externalRef,
			String translatedType, String description, String thumbnailRelPath, boolean detailsAvailable,
			Long repositoryEntryKey, Long curriculumElementKey) {
		this.reservation = reservation;
		this.displayName = displayName;
		this.externalRef = externalRef;
		this.translatedType = translatedType;
		this.description = description;
		this.thumbnailRelPath = thumbnailRelPath;
		this.detailsAvailable = detailsAvailable;
		this.repositoryEntryKey = repositoryEntryKey;
		this.curriculumElementKey = curriculumElementKey;
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

	public String getExternalRef() {
		return externalRef;
	}

	public String getTranslatedType() {
		return translatedType;
	}

	public String getDescription() {
		return description;
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}

	public boolean isThumbnailAvailable() {
		return thumbnailRelPath != null;
	}

	public boolean isDetailsAvailable() {
		return detailsAvailable;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
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
