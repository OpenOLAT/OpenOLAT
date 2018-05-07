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
package org.olat.modules.forms.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.ui.model.BarSeriesDataSource;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BarChartController extends BasicController {
	
	private VelocityContainer mainVC;

	public BarChartController(UserRequest ureq, WindowControl wControl, BarSeriesDataSource dataSource) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("bar_chart");
		
		BarChartComponent chart = new BarChartComponent("o_eve_bc_" + CodeHelper.getRAMUniqueID());
		chart.setYLegend(translate("chart.count"));
		chart.addSeries(dataSource.getBarSeries());
		mainVC.put("chart", chart);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final static class TextInputListingWrapper {
		
		private final String name;
		private final String color;
		private final String content;
		
		public TextInputListingWrapper(String name, String color, String content) {
			this.name = name;
			this.color = color;
			this.content = content;
		}
		
		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public String getContent() {
			return content;
		}
	}

}
