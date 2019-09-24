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
public class LectureBlockAbsenceWarningCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public LectureBlockAbsenceWarningCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		
		if(cellValue instanceof LectureBlockBlockStatistics) {
			LectureBlockBlockStatistics stats = (LectureBlockBlockStatistics)cellValue;
			if(stats.getNumOfNoticeOfAbsenceUnauthorized() > 0 || stats.getNumOfDispensationUnauthorized() > 0) {
				target.append("<span title=\"");
				if(stats.getNumOfNoticeOfAbsenceUnauthorized() > 0) {
					target.append(translator.translate("num.warnings.notice.absence.explain", new String[] { Integer.toString(stats.getNumOfNoticeOfAbsenceUnauthorized()) }))
					      .append(" ");
				}
				if(stats.getNumOfDispensationUnauthorized() > 0) {
					target.append(translator.translate("num.warnings.dispensation.explain", new String[] { Integer.toString(stats.getNumOfDispensationUnauthorized()) }));
				}
				target.append("\"><i class='o_icon o_icon_warning'> </i> ")
				      .append((stats.getNumOfNoticeOfAbsenceUnauthorized() + stats.getNumOfDispensationUnauthorized()))
				      .append("</span>");
			}
		}
	}
}
