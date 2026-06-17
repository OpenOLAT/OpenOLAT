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
package org.olat.modules.selectus.model.review;

import java.util.Date;

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
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationLightImpl;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rreviewresponse")
@Table(name="o_selectus_review_response")
public class ReviewResponseImpl implements ReviewResponse, CreateInfo, Persistable {

	private static final long serialVersionUID = -3773932740233787011L;

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
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="r_string_value", nullable=true, insertable=true, updatable=true)
	private String stringValue;
	@Column(name="r_integer_value", nullable=true, insertable=true, updatable=true)
	private Integer integerValue;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_reviewer_id", nullable=false, insertable=true, updatable=false)
	private Identity reviewer;
	
	@ManyToOne(targetEntity=ApplicationLightImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_application_id", nullable=false, insertable=true, updatable=false)
	private ApplicationLight application;

	@ManyToOne(targetEntity=ReviewElementDefinitionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_element_id", nullable=false, insertable=true, updatable=false)
	private ReviewElementDefinition element;
	
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
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public Integer getIntegerValue() {
		return integerValue;
	}
	
	@Override
	public void setIntegerValue(Integer integerValue) {
		this.integerValue = integerValue;
	}

	@Override
	public Identity getReviewer() {
		return reviewer;
	}

	public void setReviewer(Identity reviewer) {
		this.reviewer = reviewer;
	}

	public ApplicationLight getApplication() {
		return application;
	}

	public void setApplication(ApplicationLight application) {
		this.application = application;
	}

	@Override
	public ReviewElementDefinition getElement() {
		return element;
	}

	public void setElement(ReviewElementDefinition element) {
		this.element = element;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 44553 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReviewResponseImpl) {
			ReviewResponseImpl response = (ReviewResponseImpl)obj;
			return getKey() != null && getKey().equals(response.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
