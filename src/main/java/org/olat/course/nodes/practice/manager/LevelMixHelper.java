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
package org.olat.course.nodes.practice.manager;

import java.util.List;

/**
 * 
 * Initial date: 16 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LevelMixHelper {
	
	public static int numOfDaysBeforeRepetition(int level) {
		switch(level) {
			case 0: return 0;
			case 1: return 1;
			case 2: return 4;
			case 3: return 10;
			case 4: return 30;
			default: return 150;
		}
	}
	
	public static List<MixLevel> mix(int currentSession) {
		switch(currentSession) {
			case 0:
			case 1: return mix(100.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
			case 2: return mix(50.0d, 50.0d, 0.0d, 0.0d, 0.0d, 0.0d);
			case 3: return mix(50.0d, 30.0d, 20.0d, 0.0d, 0.0d, 0.0d);
			case 4: return mix(50.0d, 25.0d, 15.0d, 10.0d, 0.0d, 0.0d);
			case 5: return mix(50.0d, 20.0d, 15.0d, 10.0d, 5.0d, 0.0d);
			case 6: return mix(50.0d, 20.0d, 15.0d, 10.0d, 3.0d, 2.0d);
			default: return mix(50.0d, 20.0d, 15.0d, 10.0d, 3.0d, 2.0d);
		}	
	}

	public static List<MixLevel> mix(double partLevel0, double partLevel1, double partLevel2,
			double partLevel3, double partLevel4, double partLevel5) {
		return List.of(new MixLevel(0, partLevel0), new MixLevel(1, partLevel1), new MixLevel(2, partLevel2),
				new MixLevel(3, partLevel3), new MixLevel(4, partLevel4), new MixLevel(5, partLevel5));
	}
	
	public static class MixLevel {
		
		
		private final int level;
		private final double part;
		
		public MixLevel(int level, double part) {
			this.level = level;
			this.part = part;
		}
		
		public int getLevel() {
			return level;
		}
		
		public boolean isEmpty() {
			return part <= 0.0d;
		}
		
		public int getNumOfDaysBeforeRepetition() {
			return numOfDaysBeforeRepetition(level);
		}
	
		/**
		 * Part of questions in %
		 * @return 
		 */
		public double getPart() {
			return part;
		}
		
		public int numOfQuestions(int total) {
			double p = part / 100.0d;
			return (int)Math.round(p * total);
		}
	}
}
