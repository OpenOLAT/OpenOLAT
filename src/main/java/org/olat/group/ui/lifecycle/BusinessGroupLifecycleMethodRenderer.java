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
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 13 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupLifecycleMethodRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	private final Boolean days;
	private final boolean automaticMethod;
	
	public BusinessGroupLifecycleMethodRenderer(Translator translator, boolean automaticMethod, Boolean days) {
		this.translator = translator;
		this.days = days;
		this.automaticMethod = automaticMethod;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		
		boolean excluded = false;
		if(cellValue instanceof Boolean) {
			excluded = ((Boolean)cellValue).booleanValue();
		}
		render(target, excluded);
	}
	
	public void render(StringOutput sb, boolean excluded) {
		if(automaticMethod && !excluded) {
			sb.append(translator.translate("table.lifecycle.auto"));
		} else {
			sb.append(translator.translate("table.lifecycle.manual"));
		}

		if(days != null) {
			String dayI18n = days.booleanValue() ? "process.with.email.short" : "process.without.email";
			sb.append(" - ").append(translator.translate(dayI18n));
		}
	}
}
