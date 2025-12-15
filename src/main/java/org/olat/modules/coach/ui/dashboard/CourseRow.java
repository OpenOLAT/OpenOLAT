/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.coach.ui.dashboard;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Oct 31, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseRow implements OLATResourceable {
	
	private Long key;
	private String externalRef;
	private String translatedTechnicalType;
	private String displayName;
	private String thumbnailRelPath;
	private String completion;
	private String successStatus;
	private String resourceableTypeName;
	private Long resourceableId;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}
	
	@Override
	public String getResourceableTypeName() {
		return resourceableTypeName;
	}

	public void setResourceableTypeName(String resourceableTypeName) {
		this.resourceableTypeName = resourceableTypeName;
	}

	@Override
	public Long getResourceableId() {
		return resourceableId;
	}
	
	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}

	public String getTranslatedTechnicalType() {
		return translatedTechnicalType;
	}

	public void setTranslatedTechnicalType(String translatedTechnicalType) {
		this.translatedTechnicalType = translatedTechnicalType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}

	public String getCompletion() {
		return completion;
	}

	public void setCompletion(String completion) {
		this.completion = completion;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}
	
}
