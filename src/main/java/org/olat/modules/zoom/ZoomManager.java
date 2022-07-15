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
package org.olat.modules.zoom;

import org.olat.core.id.User;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.zoom.manager.ZoomProfileDAO;
import org.olat.repository.RepositoryEntry;

import java.util.List;
import java.util.Optional;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public interface ZoomManager {

    class KeysAndValues {
        public String[] keys;
        public String[] values;

        public boolean isEmpty() {
            if (keys == null || values == null) {
                return true;
            }
            return keys.length == 0 && values.length == 0;
        }
    }

    enum ApplicationType {
        courseElement,
        courseTool,
        groupTool
    }

    ZoomProfile createProfile(String name, String ltiKey, String clientId, String token);

    ZoomProfile copyProfile(ZoomProfile zoomProfile);

    ZoomProfile getProfile(String key);

    List<ZoomProfile> getProfiles();

    List<ZoomProfileDAO.ZoomProfileWithConfigCount> getProfilesWithConfigCount();

    KeysAndValues getProfilesAsKeysAndValues();

    void initializeConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, ApplicationType applicationType, User user);

    ZoomProfile updateProfile(ZoomProfile zoomProfile);

    void deleteProfile(ZoomProfile zoomProfile);

    boolean isInUse(ZoomProfile zoomProfile);

    boolean configExists(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);

    ZoomConfig getConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);

    Optional<ZoomConfig> getConfig(String contextId);

    void recreateConfig(ZoomConfig config, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, ZoomProfile profile);

    LTI13ToolDeployment createLtiToolDeployment(LTI13Tool tool, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);

    void deleteConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);

    ZoomConnectionResponse checkConnection(String ltiKey, String clientId, String ltiMessageHint);

    String getMailDomainForUser(User user);

    class ZoomConnectionResponse {

        private final int status;
        private final String content;

        public ZoomConnectionResponse(int status, String content) {
            this.status = status;
            this.content = content;
        }

        public boolean isOk() {
            return status == 200;
        }

        public int getStatus() {
            return status;
        }

        public String getContent() {
            return content;
        }
    }
}
