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
package org.olat.core.commons.services.doceditor.office365.restapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 
 * Initial date: 26.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckFileInfoVO {
	
	// Sources
	// https://wopi.readthedocs.io/projects/wopirest/en/latest/files/CheckFileInfo.html

	private final String baseFileName;
	private final long size;
	private final String version;
	private final String ownerId;
	private final String userId;

	private final Boolean supportsGetLock;
	private final Boolean supportsLocks;
	private final Boolean supportsExtendedLockLength;
	private final Boolean supportsUpdate;
	private final Boolean supportsRename;
	
	private final Boolean readOnly;
	private final Boolean userCanWrite;
	private final Boolean userCanNotWriteRelative;
	private final Boolean userCanRename;
	
	private final String lastModifiedTime;
	private final String userFriendlyName;
	
	private CheckFileInfoVO(Builder builder) {
		this.baseFileName = builder.baseFileName;
		this.size = builder.size;
		this.version = builder.version;
		this.ownerId = builder.ownerId;
		this.userId = builder.userId;
		this.supportsGetLock = builder.supportsGetLock;
		this.supportsLocks = builder.supportsLocks;
		this.supportsExtendedLockLength = builder.supportsExtendedLockLength;
		this.supportsUpdate = builder.supportsUpdate;
		this.supportsRename = builder.supportsRename;
		this.readOnly = builder.readOnly;
		this.userCanWrite = builder.userCanWrite;
		this.userCanNotWriteRelative = builder.userCanNotWriteRelative;
		this.userCanRename = builder.userCanRename;
		this.lastModifiedTime = builder.lastModifiedTime;
		this.userFriendlyName = builder.userFriendlyName;
	}
	
	public String getBaseFileName() {
		return baseFileName;
	}

	public long getSize() {
		return size;
	}

	public String getVersion() {
		return version;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getUserId() {
		return userId;
	}

	public Boolean getSupportsGetLock() {
		return supportsGetLock;
	}

	public Boolean getSupportsLocks() {
		return supportsLocks;
	}

	public Boolean getSupportsExtendedLockLength() {
		return supportsExtendedLockLength;
	}

	public Boolean getSupportsUpdate() {
		return supportsUpdate;
	}

	public Boolean getSupportsRename() {
		return supportsRename;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public Boolean getUserCanWrite() {
		return userCanWrite;
	}

	public Boolean getUserCanNotWriteRelative() {
		return userCanNotWriteRelative;
	}

	public Boolean getUserCanRename() {
		return userCanRename;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public String getUserFriendlyName() {
		return userFriendlyName;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		private String baseFileName;
		private long size;
		private String version;
		private String ownerId;
		private String userId;
		private Boolean supportsGetLock;
		private Boolean supportsLocks;
		private Boolean supportsExtendedLockLength;
		private Boolean supportsUpdate;
		private Boolean supportsRename;
		private Boolean readOnly;
		private Boolean userCanWrite;
		private Boolean userCanNotWriteRelative;
		private Boolean userCanRename;
		private String lastModifiedTime;
		private String userFriendlyName;

		private Builder() {
		}

		public Builder withBaseFileName(String baseFileName) {
			this.baseFileName = baseFileName;
			return this;
		}

		public Builder withSize(long size) {
			this.size = size;
			return this;
		}

		public Builder withVersion(String version) {
			this.version = version;
			return this;
		}

		public Builder withOwnerId(String ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public Builder withUserId(String userId) {
			this.userId = userId;
			return this;
		}

		public Builder withSupportsGetLock(Boolean supportsGetLock) {
			this.supportsGetLock = supportsGetLock;
			return this;
		}

		public Builder withSupportsLocks(Boolean supportsLocks) {
			this.supportsLocks = supportsLocks;
			return this;
		}

		public Builder withSupportsExtendedLockLength(Boolean supportsExtendedLockLength) {
			this.supportsExtendedLockLength = supportsExtendedLockLength;
			return this;
		}

		public Builder withSupportsUpdate(Boolean supportsUpdate) {
			this.supportsUpdate = supportsUpdate;
			return this;
		}

		public Builder withSupportsRename(Boolean supportsRename) {
			this.supportsRename = supportsRename;
			return this;
		}

		public Builder withReadOnly(Boolean readOnly) {
			this.readOnly = readOnly;
			return this;
		}

		public Builder withUserCanWrite(Boolean userCanWrite) {
			this.userCanWrite = userCanWrite;
			return this;
		}

		public Builder withUserCanNotWriteRelative(Boolean userCanNotWriteRelative) {
			this.userCanNotWriteRelative = userCanNotWriteRelative;
			return this;
		}

		public Builder withUserCanRename(Boolean userCanRename) {
			this.userCanRename = userCanRename;
			return this;
		}

		public Builder withLastModifiedTime(String lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
			return this;
		}

		public Builder withUserFriendlyName(String userFriendlyName) {
			this.userFriendlyName = userFriendlyName;
			return this;
		}

		public CheckFileInfoVO build() {
			return new CheckFileInfoVO(this);
		}
	}
	
}

