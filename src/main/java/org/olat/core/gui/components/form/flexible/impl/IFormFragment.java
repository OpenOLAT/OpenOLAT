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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.modules.IModuleConfiguration;

/**
 * A form fragment is a simple abstraction that allows composing {@code Form} instances from
 * reusable groups of fields. This is a lighter weight level of reuse than to embed entire 
 * forms inside containing forms. In the case of fragments, there is a single controller 
 * managing multiple fragments.
 * 
 * <p>Fragments are hosted by a {@link IFormFragmentContainer}.
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public interface IFormFragment extends Disposable {

	// this is here for compatibility to help migrate chunks of existing code into fragments
	default void initForm(IFormFragmentContainer container, Controller _listener, UserRequest ureq, IModuleConfiguration config) {
		this.initFormFragment(ureq, container, _listener, config);
		this.validateFormLogic(ureq);
	}
	
	/**
	 * This method must be implemented by fragments however it is not recommended that form 
	 * fragment hosts call it directly. Instead fragment hosts should directly call {@code initForm}
	 * which will ensure that all initialization is performed in a single step
	 * 
	 * @param ureq
	 * @param formLayout
	 * @param listener
	 */
	void initFormFragment(UserRequest ureq, IFormFragmentContainer container, Controller listener, IModuleConfiguration config);

	/**
	 * @see FormBasicController#validateFormLogic(UserRequest)
	 * @return
	 */
	boolean validateFormLogic(UserRequest ureq);

	void readConfiguration(UserRequest ureq, IModuleConfiguration moduleConfiguration);
	void storeConfiguration(UserRequest ureq, IModuleConfiguration moduleConfiguration);

	default void refreshContents() {
		// do nothing by default
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 * @return  true if the event was processed, false otherwise
	 */
	default boolean processEvent(UserRequest ureq, Controller source, Event event) {
		return false;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 * @return  true if the event was processed, false otherwise
	 */
	default boolean processEvent(UserRequest ureq, Component source, Event event) {
		return false;
	}
	
	/**
	 * called if an element inside of form triggered an event
	 * 
	 * @param source
	 * @param event
	 * @return  true if the event was processed, false otherwise
	 */
	default boolean processFormEvent(UserRequest ureq, FormItem source, FormEvent event) {
		return false;
	}

}
