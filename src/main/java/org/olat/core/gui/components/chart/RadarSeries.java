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

/**
 * 
 * Initial date: 20 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RadarSeries {
	
	private String name;
	private String color;
	private List<RadarPoint> points = new ArrayList<>();
	
	public RadarSeries() {
		//
	}
	
	public RadarSeries(String name) {
		this.name = name;
	}
	
	public RadarSeries(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public List<RadarPoint> getPoints() {
		return points;
	}

	public void addPoint(String axis, double value) {
		points.add(new RadarPoint(axis, value));
	}
	
	public static class RadarPoint {
		
		private final String axis;
		private final double value;
		
		public RadarPoint(String axis, double value) {
			this.axis = axis;
			this.value = value;
		}
		
		public String getAxis() {
			return axis;
		}
		
		public double getValue() {
			return value;
		}
	}
}
