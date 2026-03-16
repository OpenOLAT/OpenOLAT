/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 9 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAndAttributeDefinition {
	
	private final Position position;
	private final PositionAttributeDefinition attributeDefinition;
	
	public PositionAndAttributeDefinition(Position position, PositionAttributeDefinition attributeDefinition) {
		this.position = position;
		this.attributeDefinition = attributeDefinition;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}
}
