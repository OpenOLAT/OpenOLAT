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
package org.olat.modules.scorm;

import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 17 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ScormDisplayEnum {
	
	/**
	 * The SCORM module is in the LMS, menu, tools, tabs are visible
	 */
	standard,
	/**
	 * The SCORM module is in full window mode, layout can or must be
	 * adjusted especially the height, automatically or manually.
	 */
	fullWindow,
	/**
	 * The SCORM module is in full window mode, and take the height and width
	 * of the window. SCORM menu for SCOs and navigation buttons are not visible.
	 */
	fullWidthHeight;
	
	public static final ScormDisplayEnum fromConfiguration(ModuleConfiguration config) {
		if(config.getBooleanSafe(ScormEditController.CONFIG_FULLWINDOW, true)) {
			return fullWindow;
		}
		if(config.getBooleanSafe(ScormEditController.CONFIG_FULLWINDOW_WIDTH_HEIGHT, false)) {
			return fullWidthHeight;
		}
		return standard;
	}
}
