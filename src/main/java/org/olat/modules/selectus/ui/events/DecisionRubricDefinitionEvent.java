/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 27 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricDefinitionEvent extends MultiUserEvent {

	private static final long serialVersionUID = 6833772049693307570L;

	public static final String RUBRIC_DEFINITION_CHANGED = "rubric-definition-changed";
	
	private Long identitySenderKey;
	
	public DecisionRubricDefinitionEvent(Long identitySenderKey) {
		super(RUBRIC_DEFINITION_CHANGED);
		this.identitySenderKey = identitySenderKey;
	}

	public Long getIdentitySenderKey() {
		return identitySenderKey;
	}
}
