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
package org.olat.modules.certificationprogram.ui;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.Duration;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationUIFactory {

	public static final String getConfiguration(Translator translator, CertificationProgram program) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<div class='container container-fluid'>");
		
		Duration validityDuration = program.getValidityTimelapseDuration();
		if(program.isValidityEnabled() && validityDuration != null) {
			String duration = validityDuration.unit().toString(program.getValidityTimelapse(), translator);
			append(sb, translator.translate("notifications.infos.validity"), duration);
		}
		
		if(program.isRecertificationEnabled()) {
			if(program.getRecertificationMode() == RecertificationMode.automatic) {
				append(sb, translator.translate("notifications.infos.recertification"), translator.translate("recertification.mode.automatic"));
			} else if(program.getRecertificationMode() == RecertificationMode.manual) {
				append(sb, translator.translate("notifications.infos.recertification"), translator.translate("recertification.mode.manual"));
			}
			
			if(program.isRecertificationWindowEnabled()) {
				String timeframe = program.getRecertificationWindowUnit().toString(program.getRecertificationWindow(), translator);
				append(sb, translator.translate("recertification.window"), timeframe);
			}
			
			if(program.getCreditPointSystem() != null && program.getCreditPoints() != null) {
				String points = CertificationHelper.creditPointsToString(program);
				append(sb, translator.translate("notifications.infos.creditpoints"), points);
			}
		} else {
			append(sb, translator.translate("notifications.infos.recertification"), translator.translate("unit.none"));
		}

		sb.append("</div>");
		return sb.toString();
	}
	
	private static void append(StringBuilder sb, String label, String value) {
		sb.append("<div class='row form-group'><div class='col-sm-3'><strong class='control-label text-right'>")
		  .append(label)
		  .append("</strong></div><div class='col-sm-9'>")
		  .append(value)
		  .append("</div></div>");
	}

}
