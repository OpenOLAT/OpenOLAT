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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="evaluationformresponse")
@Table(name="o_eva_form_response")
public class EvaluationFormResponseImpl implements EvaluationFormResponse, Persistable {

	private static final long serialVersionUID = 4598470475421347600L;

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
	
	@Column(name="e_responseidentifier", nullable=false, insertable=true, updatable=false)
	private String responseIdentifier;
	@Column(name="e_no_response", nullable=false, insertable=true, updatable=true)
	private boolean noResponse;
	@Column(name="e_numericalresponse", nullable=true, insertable=true, updatable=true)
	private BigDecimal numericalResponse;
	@Column(name="e_stringuifiedresponse", nullable=true, insertable=true, updatable=true)
	private String stringuifiedResponse;
	@Column(name="e_file_response_path", nullable=true, insertable=true, updatable=true)
	private String fileResponsePath;
	
	@ManyToOne(targetEntity=EvaluationFormSessionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_session", nullable=false, insertable=true, updatable=false)
	private EvaluationFormSession session;
	
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

	@Override
	public String getResponseIdentifier() {
		return responseIdentifier;
	}

	public void setResponseIdentifier(String responseIdentifier) {
		this.responseIdentifier = responseIdentifier;
	}

	@Override
	public boolean isNoResponse() {
		return noResponse;
	}

	public void setNoResponse(boolean noResponse) {
		this.noResponse = noResponse;
	}

	@Override
	public BigDecimal getNumericalResponse() {
		return numericalResponse;
	}

	public void setNumericalResponse(BigDecimal numericalResponse) {
		this.numericalResponse = numericalResponse;
	}

	@Override
	public String getStringuifiedResponse() {
		return stringuifiedResponse;
	}

	public void setStringuifiedResponse(String stringuifiedResponse) {
		this.stringuifiedResponse = stringuifiedResponse;
	}

	@Override
	public Path getFileResponse() {
		return StringHelper.containsNonWhitespace(fileResponsePath)? Paths.get(fileResponsePath): null;
	}

	public void setFileResponse(Path fileResponse) {
		this.fileResponsePath = fileResponse != null? fileResponse.toString(): null;
	}

	@Override
	public EvaluationFormSession getSession() {
		return session;
	}

	public void setSession(EvaluationFormSession session) {
		this.session = session;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 9376560 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EvaluationFormResponseImpl) {
			EvaluationFormResponseImpl result = (EvaluationFormResponseImpl)obj;
			return getKey() != null && getKey().equals(result.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
