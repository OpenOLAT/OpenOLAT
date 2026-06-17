/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ui.reference.PositionReferenceRow;

/**
 * 
 * Initial date: 27 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceAdminNoteCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String && StringHelper.containsNonWhitespace((String)cellValue)) {
			PositionReferenceRow refRow = (PositionReferenceRow)source.getFormItem().getTableDataModel().getObject(row);
			Long refKey = refRow.getReference().getKey();
			target.append("<span id='admin_note_").append(refKey).append("'><i class='o_icon o_icon-lg o_icon_memo'> </i></span>");
			
			// Attach bootstrap tooltip handler to help icon
			target.append("<script>jQuery(function () {jQuery('#admin_note_").append(refKey).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
			StringBuilder notes = Formatter.escWithBR((String)cellValue);
			target.append(StringHelper.escapeJavaScript(notes.toString()))
			      .append("\"});})</script>");
		}
	}
}
