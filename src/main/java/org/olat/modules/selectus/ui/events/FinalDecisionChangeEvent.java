/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FinalDecisionChangeEvent extends MultiUserEvent  {

	private static final long serialVersionUID = 9208385756101684581L;
	public static final String FINAL_DECISION = "final-decision";
	
	private final Long applicationKey;
	private final int decision;
	private final Long emitterKey;
	
	public FinalDecisionChangeEvent(Long applicationKey, int decision, Long emitterKey) {
		super(FINAL_DECISION);
		this.decision = decision;
		this.applicationKey = applicationKey;
		this.emitterKey = emitterKey;
	}

	public int getDecision() {
		return decision;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Long getEmitterKey() {
		return emitterKey;
	}
}
