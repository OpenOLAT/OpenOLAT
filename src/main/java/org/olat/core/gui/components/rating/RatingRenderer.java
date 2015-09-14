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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * Description:<br>
 * This renders the rating component
 * <P>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author gnaegi
 */
public class RatingRenderer extends DefaultComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		RatingComponent rating = (RatingComponent) source;
		sb.append("<div class='o_rating ");
		// Add custom css class
		if (rating.getCssClass() != null) sb.append(rating.getCssClass());
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
		if (rating.isAllowUserInput()) {
			sb.append(" o_enabled");			
		}
		sb.append("'>");


		boolean ajaxModeEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		for (int i = 0; i < labels.size(); i++) {
			// Add css class
			sb.append("<a class='o_icon o_icon-lg ");
			if (rating.getCurrentRating() >= i+1) {
				sb.append("o_icon_rating_on");				
			} else {
				sb.append("o_icon_rating_off");				
			}								
			sb.append("'");
			// Add action
			if (rating.isAllowUserInput() && rating.isEnabled()) {
				if(rating.getForm() == null) {
					// Add link
					sb.append(" ");
					ubu.buildHrefAndOnclick(sb, ajaxModeEnabled,
							new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(i+1)));
				} else {
					Form theForm = rating.getForm();
					String elementId = FormBaseComponentIdProvider.DISPPREFIX + rating.getDispatchID();
					sb.append(" href=\"javascript:")
					  .append(FormJSHelper.getXHRFnCallFor(theForm, elementId, 1, true, false,
							  new NameValuePair(VelocityContainer.COMMAND_ID, Integer.toString(i+1))))
					  .append("\" ");
				}
			} else {
				// Disabled link
				sb.append(" href='javascript:;' onclick='return false;'");
			}
			// Add item label
			String label = rating.getRatingLabel(i); 
			if (label != null) {
				if (rating.isTranslateRatingLabels()) {
					if(translator != null) {
						label = translator.translate(label);
					} else {
						label = "";
					}
				}
				StringBuilder escapedLabel = new StringBuilder();
				escapedLabel.append(StringEscapeUtils.escapeHtml(label));
				sb.append(" title=\"").append(escapedLabel).append("\"");					
			}
			sb.append("></a>");
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
	}
}
