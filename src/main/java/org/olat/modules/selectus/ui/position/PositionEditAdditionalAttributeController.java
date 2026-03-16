/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import org.olat.core.gui.control.Controller;

import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * 
 * 
 * Initial date: 9 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionEditAdditionalAttributeController extends Controller {
	
	public PositionAttributeDefinition getAttributeDefinition();
	
}
