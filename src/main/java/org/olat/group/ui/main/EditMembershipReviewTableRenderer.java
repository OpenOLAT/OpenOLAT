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
package org.olat.group.ui.main;

import static org.olat.core.util.StringHelper.blankIfNull;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Initial date: Dec 4, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class EditMembershipReviewTableRenderer extends IconCssCellRenderer{

	private EditMembershipReviewTableRow tableRow;
	private Translator translator; 
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		tableRow = getRow(source, row);
		this.translator = translator;
		
		// User 
		if (tableRow.getRowMode() == 0) {
			target.append("<span style='white-space: nowrap;'");
			String hoverText = getHoverText(cellValue);
			if (StringHelper.containsNonWhitespace(hoverText)) {
				target.append(" title=\"");
				target.appendHtmlEscaped(hoverText);
			}
			target.append("\">");
			target.append("<i class='").append(blankIfNull(getIconCssClass(cellValue))).append("'> </i> <span>");
	
			target.append(blankIfNull(getCellValue(cellValue)));
			target.append("</span></span>");
			
			if (cellValue instanceof Integer) {
				switch ((Integer) cellValue) {
					case 2: 
						target.append("<span class=\"badge o_badge_added\">").append(translator.translate("review.added")).append("</span>");
						break;
					case 4:
						target.append("<span class=\"badge o_badge_removed\">").append(translator.translate("review.removed")).append("</span>");
						break;
				}
			}
		// Category
		} else if (tableRow.getRowMode() == 1) {
			if (getTotalAdded(cellValue) != 0 || getTotalRemoved(cellValue) != 0) {
				if (getTotalAdded(cellValue) != 0) {
					target.append("<span class=\"badge o_badge_added\">").append(getTotalAdded(cellValue)).append(" ").append(translator.translate("review.added")).append("</span> ");
				}
				if (getTotalRemoved(cellValue) != 0) {
					target.append("<span class=\"badge o_badge_removed\">").append(getTotalRemoved(cellValue)).append(" ").append(translator.translate("review.removed")).append("</span> ");
				}
			} else {
				target.append("<span class=\"badge\">").append(translator.translate("review.no.changes")).append("</span> ");
			}
		}
	}
	
	private EditMembershipReviewTableRow getRow(FlexiTableComponent source, int row) {
		return (EditMembershipReviewTableRow) source.getFlexiTableElement().getTableDataModel().getObject(row);
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof Integer) {
			switch ((Integer) val) {
				case 1:
				case 2:
					return "o_icon o_icon_fw o_icon_review_added";
				case 3:
				case 4:
					return "o_icon o_icon_fw o_icon_review_removed";
				case 0:
				default: 
					return null;
				}
		}
		
		return null;
	}

	@Override
	protected String getCellValue(Object val) {		
		return null;
	}

	@Override
	protected String getHoverText(Object val) {
		if (val instanceof Integer) {
			switch ((Integer) val) {
				case 0: 
					return translator.translate("review.table.no.info");
				case 1:
					return translator.translate("review.table.yes");
				case 2:
					return translator.translate("review.table.yes.changed");
				case 3:
					return translator.translate("review.table.no");
				case 4:
					return translator.translate("review.table.no.changed");
			}
		}
		
		return null;
	}
	
	private int getTotalAdded(Object cellValue) {
		if (cellValue instanceof String) {
			return Integer.valueOf(((String) cellValue).split(",")[0]);
		}
		
		return 0;		
	}
	
	private int getTotalRemoved(Object cellValue) {
		if (cellValue instanceof String) {
			return Integer.valueOf(((String) cellValue).split(",")[1]);
		}
		
		return 0;		
	}
}
