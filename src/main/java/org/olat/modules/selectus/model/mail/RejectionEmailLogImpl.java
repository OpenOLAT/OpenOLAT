/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.mail;

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
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationLightImpl;
import org.olat.modules.selectus.model.RejectionEmailLog;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rrejectionlog")
@Table(name="o_selectus_rejection_email_log")
public class RejectionEmailLogImpl implements RejectionEmailLog, Persistable {

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
	@Column(name="status", nullable=false, insertable=true, updatable=false)
	private int status;
	@Column(name="mail_rejection", nullable=false, insertable=true, updatable=false)
	private boolean rejected;
	@Column(name="mail_template", nullable=false, insertable=true, updatable=false)
	private String mailTemplate;
	
	@ManyToOne(targetEntity=ApplicationLightImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_application_id", nullable=false, insertable=true, updatable=false)
	private ApplicationLight application;
	
	public RejectionEmailLogImpl() {
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
	public ApplicationLight getApplication() {
		return application;
	}

	public void setApplication(ApplicationLight application) {
		this.application = application;
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
		if(obj instanceof RejectionEmailLogImpl) {
			RejectionEmailLogImpl log = (RejectionEmailLogImpl)obj;
			return getKey() != null && getKey().equals(log.getKey());
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
