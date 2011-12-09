/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * This software is protected by the OLAT software license.<br>
 * Use is subject to license terms.<br>
 * See LICENSE.TXT in this distribution for details.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch
 *
 * All rights reserved.
 * <p>
 */
package ch.goodsolutions.demo.poll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.controller.SourceViewController;

public class PollDemoController extends BasicController {
	private VelocityContainer mainVC;
	private Link appearLater;
	
	private Panel updatePanel;
	private Link msgLink;
	
	public PollDemoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("index");
		
		updatePanel = new Panel("updater");
		mainVC.put("updater", updatePanel);
		
		// TODO felix -> put in a factory?
		JSAndCSSComponent jsc = new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 2000);
		mainVC.put("updatecontrol", jsc);
		
		msgLink = LinkFactory.createButton("link.message", mainVC, this);
		
		// just a no operation link 
		LinkFactory.createButton("link.noop", mainVC, this);
			
		// prepare for polling.
		// create a html fragment
		final VelocityContainer updateVc = createVelocityContainer("update");
		updateVc.contextPut("msg", "0");
		// set it into a panel
		updatePanel.setContent(updateVc);
		appearLater = LinkFactory.createButtonXSmall("appearLater", null, this);
		
		// hold the windowcontrol
		final WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
		Thread t = new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<20; i++) {
					final int j = i;
					wbo.invokeLater(new Runnable(){
						public void run() {
							updateVc.contextPut("msg", ""+j);							
						}
					});
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						//
					}
				}
				updatePanel.setContent(appearLater);
				
			}});
		t.setDaemon(true);
		t.start();
		
		//add source view control
		Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), mainVC);
		listenTo(sourceview);

		mainVC.put("sourceview", sourceview.getInitialComponent());
		putInitialPanel(mainVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == appearLater) {
			getWindowControl().setInfo("well here we go: 21 and finally, the answer is always 42");
		} else if (source == msgLink) {
			showInfo("testmsg", null);
		}
	}

	protected void doDispose() {
		//
	}

}
