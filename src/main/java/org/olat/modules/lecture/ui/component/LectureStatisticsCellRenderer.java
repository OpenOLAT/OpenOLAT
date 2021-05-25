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
 * Initial date: 17 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureStatisticsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof LectureBlockStatistics) {
			LectureBlockStatistics stats = (LectureBlockStatistics)cellValue;
			long total = stats.getTotalPersonalPlannedLectures();
			long attended = stats.getTotalAttendedLectures();
			long absent = stats.getTotalAbsentLectures();
			long authorizedAbsent = stats.getTotalAuthorizedAbsentLectures();
			long dispensed = stats.getTotalDispensationLectures();
			render(target, total, attended, absent, authorizedAbsent, dispensed);
		}
	}
	
	private void render(StringOutput target, long total, long attended, long absent, long authorizedAbsent, long dispensed) {
		long attendedPercent = (attended == 0l) ? 0l : Math.round(100.0d * ((double)attended / (double)total));
		long absentPercent = (absent == 0l) ? 0l :  Math.round(100.0d * ((double)absent / (double)total));
		long authorizedAbsentPercent = (authorizedAbsent == 0l) ? 0l :  Math.round(100.0d * ((double)authorizedAbsent / (double)total));
		long dispensedPercent = (dispensed == 0l) ? 0l :  Math.round(100.0d * ((double)dispensed / (double)total));
		
		target.append("<div class='progress'>");
		//attended
		target.append("<div class='progress-bar progress-bar-success' role='progressbar' aria-valuenow='").append(attended)
		      .append("' aria-valuemin='0' aria-valuemax='").append(total)
		      .append("' style='width: ").append(attendedPercent).append("%;'>")
		      .append("<span class='sr-only'>").append(attendedPercent).append("%</span></div>");
		
		//authorized absent
		target.append("<div class='progress-bar progress-bar-warning' role='progressbar' aria-valuenow='").append(authorizedAbsent)
	      .append("' aria-valuemin='0' aria-valuemax='").append(total)
	      .append("' style='width: ").append(authorizedAbsentPercent).append("%;'>")
	      .append("<span class='sr-only'>").append(authorizedAbsentPercent).append("%</span></div>");
		
		// dispensed
		target.append("<div class='progress-bar progress-bar-info' role='progressbar' aria-valuenow='").append(dispensed)
	      .append("' aria-valuemin='0' aria-valuemax='").append(total)
	      .append("' style='width: ").append(dispensedPercent).append("%;'>")
	      .append("<span class='sr-only'>").append(dispensedPercent).append("%</span></div>");
		
		//absent
		target.append("<div class='progress-bar progress-bar-danger' role='progressbar' aria-valuenow='").append(absent)
	      .append("' aria-valuemin='0' aria-valuemax='").append(total)
	      .append("' style='width: ").append(absentPercent).append("%;'>")
	      .append("<span class='sr-only'>").append(absentPercent).append("%</span></div>");

		target.append("</div>");
	}
}
