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
package org.olat.modules.openbadges;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * Initial date: 2023-06-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface BadgeAssertion {

	enum BadgeAssertionStatus {
		editing, issued, revoked, reset
	}

	Long getKey();

	Date getCreationDate();

	Date getLastModified();

	void setLastModified(Date lastModified);

	String getUuid();

	BadgeAssertionStatus getStatus();

	void setStatus(BadgeAssertionStatus status);

	String getRecipientObject();

	void setRecipientObject(String recipient);

	String getVerificationObject();

	void setVerificationObject(String verification);

	Date getIssuedOn();

	void setIssuedOn(Date issuedOn);

	String getBakedImage();

	String getDownloadFileName();

	void setBakedImage(String bakedImage);

	String getEvidence();

	void setEvidence(String evidence);

	String getNarrative();

	void setNarrative(String narrative);

	Date getExpires();

	void setExpires(Date expires);

	String getRevocationReason();

	void setRevocationReason(String revocationReason);

	BadgeClass getBadgeClass();

	void setBadgeClass(BadgeClass badgeClass);

	Identity getRecipient();

	void setRecipient(Identity recipient);

	Identity getAwardedBy();

	void setAwardedBy(Identity awardedBy);
}
