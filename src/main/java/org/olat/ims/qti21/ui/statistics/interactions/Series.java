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
package org.olat.ims.qti21.ui.statistics.interactions;

import java.util.List;

import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;

/**
 * 
 * Initial date: 10.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Series {
	
	private final boolean legend;
	private String itemCss;
	private String chartType;
	private final List<BarSeries> series;
	private final List<ResponseInfos> responseInfos;
	
	private final int numOfParticipants;
	private Stringuified datas;
	
	public Series(List<BarSeries> series, List<ResponseInfos> responseInfos,
			int numOfParticipants, boolean legend) {
		this.legend = legend;
		this.series = series;
		this.responseInfos = responseInfos;
		this.numOfParticipants = numOfParticipants;
	}

	public boolean isLegend() {
		return legend;
	}

	public String getItemCss() {
		return itemCss;
	}

	public void setItemCss(String itemCss) {
		this.itemCss = itemCss;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public int getNumOfParticipants() {
		return numOfParticipants;
	}

	public List<BarSeries> getSeries() {
		return series;
	}
	
	public List<ResponseInfos> getResponseInfos() {
		return responseInfos;
	}
	
	public boolean isColorsCustom() {
		Stringuified d = getDatas();
		return d.getColors().length() > 3;
	}

	public Stringuified getDatas() {
		if(datas == null) {
			datas = BarSeries.getDatasAndColors(series, "bar_default");
		}
		return datas;
	}

}
