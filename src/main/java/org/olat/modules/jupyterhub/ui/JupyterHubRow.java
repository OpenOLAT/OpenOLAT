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
package org.olat.modules.jupyterhub.ui;

import java.math.BigDecimal;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.jupyterhub.JupyterHub;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubRow {
	private final Long numberOfApplications;
	private FormLink toolLink;
	private final JupyterHub hub;

	public JupyterHubRow(JupyterHub hub, Long numberOfApplications) {
		this.hub = hub;
		this.numberOfApplications = numberOfApplications;
	}

	public String getName() {
		return hub.getName();
	}

	public JupyterHub.JupyterHubStatus getStatus() {
		return hub.getStatus();
	}

	public String getClientId() {
		return hub.getLtiTool().getClientId();
	}

	public String getRam() {
		return hub.getRam();
	}

	public BigDecimal getCpu() {
		return hub.getCpu();
	}

	public Long getNumberOfApplications() {
		return numberOfApplications;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}

	public JupyterHub getHub() {
		return hub;
	}
}
