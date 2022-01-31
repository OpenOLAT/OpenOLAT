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
package org.olat.modules.assessment.ui.component;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 28 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassedChart extends FormBaseComponentImpl {
	
	private static final PassedChartRenderer RENDERER = new PassedChartRenderer();
	
	private PassedPercent passedPercent;

	public PassedChart(String name) {
		super(name);
	}
	
	public PassedPercent getPassedPercent() {
		return passedPercent;
	}
	
	public void setPassedPercent(PassedPercent passedPercent) {
		this.passedPercent = passedPercent;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public static class PassedPercent {
		
		private final int passedPercent;
		private final int failedPercent;
		
		public PassedPercent(int passedPercent, int failedPercent) {
			this.passedPercent = passedPercent;
			this.failedPercent = failedPercent;
		}

		public int getPassedPercent() {
			return passedPercent;
		}

		public int getFailedPercent() {
			return failedPercent;
		}
		
	}

}
