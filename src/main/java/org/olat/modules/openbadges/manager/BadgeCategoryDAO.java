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
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.logging.Tracing;
import org.olat.modules.openbadges.BadgeCategory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.model.BadgeCategoryImpl;

import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-06-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeCategoryDAO {

	private static final Logger log = Tracing.createLoggerFor(BadgeCategoryDAO.class);

	@Autowired
	private DB dbInstance;

	public BadgeCategory create(Tag tag, BadgeTemplate badgeTemplate, BadgeClass badgeClass) {
		BadgeCategoryImpl badgeCategory = new BadgeCategoryImpl();
		badgeCategory.setCreationDate(new Date());
		badgeCategory.setTag(tag);
		badgeCategory.setBadgeTemplate(badgeTemplate);
		badgeCategory.setBadgeClass(badgeClass);
		dbInstance.getCurrentEntityManager().persist(badgeCategory);
		return badgeCategory;
	}

	public List<TagInfo> readBadgeCategoryTags(BadgeTemplate badgeTemplate, BadgeClass badgeClass) {
		if (badgeTemplate != null && badgeClass != null) {
			throw new IllegalArgumentException("badgeTemplate and badgeClass are mutually exclusive. Only one of the two can be specified.");
		}

		Long selectedKey = null;
		if (badgeTemplate != null) {
			selectedKey = badgeTemplate.getKey();
		}
		if (badgeClass != null) {
			selectedKey = badgeClass.getKey();
		}

		QueryBuilder qb = new QueryBuilder();
		qb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		qb.append("  tag.key,");
		qb.append("  min(tag.creationDate),");
		qb.append("  min(tag.displayName),");
		qb.append("  count(badgeCategory.badgeTemplate.key) + count(badgeCategory.badgeClass.key),");
		if (badgeTemplate != null) {
			qb.append("  sum(case when (badgeCategory.badgeTemplate.key = :selectedKey) then 1 else 0 end) as selected");
		} else if (badgeClass != null) {
			qb.append("  sum(case when (badgeCategory.badgeClass.key = :selectedKey) then 1 else 0 end) as selected");
		} else {
			qb.append("  cast(0 as long) as selected");
		}
		qb.append(") ");
		qb.append("from badgecategory badgeCategory inner join badgeCategory.tag tag");
		qb.groupBy().append("tag.key");

		TypedQuery<TagInfo> typedQuery = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), TagInfo.class);
		if (selectedKey != null) {
			typedQuery = typedQuery.setParameter("selectedKey", selectedKey);
		}
		return typedQuery.getResultList();
	}

	public List<TagInfo> readBadgeCategoryTags() {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		qb.append("  tag.key,");
		qb.append("  min(tag.creationDate),");
		qb.append("  min(tag.displayName),");
		qb.append("  count(badgeCategory.badgeTemplate.key),");
		qb.append("  cast(1 as long) as selected");
		qb.append(") ");
		qb.append("from badgecategory badgeCategory inner join badgeCategory.tag tag");
		qb.groupBy().append("tag.key");

		return dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), TagInfo.class)
				.getResultList();
	}

	public void delete(BadgeCategory badgeCategory) {
		dbInstance.deleteObject(badgeCategory);
	}

	public void delete(BadgeTemplate badgeTemplate) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("delete from badgecategory badgeCategory where badgeCategory.badgeTemplate.key = :key");
		dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString())
				.setParameter("key", badgeTemplate.getKey())
				.executeUpdate();
	}

	public void delete(BadgeClass badgeClass) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("delete from badgecategory badgeCategory where badgeCategory.badgeClass.key = :key");
		dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString())
				.setParameter("key", badgeClass.getKey())
				.executeUpdate();
	}

	public List<BadgeCategory> readBadgeCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass) {
		if (badgeTemplate == null && badgeClass == null) {
			throw new IllegalArgumentException("Either badgeTemplate or badgeClass must be specified.");
		}
		if (badgeTemplate != null && badgeClass != null) {
			throw new IllegalArgumentException("badgeTemplate and badgeClass are mutually exclusive. Only one of the two can be specified.");
		}
		Long key = badgeTemplate != null ? badgeTemplate.getKey() : badgeClass.getKey();
		QueryBuilder qb = new QueryBuilder();
		qb.append("select badgeCategory ");
		qb.append("from badgecategory badgeCategory ");
		qb.append("where ");
		if (badgeTemplate != null) {
			qb.append("badgeCategory.badgeTemplate.key = :key ");
		} else {
			qb.append("badgeCategory.badgeClass.key = :key ");
		}

		return dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), BadgeCategory.class)
				.setParameter("key", key)
				.getResultList();
	}
}
