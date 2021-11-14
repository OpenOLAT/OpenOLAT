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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.BarPoint;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.ui.component.ResponsiveBarChartComponent;
import org.olat.modules.forms.ui.model.BarSeriesDataSource;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BarChartController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private BarSeries codedBarSeries;
	private List<LegendEntry> legend;

	public BarChartController(UserRequest ureq, WindowControl wControl, BarSeriesDataSource dataSource) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("bar_chart");
		
		createCodesAndLegends(dataSource.getBarSeries());
		
		ResponsiveBarChartComponent chart = new ResponsiveBarChartComponent("o_eve_bc_" + CodeHelper.getRAMUniqueID());
		chart.setYLegend(translate("chart.count"));
		chart.addSeries(codedBarSeries);
		Double max = codedBarSeries.getPoints().stream().map(BarPoint::getValue).max(Double::compare).orElse(1.0);
		chart.setYMax(max);
		mainVC.put("chart", chart);
		mainVC.contextPut("legend", legend);
		
		putInitialPanel(mainVC);
	}
	
	private void createCodesAndLegends(BarSeries barSeries) {
		List<BarPoint> points = barSeries.getPoints();
		codedBarSeries = new BarSeries();
		legend = new ArrayList<>(points.size());
		int counter = 0;
		for (BarPoint point : points) {
			String code = String.valueOf(++counter);
			codedBarSeries.add(point.getValue(), code);
			LegendEntry legendEntry = new LegendEntry(code, point.getCategory().toString());
			legend.add(legendEntry);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static final class LegendEntry {
		
		private String labelCode;
		private String label;
		
		public LegendEntry(String labelCode, String label) {
			this.labelCode = labelCode;
			this.label = label;
		}

		public String getLabelCode() {
			return labelCode;
		}

		public String getLabel() {
			return label;
		}
		
	}
	
}
