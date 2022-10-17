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
package org.olat.ims.qti21.model.jpa;

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
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 29.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qtiassessmentresponse")
@Table(name="o_qti_assessment_response")
public class AssessmentResponseImpl implements AssessmentResponse, Persistable {

	private static final long serialVersionUID = 7341596483676802054L;

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

	@Column(name="q_responseidentifier", nullable=false, insertable=true, updatable=false)
	private String responseIdentifier;
	@Column(name="q_responsedatatype", nullable=false, insertable=true, updatable=false)
	private String responseDataType;
	@Column(name="q_responselegality", nullable=false, insertable=true, updatable=true)
	private String responseLegality;
	@Column(name="q_stringuifiedresponse", nullable=false, insertable=true, updatable=true)
	private String stringuifiedResponse;
	
	@ManyToOne(targetEntity=AssessmentItemSessionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessmentitem_session", nullable=false, insertable=true, updatable=false)
	private AssessmentItemSession assessmentItemSession;
	
	@ManyToOne(targetEntity=AssessmentTestSessionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_assessmenttest_session", nullable=true, insertable=true, updatable=false)
	private AssessmentTestSession assessmentTestSession;

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

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getResponseIdentifier() {
		return responseIdentifier;
	}

	public void setResponseIdentifier(String responseIdentifier) {
		this.responseIdentifier = responseIdentifier;
	}

	public String getResponseDataType() {
		return responseDataType;
	}

	public void setResponseDataType(String responseDataType) {
		this.responseDataType = responseDataType;
	}

	public String getResponseLegality() {
		return responseLegality;
	}

	public void setResponseLegality(String responseLegality) {
		this.responseLegality = responseLegality;
	}

	public String getStringuifiedResponse() {
		return stringuifiedResponse;
	}

	public void setStringuifiedResponse(String stringuifiedResponse) {
		this.stringuifiedResponse = stringuifiedResponse;
	}

	public AssessmentItemSession getAssessmentItemSession() {
		return assessmentItemSession;
	}

	public void setAssessmentItemSession(AssessmentItemSession assessmentItemSession) {
		this.assessmentItemSession = assessmentItemSession;
	}

	public AssessmentTestSession getAssessmentTestSession() {
		return assessmentTestSession;
	}

	public void setAssessmentTestSession(AssessmentTestSession assessmentTestSession) {
		this.assessmentTestSession = assessmentTestSession;
	}

	@Override
	public int hashCode() {
		return key == null ? -86534687 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentResponseImpl) {
			AssessmentResponseImpl response = (AssessmentResponseImpl)obj;
			return getKey() != null && getKey().equals(response.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
