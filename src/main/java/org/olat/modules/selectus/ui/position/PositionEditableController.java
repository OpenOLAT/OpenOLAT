/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 28 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionEditableController {
	
	public Position getPosition();
	
	public void updatePosition(Position updatedPosition);

}
