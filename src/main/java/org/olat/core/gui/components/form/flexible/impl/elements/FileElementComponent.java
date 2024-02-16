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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
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
	
	@Override
	public FileElementImpl getFormItem(){
		return element;
	}
	
	protected ImageFormItem getInitialPreviewElementImpl() {
		return element.getInitialPreviewFormItem();
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		super.doDispatchRequest(ureq);
	}

	@Override
	public Component getComponent(String name) {
		for(FormItem item:element.getFormItems()) {
			if(item.getComponent().getComponentName().equals(name)) {
				return item.getComponent();
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>();
		for(FormItem item:element.getFormItems()) {
			cmps.add(item.getComponent());
		}
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
