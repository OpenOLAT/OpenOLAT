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
package org.olat.course.condition.additionalconditions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.AbstractAccessableCourseNode;

/**
 * Only a placeholder to import courses from other vendors
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PasswordCondition extends AdditionalCondition {

	private static final long serialVersionUID = -2311016997086309298L;
	
	// <OLATCE-91>
	private String password;
	private String answer;
	
	public final static String PASSWORD_ENDING = "password";
	// </OLATCE-91>

	public AbstractAccessableCourseNode getNode() {
		return node;
	}

	// <OLATCE-91>
	@Override
	public Boolean evaluate() {
		boolean retVal = (password==null?true:password.equals(answer)); 
		answer=null;
		return retVal;
	}
	
	public Boolean pwdEvaluate() {
		PasswordStore store = null;
		
		if(answers != null) {
			Object obj = answers.getAnswers(node.getIdent(), courseId.toString()); 
			if(obj instanceof PasswordStore){
				store = (PasswordStore) obj;
			}
		}
		
		if(store !=null) {
			answer = store.getPassword();
		}
		return evaluate();
	}

	@Override
	public Controller getUserInputController(UserRequest ureq, WindowControl wControl){
		return new PasswordVerificationController(ureq, wControl, this);
	}
	
	@Override
	public Controller getEditorComponent(UserRequest ureq, WindowControl wControl) {
		return new PasswordConditionEditController(ureq, wControl, this);
	}

	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
