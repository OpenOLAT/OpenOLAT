/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
