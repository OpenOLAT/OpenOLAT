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

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.RecruitingService;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RatingsOverviewRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		RatingsOverviewComponent cmp = (RatingsOverviewComponent)source;
		List<UserRating> ratings = cmp.getRatings();
		sb.append("<div");
		if(!cmp.isDomReplacementWrapperRequired()) {
			sb.append(" id='o_c").append(cmp.getDispatchID()).append("'");
		}
		sb.append(" class='f_ratings_overview_container'>");
		for(UserRating rating:ratings) {
			int val = (rating == null || rating.getRating() == null ? 0 : rating.getRating().intValue());
			if(val == RecruitingService.ABSTENTION) {
				sb.append("<div class='f_rating_overview f_rating_abstention'><span> </span></div>");
			} else {
				sb.append("<div class='f_rating_overview f_rating_" + val + "'>");
				sb.append("<span>");
				if(rating != null && rating.getRating() != null) {
					sb.append(val);
				} else {
					sb.append("-");
				}
				sb.append("</span>");
				sb.append("</div>");
			}
		}
		sb.append("</div>");
	}
}
