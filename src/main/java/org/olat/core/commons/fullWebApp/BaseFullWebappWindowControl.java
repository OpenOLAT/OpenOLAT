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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.WindowControlInfoImpl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;

/**
 * 
 * Initial date: 17.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class BaseFullWebappWindowControl implements WindowControl {
	private final WindowControlInfo wci;
	private final WindowBackOffice wbo;
	private final BaseFullWebappController webappCtrl;
	
	private Component fullScreenCmp;

	public BaseFullWebappWindowControl(BaseFullWebappController webappCtrl, WindowBackOffice wbo) {
		this.wbo = wbo;
		this.webappCtrl = webappCtrl;
		wci = new WindowControlInfoImpl(webappCtrl, null);
	}

	@Override
	public void pushToMainArea(Component newMainArea) {
		webappCtrl.getCurrentGuiStack().pushContent(newMainArea);
	}

	@Override
	public void pushAsModalDialog(Component newModalDialog) {
		webappCtrl.getCurrentGuiStack().pushModalDialog(newModalDialog);
	}

	@Override
	public boolean removeModalDialog(Component comp) {
		return webappCtrl.getCurrentGuiStack().removeModalDialog(comp);
	}

	@Override
	public void pushAsTopModalDialog(Component comp) {
		webappCtrl.getCurrentGuiStack().pushTopModalDialog(comp);
	}

	@Override
	public boolean removeTopModalDialog(Component comp) {
		return webappCtrl.getCurrentGuiStack().removeTopModalDialog(comp);
	}

	@Override
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings) {
		webappCtrl.getCurrentGuiStack().pushCallout(comp, targetId, settings);
	}

	@Override
	public void pushFullScreen(Controller ctrl, String bodyClass) {
		ChiefController cc = getWindowBackOffice().getChiefController();
		String businessPath = ctrl.getWindowControlForDebug().getBusinessControl().getAsString();
		cc.getScreenMode().setMode(Mode.full, businessPath, bodyClass);
		fullScreenCmp = ctrl.getInitialComponent();
		webappCtrl.getCurrentGuiStack().pushContent(fullScreenCmp);
	}

	@Override
	public void pop() {
		// reactivate latest dialog from stack, dumping current one
		Component popedComponent = webappCtrl.getCurrentGuiStack().popContent();
		if(popedComponent != null && popedComponent == fullScreenCmp) {
			String businessPath = getBusinessControl().getAsString();
			ChiefController cc = getWindowBackOffice().getChiefController();
			cc.getScreenMode().setMode(Mode.standard, businessPath);
			fullScreenCmp = null;
		}
	}

	@Override
	public void setInfo(String info) {
		webappCtrl.getGUIMessage().setInfo(info);
		webappCtrl.getGUIMsgPanel().setContent(webappCtrl.getGUIMsgVc());

		// setInfo is called input guimsgPanel into the correct place
	}

	@Override
	public void setError(String error) {
		webappCtrl.getGUIMessage().setError(error);
		webappCtrl.getGUIMsgPanel().setContent(webappCtrl.getGUIMsgVc());
	}

	@Override
	public void setWarning(String warning) {
		webappCtrl.getGUIMessage().setWarn(warning);
		webappCtrl.getGUIMsgPanel().setContent(webappCtrl.getGUIMsgVc());
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return wci;
	}

	@Override
	public void makeFlat() {
		throw new AssertException("should never be called!");
	}

	@Override
	public BusinessControl getBusinessControl() {
		return BusinessControlFactory.getInstance().getEmptyBusinessControl();
	}

	@Override
	public WindowBackOffice getWindowBackOffice() {
		return wbo;
	}
}
