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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.progressbar;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Initial Date: Feb 2, 2004 A <b>Choice </b> is
 * 
 * @author Andreas
 */
public class ProgressBar extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new ProgressBarRenderer();

	private static final int DEFAULT_WIDTH = 200;

	private int width = DEFAULT_WIDTH;
	private boolean widthInPercent = false;
	private float actual;
	private float max;
	private boolean isNoMax = false;
	private boolean renderLabelRights = false;
	private String unitLabel;
	private boolean percentagesEnabled = true; // default
	private String info;
	
	

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
	@Override
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
	
	public void setInfo(String info) {
		this.info = info;
		setDirty(true);
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
		setDirty(true);
		unitLabel = string;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param i
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isWidthInPercent() {
		return widthInPercent;
	}

	public void setWidthInPercent(boolean widthInPercent) {
		if(widthInPercent) {
			width = 100;
		}
		this.widthInPercent = widthInPercent;
	}

	public boolean isRenderLabelRights() {
		return renderLabelRights;
	}
	
	/**
	 * The labels are rendered at the right of the progress bar (but the
	 * percent still within if configured).
	 * 
	 * @param renderLabelRights true to render the labels at the right of the progress bar
	 */
	public void setRenderLabelRights(boolean renderLabelRights) {
		this.renderLabelRights = renderLabelRights;
	}

	/**
	 * @return
	 */
	public float getActual() {
		return actual;
	}
	
	public String getInfo() {
		return info;
	}

	/**
	 * @return
	 */
	public float getMax() {
		return max;
	}

	public boolean getIsNoMax() {
		return isNoMax;
	}

	/**
	 * @return
	 */
	public String getUnitLabel() {
		return unitLabel;
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

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}