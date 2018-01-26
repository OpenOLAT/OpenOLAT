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

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 24.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RatingCellRenderer implements FlexiCellRenderer {

	private final int maxRating;
	
	public RatingCellRenderer(int maxRating) {
		this.maxRating = maxRating;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof UserRating) {
			UserRating userRating = (UserRating) cellValue;
			int rating = userRating.getRating() != null? userRating.getRating(): 0;
			
			target.append("<div class='o_rating'>");
			target.append("<div class='o_rating_items'>");
			for (int i = 0; i < maxRating; i++) {
				target.append("<a class='o_icon o_icon-lg ");
				if (rating >= i+1) {
					target.append("o_icon_rating_on");				
				} else {
					target.append("o_icon_rating_off");				
				}						
				target.append("' />");
			}
			target.append("</div>"); //o_rating_items
			target.append("</div>"); //o_rating
		}
	}

}
