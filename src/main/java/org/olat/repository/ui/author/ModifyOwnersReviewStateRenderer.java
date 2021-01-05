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
package org.olat.repository.ui.author;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: Jan 4, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersReviewStateRenderer extends DefaultFlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		ModifyOwnersReviewTableRow tableRow = (ModifyOwnersReviewTableRow) source.getFlexiTableElement().getTableDataModel().getObject(row);
		
		switch (tableRow.getState()) {
			case added:
				target.append("<i class=\"o_icon o_icon_fw o_icon_review_added\"></i>");
				target.append("<span class=\"badge o_badge_added\">").append(translator.translate("modify.owners.review.added")).append("</span> ");
				break;
			case removed:
				target.append("<i class=\"o_icon o_icon_fw o_icon_review_removed\"></i>");
				target.append("<span class=\"badge o_badge_removed\">").append(translator.translate("modify.owners.review.removed")).append("</span> ");
				break;
			case granted:
				target.append("<i class=\"o_icon o_icon_fw o_icon_review_added\"></i>");
				break;
			case denied:
				target.append("<i class=\"o_icon o_icon_fw o_icon_review_removed\"></i>");
				break;
			case resource:
				if (tableRow.getAddedOwners() > 0) {
					target.append("<span class=\"badge o_badge_added\">").append(tableRow.getAddedOwners()).append(" ").append(translator.translate("modify.owners.review.added")).append("</span> ");
				}
				if (tableRow.getRemovedOwners() > 0) {
					target.append("<span class=\"badge o_badge_removed\">").append(tableRow.getRemovedOwners()).append(" ").append(translator.translate("modify.owners.review.removed")).append("</span> ");
				}
				if (tableRow.getAddedOwners() == 0 && tableRow.getRemovedOwners() == 0) {
					target.append("<span class=\"badge\">").append(translator.translate("modify.owners.review.no.changes")).append("</span> ");
				}
				break;
		}
	}
}
