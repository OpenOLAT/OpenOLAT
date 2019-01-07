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
package org.olat.modules.edusharing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Initial date: 3 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SearchResult {

	private String objectUrl;
	private String title;
	private String mimeType;
	private String resourceType;
	private String resourceVersion;
	private Integer windowHight;
	private Integer windowWidth;
	private Double ratio;
	private String windowVersion;
	private String repoType;
	private String mediaType;

	@JsonProperty("object_url")
	public String getObjectUrl() {
		return objectUrl;
	}

	public void setObjectUrl(String objectUrl) {
		this.objectUrl = objectUrl;
	}
	
	@JsonProperty("title")
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@JsonProperty("mimetype")
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	@JsonProperty("resourcetype")
	public String getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	@JsonProperty("resourceversion")
	public String getResourceVersion() {
		return resourceVersion;
	}
	
	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}
	
	@JsonProperty("window_height")
	public Integer getWindowHight() {
		return windowHight;
	}
	
	public void setWindowHight(Integer windowHight) {
		this.windowHight = windowHight;
	}
	
	@JsonProperty("window_width")
	public Integer getWindowWidth() {
		return windowWidth;
	}
	
	public void setWindowWidth(Integer windowWidth) {
		this.windowWidth = windowWidth;
	}
	
	@JsonProperty("ratio")
	public Double getRatio() {
		return ratio;
	}
	
	public void setRatio(Double ratio) {
		this.ratio = ratio;
	}
	
	@JsonProperty("window_version")
	public String getWindowVersion() {
		return windowVersion;
	}
	
	public void setWindowVersion(String windowVersion) {
		this.windowVersion = windowVersion;
	}
	
	@JsonProperty("repotype")
	public String getRepoType() {
		return repoType;
	}
	
	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}
	
	@JsonProperty("mediatype")
	public String getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	
}
