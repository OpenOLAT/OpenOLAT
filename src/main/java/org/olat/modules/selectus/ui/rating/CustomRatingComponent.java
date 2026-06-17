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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.rating.RatingComponent;
import org.olat.core.gui.components.rating.RatingType;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  24 aug 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CustomRatingComponent extends RatingComponent {

	private static final ComponentRenderer RENDERER = new CustomRatingRenderer();

	private final List<String> ratingLabels = new ArrayList<>();
	private CustomRatingFormItem formItem;
	
	private final boolean allowAbstain;

	/**
	 * Create a rating component with no title and a default explanation and hover
	 * texts. Use the setter methods to change the values. Use NULL values to
	 * disable texts (title, explanation, labels)
	 * 
	 * @param name
	 * @param currentRating the current rating
	 * @param maxRating maximum number that can be rated
	 * @param allowUserInput
	 */
	public CustomRatingComponent(String name, float currentRating, int maxRating, boolean allowUserInput, boolean allowAbstain) {
		super(name, RatingType.stars, currentRating, maxRating, allowUserInput);
		this.allowAbstain = allowAbstain;
		
		for (int i = 0; i < maxRating; i++) {
			// style: rating.5.3 => 3 out of 5 
			ratingLabels.add("rating." + maxRating + "."+ (i+1));			
		}
		setExplanation(null);
	}
	
	public CustomRatingComponent(String name, float currentRating, int maxRating, boolean allowUserInput, boolean allowAbstain, CustomRatingFormItem formItem) {
		this(name, currentRating, maxRating, allowUserInput, allowAbstain);
		this.formItem = formItem;
	}
	
	public CustomRatingFormItem getCustomFormItem() {
		return formItem;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		super.doDispatchRequest(ureq);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public boolean isAllowAbstain() {
		return allowAbstain;
	}

	public List<String> getRatingLabels() {
		return ratingLabels;
	}
}
