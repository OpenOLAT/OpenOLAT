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
package org.olat.modules.wopi.collabora.restapi;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 
 * Initial date: 3 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckFileInfoVO {
	
	// Sources
	// https://github.com/LibreOffice/online/blob/master/wsd/reference.md#checkfileinfo-response-properties
	// https://github.com/LibreOffice/online/blob/f76b36193df424e9718bb25c589c6efd476774cc/wsd/Storage.cpp#L515

	private final String baseFileName;
	private final long size;
	private final String version;
	private final String lastModifiedTime;
	
	private final String ownerId;
	private final String userId;
	private final Boolean obfuscatedUserId;
	private final String userFriendlyName;
	private final String userExtraInfo;
	
	private final String postMessageOrigin;
	private final String templateSaveAs;
	private final String watermarkText;

	private final Boolean userCanWrite;
	private final Boolean userCanNotWriteRelative;
	private final Boolean hidePrintOption;
	private final Boolean hideExportOption;
	private final Boolean hideSaveOption;
	private final Boolean hideUserList;
	private final Boolean disablePrint;
	private final Boolean disableExport;
	private final Boolean disableCopy;
	private final Boolean disableInactiveMessages;
	private final Boolean enableOwnerTermination;
	private final Boolean enableInsertRemoteImage;
	private final Boolean enableShare;
	private final Boolean disableChangeTrackingRecord;
	private final Boolean disableChangeTrackingShow;
	private final Boolean hideChangeTrackingControls;

	@Generated("SparkTools")
	private CheckFileInfoVO(Builder builder) {
		this.baseFileName = builder.baseFileName;
		this.size = builder.size;
		this.version = builder.version;
		this.lastModifiedTime = builder.lastModifiedTime;
		this.ownerId = builder.ownerId;
		this.userId = builder.userId;
		this.obfuscatedUserId = builder.obfuscatedUserId;
		this.userFriendlyName = builder.userFriendlyName;
		this.userExtraInfo = builder.userExtraInfo;
		this.postMessageOrigin = builder.postMessageOrigin;
		this.templateSaveAs = builder.templateSaveAs;
		this.watermarkText = builder.watermarkText;
		this.userCanWrite = builder.userCanWrite;
		this.userCanNotWriteRelative = builder.userCanNotWriteRelative;
		this.hidePrintOption = builder.hidePrintOption;
		this.hideExportOption = builder.hideExportOption;
		this.hideSaveOption = builder.hideSaveOption;
		this.hideUserList = builder.hideUserList;
		this.disablePrint = builder.disablePrint;
		this.disableExport = builder.disableExport;
		this.disableCopy = builder.disableCopy;
		this.disableInactiveMessages = builder.disableInactiveMessages;
		this.enableOwnerTermination = builder.enableOwnerTermination;
		this.enableInsertRemoteImage = builder.enableInsertRemoteImage;
		this.enableShare = builder.enableShare;
		this.disableChangeTrackingRecord = builder.disableChangeTrackingRecord;
		this.disableChangeTrackingShow = builder.disableChangeTrackingShow;
		this.hideChangeTrackingControls = builder.hideChangeTrackingControls;
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

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getUserId() {
		return userId;
	}

	public Boolean getObfuscatedUserId() {
		return obfuscatedUserId;
	}

	public String getUserFriendlyName() {
		return userFriendlyName;
	}

	public String getUserExtraInfo() {
		return userExtraInfo;
	}

	public String getPostMessageOrigin() {
		return postMessageOrigin;
	}

	public String getTemplateSaveAs() {
		return templateSaveAs;
	}

	public String getWatermarkText() {
		return watermarkText;
	}

	public Boolean getUserCanWrite() {
		return userCanWrite;
	}

	public Boolean getUserCanNotWriteRelative() {
		return userCanNotWriteRelative;
	}

	public Boolean getHidePrintOption() {
		return hidePrintOption;
	}

	public Boolean getHideExportOption() {
		return hideExportOption;
	}

	public Boolean getHideSaveOption() {
		return hideSaveOption;
	}

	public Boolean getHideUserList() {
		return hideUserList;
	}

	public Boolean getDisablePrint() {
		return disablePrint;
	}

	public Boolean getDisableExport() {
		return disableExport;
	}
	
	public Boolean getDisableCopy() {
		return disableCopy;
	}

	public Boolean getDisableInactiveMessages() {
		return disableInactiveMessages;
	}

	public Boolean getEnableOwnerTermination() {
		return enableOwnerTermination;
	}

	public Boolean getEnableInsertRemoteImage() {
		return enableInsertRemoteImage;
	}

	public Boolean getEnableShare() {
		return enableShare;
	}

	public Boolean getDisableChangeTrackingRecord() {
		return disableChangeTrackingRecord;
	}

	public Boolean getDisableChangeTrackingShow() {
		return disableChangeTrackingShow;
	}

	public Boolean getHideChangeTrackingControls() {
		return hideChangeTrackingControls;
	}

	/**
	 * Creates builder to build {@link CheckFileInfoVO}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link CheckFileInfoVO}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String baseFileName;
		private long size;
		private String version;
		private String lastModifiedTime;
		private String ownerId;
		private String userId;
		private Boolean obfuscatedUserId;
		private String userFriendlyName;
		private String userExtraInfo;
		private String postMessageOrigin;
		private String templateSaveAs;
		private String watermarkText;
		private Boolean userCanWrite;
		private Boolean userCanNotWriteRelative;
		private Boolean hidePrintOption;
		private Boolean hideExportOption;
		private Boolean hideSaveOption;
		private Boolean hideUserList;
		private Boolean disablePrint;
		private Boolean disableExport;
		private Boolean disableCopy;
		private Boolean disableInactiveMessages;
		private Boolean enableOwnerTermination;
		private Boolean enableInsertRemoteImage;
		private Boolean enableShare;
		private Boolean disableChangeTrackingRecord;
		private Boolean disableChangeTrackingShow;
		private Boolean hideChangeTrackingControls;

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

		public Builder withLastModifiedTime(String lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
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

		public Builder withObfuscatedUserId(Boolean obfuscatedUserId) {
			this.obfuscatedUserId = obfuscatedUserId;
			return this;
		}

		public Builder withUserFriendlyName(String userFriendlyName) {
			this.userFriendlyName = userFriendlyName;
			return this;
		}

		public Builder withUserExtraInfo(String userExtraInfo) {
			this.userExtraInfo = userExtraInfo;
			return this;
		}

		public Builder withPostMessageOrigin(String postMessageOrigin) {
			this.postMessageOrigin = postMessageOrigin;
			return this;
		}

		public Builder withTemplateSaveAs(String templateSaveAs) {
			this.templateSaveAs = templateSaveAs;
			return this;
		}

		public Builder withWatermarkText(String watermarkText) {
			this.watermarkText = watermarkText;
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

		public Builder withHidePrintOption(Boolean hidePrintOption) {
			this.hidePrintOption = hidePrintOption;
			return this;
		}

		public Builder withHideExportOption(Boolean hideExportOption) {
			this.hideExportOption = hideExportOption;
			return this;
		}

		public Builder withHideSaveOption(Boolean hideSaveOption) {
			this.hideSaveOption = hideSaveOption;
			return this;
		}

		public Builder withHideUserList(Boolean hideUserList) {
			this.hideUserList = hideUserList;
			return this;
		}

		public Builder withDisablePrint(Boolean disablePrint) {
			this.disablePrint = disablePrint;
			return this;
		}

		public Builder withDisableExport(Boolean disableExport) {
			this.disableExport = disableExport;
			return this;
		}

		public Builder withDisableCopy(Boolean disableCopy) {
			this.disableCopy = disableCopy;
			return this;
		}

		public Builder withDisableInactiveMessages(Boolean disableInactiveMessages) {
			this.disableInactiveMessages = disableInactiveMessages;
			return this;
		}

		public Builder withEnableOwnerTermination(Boolean enableOwnerTermination) {
			this.enableOwnerTermination = enableOwnerTermination;
			return this;
		}

		public Builder withEnableInsertRemoteImage(Boolean enableInsertRemoteImage) {
			this.enableInsertRemoteImage = enableInsertRemoteImage;
			return this;
		}

		public Builder withEnableShare(Boolean enableShare) {
			this.enableShare = enableShare;
			return this;
		}

		public Builder withDisableChangeTrackingRecord(Boolean disableChangeTrackingRecord) {
			this.disableChangeTrackingRecord = disableChangeTrackingRecord;
			return this;
		}

		public Builder withDisableChangeTrackingShow(Boolean disableChangeTrackingShow) {
			this.disableChangeTrackingShow = disableChangeTrackingShow;
			return this;
		}

		public Builder withHideChangeTrackingControls(Boolean hideChangeTrackingControls) {
			this.hideChangeTrackingControls = hideChangeTrackingControls;
			return this;
		}

		public CheckFileInfoVO build() {
			return new CheckFileInfoVO(this);
		}
	}
	
}

