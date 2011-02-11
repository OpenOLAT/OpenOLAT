/**
 * 
 */
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;

/**
 * @author patrickb
 * 
 */
public class BaseFullWebappPopupBrowserWindow extends BaseFullWebappController implements PopupBrowserWindowController {

	/**
	 * @param ureq
	 * @param ouisc_wControl
	 * @param baseFullWebappControllerParts
	 */
	public BaseFullWebappPopupBrowserWindow(UserRequest ureq, WindowControl wControl,
			BaseFullWebappControllerParts baseFullWebappControllerParts) {
		super(ureq, wControl, baseFullWebappControllerParts);
		// apply custom css if available
		if (contentCtrl != null && contentCtrl instanceof MainLayoutController) {
			MainLayoutController mainLayoutCtr = (MainLayoutController) contentCtrl;
			addCurrentCustomCSSToView(mainLayoutCtr.getCustomCSS());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.control.generic.popup.PopupBrowserWindow#open(org.olat
	 * .core.gui.UserRequest)
	 */
	public void open(UserRequest ureq) {
		ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
	}

	/**
	 * @see org.olat.core.gui.control.generic.popup.PopupBrowserWindow#getPopupWindowControl()
	 */
	public WindowControl getPopupWindowControl() {
		return getWindowControl();
	}

}
