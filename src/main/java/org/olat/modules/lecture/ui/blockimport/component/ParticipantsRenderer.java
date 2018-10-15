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
package org.olat.modules.lecture.ui.blockimport.component;


import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.ui.blockimport.GroupMapping;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlock;
import org.olat.modules.lecture.ui.blockimport.GroupMapping.Type;

/**
 * 
 * Initial date: 15 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantsRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof ImportedLectureBlock) {
			ImportedLectureBlock block = (ImportedLectureBlock)cellValue;
			GroupMapping mapping = block.getGroupMapping();
			if(mapping == null) {
				target.append("-");
			} else if(mapping.type() == Type.course) {
				target.append("Course");
			} else if(mapping.type() == Type.businessGroup) {
				target.append("BGroup");
			} else if(mapping.type() == Type.curriculumElement) {
				target.append("Curriculum");
			}
		}
	}
}
