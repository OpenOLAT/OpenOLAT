/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 22.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMembershipsStats {
	
	private final int numAsSecretary;
	private final int numAsHead;
	private final int numAsExOfficio;
	
	public CommitteeMembershipsStats(int numAsSecretary, int numAsHead, int numAsExOfficio) {
		this.numAsHead = numAsHead;
		this.numAsSecretary = numAsSecretary;
		this.numAsExOfficio = numAsExOfficio;
	}
	
	public static CommitteeMembershipsStats empty() {
		return new CommitteeMembershipsStats(0, 0, 0);
	}
	
	public int getNumAsSecretary() {
		return numAsSecretary;
	}
	
	public int getNumAsHead() {
		return numAsHead;
	}
	
	public int getNumAsExOfficio() {
		return numAsExOfficio;
	}
}
