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
package org.olat.core.gui.components.form.flexible.impl;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.control.Controller;
import org.olat.modules.IModuleConfiguration;

/**
 * Base class for implementing {@link IFormFragment}
 * 
 * <p>Initial date: May 6, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public abstract class BasicFormFragment implements IFormFragment {

	protected IFormFragmentHost host;
	protected IFormFragmentContainer container;
	
	public BasicFormFragment() {
		CoreSpringFactory.autowireObject(this);		
	}
	
	@Override
	public final void initFormFragment(UserRequest ureq, IFormFragmentContainer container, Controller listener, IModuleConfiguration config) {
		FormItemContainer formLayout = container.formItemsContainer();		
		
		this.container = container;
		this.host = container.getFragmentHostInterface();		
		
		initFormFragment(formLayout, listener, ureq, config);
	}

	protected abstract void initFormFragment(FormItemContainer formLayout, Controller listener, UserRequest ureq, IModuleConfiguration config);

	protected FormUIFactory uifactory() {
		return container.getFragmentHostInterface().getUIFactory();
	}

	@Override
	public void readConfiguration(UserRequest ureq, IModuleConfiguration moduleConfiguration) {
		// do nothing by default
	}
	
}
