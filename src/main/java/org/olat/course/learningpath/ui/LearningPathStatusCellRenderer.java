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
package org.olat.course.learningpath.ui;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.learningpath.LearningPathStatus;

/**
 * 
 * Initial date: 20.02.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathStatusCellRenderer implements FlexiCellRenderer, CustomCellRenderer {
	
	private final Translator trans;
	
	private final boolean iconVisible;
	private final boolean labelVisible;
	
	public LearningPathStatusCellRenderer(Locale locale) {
		this(locale, true, true);
	}
	
	public LearningPathStatusCellRenderer(Locale locale, boolean iconVisible, boolean labelVisible) {
		trans = Util.createPackageTranslator(AssessmentStatusCellRenderer.class, locale
				, Util.createPackageTranslator(LearningPathListController.class, locale));
		this.iconVisible = iconVisible;
		this.labelVisible = labelVisible;
	}
	
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		render(renderer, sb, val, -1, null, null, renderer.getTranslator());
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof LearningPathTreeNode) {
			LearningPathTreeNode lpTreeNode = (LearningPathTreeNode)cellValue;
			if (iconVisible) {
				target.append("<i class='o_icon o_icon-lg o_status ").append(getIconCss(lpTreeNode)).append("'> </i>");
			}
			if (iconVisible && labelVisible) {
				target.append(" ");
			}
			if (labelVisible) {
				String i18nKey = getI18nKey(lpTreeNode);
				if (i18nKey != null) {
					target.append(trans.translate(i18nKey));
				}
			}
		} else {
			target.append("-");
		}
	}
	
	private String getIconCss(LearningPathTreeNode lpTreeNode) {
		if (lpTreeNode == null || lpTreeNode.getAssessmentEvaluation() == null) return "";
		
		return LearningPathStatus.of(lpTreeNode.getAssessmentEvaluation()).getCssClass();
	}
	
	private String getI18nKey(LearningPathTreeNode lpTreeNode) {
		if (lpTreeNode == null || lpTreeNode.getAssessmentEvaluation() == null) return null;
		
		return LearningPathStatus.of(lpTreeNode.getAssessmentEvaluation()).getI18nKey();
	}
	
}