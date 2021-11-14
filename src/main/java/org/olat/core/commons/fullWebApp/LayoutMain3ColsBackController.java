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
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController;

/**
 * <h3>Description:</h3> This layouter controller provides a fullscreen view
 * with a back-link
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>Event.BACK_EVENT</li>
 * </ul>
 * Initial Date: 21.01.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class LayoutMain3ColsBackController extends MainLayoutBasicController implements MainLayout3ColumnsController {

	private LayoutMain3ColsController layoutCtr;
	private VelocityContainer backVC;
	private Link backLink;
	private boolean fullScreen = false;
	private boolean deactivateOnBack = true;

	private ChiefController thebaseChief;

	/**
	 * Constructor for creating a 3 col based menu on the main area
	 * 
	 * @param ureq
	 * @param wControl
	 * @param col1
	 *            usually the left column
	 * @param col2
	 *            usually the right column
	 * @param col3
	 *            usually the content column
	 * @param layoutConfigKey
	 *            identificator for this layout to persist the users column
	 *            width settings
	 */
	public LayoutMain3ColsBackController(UserRequest ureq, WindowControl wControl, Component col1, Component col3,
			String layoutConfigKey) {
		super(ureq, wControl);

		// create a wrapper velocity container that contains the back link and
		// normal main layout
		backVC = createVelocityContainer("main_back");

		// create layout and add it to main view
		layoutCtr = new LayoutMain3ColsController(ureq, wControl, col1, col3, layoutConfigKey);
		listenTo(layoutCtr);
		backVC.put("3collayout", layoutCtr.getInitialComponent());

		// create close link
		backLink = LinkFactory.createLinkBack(backVC, this);
		// finish: use wrapper as view
		putInitialPanel(backVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			// remove the preview workflow from the stack and notify listeners
			// about the back click
			if(this.deactivateOnBack) {
				deactivate();
			}
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}

	@Override
	public Component getInitialComponent() {
		throw new RuntimeException("please use activate() instead");
	}

	/**
	 * Activate this back workflow
	 */
	public void activate() {
		getWindowControl().pushToMainArea(backVC);
	}
	
	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setAsFullscreen() {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			thebaseChief = cc;
			thebaseChief.getScreenMode().setMode(Mode.full, null);
		}
		fullScreen = true;
	}

	/**
	 * Deactivates back controller. Please do use this method here instead of
	 * getWindowControl().pop() !
	 */
	public void deactivate() {
		getWindowControl().pop();
		if (fullScreen && thebaseChief != null) {
			thebaseChief.getScreenMode().setMode(Mode.standard, null);
		}
	}

	@Override
	protected void doDispose() {
		// child controller autodisposed
		thebaseChief = null;
        super.doDispose();
	}
	
	public boolean isDeactivateOnBack() {
		return deactivateOnBack;
	}

	public void setDeactivateOnBack(boolean deactivateOnBack) {
		this.deactivateOnBack = deactivateOnBack;
	}

	//
	// Methods from the 3 col layout:
	//
	
	@Override
	public void hideCol1(boolean hide) {
		this.layoutCtr.hideCol1(hide);
	}

	@Override
	public void hideCol2(boolean hide) {
		this.layoutCtr.hideCol2(hide);
	}

	@Override
	public void hideCol3(boolean hide) {
		// ignore this: col3 is mandatory
	}

	@Override
	public void setCol1(Component col1Component) {
		this.layoutCtr.setCol1(col1Component);
	}

	@Override
	public void setCol2(Component col2Component) {
		this.layoutCtr.setCol2(col2Component);
	}

	@Override
	public void setCol3(Component col3Component) {
		this.layoutCtr.setCol3(col3Component);
	}

	@Override
	public void addCssClassToMain(String cssClass) {
		this.layoutCtr.addCssClassToMain(cssClass);
	}

	@Override
	public void addDisposableChildController(Controller toBedisposedControllerOnDispose) {
		this.layoutCtr.addDisposableChildController(toBedisposedControllerOnDispose);
	}

	@Override
	public void removeCssClassFromMain(String cssClass) {
		this.layoutCtr.removeCssClassFromMain(cssClass);
	}

}