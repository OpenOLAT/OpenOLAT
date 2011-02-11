function b_choice_toggleCheck(ref, checked) {
	var checkboxes = document.forms[ref].elements;
	len = checkboxes.length;
	if (typeof(len) == 'undefined') {
		checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			if (checkboxes[i].type == 'checkbox' && checkboxes[i].getAttribute('class') == 'b_checkbox') {
				checkboxes[i].checked=checked;
			}
		}
	}
}
