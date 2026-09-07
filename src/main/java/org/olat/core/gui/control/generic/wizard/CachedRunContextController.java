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
package org.olat.core.gui.control.generic.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;

/**
 * Wraps a sub-controller that a wizard step wants kept alive across back and
 * forward navigation, instead of the usual dispose-and-recreate lifecycle of
 * a step's own controller. The wrapped controller is cached in the wizard's
 * {@link StepsRunContext}, so it survives the owning step controller being
 * disposed and rebuilt on every visit; it is only added and removed as a raw
 * controller listener here, never disposed by the step itself.
 *
 * Initial date: 3 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public final class CachedRunContextController<T extends FormBasicController> {

	private static final String REGISTRY_KEY = "cachedRunContextControllers";

	private final T controller;

	private CachedRunContextController(T controller) {
		this.controller = controller;
	}

	/**
	 * Returns the controller cached under cacheKey, creating it with factory
	 * on the first call. Registers listener as a raw controller listener on
	 * it, so the step controller is notified of its events without cascading
	 * dispose to it.
	 */
	public static <T extends FormBasicController> CachedRunContextController<T> of(StepsRunContext runContext,
			String cacheKey, Supplier<T> factory, ControllerEventListener listener) {
		@SuppressWarnings("unchecked")
		T controller = (T) runContext.get(cacheKey);
		if (controller == null) {
			controller = factory.get();
			runContext.put(cacheKey, controller);
			register(runContext, controller);
		}
		controller.addControllerListener(listener);
		return new CachedRunContextController<>(controller);
	}

	private static void register(StepsRunContext runContext, Controller controller) {
		@SuppressWarnings("unchecked")
		List<Controller> registry = (List<Controller>) runContext.get(REGISTRY_KEY);
		if (registry == null) {
			registry = new ArrayList<>();
			runContext.put(REGISTRY_KEY, registry);
		}
		registry.add(controller);
	}

	public T get() {
		return controller;
	}

	public FormItem getStepFormItem() {
		return controller.getInitialFormItem();
	}

	public void release(ControllerEventListener listener) {
		controller.removeControllerListener(listener);
	}

	/**
	 * Disposes every controller cached via {@link #of(StepsRunContext, String, Supplier, ControllerEventListener)}
	 * in this run context. Must be called once the wizard itself is disposed,
	 * since these controllers are kept alive throughout the run instead of
	 * being disposed with their (repeatedly recreated) wizard step controller.
	 */
	public static void disposeAll(StepsRunContext runContext) {
		@SuppressWarnings("unchecked")
		List<Controller> registry = (List<Controller>) runContext.get(REGISTRY_KEY);
		if (registry != null) {
			for (Controller controller : registry) {
				controller.dispose();
			}
			registry.clear();
		}
	}

}
