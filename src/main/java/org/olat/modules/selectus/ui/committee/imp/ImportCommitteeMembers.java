/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCommitteeMembers implements ChoosePosition {
	
	private PositionLight sourcePosition;
	private final Position targetPosition;
	private List<ImportCommitteeMember> committee = new ArrayList<>();
	
	public ImportCommitteeMembers(Position targetPosition) {
		this.targetPosition = targetPosition;
	}
	
	public Position getTargetPosition() {
		return targetPosition;
	}
	
	@Override
	public List<Position> getExcludedPositions() {
		return targetPosition == null ? List.of() : List.of(targetPosition);
	}

	public PositionLight getSourcePosition() {
		return sourcePosition;
	}
	
	@Override
	public PositionLight getSelectedPosition() {
		return getSourcePosition();
	}

	@Override
	public void setSelectedPosition(PositionLight position) {
		setSourcePosition(position);
	}
	
	public void setSourcePosition(PositionLight sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
	
	public void clearCommittee() {
		committee.clear();
	}
	
	public void addCommitteeMember(Identity identity, PositionRole role) {
		committee.add(new ImportCommitteeMember(identity, role));
	}
	
	public List<ImportCommitteeMember> getCommittee() {
		return new ArrayList<>(committee);
	}
}
