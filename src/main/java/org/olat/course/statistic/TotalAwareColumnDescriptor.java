/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.statistic;

import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * An extension of DefaultColumnDescriptor with the sole purpose of rendering
 * the TOTAL_ROW_TITLE_CELL differently than the rest of the crowd.
 * <p>
 * The StatisticResult uses a special token, the StatisticResult.TOTAL_ROW_TITLE_CELL
 * which it returns for the last row, column 0 (the 'Total' title).
 * This ColumnDescriptor extends renderValue to catch the rendering of that title
 * and apply any boldness or the like (using TotalRendererHelper)
 * <P>
 *@GODO
 * Note that this ColumnDescriptor also uses the 'trick' of checking for
 * renderer==null to distinguish between normal rendering (on screen) where
 * we do have a renderer, and rendering for export where the renderer is null.
 * This is a bit hacky though and we should probably come up with a nicer generic
 * solution for the ColumnDescriptor/CellRenderer etc.
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalAwareColumnDescriptor extends DefaultColumnDescriptor {

	public TotalAwareColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale, int alignment) {
		super(headerKey, dataColumn, action, locale, alignment, alignment);
	}
	
	@Override
	public String getAction(int row) {
		if (row==table.getTableDataModel().getRowCount()-1) {
			return super.getAction(row);
		}
		return null;
	}
	
	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		Object col0 = table.getTableDataModel().getValueAt(table.getSortedRow(row), 0);
		if (col0!=StatisticResult.TOTAL_ROW_TITLE_CELL) {
			super.renderValue(sb, row, renderer);
		} else {
			if (renderer!=null) {
				TotalRendererHelper.renderTotalValuePrefix(sb);
			}
			super.renderValue(sb, row, renderer);
			if (renderer!=null) {
				TotalRendererHelper.renderTotalValuePostfix(sb);
			}
		}
	}

}
