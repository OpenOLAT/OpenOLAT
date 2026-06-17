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
package org.olat.modules.selectus.ui.components;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.UserRatingMapper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SingleRatingCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof UserRatingMapper) {
			UserRatingMapper mapper = (UserRatingMapper)cellValue;
			float currentRating = mapper.getCurrentRating();
			render(target, currentRating, translator);
		} else if(cellValue instanceof UserRating) {
			UserRating rating = (UserRating)cellValue;
			float currentRating = rating.getRating() == null ? 0.0f : rating.getRating().floatValue();
			render(target, currentRating, translator);
		}
	}

	private void render(StringOutput target, float currentRating, Translator translator) {
		if(currentRating > 2.9) {
			target.append(translator.translate("rating.2"));
		} else if(currentRating > 1.9) {
			target.append(translator.translate("rating.1"));
		} else if(currentRating > 0.9) {
			target.append(translator.translate("rating.0"));
		} else if(currentRating < -31) {
			target.append("<i class='o_icon o_icon_abstain'> </i>");
		}
	}
}
