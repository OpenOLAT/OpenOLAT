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


package org.olat.restapi.system.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Description:<br>
 * This VO holds statistics about a doceditor.
 * 
 * Initial date: 13 Jul. 2020<br>
 * @author morjen, moritz.jenny@frentix.com, http://www.frentix.com
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DocEditorStatisticsVO")
public class DocEditorStatisticsVO {

	@XmlAttribute(name="appName", required=false)
	private String appName;
	
	@XmlAttribute(name="openDocumentsRead", required=false)
	private long openDocumentsRead;
	
	@XmlAttribute(name="openDocumentsWrite", required=false)
	private long openDocumentsWrite;

	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public long getOpenDocumentsRead() {
		return openDocumentsRead;
	}
	public void setOpenDocumentsRead(long openDocumentsRead) {
		this.openDocumentsRead = openDocumentsRead;
	}
	public long getOpenDocumentsWrite() {
		return openDocumentsWrite;
	}
	public void setOpenDocumentsWrite(long openDocumentsWrite) {
		this.openDocumentsWrite = openDocumentsWrite;
	}
	
	

}
