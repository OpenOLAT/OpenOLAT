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
package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti21.ui.QTI21AssessmentTestSessionDetails.SessionStatus;

/**
 * 
 * Initial date: 3 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentTestSessionStatusRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public AssessmentTestSessionStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof SessionStatus sessionStatus) {
			switch(sessionStatus) {
				case ERROR -> renderStatus(renderer, target, "assessment.test.session.status.error", "o_icon_error", "o_assessment_test_session_status_error");
				case CANCELLED -> renderStatus(renderer, target, "assessment.test.session.status.cancelled", "o_icon_ban", "o_assessment_test_session_status_cancelled");
				case SUSPENDED -> renderStatus(renderer, target, "assessment.test.session.status.suspended", "o_icon_qti_suspend", "o_assessment_test_session_status_suspended");
				case RUNNING -> renderStatus(renderer, target, "assessment.test.session.status.running", "o_icon_timelimit_half", "o_assessment_test_session_status_running");
				case REVIEWING -> renderStatus(renderer, target, "assessment.test.session.status.reviewing", "o_icon_review", "o_assessment_test_session_status_reviewing");
				case TERMINATED -> renderStatus(renderer, target, "assessment.test.session.status.finished", "o_icon_finished", "o_assessment_test_session_status_finished");
			}
		}
	}
	
	private void renderStatus(Renderer renderer, StringOutput target, String i18nKey, String icon, String statusCssClass) {
		String label = translator.translate(i18nKey);
		if(renderer == null) {
			target.append(label);
		} else {
			target.append("<span class=\"o_labeled_light ").append(statusCssClass).append("\">")
			      .append("<i class=\"o_icon o_icon-fw  ").append(icon).append("\"> </i> ")
			      .append(label).append("</span>");
		}
	}
}
