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
package org.olat.course.highscore.ui;
/**
 * Initial Date:  10.08.2016 <br>
 * @author fkiefer
 */
import org.olat.core.id.Identity;

public class HighScoreTableEntry {
	
	public static final int RANK = 0;
	public static final int SCORE = 1;
	public static final int NAME = 2;
	
	private float score;
	private String name;
	private Identity identity;
	private int rank;
	
	public HighScoreTableEntry(float score, String name, Identity identity){
		this.score = score;
		this.name = name;
		this.identity = identity;
	}
	
	public float getScore (){
		return score;
	}
	public String getName(){
		return name;
	}
	public Identity getIdentity() {
		return identity;
	}
	public int getRank() {
		return rank;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	
}
