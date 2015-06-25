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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.components;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SimpleLabelTextComponent
 * <P>
 * Initial Date: 06.12.2006 <br>
 * 
 * @author patrickb
 */
public class SimpleLabelText extends FormBaseComponentImpl {
	private static final ComponentRenderer RENDERER = new LabelComponentRenderer();
	
	private final String text;
	private boolean componentIsMandatory;
	private final FormItem item;

	public SimpleLabelText(FormItem item, String name, String text, boolean mandatory) {
		super(name);
		this.text = text;
		this.item = item;
		this.componentIsMandatory = mandatory;
		// to minimize DOM tree we provide our own DOM ID (o_c12245)
		this.setDomReplacementWrapperRequired(false);
	}

	public boolean isComponentIsMandatory() {
		return componentIsMandatory;
	}

	public void setComponentIsMandatory(boolean componentIsMandatory) {
		this.componentIsMandatory = componentIsMandatory;
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	private static class LabelComponentRenderer extends DefaultComponentRenderer {
		@Override
		public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
				RenderResult renderResult, String[] args) {
			SimpleLabelText stc = (SimpleLabelText) source;
			sb.append("<label class='control-label ");
			if (args !=  null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String arg = args[i];
					if (arg.startsWith("col-")) {
						sb.append(arg);
					}
				}
			}
			sb.append("' id='o_c").append(source.getDispatchID()).append("'");
			// add the reference to form element for which this label stands. this is important for screen readers
			if (stc.item != null) {
				String forId = stc.item.getForId();
				if(forId != null) {
					sb.append(" for=\"").append(forId).append("\"");
				}
			}
			sb.append(">");
			if (StringHelper.containsNonWhitespace(stc.text)) {
				sb.append(stc.text);
			}
			if (stc.componentIsMandatory) {
				String hover = stc.getTranslator().translate("form.mandatory.hover");
				sb.append("<i class='o_icon o_icon_mandatory' title='").append(hover).append("'></i>");
			}
			sb.append("</label>");
		}
	}
}
