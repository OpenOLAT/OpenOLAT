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

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.model.AssessedBinderSection;
import org.olat.modules.portfolio.model.SharedItemRow;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectSectionsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof SharedItemRow) {
			SharedItemRow itemRow = (SharedItemRow)cellValue;
			List<AssessedBinderSection> sections = itemRow.getSections();
			if(sections != null && sections.size() > 0) {
				FlexiTableElementImpl ftE = source.getFormItem();
				String id = source.getFormDispatchId();
				Form rootForm = ftE.getRootForm();
				
				boolean expand = itemRow.isExpandSections();
				for(int i=0; i<sections.size(); i++) {
					if(i > 0) {
						if(expand) target.append("<br />");
						else target.append(" | ");
					}
					NameValuePair pair1 = new NameValuePair("select-section", Integer.toString(row));
					NameValuePair pair2 = new NameValuePair("section", Integer.toString(i));
					String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, true, true, pair1, pair2);
					target.append("<a href=\"javascript:").append(jsCode).append(";\"").append(">");
					if(expand) {
						target.append(StringHelper.escapeHtml(sections.get(i).getSectionTitle()));
					} else {
						target.append(i + 1);
					}
					target.append("</a>");
				}
				
				NameValuePair pair = new NameValuePair("expand-section", Integer.toString(row));
				String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, true, true, pair);
				target.append(" <a href=\"javascript:").append(jsCode).append(";\"").append(">");
				if(itemRow.isExpandSections()) {
					target.append("<i class='o_icon o_icon-sm o_icon_expand'> </i>");
				} else {
					target.append("<i class='o_icon o_icon-sm o_icon_compress'> </i>");
				}
				target.append("</a>");
			}
		}
	}
}
