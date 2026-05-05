/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.copy;

import java.util.List;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.CopyApplicationParameters;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.ui.committee.imp.ChoosePosition;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyApplicationContext implements ChoosePosition {
	
	private final Position sourcePosition;
	private PositionLight selectedPosition;
	private final List<ApplicationLight> apps;
	private List<CopyApplicationParameters.Copy> typeOfData;
	
	public CopyApplicationContext(Position sourcePosition, List<ApplicationLight> apps) {
		this.apps = apps;
		this.sourcePosition = sourcePosition;
	}
	
	public Position getSourcePosition() {
		return sourcePosition;
	}
	
	public List<ApplicationLight> getApplications() {
		return apps;
	}
	
	@Override
	public List<Position> getExcludedPositions() {
		return sourcePosition == null ? List.of() : List.of(sourcePosition);
	}

	@Override
	public PositionLight getSelectedPosition() {
		return selectedPosition;
	}

	@Override
	public void setSelectedPosition(PositionLight position) {
		selectedPosition = position;
	}

	public List<CopyApplicationParameters.Copy> getTypeOfData() {
		return typeOfData;
	}

	public void setTypeOfData(List<CopyApplicationParameters.Copy> typeOfData) {
		this.typeOfData = typeOfData;
	}

}
