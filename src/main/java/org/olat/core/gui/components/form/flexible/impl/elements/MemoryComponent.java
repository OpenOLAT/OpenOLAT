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

import java.lang.management.MemoryType;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 16.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemoryComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new MemoryRenderer();
	
	private final MemoryType memoryType;
	private final MemoryElementImpl element;
	
	public MemoryComponent(String name, MemoryType memoryType, MemoryElementImpl element) {
		super(name);
		this.memoryType = memoryType;
		this.element = element;
		setDomReplacementWrapperRequired(false);
	}
	
	@Override
	public FormItem getFormItem() {
		return element;
	}

	public MemoryType getMemoryType() {
		return memoryType;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
