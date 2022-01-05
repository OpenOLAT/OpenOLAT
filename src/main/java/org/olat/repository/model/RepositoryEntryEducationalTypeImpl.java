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
package org.olat.repository.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntryEducationalType;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="repositoryentryeducationaltype")
@Table(name="o_re_educational_type")
public class RepositoryEntryEducationalTypeImpl implements RepositoryEntryEducationalType, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = 6896170208230389754L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="r_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="r_predefined", nullable=false, insertable=true, updatable=false)
	private boolean predefined;
	@Column(name="r_css_class", nullable=true, insertable=true, updatable=true)
	private String cssClass;
	
	@Column(name="r_preset_mycourses", nullable=false, insertable=true, updatable=true)
	private boolean presetMyCourses;

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
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public boolean isPredefined() {
		return predefined;
	}

	public void setPredefined(boolean predefined) {
		this.predefined = predefined;
	}

	@Override
	public String getCssClass() {
		return cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public boolean isPresetMyCourses() {
		return presetMyCourses;
	}

	@Override
	public void setPresetMyCourses(boolean presetMyCourses) {
		this.presetMyCourses = presetMyCourses;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepositoryEntryEducationalTypeImpl other = (RepositoryEntryEducationalTypeImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
