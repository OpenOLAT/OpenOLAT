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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.components.delegating;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Event;

/**
 * Description:<BR>
 * Possibility to implement a Component by providing a component renderer
 * <P>
 * Initial Date: Jun 8, 2005
 * 
 * @author Felix Jost
 */
public class DelegatingComponent extends Component {
	/**
	 * Comment for <code>FORWARDED</code> : the only event fired by this
	 * component
	 */
	public static final Event FORWARDED = new Event("forwarded");

	private final ComponentRenderer delegateRenderer;

	/**
	 * @param name
	 * @param delegateRenderer
	 */
	public DelegatingComponent(String name, ComponentRenderer delegateRenderer) {
		super(name);
		this.delegateRenderer = delegateRenderer;
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		// forward -all- dispatch request to listeners
		fireEvent(ureq, FORWARDED);
	}

	/**
	 * @return the renderer (method used by the renderer only)
	 */
	public ComponentRenderer getDelegateRenderer() {
		return delegateRenderer;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return delegateRenderer;
	}
}
