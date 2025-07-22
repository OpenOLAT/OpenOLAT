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
package org.olat.modules.coach.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressRadialCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 22 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CompletionCellRenderer extends ProgressRadialCellRenderer {
	
	private final Translator translator;
	
	public CompletionCellRenderer(Translator translator) {
		super(BarColor.success);
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if (cellValue instanceof Double val) {
			target.append("<div class='o_coaching_completion'>");
			
			super.render(renderer, target, val, row, source, ubu, translator);
			
			long roundedVal = Math.round(val.doubleValue() * 100.0d);
			String valWithUnits = translator.translate("completion.value", Long.toString(roundedVal));
			target.append(" <span>").append(valWithUnits).append("</span></div>");
		}
	}

}
