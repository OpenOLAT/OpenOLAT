/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package ch.unizh.portal.zsuz;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.mail.MailTemplate;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStep00Form
 * 
 * <P>
 * Initial Date:  09.06.2008 <br>
 * @author patrickb
 */
class ZsuzStep00Form extends StepFormBasicController {

	private IntegerElement copies;
	private SingleSelection color;
	private SingleSelection pickup;
	private SingleSelection print;
	private SingleSelection finish;
	private final static String[] COLORS_KEYS = new String[]{"form.color.bw","form.color.color"};
	//if the order is not picked up, sending the order is done from irchel printing
	private final static String[] PICKUP_KEYS = new String[]{"form.pickup.irchel","form.pickup.zentrum","form.pickup.sending"};
	private final static Identity[] PICKUP_IDS = new Identity[]{ZentralstellePortlet.drucki, ZentralstellePortlet.druckz, ZentralstellePortlet.drucki};
	private static final String[] PRINT_KEYS = new String[]{"form.print.oneside","form.print.doubleside"};
	private static final String[] FINISH_KEYS = new String[]{"form.finish.none","form.finish.bindung","form.finish.lochung","form.finish.scripts"};
	
	/**
	 * @param ureq
	 * @param control
	 * @param rootForm
	 * @param runContext
	 * @param layout
	 * @param customLayoutPageName
	 */
	public ZsuzStep00Form(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName) {
		super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
		setBasePackage(this.getClass());
		flc.setTranslator(getTranslator());
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {

		// form has no more errors
		// save info in run context for next step.
		String copiesV = copies.getValue();
		String colorV = color.getValue(color.getSelected());
		String pickupV = pickup.getValue(pickup.getSelected());
		Identity replyto = PICKUP_IDS[pickup.getSelected()];
		String printV = print.getValue(print.getSelected());
		String finishV = finish.getValue(finish.getSelected());
		addToRunContext("copies", copiesV);
		addToRunContext("color", colorV);
		addToRunContext("pickup", pickupV);
		addToRunContext("print", printV);
		addToRunContext("finish", finishV);
		
		@SuppressWarnings("unchecked")
		List<String[]> userproperties = (List<String[]>)getFromRunContext("userproperties");
		
		MailTemplate mailtemplate = createMailTemplate(userproperties, copiesV, colorV, pickupV, printV, finishV);
		addToRunContext("mailtemplate", mailtemplate);
		addToRunContext("replyto", replyto);
		// inform surrounding Step runner to proceed
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

	}

	private MailTemplate createMailTemplate(final List<String[]> userprops, final String copiesV, final String colorV, final String pickupV, final String printV, final String finishV) {
		String subject = translate("email.subject");
		String body = translate("email.body");
		final Translator translator = getTranslator();
		MailTemplate mt = new MailTemplate(subject, body, null){
			private String mycopiesV = copiesV;
			private String mycolorV = colorV;
			private String myprintV = printV;
			private String myfinishV = finishV;
			private String mypickupV = pickupV;
			private Translator myTranslator = translator;

			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity recipient) {// Put user variables into velocity context
				User user = recipient.getUser();
				context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				context.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
				context.put("login", recipient.getName());
				//make translator available instead of putting translated keys into context
				context.put("t", myTranslator);
				StringBuffer userPropsString = new StringBuffer();
				for (String[] keyValue : userprops) {
					userPropsString.append(" ");
					userPropsString.append(keyValue[0]);
					userPropsString.append(": ");
					userPropsString.append(keyValue[1]);
					userPropsString.append("\n");
				}
				context.put("userproperties", userPropsString);
				context.put("copies",mycopiesV);
				context.put("color",mycolorV);
				context.put("print",myprintV);
				context.put("finish",myfinishV);
				context.put("pickup",mypickupV);
			}
		};
		return mt;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {	
		setFormTitle("step00.title");
		copies = uifactory.addIntegerElement("form.copies", 1, formLayout);
		copies.setMaxValueCheck(10, null);
		copies.setMinValueCheck(1, null);
		copies.setDisplaySize(2);
		
		print = uifactory.addRadiosVertical("form.print", formLayout, PRINT_KEYS, null);
		print.select("form.print.oneside", true);
		color = uifactory.addRadiosVertical("form.color", formLayout, COLORS_KEYS, null);
		color.select("form.color.bw",true);
		finish = uifactory.addRadiosVertical("form.finish", formLayout, FINISH_KEYS, null);
		finish.select("form.finish.none", true);
		pickup = uifactory.addRadiosVertical("form.pickup", formLayout, PICKUP_KEYS, null);
		pickup.select("form.pickup.irchel", true);
		
		
		uifactory.addStaticTextElement("form.konditionen", "form.label.konditionen", translate("form.konditionen"), formLayout);
		
		
	}

}
