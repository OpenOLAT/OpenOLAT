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
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
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
		
		
		createProgressBar("base", 200, 100, 25, null, null, null, null, null, null, null, null);
		createProgressBar("base2", 200, 125, 40, "Disk usage", "MB", LabelAlignment.right, true, null, null, null, null);
		createProgressBar("base3", 200, 125, 40, "Disk usage", "MB", LabelAlignment.left, true, null, null, null, null);
		createProgressBar("base4", 200, 125, 40, "Disk usage", "MB", LabelAlignment.none, true, null, null, null, null);
		createProgressBar("base5", 200, 125, 40, "Disk usage", "MB", LabelAlignment.none, false, null, null, null, null);
		
		
		createProgressBar("base-long", 300, 125, 40, "Disk usage", "MB", LabelAlignment.left, true, null, null, null, null);
		ProgressBar progressBar = createProgressBar("base-percent", 50, 125, 40, "Disk usage", "MB", LabelAlignment.right, true, null, null, null, null);
		progressBar.setWidthInPercent(true);
				
		createProgressBar("hor", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, null, null);
		createProgressBar("rad", 200, 100, 65, null, null, null, null, RenderStyle.radial, null, null, null);
		createProgressBar("pie", 200, 100, 65, null, null, null, null, RenderStyle.pie, null, null, null);

		createProgressBar("hor-s", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, RenderSize.small, null, null);
		createProgressBar("rad-s", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, null, null);
		createProgressBar("pie-s", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, null, null);

		createProgressBar("hor-l", 200, 100, 65, null, null, null, null, RenderStyle.horizontal,  RenderSize.large, null, null);
		createProgressBar("rad-l", 200, 100, 65, null, null, null, null, RenderStyle.radial,  RenderSize.large, null, null);
		createProgressBar("pie-l", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.large, null, null);

		createProgressBar("hor-i", 200, 100, 65, null, null, LabelAlignment.none, false, RenderStyle.horizontal, RenderSize.inline, null, null);
		createProgressBar("rad-i", 200, 100, 65, null, null, LabelAlignment.none, false, RenderStyle.radial, RenderSize.inline, null, null);
		createProgressBar("pie-i", 200, 100, 65, null, null, LabelAlignment.none, false, RenderStyle.pie, RenderSize.inline, null, null);

		createProgressBar("hor-i2", 200, 100, 65, "Disk usage", "MB", LabelAlignment.right, false, RenderStyle.horizontal, RenderSize.inline, null, null);
		createProgressBar("rad-i2", 200, 100, 65, "Disk usage", "MB", LabelAlignment.right, false, RenderStyle.radial, RenderSize.inline, null, null);
		createProgressBar("pie-i2", 200, 100, 65, "Disk usage", "MB", LabelAlignment.right, false, RenderStyle.pie, RenderSize.inline, null, null);

		
		createProgressBar("hor-ci", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, BarColor.info, null);
		createProgressBar("hor-cs", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, BarColor.success, null);
		createProgressBar("hor-cw", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, BarColor.warning, null);
		createProgressBar("hor-cd", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, BarColor.danger, null);
		createProgressBar("rad-ci", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, BarColor.info, null);
		createProgressBar("rad-cs", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, BarColor.success, null);
		createProgressBar("rad-cw", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, BarColor.warning, null);
		createProgressBar("rad-cd", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, BarColor.danger, null);
		createProgressBar("pie-ci", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, BarColor.info, null);
		createProgressBar("pie-cs", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, BarColor.success, null);
		createProgressBar("pie-cw", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, BarColor.warning, null);
		createProgressBar("pie-cd", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, BarColor.danger, null);

		createProgressBar("hor-anim", 200, 100, 65, null, null, null, null, RenderStyle.horizontal, null, null, true);
		createProgressBar("rad-anim", 200, 100, 65, null, null, null, null, RenderStyle.radial, RenderSize.small, null, true);
		createProgressBar("pie-anim", 200, 100, 65, null, null, null, null, RenderStyle.pie, RenderSize.small, null, true);
		
	}

	private ProgressBar createProgressBar(String name, int width, float max, float actual, 
			String info, String unitLabel, LabelAlignment labelAlignment, Boolean percentagesEnabled, 
			RenderStyle renderStyle, RenderSize renderSize, BarColor barColor, Boolean animationEnabled) {
		
		ProgressBar progressBar = new ProgressBar(name);
		progressBar.setWidth(width);
		progressBar.setMax(max);		
		progressBar.setActual(actual);

		if (info != null) progressBar.setInfo(info);
		if (unitLabel != null) progressBar.setUnitLabel(unitLabel);
		if (labelAlignment != null) progressBar.setLabelAlignment(labelAlignment);
		if (percentagesEnabled != null) progressBar.setPercentagesEnabled(percentagesEnabled);

		if (renderStyle != null) progressBar.setRenderStyle(renderStyle);
		if (renderSize != null) progressBar.setRenderSize(renderSize);

		if (barColor != null) progressBar.setBarColor(barColor);
		if (animationEnabled != null) progressBar.setProgressAnimationEnabled(animationEnabled);

		mainVC.put(progressBar.getComponentName(), progressBar);	
		return progressBar;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do
	}
}