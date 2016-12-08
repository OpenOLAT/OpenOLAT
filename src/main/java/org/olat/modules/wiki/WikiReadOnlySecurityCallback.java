package org.olat.modules.wiki;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.modules.fo.ForumCallback;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiReadOnlySecurityCallback implements WikiSecurityCallback {
	
	private final boolean isGuestOnly;
	private final boolean isModerator;
	
	public WikiReadOnlySecurityCallback(boolean isGuestOnly, boolean isModerator) {
		this.isGuestOnly = isGuestOnly;
		this.isModerator = isModerator;
	}

	@Override
	public boolean mayEditAndCreateArticle() {
		return false;
	}

	@Override
	public boolean mayEditWikiMenu() {
		return false;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

	@Override
	public boolean mayModerateForum() {
		return false;
	}

	@Override
	public ForumCallback getForumCallback() {
		return new WikiForumCallback();
	}
	
	private class WikiForumCallback implements ForumCallback {

		@Override
		public boolean mayUsePseudonym() {
			return false;
		}

		@Override
		public boolean mayOpenNewThread() {
			return false;
		}

		@Override
		public boolean mayReplyMessage() {
			return false;
		}

		@Override
		public boolean mayEditOwnMessage() {
			return false;
		}

		@Override
		public boolean mayDeleteOwnMessage() {
			return false;
		}

		@Override
		public boolean mayEditMessageAsModerator() {
			return false;
		}

		@Override
		public boolean mayDeleteMessageAsModerator() {
			return false;
		}

		@Override
		public boolean mayArchiveForum() {
			return !isGuestOnly;
		}

		@Override
		public boolean mayFilterForUser() {
			return !isGuestOnly && isModerator;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return null;
		}
	}
}
