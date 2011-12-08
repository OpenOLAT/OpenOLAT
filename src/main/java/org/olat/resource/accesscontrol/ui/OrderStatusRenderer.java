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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.resource.accesscontrol.model.OrderStatus;

/**
 * 
 * Description:<br>
 * Render the status or the simplified status of an order
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderStatusRenderer implements CustomCellRenderer {

	public OrderStatusRenderer() {
		//
	}
	
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof OrderStatus) {
			OrderStatus status = (OrderStatus)val;
			String name = status.name().toLowerCase();
			sb.append("<span class='b_with_small_icon_left b_order_status_");
			sb.append(name);
			sb.append("_icon'></span>");
		} else if (val instanceof OrderTableItem) {
			OrderTableItem item = (OrderTableItem)val;
			switch(item.getStatus()) {
				case ERROR: sb.append("<span class='b_with_small_icon_left b_order_status_error_icon'></span>"); break;
				case WARNING: sb.append("<span class='b_with_small_icon_left b_order_status_warning_icon'></span>"); break;
				case CANCELED: sb.append("<span class='b_with_small_icon_left b_order_status_canceled_icon'></span>"); break;
				default: sb.append("<span class='b_with_small_icon_left b_order_status_payed_icon'></span>");
			}
		}
	}
}
