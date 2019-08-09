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

package org.olat.core.gui.components;

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
public interface ComponentRenderer {

	/**
	 * Calls must be threadsafe and the caller set dirty to false on the component.
	 * 
	 * @param renderer
	 * @param sb
	 * @param source
	 * @param ubu
	 * @param translator
	 * @param renderResult
	 * @param args the layouting arguments (dependent on the concrete renderer,
	 *          e.g. Tablerenderer, Formrenderer, ...). if not null, then it must
	 *          be at least of size 1
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args);

	/**
	 * @deprecated please use either the validate(..) method in Component.java in your component or create a new JSAndCSSComponent(...) in your controller which you can then include in your render tree. This method here is legacy and does not support web 2.0 mode.<br>
	 * <br>
	 * things like css and .js files to be loaded in the <head>tag, e.g. <script
	 * src="/bla/blu/blo.js" />
	 * 
	 * @param renderer
	 * @param sb
	 * @param source
	 * @param ubu
	 * @param translator
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate);

	/**
	 * contributes a line to the following code: <script>
	 * function o2init() { < < < <other previous inserts>>>> < < < <here comes the
	 * insert, e.g. olat_epoz_init();>>>> } </script> ... <body onLoad="o2init()">
	 * 
	 * @param renderer
	 * @param sb
	 * @param source
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate);
}