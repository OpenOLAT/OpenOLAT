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
package org.olat.modules.selectus.model.log;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;

/**
 * 
 * This is a reduced version of @see RecruitingAuditLogImpl without the
 * before / after fields which can grow pretty large and some other fields
 * which are not strictly defined in the interface.
 * 
 * Initial date: 18 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="recruitingauditloglight")
@Table(name="o_selectus_audit_log")
public class RecruitingAuditLogLightImpl implements Persistable, RecruitingAuditLogLight {

	private static Logger log = Tracing.createLoggerFor(RecruitingAuditLogLightImpl.class);
	private static final long serialVersionUID = -1009831288341614553L;

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
	private Date creationDate;

	@Column(name="r_action", nullable=false, insertable=true, updatable=false)
	private String action;
	@Column(name="r_action_target", nullable=false, insertable=true, updatable=false)
	private String actionTarget;
	
	@Column(name="r_message", nullable=true, insertable=true, updatable=false)
	private String message;
	
	@Column(name="r_message_i18n", nullable=true, insertable=true, updatable=false)
	private String messageI18n;
	@Column(name="r_message_val_1", nullable=true, insertable=true, updatable=false)
	private String messageValue1;
	@Column(name="r_message_val_2", nullable=true, insertable=true, updatable=false)
	private String messageValue2;
	@Column(name="r_message_val_3", nullable=true, insertable=true, updatable=false)
	private String messageValue3;
	@Column(name="r_message_val_4", nullable=true, insertable=true, updatable=false)
	private String messageValue4;
	@Column(name="r_message_val_5", nullable=true, insertable=true, updatable=false)
	private String messageValue5;
	
	@Column(name="fk_position_id", nullable=true, insertable=true, updatable=false)
	private Long positionKey;
	@Column(name="fk_application_id", nullable=true, insertable=true, updatable=false)
	private Long applicationKey;
	@Column(name="fk_comment_id", nullable=true, insertable=true, updatable=false)
	private Long commentKey;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	
	/* Only there for queries */
	@OneToMany(targetEntity=RecruitingAuditLogReadImpl.class, mappedBy="auditLog", fetch=FetchType.LAZY)
	private Set<RecruitingAuditLogReadImpl> readers;

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public Action getActionEnum() {
		return action == null ? null : Action.valueOf(action);
	}

	public String getActionTarget() {
		return actionTarget;
	}

	public void setActionTarget(String actionTarget) {
		this.actionTarget = actionTarget;
	}

	@Override
	public ActionTarget getTargetEnum() {
		try {
			return actionTarget == null ? null : ActionTarget.valueOf(actionTarget);
		} catch (Exception e) {
			log.error("", e);
			return ActionTarget.application;
		}
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getMessageI18n() {
		return messageI18n;
	}

	public void setMessageI18n(String messageI18n) {
		this.messageI18n = messageI18n;
	}

	@Override
	public String[] getMessageValues() {
		if(StringHelper.containsNonWhitespace(messageValue5)) {
			return new String[] { messageValue1, messageValue2, messageValue3, messageValue4, messageValue5 };
		} else if(StringHelper.containsNonWhitespace(messageValue4)) {
			return new String[] { messageValue1, messageValue2, messageValue3, messageValue4 };
		} else if(StringHelper.containsNonWhitespace(messageValue3)) {
			return new String[] { messageValue1, messageValue2, messageValue3 };
		} else if(StringHelper.containsNonWhitespace(messageValue2)) {
			return new String[] { messageValue1, messageValue2 };
		} else if(StringHelper.containsNonWhitespace(messageValue1)) {
			return new String[] { messageValue1 };
		}
		return new String[0];
	}

	public String getMessageValue1() {
		return messageValue1;
	}

	public void setMessageValue1(String messageValue1) {
		this.messageValue1 = messageValue1;
	}

	public String getMessageValue2() {
		return messageValue2;
	}

	public void setMessageValue2(String messageValue2) {
		this.messageValue2 = messageValue2;
	}

	public String getMessageValue3() {
		return messageValue3;
	}

	public void setMessageValue3(String messageValue3) {
		this.messageValue3 = messageValue3;
	}

	public String getMessageValue4() {
		return messageValue4;
	}

	public void setMessageValue4(String messageValue4) {
		this.messageValue4 = messageValue4;
	}

	public String getMessageValue5() {
		return messageValue5;
	}

	public void setMessageValue5(String messageValue5) {
		this.messageValue5 = messageValue5;
	}

	@Override
	public Long getPositionKey() {
		return positionKey;
	}

	public void setPositionKey(Long positionKey) {
		this.positionKey = positionKey;
	}

	@Override
	public Long getApplicationKey() {
		return applicationKey;
	}

	public void setApplicationKey(Long applicationKey) {
		this.applicationKey = applicationKey;
	}

	@Override
	public Long getCommentKey() {
		return commentKey;
	}

	public void setCommentKey(Long commentKey) {
		this.commentKey = commentKey;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public int hashCode() {
		return key == null ? 236520 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RecruitingAuditLogLightImpl) {
			RecruitingAuditLogLightImpl auditLog = (RecruitingAuditLogLightImpl)obj;
			return key != null && key.equals(auditLog.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}