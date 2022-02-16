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
package org.olat.core.gui.components.progressbar;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProgressBarItem extends FormItemImpl {
	
	private final ProgressBar component;
	
	public ProgressBarItem(String name) {
		super(name);
		component = new ProgressBar(name);
	}
	
	public ProgressBarItem(String name, int width, float actual, float max, String unitLabel) {
		super(name);
		component = new ProgressBar(name, width, actual, max, unitLabel);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public void setMax(float max) {
		component.setMax(max);
	}
	
	public void setActual(float actual) {
		component.setActual(actual);
	}

	public void setWidthInPercent(boolean widthInPercent) {
		component.setWidthInPercent(widthInPercent);
	}

	public void setLabelAlignment(LabelAlignment labelAlignment) {
		component.setLabelAlignment(labelAlignment);
	}

	public void setRenderStyle(RenderStyle renderStyle) {
		component.setRenderStyle(renderStyle);
	}

	public void setRenderSize(RenderSize renderSize) {
		component.setRenderSize(renderSize);
	}
	
	public void setBarColor(BarColor barColor) {
		component.setBarColor(barColor);
	}

	public void setProgressAnimationEnabled(boolean progressAnimationEnabled) {
		component.setProgressAnimationEnabled(progressAnimationEnabled);
	}

	public void setCssClass(String cssClass) {
		component.setCssClass(cssClass);
	}
	
	public void setPercentagesEnabled(boolean enabled) {
		component.setPercentagesEnabled(enabled);
	}
	
	public void setInfo(String info) {
		component.setInfo(info);
	}

	public void setProgressCallback(ProgressBarCallback progressCallback) {
		component.setProgressCallback(progressCallback);
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
}
