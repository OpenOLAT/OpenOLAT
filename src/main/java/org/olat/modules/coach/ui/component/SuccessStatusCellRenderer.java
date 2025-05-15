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
package org.olat.modules.coach.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SuccessStatusCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof SuccessStatus status) {
			target.append("<div class='o_coaching_status'>");
			renderBars(target, status);
			renderNumber(target, status);
			target.append("</div>");
		}
	}
	
	private void renderBars(StringOutput target, SuccessStatus status) {
		long passed = status.numPassed();
		long failed = status.numFailed();
		long total = passed + failed + status.numUndefined();
		
		long passedPercent = (passed == 0l) ? 0l : Math.round(100.0d * ((double)passed / (double)total));
		long failedPercent = (failed == 0l) ? 0l :  Math.round(100.0d * ((double)failed / (double)total));

		target.append("<div class='progress'>");
		
		//passed
		target.append("<div class='progress-bar progress-bar-success' role='progressbar' aria-valuenow='").append(passed)
		      .append("' aria-valuemin='0' aria-valuemax='").append(total)
		      .append("' style='width: ").append(passedPercent).append("%;'>")
		      .append("<span class='sr-only'>").append(passedPercent).append("%</span></div>");
		//failed
		target.append("<div class='progress-bar progress-bar-danger' role='progressbar' aria-valuenow='").append(failed)
	      .append("' aria-valuemin='0' aria-valuemax='").append(total)
	      .append("' style='width: ").append(max100Percent(failedPercent, passedPercent)).append("%;'>")
	      .append("<span class='sr-only'>").append(failedPercent).append("%</span></div>");
		
		target.append("</div>");
	}
	
	private long max100Percent(long val, long... precedentVals) {
		long total = 0l;
		for(long precedentVal:precedentVals) {
			total += precedentVal;
		}
		
		long diff = 100 - val - total;
		if(diff < 0l) {
			return val + diff;
		}
		return val;
	}
	
	private void renderNumber(StringOutput target, SuccessStatus status) {
		target.append("<div>")
		      .append(status.numPassed()).append("/")
		      .append(status.numFailed()).append("/")
		      .append(status.numUndefined())
		      .append("</div>");
	}
}
