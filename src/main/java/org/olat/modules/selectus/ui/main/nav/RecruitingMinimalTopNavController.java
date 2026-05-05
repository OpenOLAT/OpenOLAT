/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.main.nav;

import org.olat.core.commons.fullWebApp.DefaultMinimalTopNavController;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingMinimalTopNavController extends BasicController implements LockableController {

	private final Link closeLink;

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public RecruitingMinimalTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(DefaultMinimalTopNavController.class, ureq.getLocale()));
		VelocityContainer topNavVC = createVelocityContainer("topnavminimal");
		closeLink = LinkFactory.createLink("header.topnav.close", topNavVC, this);
		putInitialPanel(topNavVC);
	}
	
	@Override
	public void lock() {
		//
	}

	@Override
	public void unlock() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeLink) {
			// close window (a html page which calls Window.close onLoad
			ureq.getDispatchResult().setResultingMediaResource(
					new RedirectMediaResource(StaticMediaDispatcher.createStaticURIFor("closewindow.html")));
			// release all resources and close window
			WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
			Window w = wbo.getWindow();
			Windows.getWindows(ureq).deregisterWindow(w);
			wbo.dispose();
		}
	}
}
