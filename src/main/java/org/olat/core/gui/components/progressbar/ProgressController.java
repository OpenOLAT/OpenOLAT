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
package org.olat.core.gui.components.progressbar;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * A controller which use a procentual progress bar filled at 100% at start.
 * 
 * Initial date: 28.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProgressController extends BasicController implements ProgressDelegate {

	private final VelocityContainer mainVC;
	private final ProgressBar progressBar;
	
	public ProgressController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("progress");
		progressBar = new ProgressBar("dedup", 600, 0.0f, 100.0f, "%");
		progressBar.setActual(100.0f);
		mainVC.put("progress", progressBar);
		putInitialPanel(mainVC);
	}
	
	public void setMessage(String translatedMsg) {
		mainVC.contextPut("msg", translatedMsg);
	}

	public void setMax(float i) {
		progressBar.setMax(i);
	}

	public void setPercentagesEnabled(boolean percentagesEnabled) {
		progressBar.setPercentagesEnabled(percentagesEnabled);
	}

	@Override
	public void setActual(float i) {
		progressBar.setActual(i);
	}

	@Override
	public void setInfo(String message) {
		progressBar.setInfo(message);
	}
	
	public void setUnitLabel(String label) {
		progressBar.setUnitLabel(label);
	}

	@Override
	public void finished() {
		progressBar.setActual(0.0f);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
