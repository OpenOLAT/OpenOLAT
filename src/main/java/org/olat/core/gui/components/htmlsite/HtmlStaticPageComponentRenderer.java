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

package org.olat.core.gui.components.htmlsite;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * @author Felix Jost
 */
public class HtmlStaticPageComponentRenderer implements ComponentRenderer {

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant
	 * There must be an empty contructor for the Class.forName() call
	 */
	public HtmlStaticPageComponentRenderer() {
		super();
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
		// Is called for the current inline html		
		String htmlContent = cpc.getHtmlContent();
		// Add wrapper css style if defined (Used by scaling feature)
		String wrapperCssStyle = cpc.getWrapperCssStyle();
		if (htmlContent != null && wrapperCssStyle != null) {
			htmlContent = "<div style=\"" + wrapperCssStyle + "\">" + htmlContent + "</div>";			
		}
		//S Indicate to framework that the page probably will load some media files
		renderResult.setAsyncMediaResponsible(cpc); // indicate browser fetch of e.g. images includes in the rendered page should be directed to the cpcomponent so this component can deliver the e.g. images
		if (htmlContent != null) target.append(htmlContent);
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
		// is called for the current inline html		
		String mm = cpc.getHtmlHead();
		if (mm != null ) sb.append(mm);
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
		// is called for the current inline html		
		String mm = cpc.getJsOnLoad();
		if (mm != null ) sb.append(mm);
	}
}
