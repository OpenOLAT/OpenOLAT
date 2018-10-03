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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.ceditor.ui.model.PageFragment;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageFragmentsComponent extends AbstractComponent implements ComponentCollection  {
	
	private static final PageFragmentsComponentRenderer RENDERER = new PageFragmentsComponentRenderer();
	
	private List<? extends PageFragment> fragments;
	
	public PageFragmentsComponent(String name) {
		super(name);
	}

	public List<PageFragment> getFragments() {
		if(fragments == null) {
			return new ArrayList<>(1);
		}
		return new ArrayList<>(fragments);
	}

	public void setFragments(List<? extends PageFragment> fragments) {
		this.fragments = fragments;
		setDirty(true);
	}

	@Override
	public Component getComponent(String name) {
		List<PageFragment> fragmentList = getFragments();
		for(PageFragment fragment:fragmentList) {
			if(fragment.getComponent().getComponentName().equals(name)) {
				return fragment.getComponent();
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<PageFragment> fragmentList = getFragments();
		List<Component> components = new ArrayList<>(fragmentList.size());
		for(PageFragment fragment:fragmentList) {
			components.add(fragment.getComponent());
		}
		return components;
	}
	
	public boolean validateElements(UserRequest ureq, List<ValidationMessage> messages) {
		boolean allOk = true;
		List<PageFragment> fragmentList = getFragments();
		for(PageFragment fragment:fragmentList) {
			allOk &= fragment.getPageRunElement().validate(ureq, messages);
		}
		return allOk;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
