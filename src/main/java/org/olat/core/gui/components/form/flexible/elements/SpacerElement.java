/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * Implements an HTML horizontal bar (&lt;HR&gt;) element.
 * 
 * @author twuersch
 */
public interface SpacerElement extends FormItem {

	/**
	 * Set an optional css class for the spacer element
	 * 
	 * @param spacerCssClass
	 */
	public void setSpacerCssClass(String spacerCssClass);

	/**
	 * Get an optional css class for the spacer element or null if not defined
	 * 
	 * @return
	 */
	public String getSpacerCssClass();

}
