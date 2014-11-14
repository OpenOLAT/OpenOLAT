/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.tree;

/**
 * 
 * Initial date: 13.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InsertionPoint {
	
	private final String nodeId;
	private final Position position;
	
	
	public InsertionPoint(String nodeId, Position position) {
		this.nodeId = nodeId;
		this.position = position;
	}
	
	public String getNodeId() {
		return nodeId;
	}

	public Position getPosition() {
		return position;
	}

	public enum Position {
		up,
		down,
		under;
		
		public static boolean hasPosition(Position pos, Position[] arr) {
			if(arr.length > 0) {
				if(arr[0] == pos) return true;
			}
			if(arr.length > 1) {
				if(arr[1] == pos) return true;
			}
			if(arr.length > 2) {
				if(arr[2] == pos) return true;
			}
			return false;
		}
	}
}
