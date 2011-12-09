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
 * JGS goodsolutions GmbH, http://www.goodsolutions.ch
 * <p>
 */
package ch.goodsolutions.olat.jmx;

public class AppDescriptor {

	private String contextPath;
	private String webappBasePath;
	private String state;
	private String instanceID;
	private String version;
	private String build;

	public AppDescriptor(String contextPath, String webappBasePath, String state, String instanceID, String version, String build) {
		this.contextPath = contextPath;
		this.webappBasePath = webappBasePath;
		this.state = state;
		this.instanceID = instanceID;
		this.version = version;
		this.build = build;
	}

	public boolean isGenuinOLAT() {
		return build != null;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public String getWebappBasePath() {
		return webappBasePath;
	}

	public String getState() {
		return state;
	}
	
	public String getInstanceID() {
		return instanceID;
	}

	public String getVersion() {
		return version;
	}

	public String getBuild() {
		return build;
	}

	public void setState(String state) {
		this.state = state;
	}

}
