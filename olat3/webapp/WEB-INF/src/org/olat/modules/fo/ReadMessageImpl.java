package org.olat.modules.fo;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;

public class ReadMessageImpl extends PersistentObject implements ReadMessage {

	private Identity identity;
	private Message message;
	private Forum forum;

	ReadMessageImpl() {
		//default constructor
	}

	public Forum getForum() {
		return forum;
	}

	public void setForum(Forum forum) {
		this.forum = forum;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
	
	
	
}
