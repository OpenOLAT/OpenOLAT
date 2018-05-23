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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.GeneralInformation;
import org.olat.modules.forms.model.xml.GeneralInformation.Type;
import org.olat.modules.forms.model.xml.GeneralInformations;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneralInformationLegendNameGenerator implements LegendNameGenerator {
	
	private Map<Long, String> sessionKeyToName;

	private final Form form;
	private final List<? extends EvaluationFormSessionRef> sessions;
	
	private final EvaluationFormReportDAO reportDao;
	
	public GeneralInformationLegendNameGenerator(Form form, List<? extends EvaluationFormSessionRef> sessions) {
		this.form = form;
		this.sessions = sessions;
		this.reportDao = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
		initNames();
	}

	@Override
	public String getName(EvaluationFormSession session, Identity identity) {
		if (session == null) return null;
		return sessionKeyToName.get(session.getKey());
	}

	private void initNames() {
		Map<Long, String> sessionKeyToFirstname = loadInforamtions(GeneralInformation.Type.USER_FIRSTNAME);
		Map<Long, String> sessionKeyToLastname = loadInforamtions(GeneralInformation.Type.USER_LASTNAME);
		sessionKeyToName = new HashMap<>();
		for (EvaluationFormSessionRef ref: sessions) {
			String firstname = sessionKeyToFirstname.get(ref.getKey());
			String lastname = sessionKeyToLastname.get(ref.getKey());
			String name = conacatName(firstname, lastname);
			sessionKeyToName.put(ref.getKey(), name);
		}
	}

	private String conacatName(String firstname, String lastname) {
		boolean hasFirstname = StringHelper.containsNonWhitespace(firstname);
		boolean hasLastname = StringHelper.containsNoneOfCoDouSemi(lastname);
		if (hasFirstname && hasLastname) return new StringBuilder().append(firstname).append(" ").append(lastname).toString();
		if (hasFirstname) return firstname;
		if (hasLastname) return lastname;
		return null;
	}

	private Map<Long, String> loadInforamtions(Type type) {
		List<String> responseIdentifiers = getResponseIdentifiers(type);
		List<EvaluationFormResponse> responses = reportDao.getResponses(responseIdentifiers, sessions);
		Map<Long, String> sessionKeyToInformation = new HashMap<>();
		for (EvaluationFormResponse response: responses) {
			sessionKeyToInformation.put(response.getSession().getKey(), response.getStringuifiedResponse());
		}
		return sessionKeyToInformation;
	}

	private List<String>  getResponseIdentifiers(GeneralInformation.Type type) {
		List<String> identifiers = new ArrayList<>();
		for (AbstractElement element: form.getElements()) {
			if (element instanceof GeneralInformations) {
				GeneralInformations generalInformations = (GeneralInformations) element;
				for (GeneralInformation genralInformation: generalInformations.asCollection()) {
					if (genralInformation.getType().equals(type)) {
						identifiers.add(genralInformation.getId());
					}
				}
			}
		}
		return identifiers;
	}

}
