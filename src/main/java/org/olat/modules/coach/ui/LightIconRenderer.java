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
package org.olat.modules.coach.ui;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.coach.ui.LightedValue.Light;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LightIconRenderer extends IconCssCellRenderer {

	@Override
	protected String getIconCssClass(Object val) {
		if(val == null) {
			return null;
		}
		if(val instanceof LightedValue lightedVal) {
			Light light = lightedVal.getLight();
			if(light == null) {
				return null;
			}
			switch(light) {
				case grey: return "o_icon o_black_led";
				case green: return "o_icon o_green_led";
				case yellow: return "o_icon o_yellow_led";
				case red: return "o_icon o_red_led";
				case black: return "o_icon o_black_led";
				default: return null;
			}
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		if(val == null) {
			return null;
		}
		if(val instanceof LightedValue lightedVal) {
			return lightedVal.getValue();
		}
		if(val instanceof Float fVal) {
			return AssessmentHelper.getRoundedScore(fVal);
		}
		return val.toString();
	}

}
