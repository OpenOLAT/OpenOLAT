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
package org.olat.modules.openbadges.model;

import java.io.Serial;
import java.util.Date;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.model.TagImpl;
import org.olat.core.id.Persistable;
import org.olat.modules.openbadges.BadgeCategory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-06-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgecategory")
@Table(name="o_badge_category")
public class BadgeCategoryImpl implements Persistable, BadgeCategory {

	@Serial
	private static final long serialVersionUID = -7296608819522958294L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@ManyToOne(targetEntity = TagImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_tag", nullable = false, insertable = true, updatable = false)
	private Tag tag;

	@ManyToOne(targetEntity = BadgeTemplateImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_template", nullable = true, insertable = true, updatable = false)
	private BadgeTemplate badgeTemplate;

	@ManyToOne(targetEntity = BadgeClassImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_class", nullable = true, insertable = true, updatable = false)
	private BadgeClass badgeClass;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	@Override
	public BadgeTemplate getBadgeTemplate() {
		return badgeTemplate;
	}

	public void setBadgeTemplate(BadgeTemplate badgeTemplate) {
		this.badgeTemplate = badgeTemplate;
	}

	@Override
	public BadgeClass getBadgeClass() {
		return badgeClass;
	}

	public void setBadgeClass(BadgeClass badgeClass) {
		this.badgeClass = badgeClass;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof BadgeCategoryImpl badgeCategory) {
			return getKey() != null && getKey().equals(badgeCategory.getKey());
		}
		return false;
	}
}
