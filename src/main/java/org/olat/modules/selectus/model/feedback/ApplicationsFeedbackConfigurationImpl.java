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
package org.olat.modules.selectus.model.feedback;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionImpl;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rappsfeedback")
@Table(name="o_selectus_apps_feedback")
public class ApplicationsFeedbackConfigurationImpl implements ApplicationsFeedbackConfiguration, Persistable {

	private static final long serialVersionUID = -4520443456692449610L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_modified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="r_name", nullable=false, insertable=true, updatable=true)
	private String configurationName;
	
	@Column(name="r_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	
	@Column(name="r_deadline", nullable=true, insertable=true, updatable=true)
	private Date deadline;
	@Column(name="r_mail_subject", nullable=true, insertable=true, updatable=true)
	private String mailSubject;
	@Column(name="r_mail_template", nullable=true, insertable=true, updatable=true)
	private String mailTemplate;
	@Column(name="r_mail_letter", nullable=true, insertable=true, updatable=true)
	private String mailLetter;
	
	@Column(name="r_docs", nullable=true, insertable=true, updatable=true)
	private String docs;
	@Column(name="r_experts_docs", nullable=false, insertable=true, updatable=true)
	private boolean expertsDocs;
	@Column(name="r_referees_docs", nullable=false, insertable=true, updatable=true)
	private boolean refereesDocs;
	@Column(name="r_experts_comp_assessment_docs", nullable=false, insertable=true, updatable=true)
	private boolean expertsComparativeAssessmentDocs;
	
	@Column(name="r_fields", nullable=true, insertable=true, updatable=true)
	private String fieldsString;
	
	@ManyToOne(targetEntity=PositionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_position_id", nullable=false, insertable=true, updatable=false)
	private Position position;
	
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
	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}
	
	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	@Override
	public String getMailSubject() {
		return mailSubject;
	}

	@Override
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	@Override
	public String getMailTemplate() {
		return mailTemplate;
	}

	@Override
	public void setMailTemplate(String mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	@Override
	public String getMailLetter() {
		return mailLetter;
	}

	@Override
	public void setMailLetter(String mailLetter) {
		this.mailLetter = mailLetter;
	}

	public String getDocs() {
		return docs;
	}

	public void setDocs(String docs) {
		this.docs = docs;
	}

	@Override
	public boolean isRefereesDocs() {
		return refereesDocs;
	}

	@Override
	public void setRefereesDocs(boolean refereesDocs) {
		this.refereesDocs = refereesDocs;
	}
	
	@Override
	public boolean isExpertsComparativeAssessmentDocs() {
		return expertsComparativeAssessmentDocs;
	}

	@Override
	public void setExpertsComparativeAssessmentDocs(boolean expertsDocs) {
		this.expertsComparativeAssessmentDocs = expertsDocs;
	}

	@Override
	public boolean isExpertsDocs() {
		return expertsDocs;
	}

	@Override
	public void setExpertsDocs(boolean expertsDocs) {
		this.expertsDocs = expertsDocs;
	}

	@Override
	public Set<String> getDocuments() {
		return DocumentEnum.documentStringToSet(getDocs());
	}

	@Override
	public void setDocuments(Set<String> docs) {
		setDocs(DocumentEnum.documentStringSetToString(docs));
	}

	public String getFieldsString() {
		return fieldsString;
	}

	public void setFieldsString(String fields) {
		this.fieldsString = fields;
	}

	@Override
	public Set<String> getFields() {
		Set<String> fields = new HashSet<>();
		if(StringHelper.containsNonWhitespace(fieldsString)) {
			String[] fieldsArr = fieldsString.split(",");
			for(String field:fieldsArr) {
				if(StringHelper.containsNonWhitespace(field)) {
					fields.add(field);
				}
			}
		}
		return fields;
	}

	@Override
	public void setFields(Set<String> fields) {
		if(fields == null || fields.isEmpty()) {
			fieldsString = null;
		} else {
			fieldsString = String.join(",", fields);
		}
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 762325 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof ApplicationsFeedbackConfigurationImpl) {
			ApplicationsFeedbackConfigurationImpl config = (ApplicationsFeedbackConfigurationImpl)obj;
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
