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
package org.olat.modules.video.ui;

/**
 * 
 * 
 * Initial date: 6 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoDisplayOptions {
	
	private boolean autoplay;
	private boolean showComments;
	private boolean showRating;
	private boolean useContainerForCommentsAndRatings;
	private boolean showTitle;
	private boolean showDescription;
	private boolean autoWidth;
	private boolean readOnly;
	private String descriptionText;
	private boolean showAnnotations;
	private boolean showQuestions;
	private boolean showSegments;
	private boolean showPoster;
	private boolean alwaysShowControls;
	private boolean dragAnnotations;
	private boolean clickToPlayPause;
	private boolean authorMode;
	private boolean forwardSeekingRestricted;
	
	public static VideoDisplayOptions valueOf(boolean autoplay, boolean showComments, boolean showRating, boolean useContainerForCommentsAndRatings, boolean showTitle, boolean showDescription,
			boolean autoWidth, String descriptionText, boolean authorMode, boolean readOnly, boolean forwardSeekingRestricted) {
		VideoDisplayOptions options = new VideoDisplayOptions();
		options.setAutoplay(autoplay);
		options.setAutoWidth(autoWidth);
		options.setDescriptionText(descriptionText);
		options.setReadOnly(readOnly);
		options.setShowComments(showComments);
		options.setShowRating(showRating);
		options.setUseContainerForCommentsAndRatings(useContainerForCommentsAndRatings);
		options.setShowTitle(showTitle);
		options.setShowDescription(showDescription);
		options.setForwardSeekingRestricted(forwardSeekingRestricted);
		options.setShowPoster(true);
		options.setShowQuestions(true);
		options.setShowAnnotations(true);
		options.setAlwaysShowControls(false);
		options.setDragAnnotations(false);
		options.setClickToPlayPause(true);
		options.setAuthorMode(authorMode);
		return options;
	}
	
	public static VideoDisplayOptions disabled() {
		VideoDisplayOptions options = new VideoDisplayOptions();
		options.setAutoplay(false);
		options.setAutoWidth(false);
		options.setDescriptionText(null);
		options.setReadOnly(false);
		options.setShowComments(false);
		options.setShowRating(false);
		options.setShowTitle(false);
		options.setShowDescription(false);
		options.setForwardSeekingRestricted(false);
		options.setShowPoster(true);
		options.setShowQuestions(false);
		options.setShowAnnotations(false);
		options.setAlwaysShowControls(false);
		options.setDragAnnotations(false);
		options.setClickToPlayPause(true);
		options.setAuthorMode(false);
		return options;
	}
	
	public boolean isAutoplay() {
		return autoplay;
	}
	
	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}
	
	public boolean isAlwaysShowControls() {
		return alwaysShowControls;
	}

	public void setAlwaysShowControls(boolean alwaysShowControls) {
		this.alwaysShowControls = alwaysShowControls;
	}

	public boolean isClickToPlayPause() {
		return clickToPlayPause;
	}

	public void setClickToPlayPause(boolean clickToPlayPause) {
		this.clickToPlayPause = clickToPlayPause;
	}

	public boolean isShowComments() {
		return showComments;
	}
	
	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}
	
	public boolean isShowRating() {
		return showRating;
	}
	
	public void setShowRating(boolean showRating) {
		this.showRating = showRating;
	}
	
	public boolean isShowAnnotations() {
		return showAnnotations;
	}

	public void setShowAnnotations(boolean showAnnotations) {
		this.showAnnotations = showAnnotations;
	}
	
	public boolean isDragAnnotations() {
		return dragAnnotations;
	}

	public void setDragAnnotations(boolean dragAnnotations) {
		this.dragAnnotations = dragAnnotations;
	}

	public boolean isShowQuestions() {
		return showQuestions;
	}

	public void setShowQuestions(boolean showQuestions) {
		this.showQuestions = showQuestions;
	}

	public boolean isShowSegments() {
		return showSegments;
	}

	public void setShowSegments(boolean showSegments) {
		this.showSegments = showSegments;
	}

	public boolean isShowTitle() {
		return showTitle;
	}
	
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}
	
	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}
	
	public boolean isAutoWidth() {
		return autoWidth;
	}
	
	public void setAutoWidth(boolean autoWidth) {
		this.autoWidth = autoWidth;
	}
	
	public boolean isShowPoster() {
		return showPoster;
	}

	public void setShowPoster(boolean showPoster) {
		this.showPoster = showPoster;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public String getDescriptionText() {
		return descriptionText;
	}
	
	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
	}

	public boolean isAuthorMode() {
		return authorMode;
	}

	public void setAuthorMode(boolean authorMode) {
		this.authorMode = authorMode;
	}

	public boolean isForwardSeekingRestricted() {
		return forwardSeekingRestricted;
	}
	
	public void setForwardSeekingRestricted(boolean forwardSeekingRestricted) {
		this.forwardSeekingRestricted = forwardSeekingRestricted;
	}

	/**
	 * @return true: store comments and ratings using the container resource given
	 *         to the VideoDisplayController (e.g. the course resource); 
	 *         false: use the video resource to store the comments and ratings
	 */
	public boolean isUseContainerForCommentsAndRatings() {
		return useContainerForCommentsAndRatings;
	}
	
	/**
	 * true: store comments and ratings using the container resource given to the
	 * VideoDisplayController (e.g. the course resource); false: use the video
	 * resource to store the comments and ratings
	 * 
	 * @param useContainerForCommentsAndRatings
	 */
	public void setUseContainerForCommentsAndRatings(boolean useContainerForCommentsAndRatings) {
		this.useContainerForCommentsAndRatings = useContainerForCommentsAndRatings;
	}
}
