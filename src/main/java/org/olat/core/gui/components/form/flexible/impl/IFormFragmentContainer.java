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

import java.util.function.Consumer;

import org.olat.core.gui.components.form.flexible.FormItemContainer;

/**
 * Interface implemented by a visual element (usually a Form controller) that can contain
 * one or more {@link IFormFragment} instances 
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public interface IFormFragmentContainer {

	/**
	 * Return an instance of the interface used 
	 * @return
	 */
	default IFormFragmentHost getFragmentHostInterface() {
		return null;
	}
	
	/**
	 * Called by a hosted fragment to tell its container that the current presentation
	 * no longer accurately reflects the current internal state of the fragment. The 
	 * container would generally use this information to trigger a re-evaluation of its
	 * layout
	 */
	void setNeedsLayout();

	/**
	 * Used by the fragment in order to add contents to the visual presentation of the
	 * container 
	 * 
	 * @return
	 */
	FormItemContainer formItemsContainer();

	/**
	 * Used for registering the existance of a fragment with the underlying form controller
	 * 
	 * @param fragment
	 */
	void registerFormFragment(IFormFragment fragment);

	/**
	 * This method can be used to perform an operation on all the fragments currently hosted
	 * by this container
	 * 
	 * @param handler
	 */
	void forEachFragment(Consumer<IFormFragment> handler);
	
}
