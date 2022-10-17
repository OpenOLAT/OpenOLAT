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
package org.olat.user.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.user.UserDataExport;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="userdataexport")
@Table(name="o_user_data_export")
@NamedQuery(name="loadUserExportDataByKey", query="select data from userdataexport data where data.key=:dataKey")
@NamedQuery(name="loadUserExportDataByIdentity", query="select data from userdataexport data where data.identity.key=:identityKey")
public class UserDataExportImpl implements UserDataExport, Persistable {

	private static final long serialVersionUID = 3359965105813614815L;

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
	
	@Column(name="u_directory", nullable=true, insertable=true, updatable=false)
	private String directory;
	@Column(name="u_status", nullable=true, insertable=true, updatable=true)
	private String statusString;
	@Column(name="u_export_ids", nullable=true, insertable=true, updatable=true)
	private String exporterIdList;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
    private Identity identity;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_request_by", nullable=true, insertable=true, updatable=false)
    private Identity requestBy;

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
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	@Override
	public ExportStatus getStatus() {
		return StringHelper.containsNonWhitespace(statusString)
				? ExportStatus.valueOf(statusString) : ExportStatus.none;
	}

	@Override
	public void setStatus(ExportStatus status) {
		if(status == null) {
			statusString = ExportStatus.none.name();
		} else {
			statusString = status.name();
		}
	}

	@Override
	@Transient
	public Set<String> getExportIds() {
		if(StringHelper.containsNonWhitespace(exporterIdList)) {
			String[] ids = exporterIdList.split("[,]");
			Set<String> idSet = new HashSet<>();
			for(String id:ids) {
				idSet.add(id);
			}
			return idSet;
		}
		return Collections.emptySet();
	}

	public String getExporterIdList() {
		return exporterIdList;
	}

	public void setExporterIdList(String exporterIdList) {
		this.exporterIdList = exporterIdList;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Identity getRequestBy() {
		return requestBy;
	}

	public void setRequestBy(Identity requestBy) {
		this.requestBy = requestBy;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2387646 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserDataExportImpl) {
			UserDataExportImpl data = (UserDataExportImpl)obj;
			return getKey() != null && getKey().equals(data.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
