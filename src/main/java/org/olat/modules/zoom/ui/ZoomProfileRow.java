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
package org.olat.modules.zoom.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.ZoomProfile.ZoomProfileStatus;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomProfileRow {
    private FormLink toolLink;
    private final ZoomProfile zoomProfile;
    private final String clientId;
    private final Long numberOfApplications;

    public ZoomProfileRow(ZoomProfile zoomProfile, Long numberOfApplications) {
        this.zoomProfile = zoomProfile;
        this.clientId = zoomProfile.getLtiTool().getClientId();
        this.numberOfApplications = numberOfApplications;
    }

    public ZoomProfile getZoomProfile() {
        return zoomProfile;
    }

    public FormLink getToolLink() {
        return toolLink;
    }

    public void setToolLink(FormLink toolLink) {
        this.toolLink = toolLink;
    }

    public String getName() {
        return zoomProfile.getName();
    }

    public ZoomProfileStatus getStatus() {
        return zoomProfile.getStatus();
    }

    public String getLtiKey() {
        return zoomProfile.getLtiKey();
    }

    public String getMailDomains() {
        return zoomProfile.getMailDomains();
    }

    public boolean isStudentsCanHost() {
        return zoomProfile.isStudentsCanHost();
    }

    public String getClientId() {
        return clientId;
    }

    public Long getNumberOfApplications() {
        return numberOfApplications;
    }
}
