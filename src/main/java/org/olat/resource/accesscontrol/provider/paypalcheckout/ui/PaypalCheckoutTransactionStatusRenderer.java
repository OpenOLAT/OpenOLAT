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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Description:<br>
 * Render an icon or message for the status of a paypal transaction
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalCheckoutTransactionStatusRenderer  implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public PaypalCheckoutTransactionStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof String) {
			String status = ((String)cellValue).toLowerCase();
			String cssClass;
			String i18nKey;
			switch(status) {
				case "completed":
				case "success": 
					i18nKey = "status.success";
					cssClass = "o_ac_status_success_icon";
					break;
				case "error":
					i18nKey = "status.error";
					cssClass = "o_ac_status_error_icon";
					break;
				case "canceled":
					i18nKey = "status.canceled";
					cssClass = "o_ac_status_canceled_icon";
					break;
				case "new":
					i18nKey = "status.new";
					cssClass = "o_ac_status_new_icon";
					break;
				default:
					i18nKey = "status.unkown";
					cssClass = "o_ac_status_unnkown_icon";
					break;
			}
			String title = translator.translate(i18nKey, new String[] { status });
			sb.append("<span title=\"").append(title).append("\"><i class='o_icon o_icon-fw ").append(cssClass).append("'> </i></span>");
		}
	}
}
