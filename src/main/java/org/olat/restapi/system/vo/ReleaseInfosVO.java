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
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "versionVO")
public class ReleaseInfosVO {
	
	@XmlAttribute(name="buildVersion", required=true)
	private String buildVersion;
	@XmlAttribute(name="olatVersion", required=true)
	private String olatVersion;
	@XmlAttribute(name="repoRevision", required=false)
	private String repoRevision;
	@XmlAttribute(name="instanceID",required=true)
	private String instanceID;
	
	public ReleaseInfosVO() {
		//make JAXB happy
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public String getOlatVersion() {
		return olatVersion;
	}

	public void setOlatVersion(String olatVersion) {
		this.olatVersion = olatVersion;
	}

	public String getRepoRevision() {
		return repoRevision;
	}

	public String getInstanceID(){
		return instanceID;
	}
	
	public void setInstanceID(String instanceid){
		this.instanceID = instanceid;
	}
	
	public void setRepoRevision(String repoRevision) {
		this.repoRevision = repoRevision;
	}
}