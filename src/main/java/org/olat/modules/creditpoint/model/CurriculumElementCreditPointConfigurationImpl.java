/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.model;

import java.math.BigDecimal;
import java.util.Date;

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

import org.olat.core.id.Persistable;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CurriculumElementCreditPointConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="creditpointcurriculumelement")
@Table(name="o_cp_cur_element_config")
public class CurriculumElementCreditPointConfigurationImpl implements Persistable, CurriculumElementCreditPointConfiguration {
	
	private static final long serialVersionUID = -317857720027314194L;

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

	@Column(name="c_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="c_creditpoints", nullable=true, insertable=true, updatable=true)
	private BigDecimal creditPoints;
	
	@ManyToOne(targetEntity=CreditPointSystemImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_system", nullable=true, insertable=true, updatable=true)
	private CreditPointSystem creditPointSystem;
	
	@ManyToOne(targetEntity=CurriculumElementImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_element", nullable=false, insertable=true, updatable=false)
    private CurriculumElement curriculumElement;
	
	public CurriculumElementCreditPointConfigurationImpl() {
		//
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public BigDecimal getCreditPoints() {
		return creditPoints;
	}

	@Override
	public void setCreditPoints(BigDecimal creditPoints) {
		this.creditPoints = creditPoints;
	}

	@Override
	public CreditPointSystem getCreditPointSystem() {
		return creditPointSystem;
	}

	@Override
	public void setCreditPointSystem(CreditPointSystem creditPointSystem) {
		this.creditPointSystem = creditPointSystem;
	}

	@Override
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -1983210546 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementCreditPointConfigurationImpl config) {
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
