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
package org.olat.modules.selectus.model.position;

import java.util.HashMap;
import java.util.Map;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabsConfiguration {
	
	private Map<Tab,TabConfiguration> configurations = new HashMap<>();
	
	public TabConfiguration getConfiguration(Tab tab) {
		return configurations.get(tab);
	}
	
	public void setConfiguration(Tab tab, TabConfiguration configuration) {
		configurations.put(tab, configuration);
	}

	public enum Tab {
		instructions(null, false),
		dataProtection(null, false),
		personalData(RecruitingModule.APP_SECTION_PERSON, false),
		academicalBackground(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, false),
		documents(null, false),
		project(RecruitingModule.APP_SECTION_PROJECT, false),
		referees(null, false),
		custom1("custom1", true),
		custom2("custom2", true),
		custom3("custom3", true),
		custom4("custom4", true),
		reviewAndSubmit(null, false),
		confirmation(null, false);
		
		private final String attributesTabKey;
		private final boolean customStep;
		
		private Tab(String attributesTabKey, boolean customStep) {
			this.customStep = customStep;
			this.attributesTabKey = attributesTabKey;
		}
		
		public boolean customStep() {
			return customStep;
		}
		
		public PositionApplicationAttributeTabEnum attributesTab() {
			switch(this) {
				case personalData: return PositionApplicationAttributeTabEnum.personalData;
				case academicalBackground: return PositionApplicationAttributeTabEnum.academicalBackground;
				case project: return PositionApplicationAttributeTabEnum.project;
				case custom1: return PositionApplicationAttributeTabEnum.custom1;
				case custom2: return PositionApplicationAttributeTabEnum.custom2;
				case custom3: return PositionApplicationAttributeTabEnum.custom3;
				case custom4: return PositionApplicationAttributeTabEnum.custom4;
				default: return null;
			}
		}
		
		public String attributesTabKey() {
			return attributesTabKey;
		}

		public Tab secureValueOf(String val) {
			for(Tab t:Tab.values()) {
				if(t.name().equalsIgnoreCase(val)) {
					return t;
				}
			}
			return null;
		}
	}
}
