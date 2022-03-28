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
package org.olat.course.assessment.ui.tool;

import java.util.Locale;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 08.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatusCellRenderer extends IconCssCellRenderer {
	
	private final Translator trans;
	private final boolean showNoStatus;
	
	public AssessmentStatusCellRenderer(Locale locale) {
		trans = Util.createPackageTranslator(AssessmentStatusCellRenderer.class, locale);
		showNoStatus = true;
	}
	
	public AssessmentStatusCellRenderer(Translator trans, boolean showNoStatus) {
		this.trans = trans;
		this.showNoStatus = showNoStatus;
	}
	
	public String render(AssessmentEntryStatus status) {
		StringOutput stringOutput = new StringOutput();
		render(stringOutput, status);
		return stringOutput.toString();
	}

	@Override
	protected String getIconCssClass(Object val) {
		if(val instanceof AssessmentEntryStatus) {
			AssessmentEntryStatus status = (AssessmentEntryStatus)val;
			switch(status) {
				case notReady: return "o_icon o_icon-fw o_icon_status_not_ready";
				case notStarted: return "o_icon o_icon-fw o_icon_status_not_started";
				case inProgress: return "o_icon o_icon-fw o_icon_status_in_progress";
				case inReview: return "o_icon o_icon-fw o_icon_status_in_review";
				case done: return "o_icon o_icon-fw o_icon_status_done";
			}	
		} else if (showNoStatus) {
			return "o_icon o_icon-fw o_icon_status_not_started";
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		if(val instanceof AssessmentEntryStatus) {
			AssessmentEntryStatus status = (AssessmentEntryStatus)val;
			switch(status) {
				case notReady: return trans.translate("assessment.status.notReady");
				case notStarted: return trans.translate("assessment.status.notStart");
				case inProgress: return trans.translate("assessment.status.inProgress");
				case inReview: return trans.translate("assessment.status.inReview");
				case done: return trans.translate("assessment.status.done");
			}	
		} else if (showNoStatus) {
			return trans.translate("assessment.status.notStart");
		}
		return null;
	}

}