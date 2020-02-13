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
package org.olat.modules.grading.ui.component;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.grading.GraderStatus;

/**
 * 
 * Initial date: 21 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GraderStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public GraderStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof List) {
			@SuppressWarnings("unchecked")
			GraderStatus status = getFinalStatus((List<GraderStatus>)cellValue);
			if(status != null) {
				String iconCssClass;
				if(status == GraderStatus.activated) {
					iconCssClass = "o_icon o_grader_active";
				} else if(status == GraderStatus.deactivated) {
					iconCssClass = "o_icon o_grader_inactive";
				} else {
					iconCssClass = "o_icon o_grader_absence";
				}
				
				String label = translator.translate("grader.status.".concat(status.name()));
				target.append("<span><i class='")
				      .append(iconCssClass).append("'> </i> ")
				      .append(label).append("</span>");
			}
		}
	}
	
	public static GraderStatus getFinalStatus(List<GraderStatus> status) {
		if(status == null || status.isEmpty()) {
			return null;
		}
		if(status.size() == 1) {
			return status.get(0);
		}

		if(status.contains(GraderStatus.activated)) {
			return GraderStatus.activated;
		}
		if(status.contains(GraderStatus.deactivated)) {
			return GraderStatus.deactivated;
		}
		return GraderStatus.removed;
	}
}
