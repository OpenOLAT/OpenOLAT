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
package org.olat.modules.forms.model.jpa;

import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="evaluationformsurvey")
@Table(name="o_eva_form_survey")
public class EvaluationFormSurveyImpl implements EvaluationFormSurvey, Persistable {

	private static final long serialVersionUID = 2039825688298350338L;
	
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
	
	@Column(name="e_resname", nullable=true, insertable=true, updatable=false)
	private String resName;
	@Column(name="e_resid", nullable=true, insertable=true, updatable=false)
	private Long resId;
	@Column(name="e_sub_ident", nullable=true, insertable=true, updatable=false)
	private String resSubident;
	@Column(name="e_sub_ident2", nullable=true, insertable=true, updatable=false)
	private String resSubident2;
	@Column(name="e_series_key", nullable=true, insertable=true, updatable=true)
	private Long seriesKey;
	@Column(name="e_series_index", nullable=true, insertable=true, updatable=true)
	private Integer seriesIndex;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_form_entry", nullable=false, insertable=true, updatable=true)
	private RepositoryEntry formEntry;
	@OneToOne(targetEntity=EvaluationFormSurveyImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_series_previous", nullable=true, insertable=true, updatable=true)
	private EvaluationFormSurvey seriesPrevious;
	
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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public Long getResId() {
		return resId;
	}

	public void setResId(Long resId) {
		this.resId = resId;
	}

	public String getResSubident() {
		return resSubident;
	}

	public void setResSubident(String resSubident) {
		this.resSubident = resSubident;
	}

	public String getResSubident2() {
		return resSubident2;
	}

	public void setResSubident2(String resSubident2) {
		this.resSubident2 = resSubident2;
	}

	@Override
	public EvaluationFormSurveyIdentifier getIdentifier() {
		return of(getOLATResourceable() , resSubident, resSubident2);
	}
	
	private OLATResourceable getOLATResourceable() {
		return new OLATResourceable() {
			
			@Override
			public String getResourceableTypeName() {
				return resName;
			}
			
			@Override
			public Long getResourceableId() {
				return resId;
			}
		};
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	public void setFormEntry(RepositoryEntry formEntry) {
		this.formEntry = formEntry;
	}

	@Override
	public Long getSeriesKey() {
		return seriesKey;
	}

	public void setSeriesKey(Long seriesKey) {
		this.seriesKey = seriesKey;
	}

	@Override
	public Integer getSeriesIndex() {
		return seriesIndex;
	}

	public void setSeriesIndex(Integer seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	@Override
	public EvaluationFormSurvey getSeriesPrevious() {
		return seriesPrevious;
	}

	public void setSeriesPrevious(EvaluationFormSurvey previous) {
		this.seriesPrevious = previous;
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
		EvaluationFormSurveyImpl other = (EvaluationFormSurveyImpl) obj;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EvaluationFormSurveyImpl [key=");
		builder.append(key);
		builder.append(", resName=");
		builder.append(resName);
		builder.append(", resId=");
		builder.append(resId);
		builder.append(", resSubident=");
		builder.append(resSubident);
		builder.append(", formEntryKey=");
		builder.append(formEntry.getKey());
		builder.append("]");
		return builder.toString();
	}

}
