/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.mail;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplateRef {
	
	private Long key;
	private final String id;
	private final String name;
	
	public PositionMailTemplateRef(Long key, String id, String name) {
		this.key = key;
		this.id = id;
		this.name = name;
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) { 
		this.key = key;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public boolean match(String templateName) {
		return (key != null && key.toString().equals(templateName))
				|| (id != null && id.equalsIgnoreCase(templateName))
				|| (name != null && name.equalsIgnoreCase(templateName));
	}
}
