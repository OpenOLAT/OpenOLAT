package com.frentix.olat.vitero.model;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public enum GroupRole {
	//position is important! 
	participant(0),
	assistant(1),
	teamleader(2),
	audience(3);
	
	private final int vmsValue;
	
	private GroupRole(int val) {
		this.vmsValue = val;
	}

	public int getVmsValue() {
		return vmsValue;
	}
}
