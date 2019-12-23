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
package org.olat.admin.sysinfo.gui;

import java.text.DecimalFormat;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class LargeFilesAgeCellRenderer implements FlexiCellRenderer {
	private static final DecimalFormat ageFormat = new DecimalFormat("###.#");
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Long) {
			long ageMillis = ((Long) cellValue).longValue();
			
			if(ageMillis < (1000*60)) {
				double age = ageMillis / (1000.0d);
				formatAge(age, "sec", target);
			} else if(ageMillis < (1000l * 60 * 60)) {
				double age = ageMillis / (1000.0d * 60);
				formatAge(age, "min", target);
			} else if(ageMillis < (1000l * 60 * 60 * 24)) {
				double age = ageMillis / (1000.0d * 60 * 60);
				formatAge(age, "hours", target);
			} else if(ageMillis < (1000l * 60 * 60 * 24 * 30)) {
				double age = ageMillis / (1000.0d * 60 * 60 * 24);
				formatAge(age, "days", target);
			} else if(ageMillis < (1000l * 60 * 60 * 24 * 30 * 12)) {
				double age = ageMillis / (1000.0d * 60 * 60 * 24 * 30);
				formatAge(age, "months", target);
			} else if(ageMillis >= (1000l * 60 * 60 * 24 * 30 * 12)) {
				double age = ageMillis / (1000.0d * 60 * 60 * 24 * 30 * 12);
				formatAge(age, "years", target);
			}
		}
	}
	
	private synchronized void formatAge(double age, String timeFormat, StringOutput target) {
		target.append(ageFormat.format(age)).append(" ").append(timeFormat);
	}
}