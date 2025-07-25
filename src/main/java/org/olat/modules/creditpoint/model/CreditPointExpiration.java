package org.olat.modules.creditpoint.model;

import org.olat.modules.creditpoint.CreditPointExpirationType;

/**
 * 
 * Initial date: 25 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record CreditPointExpiration(Integer value, CreditPointExpirationType unit) {
	//
}
