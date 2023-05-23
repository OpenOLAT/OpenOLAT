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
package org.olat.modules.jupyterhub.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class JupyterManagerImpl implements JupyterManager, RepositoryEntryDataDeletable {

	private static final String JUPYTER_PROFILE_SUFFIX = " (Jupyter Hub Profile)";
	private static final String JUPYTER_HUB_INITIATE_LOGIN_URL_SUFFIX = "/hub/lti13/oauth_login";
	private static final String JUPYTER_REDIRECT_URL_SUFFIX = "/hub/lti13/oauth_callback";
	private static final String JUPYTER_PUBLIC_KEY_URL_SUFFIX = "/hub/lti13/jwks";

	@Autowired
	private JupyterHubDAO jupyterHubDAO;
	@Autowired
	private JupyterDeploymentDAO jupyterDeploymentDAO;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private LTI13ToolDAO lti13ToolDAO;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDAO;
	@Autowired
	private LTI13IDGenerator idGenerator;

	@Override
	public JupyterHub getJupyterHub(String selectedKey) {
		return jupyterHubDAO.getJupyterHub(Long.parseLong(selectedKey));
	}

	@Override
	public List<JupyterHub> getJupyterHubs() {
		return jupyterHubDAO.getJupyterHubs();
	}

	@Override
	public List<JupyterHubDAO.JupyterHubWithApplicationCount> getJupyterHubsWithApplicationCounts() {
		return jupyterHubDAO.getJupyterHubsWithApplicationCounts();
	}

	@Override
	public List<JupyterHubDAO.JupyterHubApplication> getJupyterHubApplications(Long key) {
		return jupyterHubDAO.getApplications(key);
	}

	@Override
	public SelectionValues getJupyterHubsKV() {
		SelectionValues jupyterHubsKV = new SelectionValues();
		for (JupyterHub jupyterHub : getJupyterHubs()) {
			jupyterHubsKV.add(SelectionValues.entry(jupyterHub.getKey().toString(), jupyterHub.getName()));
		}
		return jupyterHubsKV;
	}

	@Override
	public JupyterHub createJupyterHub(String name, String jupyterHubUrl, String clientId, String ram, BigDecimal cpu,
									   JupyterHub.AgreementSetting agreementSetting) {
		LTI13Tool ltiTool = createLtiTool(name, jupyterHubUrl, clientId);
		return jupyterHubDAO.createJupyterHub(name, ram, cpu, ltiTool, agreementSetting);
	}

	LTI13Tool createLtiTool(String name, String jupyterHubUrl, String clientId) {
		String toolName = name + JUPYTER_PROFILE_SUFFIX;
		String initiateLoginUrl = jupyterHubUrl + JUPYTER_HUB_INITIATE_LOGIN_URL_SUFFIX;
		String redirectUrl = jupyterHubUrl + JUPYTER_REDIRECT_URL_SUFFIX;
		LTI13Tool ltiTool = lti13Service.createExternalTool(toolName, jupyterHubUrl, clientId, initiateLoginUrl,
				redirectUrl, LTI13ToolType.JUPYTER_HUB);
		ltiTool.setPublicKeyTypeEnum(LTI13Tool.PublicKeyType.URL);
		ltiTool.setPublicKey(null);
		ltiTool.setPublicKeyUrl(jupyterHubUrl + JUPYTER_PUBLIC_KEY_URL_SUFFIX);
		return ltiTool;
	}

	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		return true;
	}

	@Override
	public boolean isInUse(JupyterHub jupyterHub) {
		return jupyterHubDAO.isInUse(jupyterHub);
	}

	@Override
	public JupyterHub updateJupyterHub(JupyterHub jupyterHub) {
		String toolName = jupyterHub.getName() + JUPYTER_PROFILE_SUFFIX;
		LTI13Tool ltiTool = jupyterHub.getLtiTool();
		String initiateLoginUrl = ltiTool.getToolUrl() + JUPYTER_HUB_INITIATE_LOGIN_URL_SUFFIX;
		String redirectUrl = ltiTool.getToolUrl() + JUPYTER_REDIRECT_URL_SUFFIX;
		String publicKeyUrl = ltiTool.getToolUrl() + JUPYTER_PUBLIC_KEY_URL_SUFFIX;
		ltiTool.setToolName(toolName);
		ltiTool.setInitiateLoginUrl(initiateLoginUrl);
		ltiTool.setRedirectUrl(redirectUrl);
		ltiTool.setPublicKeyUrl(publicKeyUrl);
		lti13ToolDAO.updateTool(ltiTool);

		return jupyterHubDAO.updateJupyterHub(jupyterHub);
	}

	@Override
	public void deleteJupyterHub(JupyterHub jupyterHub) {
		LTI13Tool ltiTool = lti13ToolDAO.loadToolByKey(jupyterHub.getLtiTool().getKey());
		jupyterHubDAO.deleteJupyterHub(jupyterHub);
		lti13ToolDAO.deleteTool(ltiTool);
	}

	@Override
	public void copyProfile(JupyterHub jupyterHub, String copySuffix) {
		String copiedName = jupyterHub.getName() + " " + copySuffix;
		String copiedToolName = copiedName + JUPYTER_PROFILE_SUFFIX;
		LTI13Tool originalTool = jupyterHub.getLtiTool();
		LTI13Tool copiedTool = lti13Service.createExternalTool(copiedToolName, originalTool.getToolUrl(),
				idGenerator.newId(), originalTool.getInitiateLoginUrl(), originalTool.getRedirectUrl(),
				originalTool.getToolTypeEnum());
		copiedTool.setPublicKeyTypeEnum(originalTool.getPublicKeyTypeEnum());
		copiedTool.setPublicKey(originalTool.getPublicKey());
		copiedTool.setPublicKeyUrl(originalTool.getPublicKeyUrl());
		LTI13Tool updatedCopiedTool = lti13Service.updateTool(copiedTool);

		jupyterHubDAO.createJupyterHub(copiedName, jupyterHub.getRam(), jupyterHub.getCpu(), updatedCopiedTool,
				jupyterHub.getAgreementSetting());
	}

	@Override
	public JupyterDeployment getJupyterDeployment(RepositoryEntry repositoryEntry, String subIdent) {
		return jupyterDeploymentDAO.getJupyterDeployment(repositoryEntry, subIdent);
	}

	@Override
	public void initializeJupyterHubDeployment(RepositoryEntry repositoryEntry, String subIdent, String clientId,
											   String image, Boolean suppressDataTransmissionAgreement) {
		if (jupyterDeploymentDAO.exists(repositoryEntry, subIdent)) {
			return;
		}

		JupyterHub jupyterHub = jupyterHubDAO.getJupyterHub(clientId);
		if (jupyterHub == null) {
			jupyterHub = getDefaultJupyterHub();
		}

		LTI13ToolDeployment toolDeployment = createLtiToolDeployment(jupyterHub.getLtiTool(), repositoryEntry, subIdent,
				jupyterHub, image);
		String description = repositoryEntry.getKey().toString() + "-" + subIdent;
		jupyterDeploymentDAO.createJupyterHubDeployment(jupyterHub, toolDeployment, description,
				StringHelper.blankIfNull(image), suppressDataTransmissionAgreement);
	}

	@Override
	public LTI13ToolDeployment createLtiToolDeployment(LTI13Tool ltiTool, RepositoryEntry repositoryEntry,
													   String subIdent, JupyterHub jupyterHub, String image) {
		LTI13ToolDeployment toolDeployment = lti13Service.createToolDeployment(ltiTool.getToolUrl(), ltiTool, repositoryEntry, subIdent, null);
		toolDeployment.setDisplay("window");
		toolDeployment.setSkipLaunchPage(false);
		toolDeployment.setDisplayWidth("auto");
		toolDeployment.setDisplayHeight("auto");
		toolDeployment.setParticipantRoles("Learner");
		toolDeployment.setCoachRoles("TeachingAssistant,Instructor,Mentor");
		toolDeployment.setAuthorRoles("ContentDeveloper,Administrator,TeachingAssistant,Instructor,Mentor");
		toolDeployment.setSendUserAttributesList(List.of("email", "firstName", "lastName"));
		setCustomAttributes(toolDeployment, jupyterHub, image);
		return lti13Service.updateToolDeployment(toolDeployment);
	}

	private static class CustomAttributesBuilder {
		private final List<String> customAttributes = new ArrayList<>();

		CustomAttributesBuilder add(String key, String value) {
			customAttributes.add(key + "=" + value);
			return this;
		}

		String build() {
			return String.join(";", customAttributes);
		}
	}

	private void setCustomAttributes(LTI13ToolDeployment toolDeployment, JupyterHub jupyterHub, String image) {
		CustomAttributesBuilder builder = new CustomAttributesBuilder();
		builder
				.add("singleuser_image", StringHelper.blankIfNull(image))
				.add("memory_guarantee", "128M")
				.add("memory_limit", jupyterHub != null ? JupyterHub.standardizeRam(jupyterHub.getRam()) : "1G")
				.add("cpu_guarantee", "1")
				.add("cpu_limit", jupyterHub != null ? jupyterHub.getCpu().stripTrailingZeros().toPlainString() : "1")
				.add("username", LTIManager.USER_PROPS_PREFIX + LTIManager.USER_NAME_PROP)
				.add("course_id", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_COURSE_ID)
				.add("course_url", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_COURSE_URL)
				.add("node_id", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_NODE_ID)
				.add("node_url", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_NODE_URL);
		toolDeployment.setSendCustomAttributes(builder.build());
	}

	private JupyterHub getDefaultJupyterHub() {
		List<JupyterHub> jupyterHubs = getJupyterHubs();
		if (jupyterHubs.isEmpty()) {
			throw new OLATRuntimeException("No Jupyter Hub available when trying to set default Jupyter Hub configuration");
		}
		return jupyterHubs.get(0);
	}

	@Override
	public void recreateJupyterHubDeployment(JupyterDeployment jupyterDeployment, RepositoryEntry repositoryEntry,
											 String subIdent, JupyterHub jupyterHub) {
		LTI13ToolDeployment existingToolDeployment = jupyterDeployment.getLtiToolDeployment();
		jupyterDeploymentDAO.deleteJupyterDeployment(jupyterDeployment);
		LTI13ToolDeployment toolDeployment = createLtiToolDeployment(jupyterHub.getLtiTool(), repositoryEntry, subIdent,
				jupyterHub, jupyterDeployment.getImage());
		jupyterDeploymentDAO.createJupyterHubDeployment(jupyterHub, toolDeployment, jupyterDeployment.getDescription(),
				jupyterDeployment.getImage(), jupyterDeployment.getSuppressDataTransmissionAgreement());
		lti13ToolDeploymentDAO.deleteToolDeployment(existingToolDeployment);
	}

	@Override
	public void deleteJupyterHub(RepositoryEntry repositoryEntry, String subIdent) {
		JupyterDeployment jupyterDeployment = getJupyterDeployment(repositoryEntry, subIdent);
		if (jupyterDeployment != null) {
			LTI13ToolDeployment toolDeployment = jupyterDeployment.getLtiToolDeployment();
			jupyterDeploymentDAO.deleteJupyterDeployment(jupyterDeployment);
			lti13ToolDeploymentDAO.deleteToolDeployment(toolDeployment);
		}
	}

	@Override
	public void updateJupyterDeployment(JupyterDeployment jupyterDeployment) {
		LTI13ToolDeployment toolDeployment = jupyterDeployment.getLtiToolDeployment();
		jupyterDeploymentDAO.updateJupyterDeployment(jupyterDeployment);
		setCustomAttributes(toolDeployment, jupyterDeployment.getJupyterHub(), jupyterDeployment.getImage() );
		lti13ToolDeploymentDAO.updateToolDeployment(toolDeployment);
	}
}
