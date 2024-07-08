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
package org.olat.modules.sharepoint.model;

import java.util.Objects;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteAndDriveConfiguration {
	
	private String siteName;
	private String siteDisplayName;
	private String siteId;
	
	private String driveName;
	private String driveId;
	
	public SiteAndDriveConfiguration() {
		//
	}
	
	public SiteAndDriveConfiguration(String siteName, String siteDisplayName, String siteId) {
		this(siteName, siteDisplayName, siteId, null, null);
	}
	
	public SiteAndDriveConfiguration(String siteName, String siteDisplayName, String siteId, String driveName, String driveId) {
		this.siteName = siteName;
		this.siteDisplayName = siteDisplayName;
		this.siteId = siteId;
		this.driveName = driveName;
		this.driveId = driveId;
	}
	
	public String getSiteName() {
		return siteName;
	}
	
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	
	public String getSiteDisplayName() {
		return siteDisplayName;
	}

	public void setSiteDisplayName(String siteDisplayName) {
		this.siteDisplayName = siteDisplayName;
	}

	public String getSiteId() {
		return siteId;
	}
	
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public String getDriveName() {
		return driveName;
	}
	
	public void setDriveName(String driveName) {
		this.driveName = driveName;
	}
	
	public String getDriveId() {
		return driveId;
	}
	
	public void setDriveId(String driveId) {
		this.driveId = driveId;
	}

	@Override
	public int hashCode() {
		return (siteId == null ? 6290865 : siteId.hashCode())
				+ (driveId == null ? 88756 : driveId.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof SiteAndDriveConfiguration config) {
			return Objects.equals(siteId, config.siteId)
					&& Objects.equals(driveId, config.driveId);
		}
		return super.equals(obj);
	}
}
