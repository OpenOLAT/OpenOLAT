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
package org.olat.core.gui.control.winmgr;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.i18n.I18nModule;

/**
 * Description:<br>
 * TODO: 
 * 
 * <P>
 * Initial Date: 02.02.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ScreenReaderHandlerInstance implements InterceptHandlerInstance {
	//private ComponentRenderer cr;
	
	//private Component rootComponent;
	/**
	 * 
	 */
	public ScreenReaderHandlerInstance() {
		// created for one render cycle.
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.render.intercept.InterceptHandlerInstance#createInterceptComponentRenderer(org.olat.core.gui.components.ComponentRenderer)
	 */
	public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
		return new ComponentRenderer(){

			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
				//source.getChangedExplanationObject
				if (source.isDomReplaceable() && source.isDirtyForUser() && isLargestDirty(source)) {
					Translator trans = translator;
					Component parent = source.getParent();
					while (trans == null) {
						if (parent == null) {
							// Ups, on top of component tree and still no translator found? Use default.
							trans = new PackageTranslator("org.olat.core", I18nModule.getDefaultLocale());
						} else {
							trans = parent.getTranslator();
							parent = parent.getParent();							
						}
					}
					sb.append("<fieldset><legend><a accesskey=\"u\" href=\"#updated\" title=\"");
					sb.append(StringEscapeUtils.escapeHtml(trans.translate("web.2a.updated.alt")));
					sb.append("\">");
					sb.append(trans.translate("web.2a.updated"));
					sb.append("</a></legend>");
					originalRenderer.render(renderer, sb, source, ubu, translator, renderResult, args);
					sb.append("</fieldset>");
				} else {
					originalRenderer.render(renderer, sb, source, ubu, translator, renderResult, args);
				}
			}

			private boolean isLargestDirty(Component source) {
				// check if this component is dirty, and also if there is no parent element that is dirty
				Component cur = source;
				boolean outerDirty = false;
				while (!outerDirty && (cur = cur.getParent())!= null) {
					outerDirty = cur.isDomReplaceable() && cur.isDirty();
				}
				return !outerDirty;
			}

			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
				// nothing
			}

			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
				// nothing
			}};
	}

}
