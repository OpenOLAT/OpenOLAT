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
package org.olat.core.gui.components.table;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 08.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionStatusRenderer extends IconCssCellRenderer {

	private final Translator translator;
	
	public QuestionStatusRenderer(Translator translator) {
		this.translator = translator;
	}
	
	@Override
	protected String getCssClass(Object val) {
		if (val instanceof QuestionStatus) {
			QuestionStatus status = (QuestionStatus) val;
			switch (status) {
				case draft: return "o_icon o_icon_qitem_draft";
				case revised: return "o_icon o_icon_qitem_revised";
				case review: return "o_icon o_icon_qitem_review";
				case finalVersion: return "o_icon o_icon_qitem_finalVersion";
				case endOfLife: return "o_icon o_icon_qitem_endOfLife";
				default: break;
			}
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		if (val instanceof QuestionStatus) {
			QuestionStatus status = (QuestionStatus) val;
			return translator.translate("lifecycle.status." + status.name());
		}
		return null;
	}

	@Override
	protected String getHoverText(Object val) {
		return null;
	}

}
