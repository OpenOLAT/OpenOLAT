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
package org.olat.modules.ceditor.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.FormMultipartItem;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.ui.model.PageFragment;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;


/**
 * 
 * Initial date: 17 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageFragmentsElementImpl extends FormItemImpl implements FormItemCollection  {
	
	private final PageFragmentsComponent component;
	
	public PageFragmentsElementImpl(String name) {
		super(name);
		component = new PageFragmentsComponent(name);
	}
	
	public void setFragments(List<? extends PageFragment> fragments) {
		component.setFragments(fragments);
		rootFormAvailable();
		
		forEachEvaluationFormExecutionElement(execElement -> {
			if(execElement.hasFormItem() && execElement.getFormItem() instanceof FormMultipartItem) {
				getRootForm().setMultipartEnabled(true);
			}
		});
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<? extends PageFragment> fragments = component.getFragments();
		List<FormItem> items = new ArrayList<>(fragments.size());
		forEachEvaluationFormExecutionElement(execElement -> {
			if(execElement.hasFormItem()) {
				items.add(execElement.getFormItem());
			}
		});
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		List<? extends PageFragment> fragments = component.getFragments();
		for(PageFragment fragment:fragments) {
			PageRunElement runEl = fragment.getPageRunElement();
			if(fragment.getComponentName().equals(name) && runEl instanceof EvaluationFormExecutionElement) {
				EvaluationFormExecutionElement execEl = (EvaluationFormExecutionElement)runEl;
				if(execEl.hasFormItem()) {
					return execEl.getFormItem();
				}
			}
		}
		return null;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected void rootFormAvailable() {
		forEachEvaluationFormExecutionElement(execElement -> {
			if(execElement.hasFormItem()) {
				rootFormAvailable(execElement.getFormItem());
			}
		});
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && getRootForm() != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}
	
	private void forEachEvaluationFormExecutionElement(Consumer<EvaluationFormExecutionElement> consumer) {
		List<? extends PageFragment> fragments = component.getFragments();
		if(fragments != null) {
			for(PageFragment fragment:fragments) {
				if(fragment.getPageRunElement() instanceof EvaluationFormExecutionElement) {
					EvaluationFormExecutionElement execEl = (EvaluationFormExecutionElement)fragment.getPageRunElement();
					consumer.accept(execEl);
				}
			}
		}
	}
}
