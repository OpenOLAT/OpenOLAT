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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * Render an icon around the link
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupNameCellRenderer extends CustomCssCellRenderer {

	private static final String cssClass = "b_small_table_icon b_group_icon";
	private static final String managedCssClass = "b_small_table_icon b_group_icon b_managed_icon";
	
	public BusinessGroupNameCellRenderer() {
		//
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
			return group.getName() == null ? "" : StringEscapeUtils.escapeHtml(group.getName());
		}
		return val == null ? "" : val.toString();
	}

	@Override
	protected String getHoverText(Object val) {
		return "";
	}
}