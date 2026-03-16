/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.attributes;

import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;

/**
 * 
 * Initial date: 19 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAttributeDefinitionConfiguration {
	
	private final AttributeConfiguration configuration;
	private final PositionAttributeDefinitionTypeEnum type;
	private final PositionAttributeDefinition attributeDefinition;
	
	public PositionAttributeDefinitionConfiguration(PositionAttributeDefinition attributeDefinition,
			PositionAttributeDefinitionTypeEnum type, AttributeConfiguration configuration) {
		this.type = type;
		this.configuration = configuration;
		this.attributeDefinition = attributeDefinition;
	}
	
	public Long getDefinitionKey() {
		return attributeDefinition.getKey();
	}
	
	public PositionAttributeDefinitionTypeEnum type() {
		return type;
	}
	
	public AttributeConfiguration configuration() {
		return configuration;
	}

	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}
}
