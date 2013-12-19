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
package org.olat.course.assessment;

import org.olat.course.nodes.CourseNode;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NodeTableRow {
	
	private final int indent;
	private final String ident;
	private final String type;
	private final String shortTitle;
	private final String longTitle;
	
	private Float minScore;
	private Float maxScore;
	
	private boolean selectable;
	private boolean onyx = false;
	
	public NodeTableRow(int indent, CourseNode courseNode) {
		this(indent, courseNode.getIdent(), courseNode.getType(), courseNode.getShortTitle(), courseNode.getLongTitle());
	}

	public NodeTableRow(int indent, String ident, String type, String shortTitle, String longTitle) {
		this.indent = indent;
		this.ident = ident;
		this.type = type;
		this.shortTitle = shortTitle;
		this.longTitle = longTitle;
	}

	public int getIndent() {
		return indent;
	}

	public String getIdent() {
		return ident;
	}

	public String getType() {
		return type;
	}


	public String getShortTitle() {
		return shortTitle;
	}

	public String getLongTitle() {
		return longTitle;
	}
	
	public Float getMinScore() {
		return minScore;
	}


	public void setMinScore(Float minScore) {
		this.minScore = minScore;
	}


	public Float getMaxScore() {
		return maxScore;
	}


	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}


	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean isOnyx() {
		return onyx;
	}

	public void setOnyx(boolean onyx) {
		this.onyx = onyx;
	}
}
