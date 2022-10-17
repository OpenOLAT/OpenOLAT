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

package org.olat.modules.invitation.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.invitation.InvitationAdditionalInfos;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.manager.InvitationAdditionalInfosXStream;

/**
 * 
 * Description:<br>
 * Implementation of Invitation
 * 
 * <P>
 * Initial Date:  10 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="binvitation")
@Table(name="o_bs_invitation")
public class InvitationImpl implements Persistable, Invitation {

	private static final long serialVersionUID = -9122616013810215550L;
	
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

	@Column(name="token", nullable=true, unique=true, insertable=true, updatable=true)
	private String token;
	@Enumerated(EnumType.STRING)
	@Column(name="i_status", nullable=true, unique=true, insertable=true, updatable=true)
	private InvitationStatusEnum status;
	
	@Column(name="first_name", nullable=true, unique=true, insertable=true, updatable=true)
	private String firstName;
	@Column(name="last_name", nullable=true, unique=true, insertable=true, updatable=true)
	private String lastName;
	@Column(name="mail", nullable=true, unique=true, insertable=true, updatable=true)
	private String mail;
	@Column(name="i_additional_infos", nullable=true, unique=false, insertable=true, updatable=true)
	private String additionalInfosString;

	@Enumerated(EnumType.STRING)
	@Column(name="i_type", nullable=false, unique=false, insertable=true, updatable=false)
	private InvitationTypeEnum type;
	@Column(name="i_roles", nullable=true, unique=false, insertable=true, updatable=true)
	private String roles;
	@Column(name="i_registration", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean registration;
	@Column(name="i_url", nullable=true, unique=false, insertable=true, updatable=true)
	private String url;

	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	//optional, nullable and updatable for compatible reasons
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=true)
	private Identity identity;
	
	public InvitationImpl() {
		//
	}
	
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

	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public InvitationStatusEnum getStatus() {
		return status;
	}

	@Override
	public void setStatus(InvitationStatusEnum status) {
		this.status = status;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String getMail() {
		return mail;
	}

	@Override
	public void setMail(String mail) {
		this.mail = mail;
	}

	@Override
	public InvitationTypeEnum getType() {
		return type;
	}

	public void setType(InvitationTypeEnum type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	@Override
	@Transient
	public List<String> getRoleList() {
		if(StringHelper.containsNonWhitespace(roles)) {
			String[] rolesArr = roles.split("[,]");
			return List.of(rolesArr);
		}
		return List.of();
	}

	@Override
	public void setRoleList(List<String> roles) {
		if(roles == null || roles.isEmpty()) {
			this.roles = null;
		} else {
			this.roles = String.join(",", roles);
		}
	}
	
	@Override
	public boolean isRegistration() {
		return registration;
	}

	@Override
	public void setRegistration(boolean registration) {
		this.registration = registration;
	}

	public String getAdditionalInfosString() {
		return additionalInfosString;
	}

	public void setAdditionalInfosString(String additionalInfos) {
		this.additionalInfosString = additionalInfos;
	}
	
	@Override
	public InvitationAdditionalInfos getAdditionalInfos() {
		return InvitationAdditionalInfosXStream.fromXml(additionalInfosString);
	}

	@Override
	public void setAdditionalInfos(InvitationAdditionalInfos infos) {
		setAdditionalInfosString(InvitationAdditionalInfosXStream.toXml(infos));
	}

	@Override
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
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
		return key == null ? -98260 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof InvitationImpl) {
			InvitationImpl invitation = (InvitationImpl)obj;
			return getKey() != null && getKey().equals(invitation.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
