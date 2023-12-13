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
package org.olat.course.assessment.ui.tool;

import org.json.JSONObject;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.v2.Profile;

/**
 * 
 * Initial date: 4 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityBadgeAssertionRow {

	private final BadgeAssertion badgeAssertion;
	private Component badgeImage;
	private final String name;
	private final String issuer;
	private String issuedOn;
	private FormLink selectLink;
	private FormLink downloadLink;

	public IdentityBadgeAssertionRow(OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize) {
		this.name = badgeAssertionWithSize.badgeAssertion().getBadgeClass().getNameWithScan();
		this.badgeAssertion = badgeAssertionWithSize.badgeAssertion();
		Profile profile = new Profile(new JSONObject(badgeAssertionWithSize.badgeAssertion().getBadgeClass().getIssuer()));
		this.issuer = profile.getNameWithScan();
	}

	public BadgeAssertion getBadgeAssertion() {
		return badgeAssertion;
	}

	public String getName() {
		return name;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getIssuedOn() {
		return issuedOn;
	}

	public void setIssuedOn(String issuedOn) {
		this.issuedOn = issuedOn;
	}
	
	public Component getBadgeImage() {
		return badgeImage;
	}

	public void setBadgeImage(Component badgeImage) {
		this.badgeImage = badgeImage;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}

	public FormLink getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(FormLink downloadLink) {
		this.downloadLink = downloadLink;
	}
}
