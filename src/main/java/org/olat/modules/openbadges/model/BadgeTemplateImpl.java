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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.BadgeTemplate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgetemplate")
@Table(name="o_badge_template")
public class BadgeTemplateImpl implements Persistable, BadgeTemplate {

	@Serial
	private static final long serialVersionUID = 4128358164663754360L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Column(name = "b_image", nullable = false, insertable = true, updatable = true)
	private String image;

	@Column(name = "b_name", nullable = false, insertable = true, updatable = true)
	private String name;

	@Column(name = "b_description", nullable = true, insertable = true, updatable = true)
	private String description;

	@Column(name = "b_scopes", nullable = true, insertable = true, updatable = true)
	private String scopes;

	@Column(name = "b_placeholders", nullable = true, insertable = true, updatable = true)
	private String placeholders;

	public BadgeTemplateImpl() {
	}

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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String getScopes() {
		return scopes;
	}

	@Override
	public Collection<String> getScopesAsCollection() {
		if (StringHelper.containsNonWhitespace(getScopes())) {
			return Arrays.stream(getScopes().split(",")).toList();
		}
		return List.of();
	}

	@Override
	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	@Override
	public void setScopesAsCollection(Collection<String> scopes) {
		if (scopes == null || scopes.isEmpty()) {
			setScopes("");
		} else {
			String scopesString = String.join(",", scopes);
			setScopes(scopesString);
		}
	}

	@Override
	public String getPlaceholders() {
		return placeholders;
	}

	@Override
	public void setPlaceholders(String placeholders) {
		this.placeholders = placeholders;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof BadgeTemplateImpl badgeTemplate) {
			return getKey() != null && getKey().equals(badgeTemplate.getKey());
		}
		return false;
	}
}
