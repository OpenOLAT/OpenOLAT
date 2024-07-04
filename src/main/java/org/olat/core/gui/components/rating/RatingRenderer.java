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
package org.olat.core.gui.components.rating;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * This renders the rating component
 * <P>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author gnaegi
 */
public class RatingRenderer extends DefaultComponentRenderer {


	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		RatingComponent rating = (RatingComponent) source;
		RatingFormItem rfi = rating.getFormItem();
		RatingType type = rating.getType();
		
		sb.append("<div class='o_rating");
		if(type == RatingType.yesNo) {
			sb.append(" o_rating_yesno");
		} else if(type == RatingType.stars) {
			sb.append(" o_rating_stars");
		}
		// Add custom css class
		if (rating.getCssClass() != null) {
			sb.append(" ").append(rating.getCssClass());
		}
		sb.append("'>");
		
		// Add Title
		String title = rating.getTitle(); 
		if (title != null) {
			sb.append("<div class='o_rating_title'>");
			if (rating.isTranslateTitle()) {
				if(translator != null) {
					title = translator.translate(title);
				} else {
					title = "";
				}
			}
			sb.append(title);				
			sb.append("</div>"); //o_rating_title
		}
		
		// Add ratings and labels		
		List<String> labels = rating.getRatingLabel();
		sb.append("<div class='o_rating_items");
		if (rating.isAllowUserInput() && rating.isEnabled()) {
			sb.append(" o_enabled");			
		}
		sb.append("'>");

		if(type == RatingType.stars) {
			renderStars(sb, rating, labels, ubu, translator);
		} else {
			renderYesNo(sb, rating, labels, ubu, translator);
		}
		
		// Add text output
		if (rating.isShowRatingAsText()) {
			sb.append("<span class='o_legend'>");
			sb.append(Formatter.roundToString(rating.getCurrentRating(), 1));
			sb.append(" / ");
			sb.append(labels.size());			
			sb.append("</span>");
		}
		sb.append("</div>"); //o_rating_items
		
		// Add explanation
		String expl = rating.getExplanation(); 
		if (expl != null) {
			sb.append("<div class='o_rating_explanation'>");
			if (rating.isTranslateExplanation()) {
				if(translator != null) {
					expl = translator.translate(expl);
				} else {
					expl = "";
				}
			}
			sb.append(expl);				
			sb.append("</div>"); //o_rating_explanation
		}
		sb.append("</div>");//o_rating
		if(rfi != null) {
			FormJSHelper.appendFlexiFormDirtyForClick(sb, rfi.getRootForm(), rfi.getFormDispatchId());
		}
	}
	
	private void renderYesNo(StringOutput sb, RatingComponent rating, List<String> labels, URLBuilder ubu, Translator translator) {
		if (rating.isAllowUserInput() && rating.isEnabled()) {
			boolean yes = rating.getCurrentRating() > 0;
			boolean no = rating.getCurrentRating() < 0;
			
			sb.append("<div class=\"btn-group\">");
			renderYesOrNoButton(sb, rating, labels.get(1), "yes", yes, ubu, translator);
			renderYesOrNoButton(sb, rating, labels.get(0), "no", no, ubu, translator);
			sb.append("</div>");
		} else if(rating.getCurrentRating() > 0.0f) {
			renderYesOrNo(sb, rating, labels.get(1), "yes", true, ubu, translator);
		} else if(rating.getCurrentRating() < 0.0f) {
			renderYesOrNo(sb, rating, labels.get(1), "no", true, ubu, translator);
		}
	}
	
	private void renderYesOrNoButton(StringOutput sb, RatingComponent rating, String label, String val, boolean selected, URLBuilder ubu, Translator translator) {
		sb.append("<a class='btn btn-default").append(" btn-primary", selected).append("'");
		// Add onclick
		renderAction(sb, rating, val, ubu);
		// Add title
		renderTitle(sb, rating, label, translator);
		sb.append("><i class=\"o_icon o_icon-lg o_icon_rating_").append(val).append("_off", "_on", selected)
		  .append("\"> </i> ");
		if (label != null) {
			if (rating.isTranslateRatingLabels()) {
				if(translator != null) {
					label = translator.translate(label);
				} else {
					label = "";
				}
			}
			sb.appendHtmlEscaped(label);
		}
		sb.append("</a>");
	}
	
	private void renderYesOrNo(StringOutput sb, RatingComponent rating, String label, String val, boolean selected, URLBuilder ubu, Translator translator) {
		sb.append("<a class='o_icon o_icon-lg o_icon_rating_")
		  .append(val).append("_on", "_off", selected).append("'");
		// Add onclick
		renderAction(sb, rating, val, ubu);
		// Add title
		renderTitle(sb, rating, label, translator);
		sb.append("> </a>");
	}
	
	private void renderStars(StringOutput sb, RatingComponent rating, List<String> labels, URLBuilder ubu, Translator translator) {
		for (int i = 0; i < labels.size(); i++) {
			// Add css class
			sb.append("<a class='o_icon o_icon-lg ");
			if (rating.getCurrentRating() >= i+1) {
				sb.append("o_icon_rating_on");			
			} else {
				sb.append("o_icon_rating_off");
			}								
			sb.append("'");
			renderAction(sb, rating, Integer.toString(i+1), ubu);
			
			// Add item label
			String label = rating.getRatingLabel(i); 
			renderTitle(sb, rating, label, translator);
			sb.append("></a>");
		}
	}

	private void renderAction(StringOutput sb, RatingComponent rating, String val, URLBuilder ubu) {
		RatingFormItem rfi = rating.getFormItem();
		if (rating.isAllowUserInput() && rating.isEnabled()) {
			NameValuePair cmd = new NameValuePair(VelocityContainer.COMMAND_ID, val);
			if(rfi == null) {
				ubu.buildHrefAndOnclick(sb, true, cmd);
			} else {
				sb.append(" href=\"javascript:;\" onclick=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(rfi.getRootForm(), rfi.getFormDispatchId(), 1, false, false, true, cmd))
				  .append("\"");
			}
		} else {
			// Disabled link
			sb.append(" href=\"javascript:;\" onclick=\"javascript:return false;\"");
		}
	}

	private void renderTitle(StringOutput sb, RatingComponent rating, String label, Translator translator) {
		if (label != null) {
			if (rating.isTranslateRatingLabels()) {
				if(translator != null) {
					label = translator.translate(label);
				} else {
					label = "";
				}
			}
			sb.append(" title=\"").append(StringHelper.escapeHtml(label)).append("\"");					
		}
	}
}
