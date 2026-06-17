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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteImpl;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ApplicationCommentVoteDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ApplicationCommentVoteImpl createVote(Identity voter, ApplicationComment comment, boolean up) {
		ApplicationCommentVoteImpl vote = new ApplicationCommentVoteImpl();
		vote.setCreationDate(new Date());
		vote.setLastModified(vote.getCreationDate());
		vote.setUp(up);
		vote.setVoter(voter);
		vote.setComment(comment);
		dbInstance.getCurrentEntityManager().persist(vote);
		return vote;
	}
	
	public ApplicationCommentVoteImpl getVote(IdentityRef voter, ApplicationComment comment) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select vote from rapplicationcommentvote as vote")
		  .append(" where vote.voter.key=:identityKey and vote.comment.key=:commentKey");
		
		List<ApplicationCommentVoteImpl> votes = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), ApplicationCommentVoteImpl.class)
			.setParameter("identityKey", voter.getKey())
			.setParameter("commentKey", comment.getKey())
			.getResultList();
		return votes == null ||  votes.isEmpty() ? null : votes.get(0);
	}
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment,")
		  .append(" (select count(distinct upVote.key) from rapplicationcommentvote as upVote where upVote.comment.key=comment.key and upVote.up=true) as votesUp,")
		  .append(" (select count(distinct downVote.key) from rapplicationcommentvote as downVote where downVote.comment.key=comment.key and downVote.up=false) as votesDown")
		  .append(" from rapplicationcomment as comment")
		  .append(" left join fetch comment.reviewer as reviewer")
		  .append(" where comment.application.key=:applicationKey");
		
		List<Object[]> votes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
		
		List<ApplicationCommentVoteStatistics> stats = new ArrayList<>(votes.size());
		for(Object[] vote:votes) {
			ApplicationComment comment = (ApplicationComment)vote[0];
			int up = vote[1] == null ? 0 : ((Number)vote[1]).intValue();
			int down = vote[2] == null ? 0 : ((Number)vote[2]).intValue();
			stats.add(new ApplicationCommentVoteStatistics(comment, up, down));
		}

		return stats;
	}
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application, IdentityRef reviewer) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment,")
		  .append(" (select count(distinct voteUp.key) from rapplicationcommentvote as voteUp where voteUp.comment.key=comment.key and voteUp.up=true) as votesUp,")
		  .append(" (select count(distinct voteDown.key) from rapplicationcommentvote as voteDown where voteDown.comment.key=comment.key and voteDown.up=false) as votesDown")
		  .append(" from rapplicationcomment as comment")
		  .append(" where comment.application.key=:applicationKey and comment.reviewer.key=:reviewerKey");
		
		List<Object[]> votes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("applicationKey", application.getKey())
				.setParameter("reviewerKey", reviewer.getKey())
				.getResultList();
		
		List<ApplicationCommentVoteStatistics> stats = new ArrayList<>(votes.size());
		for(Object[] vote:votes) {
			ApplicationComment comment = (ApplicationComment)vote[0];
			int up = vote[1] == null ? 0 : ((Number)vote[1]).intValue();
			int down = vote[2] == null ? 0 : ((Number)vote[2]).intValue();
			stats.add(new ApplicationCommentVoteStatistics(comment, up, down));
		}

		return stats;
	}
	
	public ApplicationCommentVoteStatistics getVotes(ApplicationComment comment) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment.key,")
		  .append(" (select count(distinct voteUp.key) from rapplicationcommentvote as voteUp where voteUp.comment.key=comment.key and voteUp.up=true) as votesUp,")
		  .append(" (select count(distinct voteDown.key) from rapplicationcommentvote as voteDown where voteDown.comment.key=comment.key and voteDown.up=false) as votesDown")
		  .append(" from rapplicationcomment as comment")
		  .append(" where comment.key=:commentKey");
		
		List<Object[]> votes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("commentKey", comment.getKey())
				.getResultList();
		
		int up = 0;
		int down = 0;
		if(votes != null && votes.size() == 1) {
			Object[] vote = votes.get(0);
			up = vote[1] == null ? 0 : ((Number)vote[1]).intValue();
			down = vote[2] == null ? 0 : ((Number)vote[2]).intValue();
		}

		return new ApplicationCommentVoteStatistics(comment, up, down);
	}
	
	public ApplicationCommentVoteImpl update(ApplicationCommentVoteImpl vote) {
		vote.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(vote);
	}
	
	public int deleteVotes(ApplicationComment comment) {
		String query= "delete from rapplicationcommentvote as vote where vote.comment.key=:commentKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query).setParameter("commentKey", comment.getKey())
				.executeUpdate();
	}

}
