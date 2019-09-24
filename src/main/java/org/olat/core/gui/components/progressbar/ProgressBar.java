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
	
	public enum LabelAlignment {none, middle, right};

	private static final int DEFAULT_WIDTH = 200;

	private int width = DEFAULT_WIDTH;
	private boolean widthInPercent = false;
	private float actual;
	private float max;
	private boolean isNoMax = false;
	private LabelAlignment labelAlignment = LabelAlignment.middle;
	private String unitLabel;
	private boolean percentagesEnabled = true; // default
	private String info;
	private String cssClass;
	
	public ProgressBar(String name) {
		super(name);
	}

	public ProgressBar(String name, int width, float actual, float max, String unitLabel) {
		super(name);
		this.width = width;
		this.actual = actual > max ? max : actual;
		this.max = max;
		this.unitLabel = unitLabel == null ? "" : unitLabel;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public void setActual(float i) {
		setDirty(true);
		actual = i;
	}
	
	public void setInfo(String info) {
		this.info = info;
		setDirty(true);
	}

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

	public void setUnitLabel(String string) {
		setDirty(true);
		unitLabel = string;
	}

	public int getWidth() {
		return width;
	}

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

	public LabelAlignment getLabelAlignment() {
		return labelAlignment;
	}

	/**
	 * Defines where the label is rendered (but the percent still within if
	 * configured).
	 * 
	 * @param labelAlignment
	 */
	public void setLabelAlignment(LabelAlignment labelAlignment) {
		this.labelAlignment = labelAlignment;
	}

	public float getActual() {
		return actual;
	}
	
	public String getInfo() {
		return info;
	}

	public float getMax() {
		return max;
	}

	public boolean getIsNoMax() {
		return isNoMax;
	}

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
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		this.setDirty(true);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}