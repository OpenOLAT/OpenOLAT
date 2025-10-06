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
package org.olat.course.assessment.ui.mode;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.model.EnhancedStatus;

/**
 * Initial date: 08.01.2015<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ModeStatusCellRenderer implements FlexiCellRenderer {

	private final AssessmentModeHelper helper;

	public ModeStatusCellRenderer(Translator translator) {
		helper = new AssessmentModeHelper(translator);
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
					   int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {

		if (cellValue instanceof Status status) {
			render(status, null, sb);
		} else if (cellValue instanceof EnhancedStatus enStatus) {
			renderWarning(enStatus.getWarnings(), sb);
			render(enStatus.getStatus(), enStatus.getEndStatus(), sb);
		}
	}

	private void renderWarning(List<String> warnings, StringOutput sb) {
		if (warnings != null && !warnings.isEmpty()) {
			sb.append("<i class='o_icon o_icon_warn' title='");
			for (String warning : warnings) {
				sb.append(warning).append(" ");
			}
			sb.append("'> </i> ");
		}
	}

	private void render(Status status, EndStatus endStatus, StringOutput sb) {
		String title = helper.getStatusLabel(status);
		String statusClass = "o_labeled_light o_qti_assessment_mode o_assessment_status_" + status.name();
		sb.append("<span class='").append(statusClass).append("'").append("title='").append(title).append("'><i class='o_icon o_icon-fw ").append(status.cssClass()).append("'> </i> ")
				.append("<span>").append(title).append("</span>")
				.append(" <i class='o_icon o_icon_disadvantage_compensation'> </i>", EndStatus.withoutBoth == endStatus || EndStatus.withoutDisadvantage == endStatus)
				.append(" <i class='o_icon o_icon_extra_time'> </i>", EndStatus.withoutBoth == endStatus || EndStatus.withoutExtraTime == endStatus)
				.append("</span>");
	}

	public void renderStatus(Status status, EndStatus endStatus, StringOutput sb) {
		String title = helper.getStatusLabel(status);
		String statusClass = "o_labeled o_qti_assessment_mode o_assessment_status_" + status.name();
		sb.append("<span class='").append(statusClass).append("'").append("title='").append(title).append("'><i class='o_icon o_icon-fw ").append(status.cssClass()).append("'> </i> ")
				.append("<span>").append(title).append("</span>")
				.append(" <i class='o_icon o_icon_disadvantage_compensation'> </i>", EndStatus.withoutBoth == endStatus || EndStatus.withoutDisadvantage == endStatus)
				.append(" <i class='o_icon o_icon_extra_time'> </i>", EndStatus.withoutBoth == endStatus || EndStatus.withoutExtraTime == endStatus)
				.append("</span>");
	}
}