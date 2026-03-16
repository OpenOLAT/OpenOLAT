/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.application.ApplicationRefImpl;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingChangedEvent extends MultiUserEvent  {

	private static final long serialVersionUID = 1155584122345848263L;
	public static final String RATING_CHANGED = "rating-changed"; 
	
	private final ApplicationRef application;
	private final Long doerIdentityKey;
	private final String emitter;
	
	public RatingChangedEvent(ApplicationRef application, Long doerIdentityKey, String emitter) {
		super(RATING_CHANGED);
		this.application = new ApplicationRefImpl(application.getKey());
		this.doerIdentityKey = doerIdentityKey;
		this.emitter = emitter;
	}
	
	public ApplicationRef getApplication() {
		return application;
	}
	
	public Long getDoerIdentityKey() {
		return doerIdentityKey;
	}
	
	public String getEmitter() {
		return emitter;
	}
}
