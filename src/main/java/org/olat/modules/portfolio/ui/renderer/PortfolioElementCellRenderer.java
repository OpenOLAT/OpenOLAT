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
package org.olat.modules.portfolio.ui.renderer;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 29.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioElementCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public PortfolioElementCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof PortfolioElementRow) {
			render(target, (PortfolioElementRow)cellValue, null);
		} else if(cellValue instanceof String) {
			Object objRow = source.getFlexiTableElement().getTableDataModel().getObject(row);
			if(objRow instanceof PortfolioElementRow) {
				render(target, (PortfolioElementRow)objRow, (String)cellValue);
			} else {
				target.appendHtmlEscaped((String)cellValue);
			}
		}
	}
	
	private void render(StringOutput target, PortfolioElementRow elRow, String title) {
		target.append("<span class='");
		if(elRow.isSection()) {
			target.append("o_pf_section'><i class='o_icon o_icon-fw o_icon_pf_section'> </i> ");
		} else if(elRow.isPage()) {
			target.append("o_pf_page'><i class='o_icon o_icon-fw o_icon_pf_page'> </i> ");
			if(elRow.isShared()) {
				target
					.append("<span title='").append(translator.translate("page.shared.tooltip")).append("'>")
					.append("<i class='o_icon o_icon-fw o_icon_pf_page_shared'> </i></span> ");
			}
		} else if(elRow.isPendingAssignment()) {
			target.append("o_pf_assignment'><i class='o_icon o_icon-fw o_icon_assignment'> </i> ");
		} else {
			target.append("'>");
		}
		if(StringHelper.containsNonWhitespace(title)) {
			target.appendHtmlEscaped(title);
		}
		target.append("</span>");
	}
}
