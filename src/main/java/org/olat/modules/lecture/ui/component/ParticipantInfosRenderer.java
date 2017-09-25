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
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.ui.ParticipantRow;

/**
 * 
 * Initial date: 18 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantInfosRenderer implements FlexiCellRenderer {
	
	private int count = 0;
	private final Translator translator;
	private final double defaultAttendanceRate;
	
	public ParticipantInfosRenderer(Translator translator, double defaultAttendanceRate) {
		this.translator = translator;
		this.defaultAttendanceRate = defaultAttendanceRate;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof ParticipantRow) {
			ParticipantRow participantRow = (ParticipantRow)cellValue;
			double requiredRate = participantRow.getStatistics().getRequiredRate();
			if(requiredRate >= 0.0d && Math.abs(defaultAttendanceRate - requiredRate) > 0.0001) {
				String id = "p_infos_" + ++count;
				target.append("<span id='").append(id).append("'><i class='o_icon o_icon-lg o_icon_info'>  </i></span>");
				
				// Attach bootstrap tooltip handler to help icon
				double percent = requiredRate * 100.0d;
				long rounded = Math.round(percent);
				String rateInfos = translator.translate("infos.participant.attendance.rate", new String[] { Long.toString(rounded) });
				
				target.append("<script>jQuery(function () {jQuery('#").append(id).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"")
				      .append(StringHelper.escapeJavaScript(rateInfos))
				      .append("\"});})</script>");
			}
		}
	}
}
