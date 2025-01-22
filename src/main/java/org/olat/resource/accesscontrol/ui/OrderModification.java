package org.olat.resource.accesscontrol.ui;

import org.olat.resource.accesscontrol.OrderStatus;

/**
 * 
 * Initial date: 22 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record OrderModification(Long orderKey, OrderStatus nextStatus) {
	//
}
