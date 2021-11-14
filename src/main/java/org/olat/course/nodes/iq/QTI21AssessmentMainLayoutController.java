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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;

/**
 * 
 * The assessment test display has its own main layout. This is a thin
 * wrapper which implements the MainLayoutController interface.
 * 
 * Initial date: 22 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentMainLayoutController extends MainLayoutBasicController {
	
	private ChiefController thebaseChief;
	private final StackedPanel mainPanel;
	
	private boolean fullScreen;
	
	public QTI21AssessmentMainLayoutController(UserRequest ureq, WindowControl wControl, AssessmentTestDisplayController controller) {
		super(ureq, wControl);
		mainPanel = putInitialPanel(controller.getInitialComponent());
	}
	
	/**
	 * The Controller to be set on the mainPanel in case of disposing this layout
	 * controller.
	 * 
	 * @param disposedMessageControllerOnDipsose
	 */
	public void setDisposedMessageController(Controller disposedMessageControllerOnDipsose) {
		setDisposedMsgController(disposedMessageControllerOnDipsose);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public void setAsFullscreen(UserRequest ureq) {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			thebaseChief = cc;
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			thebaseChief.getScreenMode().setMode(Mode.full, businessPath);
		} else {
			Windows.getWindows(ureq).setFullScreen(Boolean.TRUE);
		}
		fullScreen = true;
	}
	
	@SuppressWarnings("deprecation")
	public void activate() {
		getWindowControl().pushToMainArea(mainPanel);
	}
	
	public void deactivate(UserRequest ureq) {
		getWindowControl().pop();
		if (fullScreen) {
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			if(thebaseChief != null) {
				thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
			} else if (ureq != null){
				ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
				if (cc != null) {
					thebaseChief = cc;
					thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
				}
			}
		}
	}
}