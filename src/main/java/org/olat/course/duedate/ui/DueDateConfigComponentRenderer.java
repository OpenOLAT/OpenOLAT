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
package org.olat.course.duedate.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DueDateConfigComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		DueDateConfigComponent dueDateCmp = (DueDateConfigComponent)source;
		DueDateFormItemImpl dueDateItem = dueDateCmp.getFormItem();

		sb.append("<div class='form-inline'>");
		if (dueDateItem.isRelative()) {
			FormItem numOfDaysEl = dueDateItem.getNumOfDaysEl();
			if(numOfDaysEl != null && numOfDaysEl.isVisible()) {
				Component cmp = numOfDaysEl.getComponent();
				cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
				cmp.setDirty(false);
			}
			sb.append(" <span class=\"form-control-static\">").append(translator.translate("days.after")).append("</span> ");
			FormItem relativeToEl = dueDateItem.getRealtiveToDateEl();
			if(relativeToEl != null && relativeToEl.isVisible()) {
				Component cmp = relativeToEl.getComponent();
				cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
				cmp.setDirty(false);
			}
		} else {
			JSDateChooser absoluteDateEl = dueDateItem.getAbsoluteDateEl();
			if(absoluteDateEl != null && absoluteDateEl.isVisible()) {
				if(dueDateItem.getPushDateValueTo() != null
						&& !((DueDateFormItemImpl)dueDateItem.getPushDateValueTo()).isRelative()) {
					// Done in renderer because the date chooser can be create before or after the set methods are called
					JSDateChooser pushTo = ((DueDateFormItemImpl)dueDateItem.getPushDateValueTo()).getAbsoluteDateEl();
					absoluteDateEl.setPushDateValueTo(pushTo);
				}
				
				Component cmp = absoluteDateEl.getComponent();
				cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
				cmp.setDirty(false);
			}
		}
		sb.append("</div>");
		
		setRendered(dueDateItem.getNumOfDaysEl());
		setRendered(dueDateItem.getRealtiveToDateEl());
		setRendered(dueDateItem.getAbsoluteDateEl());
	}
	
	private void setRendered(FormItem item) {
		if(item != null) {
			item.getComponent().setDirty(false);
		}
	}

}
