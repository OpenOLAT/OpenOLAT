/**
 * JGS goodsolutions GmbH<br>
 * http://www.goodsolutions.ch
 * <p>
 * This software is protected by the goodsolutions software license.<br>
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland.<br>
 * All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jfreechart;

import org.jfree.chart.JFreeChart;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * This is a simple Controller wrapper to JFreeChart charts. Drop in a JFreeChart chart, set its width
 * and height and the Controller's initial component will render the chart as a PNG.
 * 
 * For JFreeChart reference please see http://www.jfree.org/jfreechart/index.php
 * <P>
 * Initial Date:  18.04.2006 <br>
 *
 * @author Mike Stock
 */
public class JFreeChartController extends DefaultController {

	ImageComponent imgComponent;
	
	/**
	 * @param wControl
	 */
	public JFreeChartController(JFreeChart chart, Long height, Long width, WindowControl wControl) {
		super(wControl);
		imgComponent = new ImageComponent("jfreechartwrapper");
		imgComponent.setWidth(width);
		imgComponent.setHeight(height);
		imgComponent.setMediaResource(new JFreeChartMediaResource(chart, width, height));
		setInitialComponent(imgComponent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do...

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to do...
	}

}
