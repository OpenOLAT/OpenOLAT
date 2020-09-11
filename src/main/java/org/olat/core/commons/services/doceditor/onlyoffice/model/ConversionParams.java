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
package org.olat.core.commons.services.doceditor.onlyoffice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * Initial date: 4 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversionParams {
	
	private String filetype;
	private String key;
	private String outputtype;
	private Thumbnail thumbnail;
	private String title;
	private String url;
	private String token;
	
	public String getFiletype() {
		return filetype;
	}
	
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getOutputtype() {
		return outputtype;
	}
	
	public void setOutputtype(String outputtype) {
		this.outputtype = outputtype;
	}
	
	public Thumbnail getThumbnail() {
		return thumbnail;
	}
	
	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
