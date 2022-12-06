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
package org.olat.ims.qti21.ui.components;


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCountDownComponent extends FormBaseComponentImpl {

	private static final AssessmentCountDownComponentRenderer RENDERER = new AssessmentCountDownComponentRenderer();
	
	private AssessmentCountDownFormItem item;
	private long timerInSeconds;
	private boolean alreadyEnded;
	
	public AssessmentCountDownComponent(String name, AssessmentCountDownFormItem item) {
		super(name);
		this.item = item;
	}
	
	public long getTimerInSeconds() {
		return timerInSeconds;
	}

	public void setTimerInSeconds(long timerInSeconds) {
		this.timerInSeconds = timerInSeconds;
	}

	public boolean isAlreadyEnded() {
		return alreadyEnded;
	}

	public void setAlreadyEnded(boolean alreadyEnded) {
		this.alreadyEnded = alreadyEnded;
	}

	@Override
	public AssessmentCountDownFormItem getFormItem() {
		return item;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// 
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
