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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.container;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Variables implements Serializable{

	private static final long serialVersionUID = 4599004441875604671L;
	private final Map<String,Variable> vars;
	
	public Variables() {
		super();
		vars = new HashMap<>(3);
	}


	public Variable getVariable(String varName) {
		return vars.get(varName);
	}

	public Variable getSCOREVariable() {
		Variable scoreVar = getVariable("SCORE");
		if (scoreVar == null) {
			// no SCORE var defined... try to fallback:
			// if there is only a single variable present, we assume, this is
			// to be the final score.
			if (vars.size() == 1) scoreVar = vars.get(vars.keySet().iterator().next());
		}
		return scoreVar;
	}

	
	public void setVariable (Variable var) {
		vars.put(var.getVarName(),var);
	}
	
	@Override
	public String toString() {
		return vars.toString()+"="+super.toString();
	}
	
}
