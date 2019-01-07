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
package org.olat.modules.edusharing;

/**
 * 
 * Initial date: 3 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GetRenderedParameter {
	
	private final String appId;
	private final String repoId;
	private final String nodeId;
	private final String resourceId;
	private final String courseId;
	private final String version;
	private final String locale;
	private final String language;
	private final String signed;
	private final String signature;
	private final String timestamp;
	private final String encryptedUserIdentifier;
	private final String displayMode;
	private String width;
	private String height;
	private String encryptedTicket;

	public GetRenderedParameter(String appId, String repoId, String nodeId, String resourceId, String courseId,
			String version, String locale, String language, String signed, String signature, String timestamp,
			String encryptedUserIdentifier, String displayMode) {
		this.appId = appId;
		this.repoId = repoId;
		this.nodeId = nodeId;
		this.resourceId = resourceId;
		this.courseId = courseId;
		this.version = version;
		this.locale = locale;
		this.language = language;
		this.signed = signed;
		this.signature = signature;
		this.timestamp = timestamp;
		this.encryptedUserIdentifier = encryptedUserIdentifier;
		this.displayMode = displayMode;
	}

	public String getAppId() {
		return appId;
	}

	public String getRepoId() {
		return repoId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getCourseId() {
		return courseId;
	}

	public String getVersion() {
		return version;
	}

	public String getLocale() {
		return locale;
	}

	public String getLanguage() {
		return language;
	}

	public String getSigned() {
		return signed;
	}

	public String getSignature() {
		return signature;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getEncryptedUserIdentifier() {
		return encryptedUserIdentifier;
	}

	public String getDisplayMode() {
		return displayMode;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getEncryptedTicket() {
		return encryptedTicket;
	}

	public void setEncryptedTicket(String encryptedTicket) {
		this.encryptedTicket = encryptedTicket;
	}
	
}
