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
package org.olat.modules.fo.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IndentCellRenderer implements FlexiCellRenderer {

	private static final int MAXINDENTS = 20;
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		SortKey[] keys = source.getFlexiTableElement().getOrderBy();
		if(keys != null && keys.length > 0 && keys[0] != null && !"natural".equals(keys[0].getKey())) {
			if(cellValue instanceof String) {
				target.append((String)cellValue);
			}
		} else {
			Object m = source.getFlexiTableElement().getTableDataModel().getObject(row);
			if(m instanceof MessageLightView && cellValue instanceof String) {
				MessageLightView message = (MessageLightView)m;
				int indent = message.getDepth();
				if (indent > MAXINDENTS) {
					indent = MAXINDENTS;
				}
				target.append("<div style=\"white-space: nowrap;")
				      .append("padding-left: ").append(indent).append("em;\">")
				      .append(Formatter.truncate((String)cellValue, 50 - indent))
				      .append("</div>");
			} else if(cellValue instanceof String) {
				target.append((String)cellValue);
			}
		}
	}
}
