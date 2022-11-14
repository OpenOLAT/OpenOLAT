/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Implements the component for an HTML horizontal bar (&lt;HR&gt;) element.
 * 
 * @author twuersch
 */
public class SpacerElementComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new SpacerElementRenderer();
	
	private String spacerCssClass;
	private final SpacerElementImpl element;
	
	public SpacerElementComponent(String name) {
		this(name, null);
	}
	
	public SpacerElementComponent(String name, SpacerElementImpl element) {
		super(name);
		this.element = element;
	}
	
	@Override
	public SpacerElementImpl getFormItem() {
		return element;
	}

	public String getSpacerCssClass() {
		return spacerCssClass;
	}

	public void setSpacerCssClass(String spacerCssClass) {
		this.spacerCssClass = spacerCssClass;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}