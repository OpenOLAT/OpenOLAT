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
package org.olat.repository.ui.list;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;

/**
 * Delegate to the controller the job of forgeing the links and the mapperUrl.
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDataSourceUIFactory {

	private final RepositoryModule repositoryModule;

	protected final FormUIFactory uifactory = FormUIFactory.getInstance();
	protected final MapperKey mapperThumbnailKey;
	protected final Translator translator;
	protected final boolean guestOnly;

	public RepositoryEntryDataSourceUIFactory(RepositoryModule repositoryModule,
											  MapperService mapperService,
											  UserRequest userRequest) {
		this.repositoryModule = repositoryModule;
		this.mapperThumbnailKey = mapperService.register(null,
				"repositoryentryImage", new RepositoryEntryImageMapper());
		this.translator = Util.createPackageTranslator(RepositoryService.class, userRequest.getLocale());
		this.guestOnly = userRequest.getUserSession().getRoles().isGuestOnly();
	}

	/**
	 * TODO sev26
	 * All this procedural "forge"-functions should be integrated in the
	 * {@link RepositoryEntryRow} class.
	 */
	public void forgeLinks(RepositoryEntryRow row) {
		forgeMarkLink(row);
		forgeSelectLink(row);
		forgeStartLink(row);
		forgeDetails(row);
		forgeRatings(row);
		forgeComments(row);
	}

	protected void forgeMarkLink(RepositoryEntryRow row) {
		if (!guestOnly) {
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setTitle(getTranslator().translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setUserObject(row);
			row.setMarkLink(markLink);
		}
	}

	protected void forgeSelectLink(RepositoryEntryRow row) {
		String displayName = StringHelper.escapeHtml(row.getDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", displayName, null, null, Link.NONTRANSLATED);
		if (row.isClosed()) {
			selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
		}
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	protected void forgeStartLink(RepositoryEntryRow row) {
		String label;
		boolean isStart = true;
		if (!row.isMembersOnly() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty() && !row.isMember()) {
			if (guestOnly) {
				if (row.getAccess() == RepositoryEntry.ACC_USERS_GUESTS) {
					label = "start";
				} else {
					return;
				}
			} else {
				label = "book";
				isStart = false;
			}
		} else {
			label = "start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS(isStart ? "o_start btn-block" : "o_book btn-block");
		startLink.setIconRightCSS("o_icon o_icon_start");
		row.setStartLink(startLink);
	}

	protected void forgeDetails(RepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsLink.setCustomEnabledLinkCSS("o_details");
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
	}

	protected void forgeRatings(RepositoryEntryRow row) {
		if (repositoryModule.isRatingEnabled()) {
			if (guestOnly) {
				Double averageRating = row.getAverageRating();
				float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();

				RatingFormItem ratingCmp
						= new RatingFormItem("rat_" + row.getKey(), averageRatingValue, 5, false);
				row.setRatingFormItem(ratingCmp);
				ratingCmp.setUserObject(row);
			} else {
				Integer myRating = row.getMyRating();
				Double averageRating = row.getAverageRating();
				long numOfRatings = row.getNumOfRatings();

				float ratingValue = myRating == null ? 0f : myRating.floatValue();
				float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
				RatingWithAverageFormItem ratingCmp
						= new RatingWithAverageFormItem("rat_" + row.getKey(), ratingValue, averageRatingValue, 5, numOfRatings);
				row.setRatingFormItem(ratingCmp);
				ratingCmp.setUserObject(row);
			}
		}
	}

	protected void forgeComments(RepositoryEntryRow row) {
		if (repositoryModule.isCommentEnabled()) {
			long numOfComments = row.getNumOfComments();
			String title = "(" + numOfComments + ")";
			FormLink commentsLink = uifactory.addFormLink("comments_" + row.getKey(), "comments", title, null, null, Link.NONTRANSLATED);
			commentsLink.setUserObject(row);
			String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
			commentsLink.setCustomEnabledLinkCSS("o_comments");
			commentsLink.setIconLeftCSS(css);
			row.setCommentsLink(commentsLink);
		}
	}

	public String getMapperThumbnailUrl() {
		return mapperThumbnailKey.getUrl();
	}

	public Translator getTranslator() {
		return translator;
	}
}
