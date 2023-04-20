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
package org.olat.modules.jupyterhub;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Tool;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface JupyterHub extends ModifiedInfo, CreateInfo {

	Long getKey();

	String getName();

	void setName(String name);

	JupyterHubStatus getStatus();

	void setStatus(JupyterHubStatus status);

	String getRam();

	void setRam(String ram);

	long getCpu();

	void setCpu(long cpu);

	String getImageCheckingServiceUrl();

	void setImageCheckingServiceUrl(String imageCheckingServiceUrl);

	String getInfoText();

	void setInfoText(String infoText);

	String getLtiKey();

	void setLtiKey(String ltiKey);

	String getAccessToken();

	void setAccessToken(String accessToken);

	AgreementSetting getAgreementSetting();

	void setAgreementSetting(AgreementSetting agreementSetting);

	LTI13Tool getLtiTool();

	void setLtiTool(LTI13Tool ltiTool);

	String getJupyterHubUrl();

	void setJupyterHubUrl(String jupyterHubUrl);

	enum JupyterHubStatus {
		active, inactive
	}

	enum AgreementSetting {
		requireAgreement,
		suppressAgreement,
		configurableByAuthor
	}

	static boolean validateRam(String ram) {
		if (!StringHelper.containsNonWhitespace(ram)) {
			return false;
		}

		ram = ram.toUpperCase();
		return ram.matches("[. 0-9]+(KB|MB|GB)");
	}

	static String standardizeRam(String ram) {
		if (ram == null) {
			return "0MB";
		}
		ram = ram.toUpperCase();
		return ram.replace(" ", "");
	}
}
