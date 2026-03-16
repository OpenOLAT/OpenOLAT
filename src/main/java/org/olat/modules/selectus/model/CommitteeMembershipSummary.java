/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 27.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMembershipSummary {
	
	private final boolean member;
	private final boolean secretary;
	private final boolean head;
	private final boolean exofficio;
	
	public CommitteeMembershipSummary(boolean member, boolean secretary, boolean head, boolean exofficio) {
		this.member = member;
		this.secretary = secretary;
		this.head = head;
		this.exofficio = exofficio;
	}

	public boolean isMember() {
		return member;
	}

	public boolean isSecretary() {
		return secretary;
	}

	public boolean isHead() {
		return head;
	}

	public boolean isExofficio() {
		return exofficio;
	}
}
