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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "monitoringInfosVO")
public class MonitoringInfosVO {

	private String type;
	private String description;
	@XmlElementWrapper(name="probes")
	@XmlElement(name="probe",nillable=true)
	private String[] probes;
	@XmlElementWrapper(name="dependencies")
	@XmlElement(name="dependency",nillable=true)
	private MonitoringDependencyVO[] dependencies;
	
	public MonitoringInfosVO() {
		//
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getProbes() {
		return probes;
	}

	public void setProbes(String[] probes) {
		this.probes = probes;
	}
	
	public MonitoringDependencyVO[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(MonitoringDependencyVO[] dependencies) {
		this.dependencies = dependencies;
	}
}
