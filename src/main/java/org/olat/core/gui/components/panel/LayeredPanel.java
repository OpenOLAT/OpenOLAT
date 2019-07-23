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
package org.olat.core.gui.components.panel;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Description:<br>
 * In contrast to the normal panel will the layered panel render all the pushed
 * components as HTML layers.
 * 
 * <P>
 * Initial Date: 28.10.2010 <br>
 * 
 * @author gnaegi
 */
public class LayeredPanel extends SimpleStackedPanel {
	private static final ComponentRenderer LAYERED_RENDERER = new LayeredPanelRenderer();
	private int startLayerIndex;
	private int indexIncrement;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Panel name
	 * @param startLayer
	 *            The z-index of the first layer in this panel
	 * @param indexIncrement
	 *            The difference of the z-index to the next layer
	 */
	public LayeredPanel(String name, int startLayer, int indexIncrement) {
		super(name);
		this.startLayerIndex = startLayer;
		this.indexIncrement = indexIncrement;
	}

	/**
	 * @return number representing the z-index of the first layer
	 */
	int getStartLayerIndex() {
		return this.startLayerIndex;
	}

	/**
	 * @return number representing the z-index difference beteween layers
	 */
	int getIndexIncrement() {
		return this.indexIncrement;
	}

	/**
	 * @return The list of layers in this panel
	 */
	List<Component> getLayers() {
		synchronized(stackList) {
			return new ArrayList<>(stackList);
		}
	}

	/**
	 * @see org.olat.core.gui.components.panel.Panel#getHTMLRendererSingleton()
	 */
	public ComponentRenderer getHTMLRendererSingleton() {
		return LAYERED_RENDERER;
	}

}
