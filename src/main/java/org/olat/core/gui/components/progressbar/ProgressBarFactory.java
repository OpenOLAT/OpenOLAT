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

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 10 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProgressBarFactory {
	
	public static ProgressBar createProgressBar(String name, Locale locale) {
		return createProgressBar(name, null, locale);
	}
	
	public static ProgressBar createProgressBar(String name, VelocityContainer vc, Locale locale) {
		ProgressBar progressBar = new ProgressBar(name, locale);
		if (vc != null) {
			vc.put(progressBar.getComponentName(), progressBar);
		}
		return progressBar;
	}
	
	public static void setValue(ProgressBar progressBar, float value, BarColor color) {
		progressBar.setSegments(List.of(new Segment(value, color, null)));
	}
	
	public static void setValue(ProgressBarItem progressBarItem, float value, BarColor color) {
		progressBarItem.setSegments(List.of(new Segment(value, color, null)));
	}
	
}
