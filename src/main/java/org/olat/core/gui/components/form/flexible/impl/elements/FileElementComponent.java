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

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.image.ImageFormItem;

/**
 * Initial Date: 08.12.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class FileElementComponent extends FormBaseComponentImpl implements ComponentCollection {

	private static final ComponentRenderer RENDERER = new FileElementRenderer();
	private final FileElementImpl element;

	public FileElementComponent(FileElementImpl element) {
		super(element.getName());
		this.element = element;
	}
	
	protected FileElementImpl getFileElementImpl(){
		return element;
	}
	
	protected ImageFormItem getPreviewElementImpl() {
		return element.getPreviewFormItem();
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		super.doDispatchRequest(ureq);
	}

	@Override
	public Component getComponent(String name) {
		if(element.getPreviewFormItem() != null &&
				element.getPreviewFormItem().getComponent().getComponentName().equals(name)) {
			return element.getPreviewFormItem().getComponent();
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		if(element.getPreviewFormItem() == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(element.getPreviewFormItem().getComponent());
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
