/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;

import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 27 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricEvent extends MultiUserEvent {

	private static final long serialVersionUID = 6833772049693307570L;
	public static final String RUBRIC_CHANGED = "rubric-changed";
	
	private Long identitySenderKey;
	private DecisionRubric rubric;
	
	public DecisionRubricEvent(DecisionRubric rubric, Long identitySenderKey) {
		super(RUBRIC_CHANGED);
		this.rubric = rubric;
		this.identitySenderKey = identitySenderKey;
	}

	public Long getIdentitySenderKey() {
		return identitySenderKey;
	}
	
	public DecisionRubric getRubric() {
		return rubric;
	}
}
