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
package org.olat.course.nodes.videotask.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.video.model.VideoTaskCategoryScore;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectionCellRenderer implements FlexiCellRenderer {
	
	private int maxCorrect = 0;
	private int maxNotCorrect = 0;

	public int getMaxCorrect() {
		return maxCorrect;
	}
	
	public void setMaxCorrect(int maxCorrect) {
		this.maxCorrect = maxCorrect;
	}

	public int getMaxNotCorrect() {
		return maxNotCorrect;
	}

	public void setMaxNotCorrect(int maxNotCorrect) {
		this.maxNotCorrect = maxNotCorrect;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof VideoTaskCategoryScore scoring) {
			if(source.getFormItem().getRendererType() == FlexiTableRendererType.external) {
				target.append("<span class='o_videotask_correct_segments'>").append(scoring.correct()).append("</span>")
				      .append("&nbsp;/&nbsp;")
				      .append("<span class='o_videotask_notcorrect_segments'>").append(scoring.notCorrect()).append("</span>");
			} else {
				rendererBlank(target, scoring.correct(), maxCorrect);
				rendererIcon(target, scoring.correct(), "o_icon_correct_answer");
				rendererIcon(target, scoring.notCorrect(), "o_icon_not_correct");
				rendererBlank(target, scoring.notCorrect(), maxNotCorrect);
			}
		}
	}
	
	private void rendererBlank(StringOutput target, int number, int max) {
		int blank = max - number;
		if(blank > 0) {
			for(int i=0; i<blank; i++) {
				target.append("<i class='o_icon o_icon-fw o_icon_disabled' aria-hidden='false' style='visibility: hidden'> </i>");
			}
		}	
	}
	
	private void rendererIcon(StringOutput target, int number, String iconCssClass) {
		if(number > 0) {
			for(int i=0; i<number; i++) {
				target.append("<i class='o_icon o_icon-fw ").append(iconCssClass).append("'> </i>");
			}
		}	
	}
}
