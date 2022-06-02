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
package org.olat.core.gui.components.chart;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;

/**
 * Very simple donut chart without legend.
 * 
 * Initial date: 17 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PieChartComponent extends DefaultD3Component {
	
	private static final ComponentRenderer renderer = new PieChartComponentRenderer();
	
	private int layer = 0;
	private final List<PiePoint> series = new ArrayList<>();
	
	public PieChartComponent(String name) {
		super(name);
		setDomReplacementWrapperRequired(false);
		this.isDomReplacementWrapperRequired();
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public List<PiePoint> getSerie() {
		return series;
	}

	public void addPoints(PiePoint... points) {
		if(points != null && points.length > 0 && points[0] != null) {
			for(PiePoint point:points) {
				series.add(point);
			}
		}
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.piechart.js");
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return renderer;
	}

}
