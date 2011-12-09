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
package org.olat.core.gui.control.generic.closablewrapper;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * wrapper for the old/new closeable modal controllers
 * 
 * <P>
 * Initial Date:  20.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CloseableModalWindowWrapperController extends CloseableModalWindowController {

	private final String title;
	private final Component modalContent;
	private CloseableModalController fallbackController;
	
	public CloseableModalWindowWrapperController(UserRequest ureq, WindowControl wControl, String title, Component modalContent, String id) {
		super(ureq, wControl, title, modalContent, id);
		
		this.title = title;
		this.modalContent = modalContent;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == fallbackController) {
			fireEvent(ureq, event);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	public void activate() {
		boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		if (ajaxOn) {
			super.activate();
		} else {
			fallbackController = new CloseableModalController(getWindowControl(), title, modalContent);
			((CloseableModalController)fallbackController).activate();
			listenTo(fallbackController);
		}
	}

	@Override
	public void deactivate() {
		if(fallbackController != null) {
			fallbackController.deactivate();
			removeAsListenerAndDispose(fallbackController);
			fallbackController = null;
		} else {
			super.deactivate();
		}
	}
	
	
}
