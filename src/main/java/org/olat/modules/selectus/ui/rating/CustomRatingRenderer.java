/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.rating;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingService;

/**
 * Description:<br>
 * This renders the rating component
 * <P>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author gnaegi
 */
public class CustomRatingRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		CustomRatingComponent rating = (CustomRatingComponent) source;
		if(rating.isEnabled()) {
			renderEnabled(sb, rating, ubu, translator);
		} else {
			renderDisabled(sb, rating, translator);
		}
	}
	
	private void renderDisabled(StringOutput sb, CustomRatingComponent rating, Translator translator) {
		int currentRating = Math.round(rating.getCurrentRating());
		StringBuilder ratingBuffy = new StringBuilder(100);
		if(currentRating == RecruitingService.ABSTENTION) {
			ratingBuffy.append("<span class='f_rating_overview f_rating_disabled f_rating_abstention'><i class='o_icon o_icon_abstain'> </i> </span>");
		} else {
			int ratingInt = currentRating < 0 ? 0 : currentRating;
			ratingBuffy.append("<span class='f_rating_overview f_rating_disabled f_rating_").append(ratingInt).append("'><span>");
			switch(ratingInt) {
				case 1: ratingBuffy.append('C'); break;
				case 2: ratingBuffy.append('B'); break;
				case 3: ratingBuffy.append('A'); break;
				default: ratingBuffy.append("&#160;&#160;");
			}
			ratingBuffy.append("</span></span>");
		}
		
		String text = translator.translate("edit.application.my_rating.vale", new String[]{ ratingBuffy.toString() });
		
		sb.append("<span");
		if(!rating.isDomReplacementWrapperRequired()) {
			sb.append(" id='o_c").append(rating.getDispatchID()).append("'");
		}
		sb.append(">").append(text).append("</span>");
	}
	
	private void renderEnabled(StringOutput sb, CustomRatingComponent rating,
			URLBuilder ubu, Translator translator) {

		sb.append("<div ");
		if(!rating.isDomReplacementWrapperRequired()) {
			sb.append(" id='o_c").append(rating.getDispatchID()).append("'");
		}
		// Add custom css class
		if (rating.getCssClass() != null) {
			sb.append(" class='").append(rating.getCssClass()).append("'");
		}
		sb.append(">");
		
		// Add ratings and labels		
		List<String> labels = rating.getRatingLabels();
		sb.append("<div class='btn-group f_rating_items");
		if (rating.isAllowUserInput()) {
			sb.append(" f_rating_enabled");
			if(rating.isAllowAbstain()) {
				sb.append(" f_rating_abstain");
			}
		}
		sb.append("'>");

		for (int i = labels.size(); i-->0;) {
			renderButton(sb, rating, i, ubu, translator);
		}
		
		if(rating.isAllowAbstain()) {
			renderAbstainButton(sb, rating, ubu, translator);
		}
		
		sb.append("</div>"); //b_rating_items
		sb.append("</div>");//b_rating
	}
	
	private void renderAbstainButton(StringOutput sb, CustomRatingComponent rating, URLBuilder ubu, Translator translator) {
		// Add css class
		sb.append("<a class='btn o_btn_rating ");
		if (rating.getCurrentRating() == RecruitingService.ABSTENTION) {
			sb.append("o_btn_rating_on o_btn_rating_abstain");				
		} else {
			sb.append("btn-default");				
		}								
		sb.append("'");
		// Add action
		if (rating.isAllowUserInput()) {
			// Add link
			sb.append(" ");
			if(rating.getCustomFormItem() != null) {
				CustomRatingFormItem cFte = rating.getCustomFormItem();
				Form theForm = cFte.getRootForm();
				sb.append("href=\"javascript:;\" onclick=\"")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, cFte.getFormDispatchId(), 1, false, false,
						  new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(RecruitingService.ABSTENTION))))
				  .append("\"");
			} else {
				ubu.buildHrefAndOnclick(sb, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(RecruitingService.ABSTENTION)));
			}
		} else {
			// Disabled link
			sb.append(" href='javascript:;' onclick='return false;'");
		}
		// Add item label
		String title = StringHelper.escapeHtml(translator.translate("abstain.title")); 
		sb.append(" title=\"").append(title).append("\"")
		  .append(" aria-label=\"").append(title).append("\"")
		  .append("><i class='o_icon o_icon_abstain'> </i> </a>");
	}
	
	private void renderButton(StringOutput sb, CustomRatingComponent rating, int i, URLBuilder ubu, Translator translator) {
		// Add css class
		sb.append("<a class='btn o_btn_rating ");
		if (rating.getCurrentRating() == i+1) {
			sb.append("o_btn_rating_on");				
		} else {
			sb.append("btn-default");				
		}								
		sb.append("'");
		// Add action
		if (rating.isAllowUserInput()) {
			// Add link
			sb.append(" ");
			if(rating.getCustomFormItem() != null) {
				CustomRatingFormItem cFte = rating.getCustomFormItem();
				Form theForm = cFte.getRootForm();
				sb.append("href=\"javascript:;\" onclick=\"")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, cFte.getFormDispatchId(), 1, false, false,
						  new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(i+1))))
				  .append("\"");
			} else {
				ubu.buildHrefAndOnclick(sb, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(i+1)));
			}
		} else {
			// Disabled link
			sb.append(" href='javascript:;' onclick='return false;'");
		}
		// Add item label
		String label = rating.getRatingLabel(i); 
		if (label != null) {
			if (rating.isTranslateRatingLabels()) {
				label = translator.translate(label);
			}
			sb.append(" title=\"").append(StringHelper.escapeForHtmlAttribute(label)).append("\"");					
		}
		sb.append(">").append(label).append("</a>");
	}
}
