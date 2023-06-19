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

import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface BadgeClass {

	enum BadgeClassStatus {
		preparation, active, inactive, revoked
	}

	enum BadgeClassTimeUnit {
		day, week, month, year;
	}

	Long getKey();

	Date getCreationDate();

	Date getLastModified();

	void setLastModified(Date lastModified);

	String getUuid();

	BadgeClassStatus getStatus();

	void setStatus(BadgeClassStatus status);

	String getVersion();

	void setVersion(String version);

	String getLanguage();

	void setLanguage(String language);

	String getImage();

	void setImage(String image);

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String getCriteria();

	void setCriteria(String criteria);

	String getSalt();

	void setSalt(String salt);

	String getIssuer();

	void setIssuer(String issuer);

	boolean isValidityEnabled();

	void setValidityEnabled(boolean validityEnabled);

	int getValidityTimelapse();

	void setValidityTimelapse(int validityTimelapse);

	BadgeClassTimeUnit getValidityTimelapseUnit();

	void setValidityTimelapseUnit(BadgeClassTimeUnit validityTimelapseUnit);

	RepositoryEntry getEntry();

	void setEntry(RepositoryEntry entry);
}
