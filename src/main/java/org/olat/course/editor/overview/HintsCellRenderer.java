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
package org.olat.course.editor.overview;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 30 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HintsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode node = (CourseEditorTreeNode)cellValue;
			if (node.getIconDecorator1CssClass() != null) {
				target.append("<span class='badge ").append(node.getIconDecorator1CssClass()).append("'><i class='o_icon ").append(node.getIconDecorator1CssClass()).append("'> </i></span>");
			}
			if (node.getIconDecorator2CssClass() != null) {
				target.append("<span class='badge ").append(node.getIconDecorator2CssClass()).append("'><i class='o_icon ").append(node.getIconDecorator2CssClass()).append("'> </i>");
			}
			if (node.getIconDecorator3CssClass() != null) {
				target.append("<span class='badge ").append(node.getIconDecorator3CssClass()).append("'><i class='o_icon ").append(node.getIconDecorator3CssClass()).append("'> </i>");
			}
		}
	}

}
