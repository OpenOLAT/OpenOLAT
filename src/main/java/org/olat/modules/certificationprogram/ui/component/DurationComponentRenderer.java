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
package org.olat.modules.certificationprogram.ui.component;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 4 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DurationComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		DurationComponent expCmp = (DurationComponent)source;
		DurationFormItem item = expCmp.getFormItem();
		
		String cmpId = expCmp.getDispatchID();
		sb.append("<div id='o_c").append(cmpId).append("' class='form-inline'>");
	
		TextElement valueEl = item.getValueElement();
		Component valueCmp = valueEl.getComponent();
		valueCmp.getHTMLRendererSingleton().render(renderer, sb, valueCmp, ubu, translator, renderResult, new String[] { "form" });
		
		sb.append(" ");

		SingleSelection typeEl = item.getTypeElement();
		Component typeCmp = typeEl.getComponent();
		typeCmp.getHTMLRendererSingleton().render(renderer, sb, typeCmp, ubu, translator, renderResult, new String[] { "form" });
		
		sb.append("</div>");
		
		valueCmp.setDirty(false);
		typeCmp.setDirty(false);
	}
}
