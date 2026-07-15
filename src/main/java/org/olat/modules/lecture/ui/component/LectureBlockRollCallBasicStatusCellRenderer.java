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
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;

/**
 * 
 * 
 * Initial date: 21 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureBlockRollCallBasicStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public LectureBlockRollCallBasicStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(renderer == null) {
			if(cellValue instanceof LectureRollCallStatus status) {
				target.append(trans.translate(status.name()));
			} else if(cellValue instanceof LectureBlock block) {
				target.append(trans.translate(block.getRollCallStatus().name()));
			}
		} else if(cellValue instanceof LectureRollCallStatus status) {
			getStatus(target, "o_labeled_light", status, translator);
		} else if(cellValue instanceof LectureBlock block) {
			getStatus(target, "o_labeled_light", block.getRollCallStatus(), translator);
		}
	}

	public static final String getStatusLabel(LectureBlock block, Translator trans) {
		StringOutput sb = new StringOutput();
		getStatus(sb, "o_labeled_light", block.getRollCallStatus(), trans);
		return sb.toString();
	}

	public static final String getStatusLabelWithIcon(LectureBlock block, Translator trans, String cssClass) {
		LectureRollCallStatus status = block.getRollCallStatus();
		String statusName = status.name();
		String iconCss = switch (status) {
			case open -> "o_icon_lecture_rollcall_open";
			case closed -> "o_icon_lecture_rollcall_closed";
			case reopen -> "o_icon_lecture_rollcall_reopen";
			case autoclosed -> "o_icon_lecture_rollcall_autoclosed";
		};
		
		return "<span class=\"" + cssClass + " o_lecture_rollcall_status_" + statusName 
				+ "\" title=\"" + trans.translate(statusName) + "\">" 
				+ "<i class=\"o_icon " + iconCss + " o_icon-fw\"> </i> " 
				+ trans.translate(statusName) + "</span>";
	}

	public static final String getStatusLabelWithIcon(LectureBlock block, Translator trans) {
		return getStatusLabelWithIcon(block, trans, "o_labeled_light");
	}
	
	public static final String getStatusString(LectureBlock block, Translator trans) {
		LectureRollCallStatus rollCallStatus = block.getRollCallStatus();
		return trans.translate(rollCallStatus.name());
	}
	
	public static final void getStatus(StringOutput target, String type, LectureRollCallStatus status, Translator trans) {
		target.append("<span class=\"").append(type).append(" o_lecture_rollcall_status_")
		      .append(status.name()).append("\">").append(trans.translate(status.name()))
		      .append("</span>");
	}
}
