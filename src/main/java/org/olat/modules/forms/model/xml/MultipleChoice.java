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
package org.olat.modules.forms.model.xml;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoice extends AbstractElement {

	private static final long serialVersionUID = -7366221315042186647L;
	
	public static final String TYPE = "formmultiplechoice";
	public enum Presentation {
		VERTICAL,
		HORIZONTAL,
		DROPDOWN
	}
	
	private String name;
	private boolean mandatory;
	private Presentation presentation;
	private Choices choices;
	private boolean withOthers;

	@Override
	public String getType() {
		return TYPE;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Presentation getPresentation() {
		if (presentation == null) {
			presentation = Presentation.VERTICAL;
		}
		return presentation;
	}

	public void setPresentation(Presentation presentation) {
		this.presentation = presentation;
	}

	public Choices getChoices() {
		return choices;
	}

	public void setChoices(Choices choices) {
		this.choices = choices;
	}

	public boolean isWithOthers() {
		return withOthers;
	}

	public void setWithOthers(boolean withOthers) {
		this.withOthers = withOthers;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MultipleChoice) {
			MultipleChoice other = (MultipleChoice)obj;
			return getId() != null && getId().equals(other.getId());
		}
		return super.equals(obj);
	}
}
