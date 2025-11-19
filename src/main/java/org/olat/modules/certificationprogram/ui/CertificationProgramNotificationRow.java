/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.ui.component.Duration;

/**
 * 
 * Initial date: 10 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramNotificationRow {
	
	private final FormLink toolsLink;
	private final FormToggle statusEl;
	private final String notificationLabel;
	
	private CertificationProgramMailConfiguration mailConfiguration;
	
	private CertificationProgramNotificationDetailsController detailsCtrl;
	
	public CertificationProgramNotificationRow(String notificationLabel, CertificationProgramMailConfiguration mailConfiguration,
			FormToggle statusEl, FormLink toolsLink) {
		this.statusEl = statusEl;
		this.toolsLink = toolsLink;
		this.notificationLabel = notificationLabel;
		this.mailConfiguration = mailConfiguration;
	}
	
	public Long getKey() {
		return mailConfiguration.getKey();
	}

	public String getNotificationLabel() {
		return notificationLabel;
	}
	
	public CertificationProgramMailType getType() {
		return mailConfiguration.getType();
	}
	
	public boolean isCreditBalanceTooLow() {
		return mailConfiguration.isCreditBalanceTooLow();
	}
	
	public Duration getDuration() {
		return mailConfiguration.getTimeDuration();
	}
	
	public boolean isCustomized() {
		return mailConfiguration.isCustomized();
	}
	
	public String getI18nSuffix() {
		return mailConfiguration.getI18nSuffix();
	}
	
	public FormToggle getStatusEl() {
		return statusEl;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public boolean isDetailsControllerAvailable() {
		return detailsCtrl != null;
	}
	
	public CertificationProgramNotificationDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(CertificationProgramNotificationDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
