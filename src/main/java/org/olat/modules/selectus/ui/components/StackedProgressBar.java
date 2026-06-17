/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * 
 * Initial date: 28 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StackedProgressBar extends AbstractComponent {
	
	private static final StackedProgressBarRenderer RENDERER = new StackedProgressBarRenderer();
	
	private double width;
	private List<BarItem> items;
	
	private final StackedProgressBarItem progressBarItem;
	
	public StackedProgressBar(String name, StackedProgressBarItem progressBarItem) {
		super(name);
		this.progressBarItem = progressBarItem;
	}
	
	public StackedProgressBarItem getProgressBarItem() {
		return progressBarItem;
	}
	
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
		setDirty(true);
	}
	
	public List<BarItem> getItems() {
		return items;
	}

	public void setItems(List<BarItem> items) {
		this.items = items;
		setDirty(true);
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
