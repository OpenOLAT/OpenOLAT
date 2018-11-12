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
package org.olat.core.gui.control;

/**
 * 
 * Initial date: 20.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScreenMode {
	private Mode wish;
	private boolean fullScreen;
	private String businessPath;

	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public boolean isStandardScreen() {
		return !fullScreen;
	}
	
	/**
	 * 
	 * @param mode The mode, full or standard screen
	 * @param businessPath The business path to beautify the reload (in the form [Repository:3781][CourseNode:23468732]) (optional)
	 */
	public void setMode(Mode mode, String businessPath) {
		fullScreen = Mode.full.equals(mode);
		this.wish = mode;
		this.businessPath = businessPath;
	}
	
	public boolean isWishFullScreen() {
		return wish != null && wish.equals(Mode.full);
	}
	
	public boolean isWishStandardScreen() {
		return wish != null && wish.equals(Mode.standard);
	}
	
	/**
	 * 
	 * @param erase If true, erase the wish because it probably come true
	 * @return
	 */
	public boolean wishScreenModeSwitch(boolean erase) {
		Mode w = wish;
		if(erase) {
			wish = null;
		}
		return w != null;
	}
	
	public String getBusinessPath() {
		return businessPath;
	}
	
	public boolean wishFullScreen(boolean erase) {
		Mode w = wish;
		if(erase) {
			wish = null;
		}
		return w != null && w.equals(Mode.full);
	}
	
	public boolean wishStandardScreen(boolean erase) {
		Mode w = wish;
		if(erase) {
			wish = null;
		}
		return w != null && w.equals(Mode.standard);
	}
	
	public void reset() {
		wish = null;
		businessPath = null;
	}
	
	public enum Mode {
		standard,
		full
	}
}
