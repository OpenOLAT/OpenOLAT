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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * Render an icon around the link
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupNameCellRenderer extends CustomCssCellRenderer implements FlexiCellRenderer {

	private static final String cssClass = "o_icon o_icon_group";
	private static final String managedCssClass = "o_icon o_icon_group o_icon_managed";
	
	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof BusinessGroupShort) {
			BusinessGroupShort group = (BusinessGroupShort)cellValue;
			String css = group.getManagedFlags().length == 0 ? cssClass : managedCssClass;
			sb.append("<i class='").append(css).append("'> </i> ")
			  .append(StringHelper.escapeHtml(group.getName()));
		}
	}



	@Override
	protected String getCssClass(Object val) {
		if(val instanceof BusinessGroupShort) {
			BusinessGroupShort group = (BusinessGroupShort)val;
			return group.getManagedFlags().length == 0 ? cssClass : managedCssClass;
		}
		return cssClass;
	}

	@Override
	protected String getCellValue(Object val) {
		if(val instanceof BusinessGroupShort) {
			BusinessGroupShort group = (BusinessGroupShort)val;
			return group.getName() == null ? "" : StringHelper.escapeHtml(group.getName());
		}
		return val == null ? "" : val.toString();
	}

	@Override
	protected String getHoverText(Object val) {
		return "";
	}
}