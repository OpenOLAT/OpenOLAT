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

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: 11.01.2007 <br>
 * 
 * @author patrickb
 */
public class FormJSHelper {
	
	private static final Logger log = Tracing.createLoggerFor(FormJSHelper.class);

	private static final String[] EXTJSACTIONS = { "dblclick", "click", "change", "keyup", "blur" };

	/**
	 * create for example an
	 * <code>onclick="o_ffEvent('ofo_1377','ofo_1377_dispatchuri','o_fi1399','ofo_1377_eventval','1')"</code>
	 * 
	 * @param form The form
	 * @param id The id fo the target component
	 * @param actions The action
	 * @return
	 */
	public static StringBuilder getRawJSFor(Form form, String id, int actions) {
		return getRawJSFor(form, id, actions, false, null, id);
	}
	
	/**
	 * 
	 * @param form       The form
	 * @param id         The id
	 * @param actions    The action
	 * @param newWindow  true if a new window is wanted
	 * @param tmpCommand Write a temporary cid in the form submit
	 * @param focusId    The element id that will get the focus event (not the same
	 *                   for special cases such as radios) or NULL if re-focus feature
	 *                   shall not be used
	 * @return The on... event with the o_ffEvent call
	 */
	public static StringBuilder getRawJSFor(Form form, String id, int actions, boolean newWindow, String tmpCommand, String focusId) {
		StringBuilder sb = new StringBuilder(64);
		// find correct action! only one action supported
		for (int i = FormEvent.ON_DOTDOTDOT.length - 1; i >= 0; i--) {
			if (actions - FormEvent.ON_DOTDOTDOT[i] > 0)
				throw new AssertionError("only one actions supported here");
			if (actions - FormEvent.ON_DOTDOTDOT[i] == 0) {
				sb.append(" on").append(EXTJSACTIONS[i]);// javascript action
				sb.append("=\"");
				sb.append(getJSFnCallFor(form, id, i, newWindow, tmpCommand));
				sb.append("\"");
				break;
			}
		}
		// Remember the form item when being focused for next server roundtrip
		if (focusId != null) {
			sb.append(" onfocus=\"o_info.lastFormFocusEl='").append(focusId).append("'; \"");					
		}
		return sb;
	}

	public static String getJSFnCallFor(Form form, String id, int actionIndex) {
		return getJSFnCallFor(form, id, actionIndex, false, null);
	}

	public static String getJSFnCallFor(Form form, String id, int actionIndex, boolean newWindow, String tmpCommand) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("o_ffEvent('")
		  .append(form.getFormName()).append("','")
		  .append(form.getDispatchFieldId()).append("','")
		  .append(id).append("','")
		  .append(form.getEventFieldId()).append("','")
		  .append(FormEvent.ON_DOTDOTDOT[actionIndex])
		  .append("'");
		if(newWindow || StringHelper.containsNonWhitespace(tmpCommand)) {
			sb.append(",").append(newWindow).append(",");
			if(StringHelper.containsNonWhitespace(tmpCommand)) {
				sb.append("'").append(tmpCommand).append("'");
			} else {
				sb.append("null");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Wrap the JavaScript method for onclick and onkeyup (limit to enter).
	 * 
	 * @param fnCall The function
	 * @return The html code
	 */
	public static String onClickKeyEnter(String fnCall) {
		StringBuilder sb = new StringBuilder(128);
		onClickKeyEnter(sb, fnCall);
		return sb.toString();
	}
	
	/**
	 * Wrap the JavaScript method for onclick and onkeyup (limit to enter).
	 * 
	 * @param sb The buffer to append the html code to
	 * @param fnCall The function
	 * @return The html code
	 */
	public static void onClickKeyEnter(StringBuilder sb, String fnCall) {
		sb.append(" onclick=\"")
		  .append(fnCall).append(";\" onkeyup=\"if(event.which == 13 || event.keyCode == 13) {")
		  .append(fnCall)
		  .append("; }\"");
	}
	
	/**
	 * Build the JavaScript method to send a flexi form event.
	 * 
	 * @param item The form item
	 * @param dirtyCheck If false, the dirty check is by passed
	 * @param pushState If true, the state (visible url in browser) will be pushed to the browser
	 * @param pairs Additional name value pairs send by the link
	 * @return
	 */
	public static String getXHRFnCallFor(FormItem item, boolean dirtyCheck, boolean pushState, boolean submit, NameValuePair... pairs) {
		return getXHRFnCallFor(item.getRootForm(), item.getFormDispatchId(), 1, dirtyCheck, pushState, submit, pairs);
	}
	
	/**
	 * Build the JavaScript method to send a flexi form event.
	 * 
	 * @param form The form object
	 * @param id The id of the element
	 * @param actionIndex The type of event (click...)
	 * @param dirtyCheck If false, the dirty check is by passed
	 * @param pushState If true, the state (visible url in browser) will be pushed to the browser
	 * @param pairs Additional name value pairs send by the link
	 * @return
	 */
	public static String getXHRFnCallFor(Form form, String id, int actionIndex, boolean dirtyCheck, boolean pushState, NameValuePair... pairs) {
		return getXHRFnCallFor(form, id, actionIndex, dirtyCheck, pushState, false, pairs);
	}
	
	/**
	 * Build the JavaScript method to send a flexi form event with all possible settings.
	 * 
	 * @param form The form object
	 * @param id The id of the element
	 * @param actionIndex The type of event (click...)
	 * @param dirtyCheck If false, the dirty check is by passed
	 * @param pushState If true, the state (visible url in browser) will be pushed to the browser
	 * @param submit If true, the form will be submitted but it only works for none multi part forms.
	 * @param pairs Additional name value pairs send by the link
	 * @return The code
	 */
	public static String getXHRFnCallFor(Form form, String id, int actionIndex, boolean dirtyCheck, boolean pushState, boolean submit, NameValuePair... pairs) {
		try(StringOutput sb = new StringOutput(128)) {
			sb.append("o_ffXHREvent('")
			  .append(form.getFormName()).append("','")
			  .append(form.getDispatchFieldId()).append("','")
			  .append(id).append("','")
			  .append(form.getEventFieldId()).append("','")
			  .append(FormEvent.ON_DOTDOTDOT[actionIndex])
			  .append("',").append(dirtyCheck)
			  .append(",").append(pushState)
			  .append(",").append(submit);
	
			if(pairs != null && pairs.length > 0) {
				for(NameValuePair pair:pairs) {
					sb.append(",'").append(pair.getName()).append("','").append(pair.getValue()).append("'");
				}
			}
	
			sb.append(")");
			return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
	
	/**
	 * This send an event to the server but don't wait for an answer in the form
	 * of HTML to render.
	 * 
	 * @param form The form object
	 * @param id The id of the element
	 * @param actionIndex The type of event (click...)
	 * @param pairs Additional name value pairs send by the AJAX call
	 * @return The code
	 */
	public static String getXHRNFFnCallFor(Form form, String id, int actionIndex, NameValuePair... pairs) {
		try(StringOutput sb = new StringOutput(128)) {
			sb.append("o_ffXHRNFEvent('")
			  .append(form.getFormName()).append("','")
			  .append(form.getDispatchFieldId()).append("','")
			  .append(id).append("','")
			  .append(form.getEventFieldId()).append("','")
			  .append(FormEvent.ON_DOTDOTDOT[actionIndex])
			  .append("'");
	
			if(pairs != null && pairs.length > 0) {
				for(NameValuePair pair:pairs) {
					sb.append(",'").append(pair.getName()).append("','").append(pair.getValue()).append("'");
				}
			}
	
			sb.append(")");
		return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
	
	public static String generateXHRFnCallVariables(Form form, String id, int actionIndex) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("var formNam = '").append(form.getFormName()).append("';\n")
		  .append("var dispIdField = '").append(form.getDispatchFieldId()).append("';\n")
		  .append("var dispId = '").append(id).append("';\n")
		  .append("var eventIdField = '").append(form.getEventFieldId()).append("';\n")
		  .append("var eventInt = ").append(FormEvent.ON_DOTDOTDOT[actionIndex]).append(";\n");
		return sb.toString();
	}
	
	public static String getXHRSubmit(Form form, NameValuePair... pairs) {
		try(StringOutput sb = new StringOutput(128)) {
			sb.append("o_ffXHRNFEvent('")
			   .append(form.getFormName()).append("'");
			if(pairs != null && pairs.length > 0) {
				for(NameValuePair pair:pairs) {
					sb.append(",'").append(pair.getName()).append("','").append(pair.getValue()).append("'");
				}
			}
			sb.append(")");
			return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}

	/**
	 * 
	 * @param sb
	 * @param jsonRenderInstruction
	 * @param acceptedInstructions
	 */
	public static void appendRenderInstructions(StringOutput sb,
			String jsonRenderInstruction, Set<String> acceptedInstructions) {
		JSONObject instr;
		try {
			instr = new JSONObject(jsonRenderInstruction);
			sb.append(" ");// ensure blank before appending instructions -> '
							// '...
			for (Iterator<String> iter = acceptedInstructions.iterator(); iter
					.hasNext();) {
				String accepted = iter.next();
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
		StringBuilder sb = new StringBuilder(150);
		sb.append(" <script>\n /* <![CDATA[ */ \n");
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		sb.append("(function() {");
		sb.append("var ").append(id).append(" = jQuery('#").append(id).append("'); ");
		return sb.toString();
	}
	
	public static String getJSStart(){
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		return " <script>\n /* <![CDATA[ */ \n (function() {";
	}
	
	public static String getJSEnd(){
		// Execute anonymous function (closure) now (OLAT-5755)
		return "})();\n /* ]]> */ \n</script>";
	}
	
	// Execute code within an anonymous function (closure) to not leak
	// variables to global scope (OLAT-5755)
	public static StringOutput appendFlexiFormDirty(StringOutput sb, Form form, String id) {
		return appendFlexiFormDirtyOn(sb, form, "change keypress", id);
	}
	
	public static StringOutput appendFlexiFormDirtyForCheckbox(StringOutput sb, Form form, String formDispatchId) {
		return appendFlexiFormDirtyOn(sb, form, "change mouseup", formDispatchId);
	}
	
	public static StringOutput appendFlexiFormDirtyForClick(StringOutput sb, Form form, String formDispatchId) {
		return appendFlexiFormDirtyOn(sb, form, "click", formDispatchId);
	}
	
	/**
	 * 
	 * @param sb The output
	 * @param form The form containing the button to be dirty
	 * @param events A list of space separated javascript events
	 * @param formDispatchId
	 * @return
	 */
	public static StringOutput appendFlexiFormDirtyOn(StringOutput sb, Form form, String events, String formDispatchId) {
		sb.append("<script>")
		  .append("(function() { \"use strict\";\njQuery('#").append(formDispatchId).append("').on('").append(events).append("', {formId:\"").append(form.getDispatchFieldId()).append("\", hideMessage:").append(form.isHideDirtyMarkingMessage()).append("}, setFlexiFormDirtyByListener);")
		  .append("})();</script>");
		return sb;
	}

	/**
	 * Set the flexi form dirty.
	 * 
	 * @param form The flexi-form
	 * @return A command
	 */
	public static Command getFlexiFormDirtyOnLoadCommand(Form form) {
		return CommandFactory.createDirtyForm(form);
	}
	
	public static String getSetFlexiFormDirtyFnCallOnly(Form form){
		if(form.isDirtyMarking()){
			return "setFlexiFormDirty('"+form.getDispatchFieldId()+"');";
		}
		return " ";
	}

	/**
	 * creates the JS fragment needed for the {@link #getInlineEditOkCancelHTML(StringOutput, String, String, String)} HTML fragment.
	 * @param sb where to append
	 * @param id formItemId of the InlineEdit FormItem
	 * @param oldHtmlValue escaped HTML value
	 * @param rootForm to extract the ID of the Form where to submit to.
	 */
	public static void getInlineEditOkCancelJS(StringOutput sb, String id, String oldHtmlValue, Form rootForm) {
		/*
		 * yesFn emulates a click on the input field, which in turn "submits" to the inlineElement to extract the value
		 */
		sb.append("var ").append(id).append("=jQuery('#").append(id).append("');")
		  .append(id).append(".focus(1);")//defer focus
		  .append("var o_ff_inline_yesFn = function(e){")
		  .append(FormJSHelper.getJSFnCallFor(rootForm, id, FormEvent.ONCLICK)).append(";};")
		  .append("jQuery('#").append(id).append("').on('blur',o_ff_inline_yesFn);");		

		/*
		 * noFn replaces the old value in the input field, and then "submits" to the inlineElement via yesFn
		 */
		sb.append("var o_ff_inline_noFn = function(e){ jQuery('#").append(id).append("').val('").append(oldHtmlValue).append("'); o_ff_inline_yesFn(e); };")
		  .append("jQuery('#").append(id).append("').keydown(function(e) {")
	      .append(" if(e.which == 27) {")
	      .append("   o_ff_inline_noFn();")
	      .append(" } else if(e.which == 10 || e.which == 13) {")
	      .append("   o_ff_inline_yesFn();")
	      .append(" }")
	      .append("});");
	}
	
	/**
	 * submits a form when the enter key is pressed.
	 * TextAreas are handled special and do not propagate the enter event to the outer world
	 * @param formName
	 * @return
	 */
	public static String submitOnKeypressEnter(String formName) {
		StringBuilder sb = new StringBuilder();
		sb.append(getJSStart())
		  .append("jQuery('#").append(formName).append("').keypress(function(event) {\n")
		  .append(" if (13 == event.keyCode) {\n")
		  .append("  event.preventDefault();\n")
		  .append("  if (this.onsubmit()) { this.submit(); }\n")
		  .append(" }\n")
		  .append("});\n")
		  .append(getJSEnd());
		return sb.toString();
	}


	/**
	 * JS command to set the form focus to a specific form item, to the first error or
	 * the last focused element
	 * 
	 * @param formName The root form name
	 * @param formItemId null or the form item id that needs to get the focus
	 * @return A JSCommand object
	 */
	public static Command getFormFocusCommand(String formName, String formItemId) {
		return CommandFactory.createFlexiFocus(formName, formItemId);
	}
}
