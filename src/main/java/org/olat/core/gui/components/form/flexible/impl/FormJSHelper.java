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
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl;

import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormJSHelper
 * 
 * <P>
 * Initial Date: 11.01.2007 <br>
 * 
 * @author patrickb
 */
public class FormJSHelper {
	private static final String READONLYA = "<div class=\"b_form_disabled\">";

	private static final String READONLYB = "</div>";

	// EXTJS DEP
	private static final String[] EXTJSACTIONS = { "dblclick", "click",
			"change" };

	/**
	 * create for example an
	 * <code>onclick="o_ffEvent('ofo_1377','ofo_1377_dispatchuri','o_fi1399','ofo_1377_eventval','1')"</code>
	 * 
	 * @param form
	 * @param id
	 * @param actions
	 * @return
	 */
	public static StringBuilder getRawJSFor(Form form, String id, int actions) {
		StringBuilder sb = new StringBuilder(64);
		// find correct action! only one action supported
		for (int i = FormEvent.ON_DOTDOTDOT.length - 1; i >= 0; i--) {
			if (actions - FormEvent.ON_DOTDOTDOT[i] > 0)
				throw new AssertionError("only one actions supported here");
			if (actions - FormEvent.ON_DOTDOTDOT[i] == 0) {
				sb.append(" on" + EXTJSACTIONS[i]);// javascript action
				sb.append("=\"");
				sb.append(getJSFnCallFor(form, id, i));
				sb.append("\"");
				break;
				// actions = actions - FormEvent.ON_DOTDOTDOT[i];
			}
		}
		return sb;
	}

	public static String getJSFnCallFor(Form form, String id, int actionIndex) {
		String content = "o_ffEvent('";
		content += form.getFormName() + "','";
		content += form.getDispatchFieldId() + "','";
		content += id + "','";
		content += form.getEventFieldId() + "','";
		content += (FormEvent.ON_DOTDOTDOT[actionIndex]);
		content += ("')");
		return content;
	}

	public static void appendReadOnly(String text, StringOutput sb) {
		sb.append(READONLYA);
		sb.append(text);
		sb.append(READONLYB);
	}

	/**
	 * 
	 * @param sb
	 * @param jsonRenderInstruction
	 * @param acceptedInstructions
	 */
	public static void appendRenderInstructions(StringOutput sb,
			String jsonRenderInstruction, Set acceptedInstructions) {
		JSONObject instr;
		try {
			instr = new JSONObject(jsonRenderInstruction);
			sb.append(" ");// ensure blank before appending instructions -> '
							// '...
			for (Iterator iter = acceptedInstructions.iterator(); iter
					.hasNext();) {
				String accepted = (String) iter.next();
				if (instr.get(accepted) != null) {
					// generates i.e. 'class=\"thevalueclass\" '
					sb.append(accepted);// accepted key is also use as attribute
					sb.append("=\"");
					sb.append(instr.getString(accepted));
					sb.append("\" ");
				}
			}
		} catch (JSONException e) {
			throw new OLATRuntimeException(
					"error retrieving JSON style render instruction", e);
		}
	}

	/**
	 * @param rootForm
	 * @param id
	 * @return
	 */
	public static String getJSSubmitRegisterFn(Form form, String id) {
		String content = "o_ffRegisterSubmit('";
		content += form.getDispatchFieldId() + "','";
		content += id + "');";
		return content;
	}
	
	public static String getJSStartWithVarDeclaration(String id){
		StringBuffer sb = new StringBuffer(120);
		sb.append(" <script type=\"text/javascript\">\n /* <![CDATA[ */ \n");
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		sb.append("(function() {");
		sb.append("var ").append(secureJSVarName(id)).append(" = Ext.get('").append(id).append("'); ");
		return sb.toString();
	}
	
	/**
	 * OO-98 : a fix in FormUIFactory changed the id from "null" to
	 * "something.like.this" for selectionElements (like radio-buttons)
	 * this led to js-errors because output was:  var o_fisomething.like.this [..]
	 * now this method ensures that the id does not contain dots 
	 * 
	 * @param id
	 * @return
	 */
	public static String secureJSVarName(String id){
		return id.replace(".", "_");
	}
	
	public static String getJSStart(){
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		return " <script type=\"text/javascript\">\n /* <![CDATA[ */ \n (function() {";
	}
	
	public static String getJSEnd(){
		// Execute anonymous function (closure) now (OLAT-5755)
		return "})();\n /* ]]> */ \n</script>";
	}
	
	public static String getExtJSVarDeclaration(String id){
		return "var "+secureJSVarName(id)+" = Ext.get('"+id+"'); ";
	}
	
	public static String getSetFlexiFormDirty(Form form, String id){
		String result;
		String prefix = secureJSVarName(id) + ".on('";
		// examples:
		// o_fi400.on({'click',setFormDirty,this,{formId:"ofo_100"}});
		// o_fi400.on({'change',setFormDirty,this,{formId:"ofo_100"}});
		String postfix = "',setFlexiFormDirtyByListener,document,{formId:\"" + form.getDispatchFieldId() + "\"});";
		result = prefix + "change" + postfix;
		result += prefix + "keypress" + postfix;

		return result;
	}
	
	public static String getSetFlexiFormDirtyForCheckbox(Form form, String id){
		String result;
		String prefix = secureJSVarName(id) + ".on('";
		// examples:
		// o_fi400.on({'click',setFormDirty,this,{formId:"ofo_100"}});
		// o_fi400.on({'change',setFormDirty,this,{formId:"ofo_100"}});
		String postfix = "',setFlexiFormDirtyByListener,document,{formId:\"" + form.getDispatchFieldId() + "\"});";
		result = prefix + "change" + postfix;
		result += prefix + "mouseup" + postfix;
		return result;
	}

	public static String getFocusFor(String id){
		// deactivated due OLAT-3094 and OLAT-3040
		return id +".focus();";
	}
	
	public static String getSetFlexiFormDirtyFnCallOnly(Form form){
		if(form.isDirtyMarking()){
			return "setFlexiFormDirty('"+form.getDispatchFieldId()+"');";
		}else{
			return " ";
		}
	}

	/**
	 * creates the JS fragment needed for the {@link #getInlineEditOkCancelHTML(StringOutput, String, String, String)} HTML fragment.
	 * @param sb where to append
	 * @param id formItemId of the InlineEdit FormItem
	 * @param oldHtmlValue escaped HTML value TODO:2009-09-26:pb: escaped values appear as &apos; and are not escaped back.
	 * @param rootForm to extract the ID of the Form where to submit to.
	 */
	public static void getInlineEditOkCancelJS(StringOutput sb, String id, String oldHtmlValue, Form rootForm) {
		/*
		 * yesFn emulates a click on the input field, which in turn "submits" to the inlineElement to extract the value
		 */
		sb.append(FormJSHelper.getExtJSVarDeclaration(id));
		sb.append(id+".focus(1);");//defer focus,based on EXT
		sb.append("var o_ff_inline_yesFn = function(e){");
		sb.append(FormJSHelper.getJSFnCallFor(rootForm, id, FormEvent.ONCLICK)).append(";};");
		sb.append("Ext.get('"+id+"').on('blur',o_ff_inline_yesFn);");		

		/*
		 * noFn replaces the old value in the input field, and then "submits" to the inlineElement via yesFn
		 */
		sb.append("var o_ff_inline_noFn = function(e){Ext.get('").append(id).append("').dom.value = '").append(oldHtmlValue).append("';o_ff_inline_yesFn(e);};");
		sb.append("\n");
		sb.append("var nav = new Ext.KeyNav("+id+", {");
	    sb.append("\"esc\" : function(e){");
	    sb.append("o_ff_inline_noFn();Ext.EventManager.removeAll("+id+");");
	    sb.append("},");
	    sb.append("\"enter\" : function(e){");
	    sb.append("o_ff_inline_yesFn();Ext.EventManager.removeAll("+id+");");
	    sb.append("},");
	    sb.append("scope : this");
	    sb.append("});");
	}

	/**
	 * submits a form when the enter key is pressed.
	 * TextAreas are handeled special and do not propagate the enter event to the outer world
	 * @param formName
	 * @return
	 */
	public static String submitOnKeypressEnter(String formName) {
		StringBuilder sb = new StringBuilder();
		sb.append(getJSStart());
		sb.append("Ext.get(document.forms['").append(formName).append("']).on('keypress', function(event) {if (13 == event.keyCode) {if (document.forms['").append(formName).append("'].onsubmit()) {document.forms['").append(formName).append("'].submit();}}})");
		sb.append(getJSEnd());
		return sb.toString();
	}
	
}
