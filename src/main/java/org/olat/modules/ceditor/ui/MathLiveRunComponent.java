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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.math.MathLiveComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.model.MathElement;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveRunComponent extends PageRunComponent {
	
	public MathLiveRunComponent(MathLiveComponent component) {
		super(component);
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if((source instanceof ModalInspectorController || source instanceof PageElementEditorController)
				&& event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			PageElement element = cpe.getElement();
			if(element instanceof MathElement) {
				MathElement math = (MathElement)element;
				((MathLiveComponent)getComponent()).setValue(math.getContent());
			}
		}
	}
}
