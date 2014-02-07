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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti.process.AssessmentInstance;

/**
 * Initial Date:  Mar 3, 2004
 *
 * @author Mike Stock
 */
public class IQComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new IQComponentRenderer();

	private AssessmentInstance ai;
	private IQMenuDisplayConf mdc;
	private boolean provideMemoField;
	
	/**
	 * 
	 */
	public IQComponent(String name, Translator translator, AssessmentInstance ai, IQMenuDisplayConf menuDispConf, boolean provideMemoField) {
		super(name, translator);
		this.ai = ai;
		this.mdc = menuDispConf;
		this.provideMemoField = provideMemoField;
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		if (ureq.getParameter("cid") != null) {
			fireEvent(ureq, new Event(ureq.getParameter("cid")));
		}
	}

	/**
	 * @return
	 */
	public AssessmentInstance getAssessmentInstance() {
		return ai;
	}

	/**
	 * @return Returns the mdc.
	 */
	public IQMenuDisplayConf getMenuDisplayConf() {
		return mdc;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public boolean provideMemoField () {
		return provideMemoField;
	}
	
	/**
	 * Return true to force incrementing of the timestamp. This protect
	 * the test against back button which is not supported.
	 */
	@Override
	public boolean isSilentlyDynamicalCmp() {
		return true;
	}
}
