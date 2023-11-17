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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13ContextDAO;
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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class JupyterManagerImpl implements JupyterManager, RepositoryEntryDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(JupyterManagerImpl.class);

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
	private LTI13ContextDAO lti13ContextDAO;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDAO;
	@Autowired
	private LTI13IDGenerator idGenerator;
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private HttpClientService httpClientService;

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

		LTI13Context ltiContext = createLtiContext(jupyterHub.getLtiTool(), repositoryEntry, subIdent,
				jupyterHub, image);
		String description = repositoryEntry.getKey().toString() + "-" + subIdent;
		jupyterDeploymentDAO.createJupyterHubDeployment(jupyterHub, ltiContext, description,
				StringHelper.blankIfNull(image), suppressDataTransmissionAgreement);
	}

	@Override
	public LTI13Context createLtiContext(LTI13Tool ltiTool, RepositoryEntry repositoryEntry,
													   String subIdent, JupyterHub jupyterHub, String image) {
		LTI13ToolDeployment toolDeployment = lti13Service.createToolDeployment(ltiTool.getToolUrl(), LTI13ToolDeploymentType.SINGLE_CONTEXT, null, ltiTool);
		LTI13Context context = lti13Service.createContext(ltiTool.getToolUrl(), toolDeployment, repositoryEntry, subIdent, null);		
		context.setDisplay("window");
		context.setSkipLaunchPage(false);
		context.setDisplayWidth("auto");
		context.setDisplayHeight("auto");
		context.setParticipantRoles("Learner");
		context.setCoachRoles("Instructor,Learner");
		context.setAuthorRoles("Instructor,Learner");
		context.setSendUserAttributesList(List.of("email", "firstName", "lastName"));
		context.setNameAndRolesProvisioningServicesEnabled(true);
		setCustomAttributes(context, jupyterHub, image);
		return lti13Service.updateContext(context);
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

	private void setCustomAttributes(LTI13Context context, JupyterHub jupyterHub, String image) {
		CustomAttributesBuilder builder = new CustomAttributesBuilder();
		builder
				.add("singleuser_image", StringHelper.blankIfNull(image))
				.add("memory_guarantee", "128M")
				.add("memory_limit", jupyterHub != null ? JupyterHub.standardizeRam(jupyterHub.getRam()) : "1G")
				.add("cpu_guarantee", "0.5")
				.add("cpu_limit", jupyterHub != null ? jupyterHub.getCpu().stripTrailingZeros().toPlainString() : "1")
				.add("username", LTIManager.USER_PROPS_PREFIX + LTIManager.USER_NAME_PROP)
				.add("course_id", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_COURSE_ID)
				.add("course_url", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_COURSE_URL)
				.add("node_id", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_NODE_ID)
				.add("node_url", LTIManager.COURSE_INFO_PREFIX + LTIManager.COURSE_INFO_NODE_URL);
		context.setSendCustomAttributes(builder.build());
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
		LTI13Context existingContext = jupyterDeployment.getLtiContext();
				
		jupyterDeploymentDAO.deleteJupyterDeployment(jupyterDeployment);
		LTI13Context ltiContext = createLtiContext(jupyterHub.getLtiTool(), repositoryEntry, subIdent,
				jupyterHub, jupyterDeployment.getImage());
		jupyterDeploymentDAO.createJupyterHubDeployment(jupyterHub, ltiContext, jupyterDeployment.getDescription(),
				jupyterDeployment.getImage(), jupyterDeployment.getSuppressDataTransmissionAgreement());
		lti13ContextDAO.deleteContext(existingContext);
		lti13ToolDeploymentDAO.deleteToolDeployment(existingContext.getDeployment());
	}

	@Override
	public void deleteJupyterHub(RepositoryEntry repositoryEntry, String subIdent) {
		JupyterDeployment jupyterDeployment = getJupyterDeployment(repositoryEntry, subIdent);
		if (jupyterDeployment != null) {
			LTI13Context context = jupyterDeployment.getLtiContext();
			jupyterDeploymentDAO.deleteJupyterDeployment(jupyterDeployment);
			lti13ContextDAO.deleteContext(context);
			lti13ToolDeploymentDAO.deleteToolDeployment(context.getDeployment());
		}
	}

	@Override
	public void updateJupyterDeployment(JupyterDeployment jupyterDeployment) {
		LTI13Context ltiContext = jupyterDeployment.getLtiContext();
		jupyterDeploymentDAO.updateJupyterDeployment(jupyterDeployment);
		setCustomAttributes(ltiContext, jupyterDeployment.getJupyterHub(), jupyterDeployment.getImage());
		lti13ContextDAO.updateContext(ltiContext);
	}

	@Override
	public CheckConnectionResponse checkConnection(String jupyterHubUrl, String clientId, String ltiMessageHint) {
		LTI13Tool temporaryTool = null;
		LTI13Context temporaryContext = null;
		try {
			temporaryTool = createLtiTool("JupyterHub Connection Check", jupyterHubUrl, idGenerator.newId());
			temporaryContext = createLtiContext(temporaryTool, null, null, null, null);
			dbInstance.commit();

			String loginHint = getLoginHint(temporaryContext);

			log.debug("Try connecting to '{}'", temporaryTool.getInitiateLoginUrl());

			HttpPost post = new HttpPost(temporaryTool.getInitiateLoginUrl());
			post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
			post.addHeader("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,it;q=0.7");
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.addHeader("Referer", lti13Module.getPlatformIss());
			for (Header header : post.getAllHeaders()) {
				log.debug("Header: {} = {}", header.getName(), header.getValue());
			}

			List<NameValuePair> parameters = new ArrayList<>(10);
			parameters.add(new BasicNameValuePair("client_id", clientId));
			parameters.add(new BasicNameValuePair("iss", lti13Module.getPlatformIss()));
			parameters.add(new BasicNameValuePair("login_hint", loginHint));
			parameters.add(new BasicNameValuePair("lti_deployment_id", temporaryContext.getDeployment().getDeploymentId()));
			parameters.add(new BasicNameValuePair("lti_message_hint", ltiMessageHint));
			parameters.add(new BasicNameValuePair("target_link_uri", jupyterHubUrl));
			post.setEntity(new UrlEncodedFormEntity(parameters));
			for (NameValuePair parameter : parameters) {
				log.debug("Parameter: {} = {}", parameter.getName(), parameter.getValue());
			}

			return execute(post);
		} catch (Exception e) {
			log.error(e);
			return new CheckConnectionResponse(false, e.getMessage());
		} finally {
			if (temporaryContext != null) {
				lti13ContextDAO.deleteContext(temporaryContext);
				lti13ToolDeploymentDAO.deleteToolDeployment(temporaryContext.getDeployment());
			}
			if (temporaryTool != null) {
				lti13ToolDAO.deleteTool(temporaryTool);
			}
			dbInstance.commit();
		}
	}

	private CheckConnectionResponse execute(HttpPost request) {
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
			 CloseableHttpResponse response = httpClient.execute(request)) {
			int status = response.getStatusLine().getStatusCode();
			if (status != 302) {
				return new CheckConnectionResponse(false, "Expected a redirect status code 302 from " + request.getURI().toString());
			}
			Header[] locations = response.getHeaders("location");
			if (locations.length < 1 || !StringHelper.containsNonWhitespace(locations[0].getValue())) {
				return new CheckConnectionResponse(false, "Redirect without a location header");
			}
			String location = locations[0].getValue();
			if (!location.startsWith(lti13Module.getPlatformIss())) {
				URI locationUri = new URI(location);
				String locationSchemeAndHost = locationUri.getScheme() + "://" + locationUri.getHost();
				return new CheckConnectionResponse(false, "Expecting a redirect to \"" +
						lti13Module.getPlatformIss() + "\", but got a redirect to \"" + locationSchemeAndHost + "\" instead.");
			}
			return new CheckConnectionResponse(true, null);
		} catch (IOException | URISyntaxException e) {
			log.error(e);
			return new CheckConnectionResponse(false, e.getMessage());
		}
	}

	private String getLoginHint(LTI13Context ltiContext) {
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		LTI13ToolDeployment deployment = ltiContext.getDeployment();

		log.debug("Login hint: admin={}, coach={}, participant={}, deployment={}/{}, keyId={}",
				true, false, false, deployment.getKey(), deployment.getDeploymentId(), platformKey.getKeyId());

		JwtBuilder builder = Jwts.builder()
				.setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
				.setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
				.setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
				.claim("deploymentKey", deployment.getKey())
				.claim("deploymentId", deployment.getDeploymentId())
				.claim("courseadmin", Boolean.toString(true))
				.claim("coach", Boolean.toString(false))
				.claim("participant", Boolean.toString(false));

		return builder.signWith(platformKey.getPrivateKey()).compact();
	}
}
