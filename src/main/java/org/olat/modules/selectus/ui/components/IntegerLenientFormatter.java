/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import java.util.function.UnaryOperator;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IntegerLenientFormatter implements UnaryOperator<String> {

	@Override
	public String apply(String t) {
		return StringHelper.lenientInteger(t);
	}	
}
