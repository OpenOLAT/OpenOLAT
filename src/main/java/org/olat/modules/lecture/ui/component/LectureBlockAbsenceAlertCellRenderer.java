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
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;

/**
 * 
 * Initial date: 6 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAbsenceAlertCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof LectureBlockBlockStatistics) {
			LectureBlockBlockStatistics stats = (LectureBlockBlockStatistics)cellValue;
			if(stats.getNumOfAbsenceUnauthorized() > 0) {
				target.append("<span title=\"");
				if(stats.getNumOfAbsenceUnauthorized() > 0) {
					target.append(translator.translate("num.alert.absence.explain", new String[] { Integer.toString(stats.getNumOfAbsenceUnauthorized()) }))
					      .append(" ");
				}
				target.append("\"><i class='o_icon o_icon_error'> </i> ")
				      .append((stats.getNumOfAbsenceUnauthorized()))
				      .append("</span>");
			}
		}
	}


}
