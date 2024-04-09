/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * Shows usage of BoxPlot component. Will render statistical date in a very
 * compact form, normalized to a give scale/width:
 * <p>
 * Minimum measured value (smallest sample)<br>
 * Maximum measured value (largest sample)<br>
 * Average<br>
 * <p>
 * If you have enough data samples (>10 samples recommended for meaning full effect)
 * First quartile<br>
 * Third quartile<br>
 * Median<br>
 * <P>
 * Initial Date: 09 April 2024<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */

public class GuiDemoBoxPlotController extends BasicController {
	
	VelocityContainer mainVC;


	public GuiDemoBoxPlotController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);		             
		mainVC = createVelocityContainer("guidemo-boxPlot");
		putInitialPanel(mainVC);		

		
		BoxPlot simple = new BoxPlot("simple", 160, 10, 100, 70, null);
		mainVC.put(simple.getComponentName(), simple);	

		BoxPlot wider = new BoxPlot("wider", 160, 10, 100, 70, null);
		mainVC.put(wider.getComponentName(), wider);	
		wider.setWidth(250);

		BoxPlot responsive = new BoxPlot("responsive", 160, 10, 100, 70, null);
		responsive.setResponsiveScaling(true);
		mainVC.put(responsive.getComponentName(), responsive);	

		BoxPlot larger = new BoxPlot("larger", 500, 10, 100, 70, null);
		mainVC.put(larger.getComponentName(), larger);	

		BoxPlot largerWider = new BoxPlot("largerWider", 500, 10, 100, 70, null);
		largerWider.setWidth(500);
		mainVC.put(largerWider.getComponentName(), largerWider);	
		
		BoxPlot color1 = new BoxPlot("color1", 160, 10, 100, 70, "o_rubric_insufficient");
		mainVC.put(color1.getComponentName(), color1);	
		BoxPlot color2 = new BoxPlot("color2", 160, 10, 100, 70, "o_rubric_neutral");
		mainVC.put(color2.getComponentName(), color2);	
		BoxPlot color3 = new BoxPlot("color3", 160, 10, 100, 70, "o_rubric_sufficient");
		mainVC.put(color3.getComponentName(), color3);	
		
		
		BoxPlot full1 = new BoxPlot("full1", 160, 10, 140, 30, 50, 90, 85, "o_rubric_insufficient");
		mainVC.put(full1.getComponentName(), full1);	

		BoxPlot full2 = new BoxPlot("full2", 250, 20, 200, 120, 160, 190, 165, "o_rubric_neutral");
		full2.setWidth(250);
		mainVC.put(full2.getComponentName(), full2);	

		BoxPlot full3 = new BoxPlot("full3", 160, 45, 150, 90, 80, 140, 100, "o_rubric_sufficient");
		full3.setResponsiveScaling(true);;
		mainVC.put(full3.getComponentName(), full3);	
		

		BoxPlot edge1 = new BoxPlot("edge1", 160, 0, 160, 80, null);
		mainVC.put(edge1.getComponentName(), edge1);	

		BoxPlot edge2 = new BoxPlot("edge2", 160, 0, 160, 80, 0, 160, 80, null);
		mainVC.put(edge2.getComponentName(), edge2);	
		
		BoxPlot edge3 = new BoxPlot("edge3", 160, 0, 5, 2, null);
		mainVC.put(edge3.getComponentName(), edge3);	

		BoxPlot edge4 = new BoxPlot("edge4", 160, 0, 120, 60, 0, 100, 40, null);
		mainVC.put(edge4.getComponentName(), edge4);	

		BoxPlot edge5 = new BoxPlot("edge5", 160, 35, 35, 35, null);
		mainVC.put(edge5.getComponentName(), edge5);	

	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do
	}
}