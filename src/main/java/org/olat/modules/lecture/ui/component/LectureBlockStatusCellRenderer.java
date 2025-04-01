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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;

/**
 * 
 * Initial date: 6 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public LectureBlockStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(renderer == null) {
			if(cellValue instanceof LectureBlockStatus status) {
				target.append(translator.translate(status.name()));
			} else if(cellValue instanceof LectureBlock block) {
				target.append(translator.translate(block.getStatus().name()));
			}
		} else if(cellValue instanceof LectureBlock block) {
			getStatus(target, "o_labeled_light", block, translator);
		}
	}
	
	public static final String getStatusLabel(LectureBlock block, Translator trans) {
		StringOutput sb = new StringOutput();
		getStatus(sb, "o_labeled_light", block, trans);
		return sb.toString();
	}
	
	public static final String getStatusString(LectureBlock block, Translator trans) {
		LectureBlockVirtualStatus vStatus = calculateStatus(block);
		return vStatus == null ? null: trans.translate(vStatus.name());
	}
	
	private static final void getStatus(StringOutput target, String type, LectureBlock block, Translator trans) {
		LectureBlockVirtualStatus vStatus = calculateStatus(block);
		if(vStatus != null) {
			String statusName = vStatus.name().toLowerCase();
			target.append("<span class=\"").append(type).append(" o_lecture_status_")
			      .append(statusName).append("\">").append(trans.translate(statusName))
			      .append("</span>");
		}
	}
	
	public static final LectureBlockVirtualStatus calculateStatus(LectureBlock block) {
		LectureBlockStatus status = block.getStatus();
		LectureBlockVirtualStatus vStatus;
		if(status == LectureBlockStatus.cancelled) {
			vStatus = LectureBlockVirtualStatus.CANCELLED;
		} else {
			Date now = new Date();
			Date start = block.getStartDate();
			Date end = block.getEndDate();
			if(end != null && end.before(now)) {
				vStatus = LectureBlockVirtualStatus.DONE;
			} else if(start != null && start.before(now) && end != null && end.after(now)) {
				vStatus = LectureBlockVirtualStatus.RUNNING;
			} else if(start != null && start.after(now)) {
				vStatus = LectureBlockVirtualStatus.PLANNED;
			} else if(status == LectureBlockStatus.done) {
				vStatus = LectureBlockVirtualStatus.DONE;
			} else {
				vStatus = null;
			}
		}
		return vStatus;
	}
	
	public enum LectureBlockVirtualStatus {
		PLANNED,
		RUNNING,
		DONE,
		CANCELLED
	}
}
