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
package org.olat.modules.selectus.ui.components;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;

/**
 * 
 * Initial date: 18.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionCellRenderer implements CustomCellRenderer, FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		render(sb, val);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {	
		render(sb, val);
	}
	
	private void render(StringOutput sb, Object val) {
		if(val instanceof Application) {
			Application app = (Application)val;
			val = app.getDecision();
		} else if(val instanceof ApplicationLight) {
			ApplicationLight app = (ApplicationLight)val;
			val = app.getDecision();
		}
		
		if(val instanceof Integer) {
			int value = ((Integer)val).intValue();
			sb.append("<span class='fx_r_decision'>");
			switch(value) {
				case 1: sb.append("C"); break;
				case 2: sb.append("B"); break;
				case 3: sb.append("A"); break;
				default:{}
			}
			sb.append("</span>");
		}
	}
}
