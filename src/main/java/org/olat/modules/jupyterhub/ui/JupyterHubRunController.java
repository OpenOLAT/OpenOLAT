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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.UserConstants;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.basiclti.LTIDataExchangeDisclaimerController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti13.ui.LTI13DisplayController;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-17<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubRunController extends BasicController {
	private static final String PROP_NAME_JUPYTER_DATA_EXCHANGE_ACCEPTED = "JupyterDataExchangeAccepted";

	private final CourseNode courseNode;
	JupyterDeployment jupyterDeployment;
	private LTI13DisplayController ltiCtrl;
	private LTIDataExchangeDisclaimerController disclaimerCtrl;
	private Link runButton;
	private VelocityContainer mainVC;
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	JupyterManager jupyterManager;

	public JupyterHubRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								   String subIdent, String clientId, String image,
								   Boolean suppressDataTransmissionAgreement, UserCourseEnvironment userCourseEnv,
								   CourseNode courseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;

		jupyterDeployment = jupyterManager.getJupyterDeployment(repositoryEntry, subIdent);
		if (jupyterDeployment == null && clientId != null && image != null) {
			jupyterManager.initializeJupyterHubDeployment(repositoryEntry, subIdent, clientId, image, suppressDataTransmissionAgreement);
			jupyterDeployment = jupyterManager.getJupyterDeployment(repositoryEntry, subIdent);
		}

		if (jupyterDeployment == null) {
			String title = translate("jupyterHub.error.missing.title");
			String text = translate("jupyterHub.error.missing");
			Controller ctrl = MessageUIFactory.createErrorMessage(ureq, wControl, title, text);
			StackedPanel errorPanel = new SimpleStackedPanel("jupyterHubErrorPanel");
			errorPanel.setContent(ctrl.getInitialComponent());
			putInitialPanel(errorPanel);
		} else if (jupyterDeployment.getJupyterHub().getStatus() == JupyterHub.JupyterHubStatus.inactive) {
			String title = translate("jupyterHub.info.inactive.title");
			String text = translate("jupyterHub.info.inactive", jupyterDeployment.getJupyterHub().getName());
			Controller ctrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, text);
			StackedPanel errorPanel = new SimpleStackedPanel("jupyterHubInfoPanel");
			errorPanel.setContent(ctrl.getInitialComponent());
			putInitialPanel(errorPanel);
		} else {
			mainVC = createVelocityContainer("run_jupyter_hub");

			initDataExchange(ureq, wControl);
			initLti(ureq, wControl);
			initRunButton();

			if (needToShowDataExchange()) {
				mainVC.put("disclaimer", disclaimerCtrl.getInitialComponent());
			} else {
				mainVC.put("runButton", runButton);
			}

			putInitialPanel(mainVC);
		}
	}

	private void initDataExchange(UserRequest ureq, WindowControl wControl) {
		List<String> userAttributes = jupyterDeployment.getLtiToolDeployment().getSendUserAttributesList();
		boolean sendFirstName = userAttributes.contains(UserConstants.FIRSTNAME);
		boolean sendLastName = userAttributes.contains(UserConstants.LASTNAME);
		boolean sendName = sendFirstName || sendLastName;
		boolean sendMail = userAttributes.contains(UserConstants.EMAIL);
		String customAttributes = jupyterDeployment.getLtiToolDeployment().getSendCustomAttributes();
		disclaimerCtrl = new LTIDataExchangeDisclaimerController(ureq, wControl, sendName, sendMail, customAttributes);
		listenTo(disclaimerCtrl);
	}

	private void initLti(UserRequest ureq, WindowControl wControl) {
		ltiCtrl = new LTI13DisplayController(ureq, wControl, jupyterDeployment.getLtiToolDeployment(),
				userCourseEnv.isAdmin(), userCourseEnv.isCoach(), userCourseEnv.isParticipant());
		listenTo(ltiCtrl);
	}

	private void initRunButton() {
		runButton = LinkFactory.createButton("jupyterHub.runButton", mainVC, this);
		runButton.setCustomEnabledLinkCSS("btn btn-primary");
	}

	private boolean needToShowDataExchange() {
		switch (jupyterDeployment.getJupyterHub().getAgreementSetting()) {
			case suppressAgreement:
				return false;
			case configurableByAuthor:
				if (Boolean.TRUE.equals(jupyterDeployment.getSuppressDataTransmissionAgreement())) {
					return false;
				}
			case requireAgreement:
				break;
		}

		String dataExchangeHash = disclaimerCtrl.getHashData();
		if (dataExchangeHash == null) {
			return false;
		}

		CoursePropertyManager coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		Property property = coursePropertyManager.findCourseNodeProperty(courseNode, getIdentity(), null,
				PROP_NAME_JUPYTER_DATA_EXCHANGE_ACCEPTED);
		if (property == null) {
			return true;
		}

		String storedHash = property.getStringValue();
		if (dataExchangeHash.equals(storedHash)) {
			return false;
		}

		coursePropertyManager.deleteProperty(property);
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (disclaimerCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAcceptDisclaimer();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (runButton == source) {
			doRunInNewWindow();
		}
	}

	private void doRunInNewWindow() {
		mainVC.remove("runButton");
		mainVC.put("lti", ltiCtrl.getInitialComponent());
	}

	private void doAcceptDisclaimer() {
		String hash = disclaimerCtrl.getHashData();
		CoursePropertyManager coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		Property property = coursePropertyManager.createCourseNodePropertyInstance(courseNode, getIdentity(), null,
				PROP_NAME_JUPYTER_DATA_EXCHANGE_ACCEPTED, null, null, hash, null);
		coursePropertyManager.saveProperty(property);

		mainVC.remove("disclaimer");
		mainVC.put("runButton", runButton);
	}
}
