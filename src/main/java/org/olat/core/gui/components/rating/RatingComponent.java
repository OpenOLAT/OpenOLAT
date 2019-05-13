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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;


/**
 * Description:<br>
 * The rating controller offers a view for users to rate on something using a
 * star-like view.
 * 
 * <h3>Events fired by this conmponent:</h3> 
 * RatingEvent that contains the new rating
 * <P>
 * Initial Date:  31.10.2008 <br>
 * @author gnaegi
 */
public class RatingComponent extends AbstractComponent {
	private static final Logger log = Tracing.createLoggerFor(RatingComponent.class);
	private static final ComponentRenderer RENDERER = new RatingRenderer();
	private List<String> ratingLabels;
	private boolean translateRatingLabels;
	private String title;
	private boolean translateTitle;
	private String explanation;
	private boolean translateExplanation;
	private boolean showRatingAsText;
	private boolean allowUserInput;
	private String cssClass;
	private float currentRating;
	private final RatingFormItem ratingItem;


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
	public RatingComponent(String name, float currentRating, int maxRating, boolean allowUserInput) {
		this(null, name, currentRating, maxRating, allowUserInput, null);
	}
		
	public RatingComponent(String id, String name, float currentRating, int maxRating, boolean allowUserInput, RatingFormItem ratingItem) {
		super(id, name);
		this.ratingItem = ratingItem;
		if (currentRating > maxRating) 
			throw new AssertException("Current rating set to higher value::" + currentRating + " than the maxRating::" + maxRating);
		this.allowUserInput = allowUserInput;
		this.currentRating = currentRating;
		// use default values for the other stuff
		ratingLabels = new ArrayList<>(maxRating);
		for (int i = 0; i < maxRating; i++) {
			// style: rating.5.3 => 3 out of 5 
			ratingLabels.add("rating." + maxRating + "."+ (i+1));			
		}
		translateRatingLabels = true;
		title = null;
		translateTitle = true;
		explanation = allowUserInput ? "rating.explanation" : null;
		translateExplanation = true;
		showRatingAsText = false;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		setDirty(true);
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		try {
			float rating = Float.parseFloat(cmd);
			// update GUI
			setCurrentRating(rating);
			// notify listeners
			Event event = new RatingEvent(rating);
			fireEvent(ureq, event);
		} catch (NumberFormatException e) {
			log.error("Error while parsing rating value::" + cmd);
		}		
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	//
	// Various getter and setter methods
	//
	protected RatingFormItem getFormItem() {
		return ratingItem;
	}

	// only package scope, used by renderer
	List<String> getRatingLabel() {
		return ratingLabels;
	}
	
	public String getRatingLabel(int position) {
		if (position >= ratingLabels.size()) {
			throw new AssertException("Can not get rating at position::" + position + " in rating array of size::" + ratingLabels.size() + " in component::" + getComponentName());
		}
		return ratingLabels.get(position);
	}

	public void setLevelLabel(int position, String ratingLabel) {
		if (position >= ratingLabels.size()) {
			throw new AssertException("Can not set rating at position::" + position + " in rating array of size::" + ratingLabels.size() + " in component::" + getComponentName());
		}
		this.ratingLabels.set(position, ratingLabel);
		setDirty(true);
	}

	public boolean isTranslateRatingLabels() {
		return translateRatingLabels;
	}

	public void setTranslateRatingLabels(boolean translateRatingLabels) {
		this.translateRatingLabels = translateRatingLabels;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		setDirty(true);
	}

	public boolean isTranslateTitle() {
		return translateTitle;
	}

	public void setTranslateTitle(boolean translateTitle) {
		this.translateTitle = translateTitle;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
		setDirty(true);
	}

	public boolean isTranslateExplanation() {
		return translateExplanation;
	}

	public void setTranslateExplanation(boolean translateExplanation) {
		this.translateExplanation = translateExplanation;
	}

	public boolean isShowRatingAsText() {
		return showRatingAsText;
	}

	public void setShowRatingAsText(boolean showRatingAsText) {
		this.showRatingAsText = showRatingAsText;
	}

	public boolean isAllowUserInput() {
		return allowUserInput;
	}

	public void setAllowUserInput(boolean allowUserInput) {
		this.allowUserInput = allowUserInput;
		setDirty(true);
	}

	public int getRatingSteps() {
		return ratingLabels.size();
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		this.setDirty(true);
	}

	public float getCurrentRating() {
		return currentRating;
	}

	public void setCurrentRating(float currentRating) {
		this.currentRating = currentRating;
		setDirty(true);
	}

}
