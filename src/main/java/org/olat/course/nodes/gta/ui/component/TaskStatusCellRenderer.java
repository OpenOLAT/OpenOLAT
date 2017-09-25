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
package org.olat.course.nodes.gta.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.gta.TaskProcess;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public TaskStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof TaskProcess) {
			TaskProcess step = (TaskProcess)cellValue;
			switch(step) {
				case assignment: target.append("<i class='o_icon o_icon-fw'>&nbsp;</i>").append(translator.translate("process.assignment")); break;
				case submit: target.append("<i class='o_icon o_icon-fw'>&nbsp;</i>").append(translator.translate("process.submission")); break;
				case review: target.append("<i class='o_icon o_icon-fw o_icon_info'>&nbsp;</i>").append(translator.translate("process.review")); break;
				case revision: target.append("<i class='o_icon o_icon-fw'>&nbsp;</i>").append(translator.translate("process.revision")); break;
				case correction: target.append("<i class='o_icon o_icon-fw o_icon_info'>&nbsp;</i>").append(translator.translate("process.correction")); break;
				case solution: target.append("<i class='o_icon o_icon-fw'>&nbsp;</i>").append(translator.translate("process.solution")); break;
				case grading: target.append("<i class='o_icon o_icon-fw o_icon_info'>&nbsp;</i>").append(translator.translate("process.grading")); break;
				case graded: target.append("<i class='o_icon o_icon-fw'>&nbsp;</i>").append(translator.translate("process.graded")); break;
			}
		} else if(cellValue == null) {
			target.append("<i class='o_icon o_icon-fw '>&nbsp;</i>").append(translator.translate("process.no"));
		}
	}
}
