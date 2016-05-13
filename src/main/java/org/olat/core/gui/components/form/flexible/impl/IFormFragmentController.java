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
package org.olat.core.gui.components.form.flexible.impl;

import java.util.function.Consumer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * A {@link IFormFragment} requires access to a controller. This interface represents the very 
 * minimal set of features it expects from its controller. This subset corresponds strictly with
 * the features offered by a {@link FormBasicController} and in most cases the fragment controller
 * will be the form controller itself. Nonetheless this interface is here to guaranty that a 
 * fragment will be truly reusable by isolating it from the internal details of more powerful 
 * form controller.
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public interface IFormFragmentController {
	void removeAsListenerAndDispose(Controller controller);
	WindowControl getWindowControl();
	void listenTo(Controller controller);
	void setFormCanSubmit(boolean canSubmit);
	void fireEvent(UserRequest ureq, Event event);
	
	
	/**
	 * A helper method to help adapt an existing {@link FormBasicController} instance to
	 * the subset of features exposed by a {@link IFormFragmentController}.
	 * 
	 * @param delegate			the form controller to be adapted 
	 * @param canSubmitHandler 	a handler to be invoked when the fragment wants to change the overall readiness state of the form
	 * @return
	 */
	public static IFormFragmentController fragmentControllerAdapter(final FormBasicController delegate, final Consumer<Boolean> canSubmitHandler) {
		return new IFormFragmentController() {
			@Override
			public void setFormCanSubmit(boolean canSubmit) {
				canSubmitHandler.accept(canSubmit);
			}
			
			@Override
			public void removeAsListenerAndDispose(Controller controller) {
				delegate.removeAsListenerAndDispose(controller);
			}
			
			@Override
			public void listenTo(Controller controller) {
				delegate.listenTo(controller);
			}
			
			@Override
			public WindowControl getWindowControl() {
				return delegate.getWindowControlForDebug();
			}

			@Override
			public void fireEvent(UserRequest ureq, Event event) {
				delegate.fireEvent(ureq, event);
			}
		};		
	}
}
