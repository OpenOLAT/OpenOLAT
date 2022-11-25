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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 31.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingWithAverageRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		RatingWithAverageComponent ratingCmp = (RatingWithAverageComponent)source;
		RatingWithAverageFormItem item = ratingCmp.getFormItem();
		
		String viewIdent = item.getFormDispatchId();
		sb.append("<div id='").append(viewIdent).append("' class='o_ratings_and_comments'>")
		  .append("<div id='o_rating_wrapper_").append(viewIdent).append("' class='o_rating_wrapper'>")
		  .append("<div id='o_rating_").append(viewIdent).append("' style='display:none;'>");
		
		Component userCmp = item.getUserComponent();
		userCmp.getHTMLRendererSingleton().render(renderer, sb, userCmp, ubu, translator, renderResult, args);
		userCmp.setDirty(false);
		sb.append("</div>")
		  .append("<div id='o_rating_avg_").append(viewIdent).append("'>");
		Component averageCmp = item.getAverageComponent();
		averageCmp.getHTMLRendererSingleton().render(renderer, sb, averageCmp, ubu, translator, renderResult, args);
		averageCmp.setDirty(false);
		sb.append("</div>")
		  .append("</div>")
		  .append("<script>\n")
		  .append("\"use strict\";\n")
		  .append("jQuery(function(){\n")
		  .append("  jQuery('#o_rating_wrapper_").append(viewIdent).append("')\n")
		  .append("    .hover(function(){ console.log('ho'); jQuery('#o_rating_").append(viewIdent).append("').show(); jQuery('#o_rating_avg_").append(viewIdent).append("').hide(); },\n")
		  .append("      function(){ console.log('ver'); jQuery('#o_rating_").append(viewIdent).append("').hide(); jQuery('#o_rating_avg_").append(viewIdent).append("').show(); }\n")  
		  .append("    );\n")
		  .append("});\n")
		  .append("</script>\n")
		  .append("</div>");
	}
}
