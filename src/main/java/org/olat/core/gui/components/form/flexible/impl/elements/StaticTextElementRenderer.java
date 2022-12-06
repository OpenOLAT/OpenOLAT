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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * This class renders the StaticTextElementComponent in a flexi form context
 * 
 * <P>
 * Initial Date:  02.02.2007 <br>
 * @author patrickb
 */
class StaticTextElementRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		StaticTextElementComponent steC = (StaticTextElementComponent)source;
		String id = steC.getFormDispatchId();
		String value = steC.getValue();
		
		sb.append("<").append(steC.getDomWrapperElement().name()).append(" id=\"").append(id).append("\" ");
		sb.append(FormJSHelper.getRawJSFor(steC.getRootForm(), id, steC.getAction()));
		sb.append(" class='form-control-static ");
		if(StringHelper.containsNonWhitespace(steC.getElementCssClass())) {
			sb.append(steC.getElementCssClass());
		}
		sb.append("'>").append(value).append("</").append(steC.getDomWrapperElement().name()).append(">");
	}
}