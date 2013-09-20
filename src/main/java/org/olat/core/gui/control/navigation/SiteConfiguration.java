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
package org.olat.core.gui.control.navigation;

/**
 * XStream mapping class
 * 
 * 
 * Initial date: 19.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteConfiguration {
	
	private String id;
	private int order;
	private boolean enabled;
	private String securityCallbackBeanId;
	private String alternativeControllerBeanId;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getSecurityCallbackBeanId() {
		return securityCallbackBeanId;
	}

	public void setSecurityCallbackBeanId(String securityCallbackBeanId) {
		this.securityCallbackBeanId = securityCallbackBeanId;
	}

	public String getAlternativeControllerBeanId() {
		return alternativeControllerBeanId;
	}

	public void setAlternativeControllerBeanId(String alternativeControllerBeanId) {
		this.alternativeControllerBeanId = alternativeControllerBeanId;
	}
}
