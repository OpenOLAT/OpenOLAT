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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;


/**
 * Implements the component for an HTML horizontal bar (&lt;HR&gt;) element.
 * 
 * @author twuersch
 */
public class SpacerElementComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new SpacerElementRenderer();
	
	private SpacerElement spacerElement;
	
	public SpacerElementComponent(SpacerElement element) {
		super(element.getName());
		this.spacerElement = element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * Get the spacer element for this component
	 * @return
	 */
	public SpacerElement getSpacerElement() {
		return this.spacerElement;
	}


}
