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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BarSeries {

	private final String color;
	private final String legend;
	private final String cssClass;
	private List<BarPoint> points = new ArrayList<>();
	
	public BarSeries() {
		this(null, null, null);
	}
	
	public BarSeries(String cssClass) {
		this(cssClass, null, null);
	}
	
	public BarSeries(String cssClass, String color, String legend) {
		this.color = color;
		this.legend = legend;
		this.cssClass = cssClass;
	}

	public String getCssClass() {
		return cssClass;
	}
	
	public String getColor() {
		return color;
	}

	public String getLegend() {
		return legend;
	}

	protected List<BarPoint> getPoints() {
		return points;
	}
	
	public void add(double value, Comparable<?> category) {
		points.add(new BarPoint(value, category, null));
	}
	
	public void add(double value, Comparable<?> category, String cssColor) {
		points.add(new BarPoint(value, category, cssColor));
	}
	
	public double get(Comparable<?> category) {
		for(BarPoint point:points) {
			if(point.category.equals(category)) {
				return point.value;
			}
		}
		return Double.NaN;
	}
	
	public static final String datasToString(double[] values) {
		StringBuilder sb = new StringBuilder();
		for(double value:values) {
			if(sb.length() > 0) sb.append(",");
			sb.append(value);
		}
		return sb.toString();
	}
	
	public static final boolean hasNotNullDatas(double[] values) {
		if(values != null && values.length > 0) {
			for(double value:values) {
				if(value > 0.00001d) {
					return true;
				}
			}
		}
		return false;
	}

	public static Stringuified getDatasAndColors(List<BarSeries> seriesList, String defaultBarClass) {
		Map<Comparable<?>,String> thickSet = new LinkedHashMap<>();
		Map<Comparable<?>,String> colorsMap = new LinkedHashMap<>();
		for(BarSeries series:seriesList) {
			for(BarPoint point:series.getPoints()) {
				Comparable<?> category = point.getCategory();
				if(thickSet.containsKey(category)) {
					String currentValue = thickSet.get(category);
					thickSet.put(category, currentValue + "," + point.getValue());
				} else {
					thickSet.put(category, Double.toString(point.getValue()));
				}
				
				if(point.getCssColor() != null) {
					colorsMap.put(category, point.getCssColor());
				}
			}
		}
		
		List<Comparable<?>> thickList = new ArrayList<>(thickSet.keySet());

		StringBuilder data = new StringBuilder();
		for(int i=0; i<thickList.size(); i++) {
			if(i != 0) data.append(",");
			
			Comparable<?> category = thickList.get(i);
			String values =  thickSet.get(category);
			data.append("['").append(category).append("',").append(values).append("]");
		}

		StringBuilder colors = new StringBuilder();
		if(!colorsMap.isEmpty()) {
			for(int i=0; i<thickList.size(); i++) {
				if(i != 0) colors.append(",");
				
				Comparable<?> category = thickList.get(i);
				String cssColor =  colorsMap.get(category);
				if(cssColor == null) {
					colors.append("['").append(defaultBarClass).append("']");
				} else {
					colors.append("['").append(cssColor).append("']");
				}
			}
		}
		
		return new Stringuified(data, colors);
	}
	
	public static class Stringuified {
		private final StringBuilder colors;
		private final StringBuilder data;
		
		public Stringuified(StringBuilder data, StringBuilder colors) {
			this.data = data;
			this.colors = colors;
		}

		public StringBuilder getColors() {
			return colors;
		}

		public StringBuilder getData() {
			return data;
		}	
	}

	protected static class BarPoint {
		private final Comparable<?> category;
		private final double value;
		private final String cssColor;
		
		public BarPoint(double value, Comparable<?> category, String cssColor) {
			this.value = value;
			this.category = category;
			this.cssColor = cssColor;
		}
		
		public Comparable<?> getCategory() {
			return category;
		}
		
		public double getValue() {
			return value;
		}

		public String getCssColor() {
			return cssColor;
		}
	}
}
