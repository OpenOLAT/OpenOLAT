function tableFormInjectCommandAndSubmit(formName, cmd, param) {
	document.forms[formName].elements["cmd"].value = cmd;
	document.forms[formName].elements["param"].value = param;
	document.forms[formName].submit();
}

function b_table_toggleCheck(ref, checked) {
	var tb_checkboxes = document.forms[ref].elements["tb_ms"];
	len = tb_checkboxes.length;
	if (typeof(len) == 'undefined') {
		tb_checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			tb_checkboxes[i].checked=checked;
		}
	}
}
