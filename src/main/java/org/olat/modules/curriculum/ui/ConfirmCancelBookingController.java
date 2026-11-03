/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 *
 * Initial date: 3 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmCancelBookingController extends ConfirmationController {

	private final CurriculumElement element;

	public ConfirmCancelBookingController(UserRequest ureq, WindowControl wControl, CurriculumElement element, Price cancellationFee) {
		this(ureq, wControl, element, cancellationFee, Util.createPackageTranslator(ConfirmCancelBookingController.class, ureq.getLocale()));
	}

	private ConfirmCancelBookingController(UserRequest ureq, WindowControl wControl, CurriculumElement element, Price cancellationFee,
			Translator translator) {
		super(ureq, wControl, getMessage(translator, cancellationFee), getConfirmation(translator, cancellationFee),
				translator.translate("leave.cancel.button"), ButtonType.submitPrimary, null, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), translator));
		this.element = element;
		initForm(ureq);
	}

	private static String getMessage(Translator translator, Price cancellationFee) {
		return cancellationFee == null
				? translator.translate("leave.cancel.message")
				: translator.translate("leave.cancel.fee.message", PriceFormat.fullFormat(cancellationFee));
	}

	private static String getConfirmation(Translator translator, Price cancellationFee) {
		return cancellationFee == null
				? translator.translate("leave.cancel")
				: translator.translate("leave.cancel.fee.confirmation", PriceFormat.fullFormat(cancellationFee));
	}

	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		uifactory.addStaticTextElement("element", "leave.cancel.element", StringHelper.escapeHtml(element.getDisplayName()), confirmCont);
	}
}
