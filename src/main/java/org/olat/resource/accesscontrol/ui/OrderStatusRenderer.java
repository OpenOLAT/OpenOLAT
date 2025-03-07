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
import org.olat.resource.accesscontrol.ui.OrderTableItem.Status;

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
	
	private final Translator translator;
	
	public OrderStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(val instanceof Status status) {
			renderStatusLight(sb, status);
		}
	}
	
	protected void renderStatus(StringOutput sb, Status status) {
		renderStatus(sb, "o_labeled", status);
	}
	
	protected void renderStatusLight(StringOutput sb, Status status) {
		renderStatus(sb, "o_labeled_light", status);
	}
	
	protected void renderStatus(StringOutput sb, String labelCss, Status status) {
		switch(status) {
			case ERROR:
				render(sb, "order.status.error", "o_ac_order_status_error_icon", labelCss, "o_ac_order_status_error");
				break;
			case WARNING:
				render(sb, "order.status.warning", "o_ac_order_status_warning_icon", labelCss, "o_ac_order_status_warning");
				break;
			case WRITTEN_OFF:
				render(sb, "order.status.written.off", "o_ac_order_status_written_off_icon", labelCss, "o_ac_order_status_written_off");
				break;
			case PENDING:
				render(sb, "order.status.pending", "o_ac_order_status_pending_icon", labelCss, "o_ac_order_status_pending");
				break;	
			case OPEN:
				render(sb, "order.status.open", "o_ac_order_status_open_icon", labelCss, "o_ac_order_status_open");
				break;
			case CANCELED:
				render(sb, "order.status.canceled", "o_ac_order_status_canceled_icon", labelCss, "o_ac_order_status_canceled");
				break;
			case CANCELED_WITH_FEE:
				render(sb, "order.status.canceled.with.fee", "o_ac_order_status_canceled_icon", labelCss, "o_ac_order_status_canceled");
				break;
			case IN_PROCESS:
				render(sb, "order.status.in.process", "o_ac_order_status_inprocess_icon", labelCss, "o_ac_order_status_in_process");
				break;
			case OK_PENDING:
				render(sb, "order.status.ok.pending", "o_ac_order_status_pending_icon", labelCss, "o_ac_order_status_payed_pending");
				break;
			case PAYED:
				render(sb, "order.status.payed", "o_ac_order_status_payed_icon", labelCss, "o_ac_order_status_payed");
				break;	
			case OK:
				render(sb, "order.status.ok", "o_ac_order_status_payed_icon", labelCss, "o_ac_order_status_payed");
				break;	
			default:
		}
	}
	
	public void render(StringOutput target, String i18nLabel, String iconCssClass, String labelCss, String cssClass) {
		String label = translator.translate(i18nLabel);
		target.append("<span class='").append(labelCss).append(" ").append(cssClass).append("'>")
		  .append("<i class='o_icon ").append(iconCssClass).append(" o_icon-fw' title='").append(label).append("'> </i> ")
		  .append(label)
	      .append("</span>");
	}
}
