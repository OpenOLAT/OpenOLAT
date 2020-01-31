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

package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * Shows usage of progress bar controller
 * <P>
 * Initial Date:  31 jan 2020<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 */

public class GuiDemoProgressBarController extends BasicController {
	
	VelocityContainer mainVC;


	public GuiDemoProgressBarController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);		
		mainVC = createVelocityContainer("guidemo-progressBar");
		putInitialPanel(mainVC);		
		
		
		createProgressBar("base", 200, 100, 25, null, null, null, null, null);
		createProgressBar("base2", 300, 125, 40, null, "MB", LabelAlignment.right, null, null);
				
		createProgressBar("hor", 200, 100, 65, RenderStyle.horizontal, null, null, null, null);
		createProgressBar("rad", 200, 100, 65, RenderStyle.radial, null, null, null, null);
		createProgressBar("pie", 200, 100, 65, RenderStyle.pie, null, null, null, null);

		createProgressBar("hor-s", 200, 100, 65, RenderStyle.horizontal, null, null, RenderSize.small, null);
		createProgressBar("rad-s", 200, 100, 65, RenderStyle.radial, null, null, RenderSize.small, null);
		createProgressBar("pie-s", 200, 100, 65, RenderStyle.pie, null, null, RenderSize.small, null);

		createProgressBar("hor-l", 200, 100, 65, RenderStyle.horizontal, null, null, RenderSize.large, null);
		createProgressBar("rad-l", 200, 100, 65, RenderStyle.radial, null, null, RenderSize.large, null);
		createProgressBar("pie-l", 200, 100, 65, RenderStyle.pie, null, null, RenderSize.large, null);

		createProgressBar("hor-i", 200, 100, 65, RenderStyle.horizontal, null, null, RenderSize.inline, null);
		createProgressBar("rad-i", 200, 100, 65, RenderStyle.radial, null, null, RenderSize.inline, null);
		createProgressBar("pie-i", 200, 100, 65, RenderStyle.pie, null, null, RenderSize.inline, null);

	}

	private void createProgressBar(String name, int width, float max, float actual, 
			RenderStyle renderStyle, String unitLabel, LabelAlignment labelAlignment, RenderSize renderSize, Boolean percentagesEnabled) {
		ProgressBar progressBar = new ProgressBar(name);
		progressBar.setWidth(width);
		progressBar.setMax(max);		
		progressBar.setActual(actual);
		
		if (renderStyle != null) progressBar.setRenderStyle(renderStyle);
		if (unitLabel != null) progressBar.setUnitLabel(unitLabel);
		if (labelAlignment != null) progressBar.setLabelAlignment(labelAlignment);
		if (renderSize != null) progressBar.setRenderSize(renderSize);
		if (percentagesEnabled != null) progressBar.setPercentagesEnabled(percentagesEnabled);
		
		
		mainVC.put(progressBar.getComponentName(), progressBar);		
	}
	
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do
		
	}

	@Override
	protected void doDispose() {
		// Nothing to do
		
	}
}