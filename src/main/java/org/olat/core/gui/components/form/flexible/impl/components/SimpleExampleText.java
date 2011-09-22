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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.components;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SimpleExampleTextComponent
 * <P>
 * Initial Date: 06.12.2006 <br>
 * 
 * @author patrickb
 */
public class SimpleExampleText extends FormBaseComponentImpl {

	String text;

	public SimpleExampleText(String name, String text) {
		super(name);
		this.text = text;
	}

	private static final ComponentRenderer RENDERER = new ComponentRenderer() {
		
		@SuppressWarnings("unused")
		public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
				RenderingState rstate) {
		// not used for example
		}
		
		@SuppressWarnings("unused")
		public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		// not used for example
		}

		@SuppressWarnings("unused")
		public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
				RenderResult renderResult, String[] args) {
			SimpleExampleText stc = (SimpleExampleText) source;
			sb.append("<div class=\"b_form_example\">");
			sb.append(stc.text);
			sb.append("</div>");
		}

	};

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
