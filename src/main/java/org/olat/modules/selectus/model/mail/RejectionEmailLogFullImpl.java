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
package org.olat.modules.selectus.model.mail;

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationLightImpl;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.RejectionEmailLogFull;

/**
 * 
 * Initial date: 20 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rrejectionlogfull")
@Table(name="o_selectus_rejection_email_log")
@NamedQueries({
	@NamedQuery(name="loadFullEmailLogByKey", query="select log from rrejectionlogfull log inner join fetch log.application app where log.key=:logKey")
	
})
public class RejectionEmailLogFullImpl implements RejectionEmailLogFull, Persistable {

	private static final long serialVersionUID = 403498062232811200L;
	
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
	@Column(name="log_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;

	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Column(name="status", nullable=false, insertable=true, updatable=true)
	private int status;
	@Column(name="mail_rejection", nullable=false, insertable=true, updatable=true)
	private boolean rejected;
	@Column(name="mail_template", nullable=false, insertable=true, updatable=true)
	private String mailTemplate;
	@Column(name="mail_subject", nullable=false, insertable=true, updatable=true)
	private String mailSubject;
	@Column(name="mail_content", nullable=false, insertable=true, updatable=true)
	private String mailContent;
	
	@ManyToOne(targetEntity=ApplicationLightImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_application_id", nullable=false, insertable=true, updatable=false)
	private ApplicationLight application;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_letter_id", nullable=true, insertable=true, updatable=true)
	private Attachment letter;
	
	public RejectionEmailLogFullImpl() {
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
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public boolean isRejected() {
		return rejected;
	}

	public void setRejected(boolean rejected) {
		this.rejected = rejected;
	}

	@Override
	public String getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(String mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	@Override
	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	@Override
	public String getMailContent() {
		return mailContent;
	}

	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}

	@Override
	public ApplicationLight getApplication() {
		return application;
	}

	public void setApplication(ApplicationLight application) {
		this.application = application;
	}

	@Override
	public Attachment getLetter() {
		return letter;
	}

	public void setLetter(Attachment letter) {
		this.letter = letter;
	}

	@Override
	public int hashCode() {
		return key == null ? 285698 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RejectionEmailLogFullImpl) {
			RejectionEmailLogFullImpl log = (RejectionEmailLogFullImpl)obj;
			return getKey() != null && getKey().equals(log.getKey());
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
