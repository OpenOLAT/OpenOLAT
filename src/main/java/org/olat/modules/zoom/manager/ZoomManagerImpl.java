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
package org.olat.modules.zoom.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.User;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
@Service
public class ZoomManagerImpl implements ZoomManager, DeletableGroupData, RepositoryEntryDataDeletable {

    private static final Logger log = Tracing.createLoggerFor(ZoomManagerImpl.class);

    private static final String TARGET_LINK_URL = "https://applications.zoom.us/lti/advantage";
    private static final String LOGIN_INITIATION_URL_PREFIX = "https://applications.zoom.us/lti/advantage/login/";
    private static final String REDIRECTION_URLS = "https://applications.zoom.us/lti/rich/oauth/complete"
            + "\r\n"
            + "https://applications.zoom.us/lti/advantage/oauth/complete";
    private static final String PUBLIC_JWK_URL = "https://applications.zoom.us/lti/advantage/jwks";
    private static final String ZOOM_PROFILE_SUFFIX = " (Zoom Profile)";

    @Autowired
    private ZoomProfileDAO zoomProfileDao;

    @Autowired
    private ZoomConfigDAO zoomConfigDao;

    @Autowired
    private LTI13Service lti13Service;

    @Autowired
    private LTI13Module lti13Module;

    @Autowired
    private LTI13ToolDAO lti13ToolDAO;

    @Autowired
    private LTI13ToolDeploymentDAO lti13ToolDeploymentDAO;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private DB dbInstance;

    @Autowired
    private LTI13IDGenerator idGenerator;

    @Override
    public ZoomProfile createProfile(String name, String ltiKey, String clientId, String token) {
        LTI13Tool lti13Tool = createLtiTool(name, ltiKey, clientId);
        return zoomProfileDao.createProfile(name, ltiKey, lti13Tool, token);
    }

    LTI13Tool createLtiTool(String name, String ltiKey, String clientId) {
        String toolName = name + ZOOM_PROFILE_SUFFIX;
        String initiateLoginUrl = LOGIN_INITIATION_URL_PREFIX + ltiKey;
        LTI13Tool ltiTool = lti13Service.createExternalTool(toolName, TARGET_LINK_URL, clientId, initiateLoginUrl, REDIRECTION_URLS, LTI13ToolType.ZOOM);
        ltiTool.setPublicKeyTypeEnum(LTI13Tool.PublicKeyType.URL);
        ltiTool.setPublicKey(null);
        ltiTool.setPublicKeyUrl(PUBLIC_JWK_URL);
        return lti13Service.updateTool(ltiTool);
    }

    @Override
    public ZoomProfile copyProfile(ZoomProfile zoomProfile, String copySuffix) {
        String copiedName = copySuffix != null ? zoomProfile.getName() + " " + copySuffix : zoomProfile.getName();
        String copiedToolName = copiedName + ZOOM_PROFILE_SUFFIX;
        LTI13Tool originalTool = zoomProfile.getLtiTool();
        LTI13Tool copiedTool = lti13Service.createExternalTool(copiedToolName, originalTool.getToolUrl(),
                idGenerator.newId(), originalTool.getInitiateLoginUrl(), originalTool.getRedirectUrl(),
                originalTool.getToolTypeEnum());
        copiedTool.setPublicKeyTypeEnum(originalTool.getPublicKeyTypeEnum());
        copiedTool.setPublicKey(originalTool.getPublicKey());
        copiedTool.setPublicKeyUrl(originalTool.getPublicKeyUrl());
        LTI13Tool updatedCopiedTool = lti13Service.updateTool(copiedTool);

        ZoomProfile copiedZoomProfile = zoomProfileDao.createProfile(copiedName, zoomProfile.getLtiKey(),
                updatedCopiedTool, zoomProfile.getToken());
        copiedZoomProfile.setMailDomains(zoomProfile.getMailDomains());
        copiedZoomProfile.setStudentsCanHost(zoomProfile.isStudentsCanHost());
        copiedZoomProfile.setToken(idGenerator.newId().substring(0, 8));
        return zoomProfileDao.updateProfile(copiedZoomProfile);
    }

    @Override
    public ZoomProfile getProfile(String key) {
        return zoomProfileDao.getProfile(Long.parseLong(key));
    }

    @Override
    public List<ZoomProfile> getProfiles() {
        return zoomProfileDao.getProfiles();
    }

    @Override
	public List<ZoomProfileDAO.ZoomProfileWithConfigCount> getProfilesWithConfigCount() {
        return zoomProfileDao.getProfilesWithConfigCounts();
    }

    @Override
    public KeysAndValues getProfilesAsKeysAndValues() {
        KeysAndValues keysAndValues = new KeysAndValues();
        List<ZoomProfile> profiles = getProfiles();

        keysAndValues.keys = profiles
                .stream()
                .map(p -> p.getKey().toString())
                .toArray(String[]::new);
        keysAndValues.values = profiles
                .stream()
                .map(ZoomProfile::getName)
                .toArray(String[]::new);

        return keysAndValues;
    }

    @Override
    public ZoomProfile updateProfile(ZoomProfile zoomProfile) {
        String toolName = zoomProfile.getName() + ZOOM_PROFILE_SUFFIX;
        String initiateLoginUrl = LOGIN_INITIATION_URL_PREFIX + zoomProfile.getLtiKey();
        LTI13Tool ltiTool = zoomProfile.getLtiTool();
        ltiTool.setToolName(toolName);
        ltiTool.setInitiateLoginUrl(initiateLoginUrl);
        lti13ToolDAO.updateTool(ltiTool);

        return zoomProfileDao.updateProfile(zoomProfile);
    }

    @Override
    public void deleteProfile(ZoomProfile zoomProfile) {
        LTI13Tool ltiTool = lti13ToolDAO.loadToolByKey(zoomProfile.getLtiTool().getKey());
        zoomProfileDao.deleteProfile(zoomProfile);
        lti13ToolDAO.deleteTool(ltiTool);
    }

    @Override
    public boolean isInUse(ZoomProfile zoomProfile) {
        return zoomProfileDao.isInUse(zoomProfile);
    }

    @Override
    public void initializeConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
                                 ApplicationType applicationType, String clientId, User user) throws OLATRuntimeException {
        if (zoomConfigDao.configExists(entry, subIdent, businessGroup)) {
            return;
        }

        ZoomProfile profile = zoomProfileDao.getProfile(clientId);
        if (profile == null) {
            profile = getProfileForUser(user);
        }

        LTI13ToolDeployment toolDeployment = createLtiToolDeployment(profile.getLtiTool(), entry, subIdent, businessGroup);
        String id = businessGroup != null ? businessGroup.getKey().toString() : entry.getKey().toString() + "-" + subIdent;
        zoomConfigDao.createConfig(profile, toolDeployment, applicationType.name() + "-" + id);
   }

    private ZoomProfile getProfileForUser(User user) {
        List<ZoomProfile> profiles = getProfiles();
        if (profiles.isEmpty()) {
            throw new OLATRuntimeException("No Zoom profiles available when trying to set default Zoom configuration");
        }

        if (user == null) {
            return profiles.get(0);
        }

        String mailDomainForUser = getMailDomainForUser(user);
        for (ZoomProfile zoomProfile : profiles) {
            if (StringHelper.containsNonWhitespace(zoomProfile.getMailDomains())) {
                String[] mailDomains = zoomProfile.getMailDomains().split("\r?\n");
                for (String mailDomain : mailDomains) {
                    if (StringHelper.containsNonWhitespace(mailDomain) && mailDomain.equals(mailDomainForUser)) {
                        return zoomProfile;
                    }
                }
            }
        }

        return profiles.get(0);
    }

    @Override
	public boolean configExists(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        return zoomConfigDao.configExists(entry, subIdent, businessGroup);
    }

    @Override
    public ZoomConfig getConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        return zoomConfigDao.getConfig(entry, subIdent, businessGroup);
    }

    @Override
    public Optional<ZoomConfig> getConfig(String contextId) {
        return zoomConfigDao.getConfig(contextId);
    }

    @Override
    public void recreateConfig(ZoomConfig config, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, ZoomProfile profile) {
        LTI13ToolDeployment existingToolDeployment = config.getLtiToolDeployment();
        zoomConfigDao.deleteConfig(config);
        LTI13ToolDeployment toolDeployment = createLtiToolDeployment(profile.getLtiTool(), entry, subIdent, businessGroup);
        zoomConfigDao.createConfig(profile, toolDeployment, config.getDescription());
        lti13ToolDeploymentDAO.deleteToolDeployment(existingToolDeployment);
    }

    @Override
	public LTI13ToolDeployment createLtiToolDeployment(LTI13Tool tool, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        LTI13ToolDeployment toolDeployment = lti13Service.createToolDeployment(TARGET_LINK_URL, tool, entry, subIdent, businessGroup);
        toolDeployment.setSendUserAttributesList(List.of("email"));
        toolDeployment.setSendCustomAttributes("");
        toolDeployment.setParticipantRoles("Learner");
        toolDeployment.setCoachRoles("TeachingAssistant,Instructor,Mentor");
        toolDeployment.setAuthorRoles("ContentDeveloper,Administrator,TeachingAssistant,Instructor,Mentor");
        toolDeployment.setSkipLaunchPage(true);
        toolDeployment.setDisplay("iframe");
        toolDeployment.setDisplayWidth("auto");
        toolDeployment.setDisplayHeight("auto");
        return lti13Service.updateToolDeployment(toolDeployment);
    }

    @Override
    public void deleteConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        ZoomConfig zoomConfig = getConfig(entry, subIdent, businessGroup);
        if(zoomConfig != null) {
        	LTI13ToolDeployment toolDeployment = zoomConfig.getLtiToolDeployment();
	        zoomConfigDao.deleteConfig(zoomConfig);
	        lti13ToolDeploymentDAO.deleteToolDeployment(toolDeployment);
        }
    }

    @Override
    public boolean deleteGroupDataFor(BusinessGroup group) {
        if (configExists(null, null, group)) {
            deleteConfig(null, null, group);
        }
        return true;
    }

    @Override
    public boolean deleteRepositoryEntryData(RepositoryEntry re) {
        List<ZoomConfig> configs = zoomConfigDao.getConfigs(re.getKey());
        for (ZoomConfig config : configs) {
            try {
                deleteConfig(config.getLtiToolDeployment().getEntry(), config.getLtiToolDeployment().getSubIdent(), null);
            } catch (RuntimeException e) {
                log.warn("Failed to delete Zoom config for repository entry");
            }
        }
        return true;
    }

    @Override
    public ZoomConnectionResponse checkConnection(String ltiKey, String clientId, String ltiMessageHint) {
        LTI13Tool temporaryTool = null;
        LTI13ToolDeployment temporaryDeployment = null;
        try {
            temporaryTool = createLtiTool("Zoom connection check", ltiKey, idGenerator.newId());
            temporaryDeployment = createLtiToolDeployment(temporaryTool, null, null, null);
            dbInstance.commit();

            String loginHint = getLoginHint(temporaryDeployment, true, false, false);

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
            parameters.add(new BasicNameValuePair("lti_deployment_id", temporaryDeployment.getDeploymentId()));
            parameters.add(new BasicNameValuePair("lti_message_hint", ltiMessageHint));
            parameters.add(new BasicNameValuePair("target_link_uri", TARGET_LINK_URL));
            post.setEntity(new UrlEncodedFormEntity(parameters));
            for (NameValuePair parameter : parameters) {
                log.debug("Parameter: {} = {}", parameter.getName(), parameter.getValue());
            }

            ZoomConnectionResponse response = execute(post);
            log.debug("Zoom response: {}", response.getStatus());
            log.debug("Zoom page: {}", response.getContent());
            return response;
        } catch (Exception e) {
            log.error(e);
            return new ZoomConnectionResponse(500, "");
        } finally {
            if (temporaryDeployment != null) {
                lti13ToolDeploymentDAO.deleteToolDeployment(temporaryDeployment);
            }
            if (temporaryTool != null) {
                lti13ToolDAO.deleteTool(temporaryTool);
            }
            dbInstance.commit();
        }
    }

    private String getLoginHint(LTI13ToolDeployment deployment, boolean admin, boolean coach, boolean participant) {
        LTI13Key platformKey = lti13Service.getLastPlatformKey();

        log.debug("Login hint: admin={}, coach={}, participant={}, deployment={}/{}, keyId={}",
                admin, coach, participant, deployment.getKey(), deployment.getDeploymentId(), platformKey.getKeyId());

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam(LTI13Constants.Keys.TYPE, LTI13Constants.Keys.JWT)
                .setHeaderParam(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
                .setHeaderParam(LTI13Constants.Keys.KEY_IDENTIFIER, platformKey.getKeyId())
                .claim("deploymentKey", deployment.getKey())
                .claim("deploymentId", deployment.getDeploymentId())
                .claim("courseadmin", Boolean.toString(admin))
                .claim("coach", Boolean.toString(coach))
                .claim("participant", Boolean.toString(participant));

        return builder.signWith(platformKey.getPrivateKey()).compact();
    }

    private ZoomConnectionResponse execute(HttpUriRequest request) {
        try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String content;
            if (entity == null) {
                content = "";
            } else {
                content = EntityUtils.toString(entity);
            }
            return new ZoomConnectionResponse(status, content);
        } catch(IOException e) {
            log.error(e);
            return new ZoomConnectionResponse(500, null);
        }
    }

    @Override
    public String getMailDomainForUser(User user) {
        String email = user.getEmail();
        int index = email.lastIndexOf("@");
        if (index != -1) {
            String domain = email.substring(index + 1);
            if (StringHelper.containsNonWhitespace(domain)) {
                return domain;
            }
        }
        return "";
    }

    @Override
    public List<ZoomProfileDAO.ZoomProfileApplication> getProfileApplications(Long profileKey) {
        return zoomProfileDao.getApplications(profileKey);
    }
}
