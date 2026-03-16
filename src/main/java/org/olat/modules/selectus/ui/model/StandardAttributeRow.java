/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.model;

import java.util.Collections;
import java.util.List;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StandardAttributeRow {
	
	private final boolean mandatory;
	private final String type;
	private final String heading;
	private final String labels;
	private final List<String> fields;
	
	public StandardAttributeRow(boolean mandatory, String type, String heading, String labels, String field) {
		this(mandatory, type, heading, labels, Collections.singletonList(field));
	}
	
	public StandardAttributeRow(boolean mandatory, String type, String heading, String labels, List<String> fields) {
		this.mandatory = mandatory;
		this.type = type;
		this.heading = heading;
		this.labels = labels;
		this.fields = fields;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	public String getType() {
		return type;
	}
	
	public String getHeading() {
		return heading;
	}
	
	public String getLabels() {
		return labels;
	}
	
	public List<String> getFields() {
		return fields;
	}
}
