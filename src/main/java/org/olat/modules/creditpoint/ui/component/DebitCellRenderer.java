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
package org.olat.modules.creditpoint.ui.component;

import java.math.BigDecimal;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.CreditPointSystem;

/**
 * 
 * Initial date: 10 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DebitCellRenderer implements FlexiCellRenderer {
	
	private final CreditPointSystem system;
	
	public DebitCellRenderer(CreditPointSystem system) {
		this.system = system;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		
		if(cellValue instanceof BigDecimal amount) {
			// Abs because the - sign is rendered by the cell renderer separately
			String formattedAmount = CreditPointFormat.format(amount.abs(), system);
			target.append("<span class='o_creditpoint_amount_debit'>-</span>")
		      .append(" <span>").appendHtmlEscaped(formattedAmount).append("</span>");
		}
	}
}
