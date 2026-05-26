/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCustomRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.ui.LectureBlocksWidgetRow;

/**
 * Initial date: 2026-05-13<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class LectureBlocksWidgetRenderer extends FlexiTableCustomRenderer {

	@Override
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {

		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFormItem();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();

		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);

		String rowIdPrefix = "row_" + id + "-";
		boolean groupOpen = false;
		for (int i = firstRow; i < lastRow; i++) {
			if (dataModel.isRowLoaded(i)) {
				Object rowObj = dataModel.getObject(i);
				if (rowObj instanceof LectureBlocksWidgetRow row
						&& StringHelper.containsNonWhitespace(row.getDayAbbr())) {
					if (groupOpen) {
						target.append("</div></div>");
					}
					target.append("<div class='o_lecture_widget_day_group'>");
					target.append("<div class='o_lecture_widget_day_header'>");
					target.append("<div class='o_row_day_abbr'>").append(row.getDayAbbr()).append("</div>");
					target.append("<div class='o_row_day_num'>").append(row.getDay()).append("</div>");
					target.append("</div>");
					target.append("<div class='o_lecture_widget_day_rows'>");
					groupOpen = true;
				}
				renderRow(renderer, target, ftC, rowIdPrefix, i, ubu, translator, renderResult);
			}
		}
		if (groupOpen) {
			target.append("</div></div>");
		}
	}

}
