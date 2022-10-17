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

package org.olat.restapi.support.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;


/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  28.02.2014 <br>
 * @author Stephan Clemenz, clemenz@vcrp.de, VCRP
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fileMetadataVO")
public class FileMetadataVO {
	
	@XmlAttribute(name="fileName", required=true)
	private String fileName;
	@XmlAttribute(name="size", required=true)
	private Long size;
	@XmlAttribute(name="lastModified", required=true)
	private Long lastModified;
	@XmlAttribute(name="mimeType", required=true)
	private String mimeType;
	@XmlAttribute(name="href", required=true)
	private String href;
	
	public FileMetadataVO() {
		//make JAXB happy
	}
	
	public FileMetadataVO(String href, VFSLeaf leaf) {
		this.fileName = leaf.getName();
		this.size = leaf.getSize();
		this.lastModified = leaf.getLastModified();
		this.mimeType = WebappHelper.getMimeType(leaf.getName());
		this.href = href;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
}
