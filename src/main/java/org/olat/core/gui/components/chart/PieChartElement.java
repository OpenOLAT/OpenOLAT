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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 17 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PieChartElement extends FormItemImpl {
	
	private final PieChartComponent component;
	
	public PieChartElement(String name) {
		super(name);
		component = new PieChartComponent(name);
	}
	
	/**
	 * @return The layer of the doughnut
	 */
	public int getLayer() {
		return component.getLayer();
	}

	/**
	 * set bigger than 0 to draw a doughnut chart.
	 * 
	 * @param layer The layer of the doughnut
	 */
	public void setLayer(int layer) {
		component.setLayer(layer);
	}
	
	@Override
	public void setElementCssClass(String elementCssClass) {
		super.setElementCssClass(elementCssClass);
		component.setElementCssClass(elementCssClass);
	}
	
	public String getTitle() {
		return component.getTitle();
	}

	public void setTitle(String title) {
		component.setTitle(title);
	}

	public String getSubTitle() {
		return component.getSubTitle();
	}

	public void setSubTitle(String subTitle) {
		component.setSubTitle(subTitle);
	}

	public List<PiePoint> getSerie() {
		return component.getSerie();
	}

	public void addPoints(PiePoint... points) {
		component.addPoints(points);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}
}
