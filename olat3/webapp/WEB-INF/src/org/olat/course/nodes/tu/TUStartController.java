package org.olat.course.nodes.tu;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ModuleConfiguration;

public class TUStartController extends BasicController {
	
	private VelocityContainer runVC;
	
	public TUStartController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		
		runVC = createVelocityContainer("run");
		
		URL url = null;
		try {
			url = new URL((String)  config.get(TUConfigForm.CONFIGKEY_PROTO), 
										(String)  config.get(TUConfigForm.CONFIGKEY_HOST),
									 ((Integer) config.get(TUConfigForm.CONFIGKEY_PORT)).intValue(),
										(String)  config.get(TUConfigForm.CONFIGKEY_URI));
		} catch (MalformedURLException e) {
			// this should not happen since the url was already validated in edit mode
			runVC.contextPut("url", "");
		}
		if (url != null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(url.toString());
			// since the url only includes the path, but not the query (?...), append it here, if any
			String query = (String)config.get(TUConfigForm.CONFIGKEY_QUERY);
			if (query != null) {
				sb.append("?");
				sb.append(query);
			}
			runVC.contextPut("url", sb.toString());
		}
		
		putInitialPanel(runVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
