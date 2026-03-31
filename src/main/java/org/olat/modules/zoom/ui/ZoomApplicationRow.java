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

import org.olat.modules.zoom.manager.ZoomProfileApplication;

/**
 * Table row for the Zoom applications detail view. Stores the raw application
 * data and a pre-computed business path (built from IDs only, without loading
 * course or group objects). The display name is built lazily by the cell
 * renderer to avoid loading all course structures at once.
 *
 * Initial date: 2026-03-27<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ZoomApplicationRow {

    private final ZoomProfileApplication application;
    private final String businessPath;
    private final String groupDisplayText;

    public ZoomApplicationRow(ZoomProfileApplication application, String businessPath, String groupDisplayText) {
        this.application = application;
        this.businessPath = businessPath;
        this.groupDisplayText = groupDisplayText;
    }

    public ZoomProfileApplication getApplication() {
        return application;
    }

    public String getGroupDisplayText() {
        return groupDisplayText;
    }

    public String getBusinessPath() {
        return businessPath;
    }
}