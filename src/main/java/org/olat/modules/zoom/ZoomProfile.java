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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.ims.lti13.LTI13Tool;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public interface ZoomProfile extends ModifiedInfo, CreateInfo {
    Long getKey();

    String getName();

    void setName(String name);

    ZoomProfileStatus getStatus();

    void setStatus(ZoomProfileStatus status);

    String getLtiKey();

    void setLtiKey(String ltiKey);

    String getMailDomains();

    void setMailDomains(String mailDomains);

    boolean isStudentsCanHost();

    void setStudentsCanHost(boolean studentsCanHost);

    String getToken();

    void setToken(String token);

    LTI13Tool getLtiTool();

    void setLtiTool(LTI13Tool ltiTool);

    enum ZoomProfileStatus {
        active,
        inactive
    }
}
