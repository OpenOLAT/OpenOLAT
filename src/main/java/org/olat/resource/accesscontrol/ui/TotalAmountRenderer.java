/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.Objects;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Initial date: Mar 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TotalAmountRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof OrderTableRow order) {
			String amount = toPaymentPrice(order, getAmount(order));
			if (!StringHelper.containsNonWhitespace(amount)) {
				return;
			}
			
			if (renderer == null) {
				target.append(amount);
			} else {
				String amountOriginal = toPaymentPrice(order, getAmountOriginal(order));
				if (!Objects.equals(amount, amountOriginal)) {
					target.append("<span class=\"o_nowrap\" title=\"");
					target.append(translator.translate("amout.adjusted"));
					target.append("\"><i class=\"o_icon o_icon_info-fw o_icon_info\"></i> ");
					target.append(amount);
					target.append("</span>");
				} else {
					target.append(amount);
				}
			}
		}
	}

	protected Price getAmount(OrderTableRow order) {
		return order.getPrice();
	}

	protected Price getAmountOriginal(OrderTableRow order) {
		return order.getPriceLines();
	}
	
	private String toPaymentPrice(OrderTableRow row, Price value) {
		if(row.hasPaymentMethods()) {
			String val = PriceFormat.fullFormat(value);
			if(StringHelper.containsNonWhitespace(val)) {
				return val;
			}
		}
		return null;
	}

}
