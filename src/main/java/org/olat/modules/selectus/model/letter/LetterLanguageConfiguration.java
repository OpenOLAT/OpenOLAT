/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.letter;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterLanguageConfiguration {
	
	private Map<String,String> values = new HashMap<>();
	
	public String getValue(String id) {
		return values.get(id);
	}
	
	public void putValue(String id, String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			values.put(id, value);
		} else {
			values.remove(id);
		}
	}

}
