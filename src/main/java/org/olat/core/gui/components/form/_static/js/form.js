function b_form_toggleCheck(field, doCheck) {
	for (i = 0; i < field.length; i++) {
		field[i].checked = doCheck;
	}
}

function b_form_updateFormElementVisibility(formName, selectionElementName, dependentElementName, ruleValue, ruleResult, resetValue, hideDisabledElements, preventOppositeAction) {
	// updates a form elements visibility and editability according to
	// the configuration
	// formName: name of the form
	// selectionElementName: name of form selection element hat defines dependency
	// dependentElementName: name of form element that is dependent on the selection
	// ruleValue: the selection value that must match
	// trueResult: the visibility rule in case of a match
	// resetValue: the value to be set if visibiliy is set to false
	// hideDisabledElements: true: disabled elements will disapear from screen, false: disabled
	// elements will be disabled and greyed, but still visible
	// (fg)
	var selectionElement = document.forms[formName].elements[selectionElementName];
	var selectionValue = selectionElement.value;
	if (selectionValue == null) {
		for (i=0; i<selectionElement.length;i++) {
			var val = selectionElement[i];
			if (val.checked) selectionValue = val.value;
		}
	}
	var dependentElement = document.forms[formName].elements[dependentElementName];

	// dependentElement can be null if dependentElement is of type spacer or static text
	// in this case the element is not a form element and thus won't be found
	if (selectionValue == ruleValue) {
		if (ruleResult) {
			b_form_enableFormElement(formName, dependentElement, dependentElementName, hideDisabledElements);
		} else {
			b_form_disableFormElement(formName, dependentElement, resetValue, dependentElementName, hideDisabledElements);
			
		}	
	} else {
		if (!preventOppositeAction) {
			if (ruleResult) {
				b_form_disableFormElement(formName, dependentElement, resetValue, dependentElementName, hideDisabledElements);
			} else {
				b_form_enableFormElement(formName, dependentElement,dependentElementName, hideDisabledElements);
			}		
		}
	}	
}

function b_form_enableFormElement(formName, formElement, dependentElementName, hideDisabledElements) {
	// helper: enables a form element for editing (fg)
	if (formElement != null && formElement.style != null) {
		formElement.className="b_form_element_enabled";
		formElement.readOnly = false;
	}
	if (hideDisabledElements) {
		document.getElementById("ber_" + dependentElementName + formName).style.display="";	
	}
}

function b_form_disableFormElement(formName, formElement, resetValue, dependentElementName, hideDisabledElements) {
	// helper: disables a form element for editing (fg)
	if (formElement != null) {
		if (formElement.style != null)
			formElement.className="b_form_element_disabled";
		formElement.readOnly = true;
		if (formElement.length) {
			if (formElement.type == "select-one") {
				formElement.value = resetValue;
			} else {
				for (var i=0; i < formElement.length; ++i){
					var elem = formElement[i];
					if (elem.type == "checkbox") {
						elem.checked = (resetValue == "true" ? true : false);
					} else if (elem.type == "radio") {
						elem.checked = (elem.value == resetValue ? true : false);
					} else {
						alert("OLAT unsupported element error: elemName::" + dependentElementName + " type::" + elem.type + " resetVal::" + resetValue);
					}
				}
			}
		} else {
			if (formElement.type == "checkbox") {
				formElement.checked = (resetValue == "true" ? true : false);
			} else if (formElement.type == "radio") {
				formElement.checked = (formElement.value == resetValue ? true : false);
			} else {
				formElement.value = resetValue;
			}
		}
	}
	if (hideDisabledElements) {
		document.getElementById("ber_" + dependentElementName + formName).style.display="none";
	}
}

//resizes form element textarea automatically to fit the text size
function b_form_countLines(strtocount, cols) {
    var hard_lines = 1;
    var last = 0;
    while ( true ) {
        last = strtocount.indexOf("\n", last+1);
        hard_lines ++;
        if ( last == -1 ) break;
    }
    var soft_lines = Math.round(strtocount.length / (cols-1));
    var hard = eval("hard_lines  " + unescape("%3e") + "soft_lines;");
    if ( hard ) soft_lines = hard_lines;
    return soft_lines;
}

function b_form_resizeTextarea(id) {
    var textarea = document.getElementById(id);
    if (textarea) {
	    textarea.rows = b_form_countLines(textarea.value,textarea.cols) +1;
	    setTimeout("b_form_resizeTextarea('"+id+"');", 800);
	}
}
