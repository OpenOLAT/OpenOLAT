package org.olat.test.util.setup;

/**
 * Originally the test author could choose from a list of possible
 * setups, but later there was only one setup type used: TWO_NODE_CLUSTER. <br/>
 * It was decided that it would be wiser to run all tests with the same setup.
 * 
 * @author lavinia
 *
 */
public enum SetupType {

	/** @deprecated not supported anymore */
	CLEAN_AND_RESTARTED_SINGLE_VM,
	/** @deprecated not supported anymore */
	CLEAN_AND_RESTARTED_TWO_NODE_CLUSTER,
	/** @deprecated not supported anymore */
	RESTARTED_SINGLE_VM,
	/** @deprecated not supported anymore */
	RESTARTED_TWO_NODE_CLUSTER,
	/** @deprecated not supported anymore */
	SINGLE_VM,
	TWO_NODE_CLUSTER;
	
	/** @deprecated not supported anymore */
	public boolean isSingleVm() {
		if (this==CLEAN_AND_RESTARTED_SINGLE_VM) return true;
		if (this==RESTARTED_SINGLE_VM) return true;
		if (this==SINGLE_VM) return true;
		return false;
	}
}
