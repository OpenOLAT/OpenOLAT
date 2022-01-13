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

/**
 * 
 * Initial date: 5 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AutoCompletionMultiSelectionComponent extends FormBaseComponentImpl implements ComponentCollection {
	
	private static final AutoCompletionMultiSelectionRenderer RENDERER = new AutoCompletionMultiSelectionRenderer();
	
	private final AutoCompletionMultiSelectionImpl autoCompletionMultiSelection;
	
	public AutoCompletionMultiSelectionComponent(String name, AutoCompletionMultiSelectionImpl autoCompleter) {
		super(name);
		this.autoCompletionMultiSelection = autoCompleter;
	}

	public AutoCompletionMultiSelectionImpl getAutoCompletionMultiSelection() {
		return autoCompletionMultiSelection;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public Component getComponent(String name) {
		FormItem item = autoCompletionMultiSelection.getFormComponent(name);
		return item == null ? null : item.getComponent();
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmpList = new ArrayList<>();
		for(FormItem item:autoCompletionMultiSelection.getFormItems()) {
			cmpList.add(item.getComponent());
		}
		return cmpList;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
