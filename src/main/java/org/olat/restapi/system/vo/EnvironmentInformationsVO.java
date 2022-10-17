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

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <h3>Description:</h3>

 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "environmentVO")
public class EnvironmentInformationsVO {

	@XmlAttribute(name="arch", required=true)
	private String arch;
	@XmlAttribute(name="osName", required=true)
	private String osName;
	@XmlAttribute(name="osVersion", required=true)
	private String osVersion;
	@XmlAttribute(name="availableProcessors", required=true)
	private int availableProcessors;
	@XmlAttribute(name="runtimeName", required=true)
	private String runtimeName;
	@XmlAttribute(name="vmName", required=true)
	private String vmName;
	@XmlAttribute(name="vmVendor", required=true)
	private String vmVendor;
	@XmlAttribute(name="vmVersion", required=true)
	private String vmVersion;
	
	public EnvironmentInformationsVO() {
		//make JAXB happy
	}
	
	public EnvironmentInformationsVO(OperatingSystemMXBean bean, RuntimeMXBean runtime) {
		arch = bean.getArch();
		osName = bean.getName();
		osVersion = bean.getVersion();
		availableProcessors = bean.getAvailableProcessors();

		runtimeName = runtime.getName();
		vmName = runtime.getVmName();
		vmVendor = runtime.getVmVendor();
		vmVersion = runtime.getVmVersion();
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public String getRuntimeName() {
		return runtimeName;
	}

	public void setRuntimeName(String runtimeName) {
		this.runtimeName = runtimeName;
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getVmVendor() {
		return vmVendor;
	}

	public void setVmVendor(String vmVendor) {
		this.vmVendor = vmVendor;
	}

	public String getVmVersion() {
		return vmVersion;
	}

	public void setVmVersion(String vmVersion) {
		this.vmVersion = vmVersion;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
			.append("OS[Arch=").append(getArch()).append(':')
			.append("Name=").append(getOsName()).append(':')
			.append("Version=").append(getOsVersion()).append(':')
			.append("Proc=").append(getAvailableProcessors()).append(']');
		
		sb.append("Runtime[VM Name=").append(getRuntimeName()).append(':')
			.append("VM Name=").append(getVmName()).append(':')
			.append("Vendor=").append(getVmVendor()).append(':')
			.append("Version=").append(getVmVersion()).append(']');
		
		return sb.toString();
	}
}
