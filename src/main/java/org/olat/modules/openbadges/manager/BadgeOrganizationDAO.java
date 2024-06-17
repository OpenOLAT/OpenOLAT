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
package org.olat.modules.openbadges.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.modules.openbadges.model.BadgeOrganizationImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2024-06-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeOrganizationDAO {

	@Autowired
	private DB dbInstance;

	public BadgeOrganization createBadgeOrganization(BadgeOrganization.BadgeOrganizationType type, String key, String value) {
		BadgeOrganizationImpl badgeOrganization = new BadgeOrganizationImpl();
		badgeOrganization.setCreationDate(new Date());
		badgeOrganization.setLastModified(badgeOrganization.getCreationDate());
		badgeOrganization.setType(type);
		badgeOrganization.setOrganizationKey(key);
		badgeOrganization.setOrganizationValue(value);
		dbInstance.getCurrentEntityManager().persist(badgeOrganization);
		return badgeOrganization;
	}

	public List<BadgeOrganization> loadBadgeOrganizations(BadgeOrganization.BadgeOrganizationType orgType) {
		String query = "select org from badgeorganization org where org.type=:orgType";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, BadgeOrganization.class)
				.setParameter("orgType", orgType).getResultList();
	}

	public BadgeOrganization loadBadgeOrganization(Long key) {
		return dbInstance.getCurrentEntityManager().find(BadgeOrganizationImpl.class, key);
	}

	public BadgeOrganization loadBadgeOrganization(String organizationKey) {
		String query = "select org from badgeorganization org where org.organizationKey=:organizationKey";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, BadgeOrganization.class)
				.setParameter("organizationKey", organizationKey).getSingleResult();
	}

	public boolean isBadgeOrganizationInUse(Long organizationKey) {
		String query = "select count(bc.key) from badgeclass bc where bc.badgeOrganization.key=:organizationKey";

		Long count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("organizationKey", organizationKey).getSingleResult();

		return count != null && count > 0;
	}

	public BadgeOrganization updateBadgeOrganization(BadgeOrganization badgeOrganization) {
		badgeOrganization.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeOrganization);
	}

	public void deleteBadgeOrganization(BadgeOrganization badgeOrganization) {
		dbInstance.getCurrentEntityManager().remove(badgeOrganization);
	}
}
