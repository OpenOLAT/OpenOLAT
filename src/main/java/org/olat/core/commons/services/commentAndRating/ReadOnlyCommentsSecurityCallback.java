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
package org.olat.core.commons.services.commentAndRating;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserComment;

/**
 * 
 * Initial date: 1 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReadOnlyCommentsSecurityCallback implements CommentAndRatingSecurityCallback {

	@Override
	public boolean canViewComments() {
		return true;
	}

	@Override
	public boolean canCreateComments() {
		return false;
	}

	@Override
	public boolean canReplyToComment(UserComment comment) {
		return false;
	}

	@Override
	public boolean canUpdateComment(UserComment comment, List<UserComment> allComments) {
		return false;
	}

	@Override
	public boolean canDeleteComment(UserComment comment) {
		return false;
	}

	@Override
	public boolean canViewRatingAverage() {
		return true;
	}

	@Override
	public boolean canViewOtherUsersRatings() {
		return false;
	}

	@Override
	public boolean canRate() {
		return false;
	}
}
