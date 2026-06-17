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
package org.olat.modules.selectus;

import java.util.List;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.comment.PositionComments;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CommentService {
	
	public List<ApplicationComment> getComments(ApplicationRef application);
	

	public List<ApplicationComment> getComments(ApplicationRef application, Identity reviewer);

	
	/**
	 * The method add a comment to the specified reviewer (the review)
	 * 
	 * @param application The application
	 * @param reviewer The reviewer to comment
	 * @param comment
	 * @param author
	 * @return
	 */
	public ApplicationComment addComment(Application application, Identity reviewer, String comment, Identity author, ApplicationComment parentComment);
	
	public ApplicationComment updateComment(ApplicationComment comment, String text);
	
	public void deleteComment(ApplicationComment comment);
	
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application);
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application, Identity reviewer);
	
	public ApplicationCommentVoteStatistics getVotes(ApplicationComment appComment);
	
	public void vote(ApplicationComment comment, Identity identity, boolean agree);
	
	public PositionComments getComments(PositionRef position);

}
