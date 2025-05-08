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
package org.olat.modules.openbadges.criteria;

import java.beans.Transient;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.manager.BadgeClassDAO;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-06-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OtherBadgeEarnedCondition implements BadgeCondition {
	public static final String KEY = "otherBadgeEarned";

	private String badgeClassUuid;

	private String badgeClassRootId;

	public OtherBadgeEarnedCondition(String badgeClassRootId) {
		this.badgeClassRootId = badgeClassRootId;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator, RepositoryEntry courseEntry) {
		BadgeClassDAO badgeClassDAO = CoreSpringFactory.getImpl(BadgeClassDAO.class);
		BadgeClass badgeClass = badgeClassDAO.getBadgeClassByRootId(badgeClassRootId);
		return translator.translate("badgeCondition." + KEY, badgeClass == null ? "-" : badgeClass.getName());
	}

	@Deprecated
	public String getBadgeClassUuid() {
		return badgeClassUuid;
	}

	public void setBadgeClassUuid(String badgeClassUuid) {
		this.badgeClassUuid = badgeClassUuid;
	}

	public String getBadgeClassRootId() {
		return badgeClassRootId;
	}

	public void setBadgeClassRootId(String badgeClassRootId) {
		this.badgeClassRootId = badgeClassRootId;
	}

	@Transient
	public int upgradeBadgeDependency() {
		if (!StringHelper.containsNonWhitespace(badgeClassUuid)) {
			return 0;
		}
		BadgeClassDAO badgeClassDAO = CoreSpringFactory.getImpl(BadgeClassDAO.class);
		BadgeClass badgeClass = badgeClassDAO.getBadgeClassByUuid(badgeClassUuid);
		badgeClassRootId = badgeClass != null ? badgeClass.getRootId() : null;
		badgeClassUuid = null;
		return 1;
	}
}
