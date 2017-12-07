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
package org.olat.ims.qti21.ui.components;


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController.QtiWorksStatus;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTimerComponent extends FormBaseComponentImpl {

	private static final AssessmentTimerComponentRenderer RENDERER = new AssessmentTimerComponentRenderer();
	
	private AssessmentTimerFormItem item;
	
	private final QtiWorksStatus qtiWorksStatus;
	
	public AssessmentTimerComponent(String name, QtiWorksStatus qtiWorksStatus, AssessmentTimerFormItem item) {
		super(name);
		this.item = item;
		this.qtiWorksStatus = qtiWorksStatus;
	}
	
	public AssessmentTimerFormItem getFormItem() {
		return item;
	}

	public QtiWorksStatus getQtiWorksStatus() {
		return qtiWorksStatus;
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
