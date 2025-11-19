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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipRow extends UserPropertiesRow {
	private final String title;
	private final String extRef;
	private final Date begin;
	private final Date end;
	private final String type;
	private final Date confirmationUntil;
	private final Long curriculumElementKey;
	private final Long reservationKey;
	
	private FormLink toolsLink;
	private FormLink acceptLink;
	private FormLink declineLink;
	private MemberDetailsController detailsCtrl;

	public PendingMembershipRow(Identity identity, String title, String extRef,
								Date begin, Date end, String type, Date confirmationUntil,
								Long curriculumElementKey, Long reservationKey,
								List<UserPropertyHandler> userPropertyHandlers,
								Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.title = title;
		this.extRef = extRef;
		this.begin = begin;
		this.end = end;
		this.type = type;
		this.confirmationUntil = confirmationUntil;
		this.curriculumElementKey = curriculumElementKey;
		this.reservationKey = reservationKey;
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

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public FormLink getAcceptLink() {
		return acceptLink;
	}

	public void setAcceptLink(FormLink acceptLink) {
		this.acceptLink = acceptLink;
	}

	public FormLink getDeclineLink() {
		return declineLink;
	}

	public void setDeclineLink(FormLink declineLink) {
		this.declineLink = declineLink;
	}

	public MemberDetailsController getDetailsController() {
		return detailsCtrl;
	}

	public void setDetailsController(MemberDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if (detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}

	public boolean isDetailsControllerAvailable() {
		if (detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}


	public Long getReservationKey() {
		return reservationKey;
	}

	public boolean matchesSearchString(String searchString) {
		if (!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		searchString = searchString.toLowerCase();
		if (StringHelper.containsNonWhitespace(getTitle()) && getTitle().toLowerCase().contains(searchString)) {
			return true;
		}
		if (StringHelper.containsNonWhitespace(getExtRef()) && getExtRef().toLowerCase().contains(searchString)) {
			return true;
		}
		for (String identityProp : getIdentityProps()) {
			if (identityProp != null && identityProp.toLowerCase().contains(searchString)) {
				return true;
			}
		}
		return false;
	}
}
