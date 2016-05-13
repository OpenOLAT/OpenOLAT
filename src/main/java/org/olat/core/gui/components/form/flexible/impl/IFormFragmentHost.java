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

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.translator.Translator;

/**
 * The interface implemented by a {@link IFormFragmentContainer} to provide the {@link IFormFragment}
 * with controller over its runtime behavior. 
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public interface IFormFragmentHost {

	/**
	 * Return a {@link Translator} instance that can provide a translation specific to where the
	 * fragment is used, or a default translation associated with the fragment itself.
	 * 
	 * @return
	 */
	Translator getFragmentTranslator();

	/**
	 * Return a {@link FormUIFactory} instance that may have been tweaked to enforce certain 
	 * behavior specific to where the fragment is being embedded
	 * @return
	 */
	default FormUIFactory getUIFactory() {
		return FormUIFactory.getInstance();
	}

	/**
	 * Return the controller instance to be used by the fragment for event handling
	 * 
	 * @return
	 */
	IFormFragmentController getFragmentController();

}
