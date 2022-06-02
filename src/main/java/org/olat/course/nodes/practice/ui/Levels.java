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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Levels {

	private int not = 0;
	private int total = 0;
	private final int[] levelArr;
	
	public Levels(int numOflevels) {
		levelArr = new int[numOflevels];
		for(int i=levelArr.length; i-->0; ) {
			levelArr[i] = 0;
		}
	}
	
	public List<Integer> getLevels() {
		List<Integer> levels = new ArrayList<>();
		for(int i=0; i<levelArr.length; i++) {
			levels.add(Integer.valueOf(i + 1));
		}
		return levels;
	}
	
	public String getCssClass(int i) {
		switch(i) {
			case 0: return getNotCssClass();
			case 1: return "o_practice_progress_l1";
			case 2: return "o_practice_progress_l2";
			case 3: return "o_practice_progress_l3";
			case 4: return "o_practice_progress_l4";
			case 5: return "o_practice_progress_l5";
			default: return getNotCssClass();
		}
	}
	
	public void append(PracticeAssessmentItemGlobalRef globalRef) {
		total++;
		if(globalRef == null) {
			not++;
		} else {
			int jLevel = globalRef.getLevel() - 1;
			if(jLevel >= 0 && jLevel < levelArr.length) {
				levelArr[jLevel]++;
			} else if(jLevel >= levelArr.length) {
				levelArr[levelArr.length - 1]++;
			} else {
				levelArr[0]++;
			}
		}
	}

	public int getNot() {
		return not;
	}
	
	public long getNotPercent() {
		return Math.round(toPercent(getNot()));
	}
	
	public String getNotCssClass() {
		return "o_practice_progress_not";
	}

	public int getLevel(int level) {
		int jLevel = level - 1;
		if(jLevel >= 0 && jLevel < levelArr.length) {
			return levelArr[jLevel];
		}
		return 0;
	}
	
	public String getLevelString(Integer level) {
		if(level == null) return "";
		
		int val = getLevel(level.intValue());
		return Integer.toString(val);
	}
	
	public long getLevelPercent(int level) {
		return Math.round(toPercent(getLevel(level)));
	}
	
	public int getTotal() {
		return total;
	}
	
	public int getNumOfLevels() {
		return levelArr.length;
	}
	
	public double toPercent(int val) {
		if(total == 0) {
			return 0.0d;
		}
		return (val / (double)total) * 100.0d;
	}
}
