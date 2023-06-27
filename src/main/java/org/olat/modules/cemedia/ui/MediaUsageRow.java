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
package org.olat.modules.cemedia.ui;

import org.olat.modules.cemedia.model.MediaUsageWithStatus;

/**
 * 
 * Initial date: 5 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUsageRow {
	
	private String page;
	private String pageIconCssClass;
	
	private String resourceName;
	private String resourceIconCssClass;
	private String businessPath;
	
	private Long binderKey;
	private Long repositoryEntryKey;
	private String subIdent;
	private Long pageKey;
	
	private String versionName;
	private boolean revoked;
	
	public MediaUsageRow() {
		//
	}
	
	public static MediaUsageRow valueOf(MediaUsageWithStatus usedIn) {
		MediaUsageRow row = new MediaUsageRow();
		row.setPage(usedIn.pageTitle());
		row.setPageIconCssClass("o_icon o_icon-fw o_page_icon");
		row.setVersionName(usedIn.mediaVersionName());
		row.setRevoked(!usedIn.validGroup());
		row.setRepositoryEntryKey(usedIn.repositoryEntryKey());
		row.setSubIdent(usedIn.subIdent());
		row.setBinderKey(usedIn.binderKey());
		row.setPageKey(usedIn.pageKey());
		
		if(usedIn.binderKey() != null) {
			row.setResourceName(usedIn.binderTitle());
			row.setResourceIconCssClass("o_icon o_icon-fw o_icon_pf_binder");
		} else if(usedIn.repositoryEntryKey() != null) {
			row.setResourceName(usedIn.repositoryEntryDisplayname());
			row.setResourceIconCssClass("o_icon o_icon-fw o_CourseModule_icon");
		}
		return row;
	}
	
	public String getPage() {
		return page;
	}
	
	public void setPage(String page) {
		this.page = page;
	}
	
	public String getPageIconCssClass() {
		return pageIconCssClass;
	}
	
	public void setPageIconCssClass(String pageIconCssClass) {
		this.pageIconCssClass = pageIconCssClass;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceIconCssClass() {
		return resourceIconCssClass;
	}

	public void setResourceIconCssClass(String resourceIconCssClass) {
		this.resourceIconCssClass = resourceIconCssClass;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Long getBinderKey() {
		return binderKey;
	}

	public void setBinderKey(Long binderKey) {
		this.binderKey = binderKey;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	public Long getPageKey() {
		return pageKey;
	}

	public void setPageKey(Long pageKey) {
		this.pageKey = pageKey;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}
}
