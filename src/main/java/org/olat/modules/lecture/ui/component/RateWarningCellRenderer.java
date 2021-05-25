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
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 10 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RateWarningCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public RateWarningCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		
		if(cellValue instanceof LectureBlockStatistics) {
			LectureBlockStatistics stats = (LectureBlockStatistics)cellValue;
			if(stats.isCalculateRate() && stats.getTotalPersonalPlannedLectures() > 0l &&
					(stats.getTotalAbsentLectures() > 0l || stats.getTotalAttendedLectures() > 0l
							|| stats.getTotalAuthorizedAbsentLectures() > 0l || stats.getTotalDispensationLectures() > 0l)) {
				double attendanceRate = stats.getAttendanceRate();
				double requiredRate = stats.getRequiredRate();
				
				if(requiredRate > attendanceRate) {
					String title = translator.translate("rate.error.title");
					target.append("<i class='o_icon o_icon-lg o_icon_error' title='").append(title).append("'> </i>");
				} else if(attendanceRate - requiredRate < 0.05) {// less than 5%
					String title = translator.translate("rate.warning.title");
					target.append("<i class='o_icon o_icon-lg o_icon_warning' title='").append(title).append("'> </i>");	
				}
			}
		}
	}
}