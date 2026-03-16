/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.position;

import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 27 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionLightWithMembership implements PositionRef {
	
	private final PositionLight position;
	
	private final boolean head;
	private final boolean secretary;
	private final boolean committee;
	private final boolean exOfficio;
	
	public PositionLightWithMembership(PositionLight position, boolean head, boolean secretary, boolean committee, boolean exOfficio) {
		this.position = position;
		this.head = head;
		this.secretary = secretary;
		this.committee = committee;
		this.exOfficio = exOfficio;
	}

	@Override
	public Long getKey() {
		return position.getKey();
	}

	public PositionLight getPosition() {
		return position;
	}

	public boolean isHead() {
		return head;
	}

	public boolean isSecretary() {
		return secretary;
	}

	public boolean isCommittee() {
		return committee;
	}

	public boolean isExOfficio() {
		return exOfficio;
	}
	
	public PositionRole getRole() {
		PositionRole role = null;
		if(head) {
			role = PositionRole.head;
		} else if(secretary) {
			role = PositionRole.secretary;
		} else if(committee) {
			role = PositionRole.member;
		} else if(exOfficio) {
			role = PositionRole.exofficio;
		}
		return role;
	}
}
