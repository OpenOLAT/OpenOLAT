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
package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController.QtiWorksStatus;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestTimerComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		AssessmentTestTimerComponent cmp = (AssessmentTestTimerComponent)source;
		QtiWorksStatus qtiWorksStatus = cmp.getQtiWorksStatus();
		if(qtiWorksStatus.isAssessmentTestTimeLimit() && !qtiWorksStatus.isEnded()) {
			AssessmentTestTimerFormItem atf = cmp.getFormItem();
			AssessmentObjectFormItem qtiRun = atf.getQtiRun();
			Form form = atf.getRootForm();

			sb.append("<div id='o_c").append(cmp.getDispatchID()).append("'><div id='o_qti_assessment_test_timer' class='clearfix o_hours'><i class='o_icon o_icon_timelimit'> </i> ")
			  .append("<strong><span class='o_qti_timer_hour'></span> <span class='o_qti_timer_label_hour'>").append(translator.translate("timelimit.short.hour")).append("</span>")
			  .append(" <span class='o_qti_timer_minute'></span> <span class='o_qti_timer_label_minute'>").append(translator.translate("timelimit.short.minute")).append("</span>")
			  .append(" <span class='o_qti_timer_second'></span> <span class='o_qti_timer_label_second'>").append(translator.translate("timelimit.short.second")).append("</span>")
			  .append("</strong> \u007C ")
			  .append(translator.translate("timelimit.ending.at", new String[] { qtiWorksStatus.getAssessmentTestEndTime() }))
			  .append("<span class='o_qti_times_up' style='display:none;'>").append(translator.translate("timelimit.finished")).append("</span>")
			  .append("<span class='o_qti_times_message o_5_minutes' style='display:none;'><i class='o_icon o_icon_warning'> </i> ").append(translator.translate("timelimit.5.minutes.message")).append("</span>")
			  .append("</div>")
			  .append("<script>")
			  .append("jQuery(function() {\n")
			  .append("  jQuery('#o_qti_assessment_test_timer').qtiTimer({\n")
			  .append("    testDuration:").append(qtiWorksStatus.getAssessmentTestDuration()).append(",\n")
			  .append("    availableTime:").append(qtiWorksStatus.getAssessmentTestMaximumTimeLimits()).append(",\n")
			  .append("    formName: '").append(form.getFormName()).append("',\n")//form name
			  .append("    dispIdField: '").append(form.getDispatchFieldId()).append("',\n")//form dispatch id
			  .append("    dispId: '").append(qtiRun.getFormDispatchId()).append("',\n")//item id
			  .append("    eventIdField: '").append(form.getEventFieldId()).append("',\n") // form eventFieldId
			  .append("    csrfToken: '").append(renderer.getCsrfToken()).append("'\n") // form eventFieldId
			  .append("  })\n")
			  .append("});\n")
			  .append("</script>\n")
			  .append("</div>");
		}
	}
}
