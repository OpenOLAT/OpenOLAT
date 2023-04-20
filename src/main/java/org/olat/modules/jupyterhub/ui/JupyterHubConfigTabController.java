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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.repository.RepositoryEntry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-17<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubConfigTabController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(JupyterHubConfigTabController.class);

	private final RepositoryEntry courseEntry;
	private final String subIdent;
	private SingleSelection jupyterHubEl;
	private StaticTextElement cpuEl;
	private StaticTextElement ramEl;
	private StaticTextElement infoTextEl;
	private TextElement imageEl;
	private FormLink checkImageButton;
	private final SelectionValues suppressDataTransmissionAgreementKV;
	private MultipleSelectionElement suppressDataTransmissionAgreementEl;
	private JupyterHub jupyterHub;

	@Autowired
	private JupyterManager jupyterManager;
	@Autowired
	private HttpClientService httpClientService;

	public JupyterHubConfigTabController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
										 String subIdent) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.subIdent = subIdent;

		suppressDataTransmissionAgreementKV = new SelectionValues();
		suppressDataTransmissionAgreementKV.add(SelectionValues.entry("on", translate("on")));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("jupyterHub.configuration.title");

		SelectionValues jupyterHubKV = jupyterManager.getJupyterHubsKV();

		if (jupyterHubKV.isEmpty()) {
			setFormWarning("form.warning.noHubs");
			return;
		}

		jupyterManager.initializeJupyterHubDeployment(courseEntry, subIdent, null, null, null);

		jupyterHubEl = uifactory.addDropdownSingleselect("jupyterHub", formLayout, jupyterHubKV.keys(), jupyterHubKV.values());
		jupyterHubEl.addActionListener(FormEvent.ONCHANGE);
		JupyterDeployment jupyterDeployment = jupyterManager.getJupyterDeployment(courseEntry, subIdent);

		String hubKey = jupyterDeployment.getJupyterHub().getKey().toString();
		jupyterHubEl.select(hubKey, true);
		if (!jupyterHubEl.isOneSelected() && !jupyterHubKV.isEmpty()) {
			jupyterHubEl.select(jupyterHubKV.keys()[0], true);
		}

		cpuEl = uifactory.addStaticTextElement("cpu", "table.header.hub.cpu", "", formLayout);
		ramEl = uifactory.addStaticTextElement("ram", "table.header.hub.ram", "", formLayout);
		infoTextEl = uifactory.addStaticTextElement("infoText", "form.hub.infoText", "", formLayout);
		infoTextEl.setElementCssClass("o_jupyter_info_text");

		imageEl = uifactory.addTextElement("image", "form.image", 255, "", formLayout);
		imageEl.setMandatory(true);
		checkImageButton = uifactory.addFormLink("form.checkImage", formLayout, Link.BUTTON);

		suppressDataTransmissionAgreementEl = uifactory.addCheckboxesHorizontal("form.suppressDataTransmissionAgreement", formLayout,
				suppressDataTransmissionAgreementKV.keys(), suppressDataTransmissionAgreementKV.values());

		jupyterHub = jupyterDeployment.getJupyterHub();
		setValues(jupyterDeployment.getImage(), jupyterDeployment.getSuppressDataTransmissionAgreement());

		uifactory.addFormSubmitButton("submit", formLayout);
	}

	private void setValues(String image, Boolean suppressDataTransmissionAgreement) {
		cpuEl.setValue("" + jupyterHub.getCpu());
		ramEl.setValue(jupyterHub.getRam());
		if (StringHelper.containsNonWhitespace(jupyterHub.getInfoText())) {
			infoTextEl.setValue(jupyterHub.getInfoText());
			infoTextEl.setVisible(true);
		} else {
			infoTextEl.setVisible(false);
		}
		imageEl.setValue(image);
		checkImageButton.setVisible(StringHelper.containsNonWhitespace(jupyterHub.getImageCheckingServiceUrl()));

		switch (jupyterHub.getAgreementSetting()) {
			case suppressAgreement, requireAgreement -> suppressDataTransmissionAgreementEl.setVisible(false);
			case configurableByAuthor -> {
				suppressDataTransmissionAgreementEl.setVisible(true);
				suppressDataTransmissionAgreementEl.select(suppressDataTransmissionAgreementKV.keys()[0], Boolean.TRUE.equals(suppressDataTransmissionAgreement));
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);

		if (jupyterHubEl == source) {
			jupyterHub = jupyterManager.getJupyterHub(jupyterHubEl.getSelectedKey());
			setValues("", false);
		} else if (checkImageButton == source) {
			doCheckImage();
		}
	}

	private void doCheckImage() {
		imageEl.clearWarning();
		imageEl.clearError();
		String image = imageEl.getValue();
		if (!StringHelper.containsNonWhitespace(image)) {
			imageEl.setWarningKey("form.image.warning.noValue");
			return;
		}
		try {
			String url = String.format(jupyterHub.getImageCheckingServiceUrl(), image);
			HttpGet httpGet = new HttpGet(url);
			try (CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(true);
				 CloseableHttpResponse response = httpClient.execute(httpGet)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					getWindowControl().setInfo(translate("form.image.info.exists", image));
				} else if (statusCode == 404) {
					imageEl.setWarningKey("form.image.warning.missing", image);
				} else {
					imageEl.setErrorKey("form.image.error.checkingServer");
					log.error("Invalid status code from image checking server: " + statusCode);
				}
			} catch (Exception e) {
				imageEl.setErrorKey("form.image.error.general");
				log.error("", e);
			}
		} catch (Exception e) {
			imageEl.setErrorKey("form.image.error.format");
			log.error("", e);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateImage();

		return allOk;
	}

	private boolean validateImage() {
		imageEl.clearError();
		if (!StringHelper.containsNonWhitespace(imageEl.getValue())) {
			imageEl.setErrorKey("form.legende.mandatory");
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		JupyterDeployment jupyterDeployment = jupyterManager.getJupyterDeployment(courseEntry, subIdent);
		jupyterDeployment.setImage(imageEl.getValue());
		if (suppressDataTransmissionAgreementEl.isVisible()) {
			jupyterDeployment.setSuppressDataTransmissionAgreement(suppressDataTransmissionAgreementEl.isAtLeastSelected(1));
		}
		if (jupyterDeployment.getJupyterHub() != jupyterHub) {
			jupyterHub = jupyterManager.getJupyterHub(Long.toString(jupyterHub.getKey()));
			jupyterManager.recreateJupyterHubDeployment(jupyterDeployment, courseEntry, subIdent, jupyterHub);
		} else {
			jupyterManager.updateJupyterDeployment(jupyterDeployment);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public String getClientId() {
		if (jupyterHubEl.isOneSelected()) {
			JupyterHub jupyterHub = jupyterManager.getJupyterHub(jupyterHubEl.getSelectedKey());
			return jupyterHub.getLtiTool().getClientId();
		}
		return null;
	}

	public String getImage() {
		return imageEl.getValue();
	}

	public boolean isSuppressDataTransmissionAgreement() {
		return suppressDataTransmissionAgreementEl.isVisible() && suppressDataTransmissionAgreementEl.isAtLeastSelected(1);
	}
}
