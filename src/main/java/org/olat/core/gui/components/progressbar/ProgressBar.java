/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.components.progressbar;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Initial Date: Feb 2, 2004 A <b>Choice </b> is
 * 
 * @author Andreas
 */
public class ProgressBar extends Component {
	private static final ComponentRenderer RENDERER = new ProgressBarRenderer();

	private static final int DEFAULT_WIDTH = 200;

	private int width = DEFAULT_WIDTH;
	private float actual;
	private float max;
	private boolean isNoMax = false;
	private String unitLabel;
	private boolean percentagesEnabled = true; // default

	/**
	 * @param name
	 */
	public ProgressBar(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param width
	 * @param actual
	 * @param max
	 * @param unitLabel
	 */
	public ProgressBar(String name, int width, float actual, float max, String unitLabel) {
		super(name);
		this.width = width;
		this.actual = actual > max ? max : actual;
		this.max = max;
		this.unitLabel = unitLabel == null ? "" : unitLabel;
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
	//
	}

	/**
	 * @param i
	 */
	public void setActual(float i) {
		setDirty(true);
		actual = i;
	}

	/**
	 * @param i
	 */
	public void setMax(float i) {
		setDirty(true);
		max = i;
	}

	/**
	 * If set to true, no max limit for this progress bar.
	 * 
	 * @param noMax
	 */
	public void setIsNoMax(boolean isNoMax) {
		setDirty(true);
		this.isNoMax = isNoMax;
	}

	/**
	 * @param string
	 */
	public void setUnitLabel(String string) {
		// FIXME:fj:a remove all those unnecessary setters, put them in the
		// constructor
		setDirty(true);
		unitLabel = string;
	}

	/**
	 * @param i
	 */
	public void setWidth(int i) {
		width = i;
	}

	/**
	 * @return
	 */
	float getActual() {
		return actual;
	}

	/**
	 * @return
	 */
	float getMax() {
		return max;
	}

	boolean getIsNoMax() {
		return isNoMax;
	}

	/**
	 * @return
	 */
	String getUnitLabel() {
		return unitLabel;
	}

	/**
	 * @return
	 */
	int getWidth() {
		return width;
	}

	/**
	 * @see org.olat.core.gui.components.Component#getExtendedDebugInfo()
	 */
	public String getExtendedDebugInfo() {
		return "just a bar";
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * @return percentagesEnabled true: show percentages; false: show only unit labels, no percentags
	 */
	public boolean isPercentagesEnabled() {
		return percentagesEnabled;
	}

	/**
	 * @param percentagesEnabled true: show percentages; false: show only unit labels, no percentags
	 */
	public void setPercentagesEnabled(boolean percentagesEnabled) {
		this.percentagesEnabled = percentagesEnabled;
	}

}