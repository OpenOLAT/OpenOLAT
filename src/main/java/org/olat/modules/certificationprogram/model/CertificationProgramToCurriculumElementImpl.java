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
package org.olat.modules.certificationprogram.model;

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
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramToCurriculumElement;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

/**
 * 
 * 
 * Initial date: 1 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="certificationprogramtoelement")
@Table(name="o_cer_program_to_element")
public class CertificationProgramToCurriculumElementImpl implements Persistable, CertificationProgramToCurriculumElement {
	
	private static final long serialVersionUID = -1013382866927895186L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=CertificationProgramImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_program", nullable=true, insertable=true, updatable=true)
    private CertificationProgram certificationProgram;

	@ManyToOne(targetEntity=CurriculumElementImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_element", nullable=true, insertable=true, updatable=false)
    private CurriculumElement curriculumElement;

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
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	public void setCertificationProgram(CertificationProgram certificationProgram) {
		this.certificationProgram = certificationProgram;
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
		return key == null ? -99545887 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CertificationProgramToCurriculumElementImpl rel) {
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
