/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VariablesValidationContext extends VelocityContext {
	
	private static final long serialVersionUID = -4153392034993856837L;
	
	private final List<String> unkownVariables = new ArrayList<>();
	private final List<String> nullVariables = new ArrayList<>();
	
	public VariablesValidationContext() {
		//
	}
	
	@Override
	public Object internalGet(String key) {
		if(!key.startsWith(".literal.")) {
			if(!internalContainsKey(key)) {
				unkownVariables.add(key);
			} else if(super.internalGet(key) == null) {
				nullVariables.add(key);
			}
		}
		return super.internalGet(key);
	}
	
	public String stringuifiedUnkownVariables() {
		StringBuilder sb = new StringBuilder(128);
		for(String variable:unkownVariables) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(variable);
		}
		return sb.toString();
	}
	
	public List<String> getUnkownVariables() {
		return unkownVariables;
	}
	
	public List<String> getNullVariables() {
		return nullVariables;
	}

}
