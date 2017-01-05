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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.resource.accesscontrol.OrderStatus;

/**
 * 
 * Description:<br>
 * Render the status or the simplified status of an order
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderStatusRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(val instanceof OrderStatus) {
			OrderStatus status = (OrderStatus)val;
			String name = status.name().toLowerCase();
			sb.append("<i class='o_icon o_icon-fw o_ac_order_status_");
			sb.append(name);
			sb.append("_icon'> </i>");
		} else if (val instanceof OrderTableItem) {
			OrderTableItem item = (OrderTableItem)val;
			switch(item.getStatus()) {
				case ERROR: sb.append("<i class='o_icon o_icon-fw o_ac_order_status_error_icon'> </i>"); break;
				case WARNING: sb.append("<i class='o_icon o_icon-fw o_ac_order_status_warning_icon'> </i>"); break;
				case CANCELED: sb.append("<i class='o_icon o_icon-fw o_ac_order_status_canceled_icon'> </i>"); break;
				default: sb.append("<i class='o_icon o_icon-fw o_ac_order_status_payed_icon'> </i>");
			}
		}
	}
}
