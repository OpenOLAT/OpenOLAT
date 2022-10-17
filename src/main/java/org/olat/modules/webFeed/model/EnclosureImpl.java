/**
 * <a href="http://www.openolat.org">
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this uploadedFile except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import org.olat.modules.webFeed.Enclosure;

/**
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
@Embeddable
public class EnclosureImpl implements Enclosure, Serializable {
	
	private static final long serialVersionUID = 4665522340896625643L;
	
	private String fileName;
	private String type;
	private Long length;
	private String externalUrl;
	
	@Override
	public String getFileName() {
		return fileName;
	}
	
	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public Long getLength() {
		return length;
	}
	
	@Override
	public void setLength(Long length) {
		this.length = length;
	}
	
	@Override
	public String getExternalUrl() {
		return externalUrl;
	}
	
	@Override
	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}
	
}
