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
*/

package org.olat.modules.tu;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TunnelRenderer implements ComponentRenderer {

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant There must
	 * be an empty contructor for the Class.forName() call
	 */
	public TunnelRenderer() {
		super();
	}

	/**
	 * @param renderer
	 * @param target
	 * @param source
	 * @param ubu
	 * @param translator
	 * @param renderResult
	 * @param args
	 * 
	 */
	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TunnelComponent tuc = (TunnelComponent) source;
		// is called for the current inline html
		renderResult.setAsyncMediaResponsible(tuc);
		String htmlContent = tuc.getHtmlContent();
		if (htmlContent != null) target.append(htmlContent);
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		TunnelComponent tuc = (TunnelComponent) source;
		String htmlHead = tuc.getHtmlHead();
		if (htmlHead != null) sb.append(htmlHead);
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		TunnelComponent tuc = (TunnelComponent) source;
		String jsBodyOnLoad = tuc.getJsOnLoad();
		if (jsBodyOnLoad != null) sb.append(jsBodyOnLoad);
	}
}