/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import java.util.List;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ChoosePosition {
	
	public List<Position> getExcludedPositions();
	
	public PositionLight getSelectedPosition();
	
	public void setSelectedPosition(PositionLight position);

}
