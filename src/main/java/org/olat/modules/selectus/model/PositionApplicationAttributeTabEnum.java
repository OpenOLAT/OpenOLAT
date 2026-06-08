/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model;

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 3 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PositionApplicationAttributeTabEnum {
	
	personalData,
	academicalBackground,
	project,
	custom1,
	custom2,
	custom3,
	custom4,
	global;
	
	
	public Tab tab() {
		switch(this) {
			case personalData: return Tab.personalData;
			case academicalBackground: return Tab.academicalBackground;
			case project: return Tab.project;
			case custom1: return Tab.custom1;
			case custom2: return Tab.custom2;
			case custom3: return Tab.custom3;
			case custom4: return Tab.custom4;
			default: return null;
		}
	}
}
