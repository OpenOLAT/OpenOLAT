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
 * The label form component displays a label for the given form item. For DOM
 * reasons the label does also include information about mandatory items and
 * includes the item help text or links if available.
 * <P>
 * Initial Date: 06.12.2006 <br>
 * 
 * @author patrickb, gnaegi
 */
public class SimpleLabelText extends FormBaseComponentImpl {
	private static final ComponentRenderer RENDERER = new LabelComponentRenderer();
	
	private final String text;
	private final FormItem item;

	public SimpleLabelText(FormItem item, String name, String text) {
		super(name);
		this.text = text;
		this.item = item;
		// to minimize DOM tree we provide our own DOM ID (o_c12245)
		setDomReplacementWrapperRequired(false);
	}
	
	@Override
	public FormItem getFormItem() {
		return null;
	}

	/**
	 * return true: the component is mandatory; false: the component is optional
	 */
	public boolean isComponentIsMandatory() {
		return item.isMandatory();
	}

	/**
	 * return the context help text for this component or NULL if not available
	 */
	public String getComponentHelpText() {
		return item.getHelpText();
	}
	
	/**
	 * return the context help url for this component or NULL if not available
	 */
	public String getComponentHelpUrl() {
		return item.getHelpUrl();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	private static class LabelComponentRenderer extends DefaultComponentRenderer {
		@Override
		public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
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
			if (stc.isComponentIsMandatory()) {
				String hover = stc.getTranslator().translate("form.mandatory.hover");
				sb.append("<i class='o_icon o_icon_mandatory' title='").append(hover).append("'></i> ");
			}
			if (StringHelper.containsNonWhitespace(stc.text)) {
				sb.append(stc.text);
			}
			// component help is optional, can be text or link or both
			if (stc.getComponentHelpText() != null || stc.getComponentHelpUrl() != null) {
				String helpIconId = "o_fh" + source.getDispatchID();
				// Wrap tooltip with link to external url if available
				String helpUrl = stc.getComponentHelpUrl();
				if (helpUrl != null) {
					sb.append("<a href=\"").append(helpUrl).append("\" target='_blank'>"); 
				}
				// tooltip is bound to this icon
				sb.append("<i class='o_form_chelp o_icon o_icon-fw o_icon_help help-block' id='").append(helpIconId).append("'></i>");
				if (helpUrl != null) {
					sb.append("</a>");
				}			
				// Attach bootstrap tooltip handler to help icon
				sb.append("<script>jQuery(function () {jQuery('#").append(helpIconId).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
				String text = stc.getComponentHelpText();
				if (text != null) {
					sb.append(StringHelper.escapeJavaScript(text));
				}
				String url = stc.getComponentHelpUrl();
				if (url != null) {
					if (text != null) {
						// append spacer between custom and generic link text
						sb.append("<br />");
					}
					sb.append(translator.translate("help.tooltip.link", new String[]{"<i class='o_icon o_icon-fw o_icon_help'></i>"}));					
				}
				sb.append("\"});})</script>");		
			}
			sb.append("</label>");
		}
	}
}
