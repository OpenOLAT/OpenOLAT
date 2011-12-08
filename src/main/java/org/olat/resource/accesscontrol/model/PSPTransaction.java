package org.olat.resource.accesscontrol.model;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for PSPTransaction
 * 
 * <P>
 * Initial Date:  15 juin 2011 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface PSPTransaction {
	
	public Long getOrderId();
	
	public Long getOrderPartId();
	
	public PSPTransactionStatus getSimplifiedStatus();
}
