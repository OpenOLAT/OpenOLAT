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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ZoomManagerImpl implements ZoomManager, DeletableGroupData, RepositoryEntryDataDeletable {

    private static final Logger log = Tracing.createLoggerFor(ZoomManagerImpl.class);

    @Autowired
    private ZoomProfileDAO zoomProfileDao;

    @Autowired
    private ZoomConfigDAO zoomConfigDao;

    @Autowired
    private LTI13Service lti13Service;

    @Autowired
    private LTI13ToolDeploymentDAO lti13ToolDeploymentDAO;

    @Override
    public ZoomProfile createProfile(String name, String ltiKey, String clientId, String token) {
        LTI13Tool lti13Tool = createLtiTool(name, ltiKey, clientId);
        return zoomProfileDao.createProfile(name, ltiKey, lti13Tool, token);
    }

    LTI13Tool createLtiTool(String name, String ltiKey, String clientId) {
        String toolName = name + " (Zoom Profile)";
        String toolUrl = "https://applications.zoom.us/lti/advantage";
        String initiateLoginUrl = "https://applications.zoom.us/lti/advantage/login/" + ltiKey;
        String redirectionUrls = "https://applications.zoom.us/lti/rich/oauth/complete"
                + "\r\n"
                + "https://applications.zoom.us/lti/advantage/oauth/complete";
        LTI13Tool ltiTool = lti13Service.createExternalTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectionUrls, LTI13ToolType.ZOOM);
        ltiTool.setPublicKeyTypeEnum(LTI13Tool.PublicKeyType.URL);
        ltiTool.setPublicKey(null);
        ltiTool.setPublicKeyUrl("https://applications.zoom.us/lti/advantage/jwks");
        return lti13Service.updateTool(ltiTool);
    }

    @Override
    public ZoomProfile copyProfile(ZoomProfile zoomProfile) {
        ZoomProfile copiedZoomProfile = zoomProfileDao.createProfile(zoomProfile.getName(), zoomProfile.getLtiKey(), zoomProfile.getLtiTool(),zoomProfile.getToken());
        copiedZoomProfile.setMailDomains(zoomProfile.getMailDomains());
        copiedZoomProfile.setStudentsCanHost(zoomProfile.isStudentsCanHost());
        copiedZoomProfile.setToken(zoomProfile.getToken());
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
        return zoomProfileDao.updateProfile(zoomProfile);
    }

    @Override
    public void deleteProfile(ZoomProfile zoomProfile) {
        zoomProfileDao.deleteProfile(zoomProfile);
    }

    @Override
    public boolean isInUse(ZoomProfile zoomProfile) {
        return zoomProfileDao.isInUse(zoomProfile);
    }

    @Override
    public void initializeConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, ApplicationType applicationType) throws OLATRuntimeException {
        if (zoomConfigDao.configExists(entry, subIdent, businessGroup)) {
            return;
        }

        List<ZoomProfile> profiles = getProfiles();
        if (profiles.isEmpty()) {
            throw new OLATRuntimeException("No Zoom profiles available when trying to set default Zoom configuration");
        }
        ZoomProfile profile = profiles.get(0);

        LTI13ToolDeployment toolDeployment = createLtiToolDeployment(profile.getLtiTool(), entry, subIdent, businessGroup);
        String id = businessGroup != null ? businessGroup.getKey().toString() : entry.getKey().toString() + "-" + subIdent;
        zoomConfigDao.createConfig(profile, toolDeployment, applicationType.name() + "-" + id);
   }

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

    public LTI13ToolDeployment createLtiToolDeployment(LTI13Tool tool, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        String targetUrl = "https://applications.zoom.us/lti/advantage";
        LTI13ToolDeployment toolDeployment = lti13Service.createToolDeployment(targetUrl, tool, entry, subIdent, businessGroup);
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
}
