// Load OLAT translator
function findMainWindow(win) {
	if (win.b_jsTranslatorFactory) return win;
	else if (win.opener) return findMainWindow(opener);
	else return null;
}
var mainWin = findMainWindow(window);
var translator;
if (mainWin) {
	translator = mainWin.b_jsTranslatorFactory.getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer')	
} else {
	// implement dummy-translator
	translator = {	translate : function(key) { return key; } }
}

function contextHelpWindow(URI) {
	var helpWindow = window.open(URI, "HelpWindow", "height=760, width=940, left=0, top=0, location=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no");
	helpWindow.focus();
}

function setBool(pl, p, n) {
	if (typeof(pl[n]) == "undefined")
		return;
	var checked = (pl[n] == "true" ||  pl[n] == true ? true : false);
	document.forms[0].elements[(p != null ? p + "_" : '') + n].checked=checked;
}

function setStr(pl, p, n) {
	var f = document.forms[0], e = f.elements[(p != null ? p + "_" : '') + n];

	if (typeof(pl[n]) == "undefined")
		return;

	if (e.type == "text" || e.type == "hidden")
		e.value = pl[n];
	else
		selectByValue(f, (p != null ? p + "_" : '') + n, pl[n]);
}

function getBool(p, n, d, tv, fv) {
	var v = document.forms[0].elements[(p != null ? p + "_" : "") + n].checked;

	tv = typeof(tv) == 'undefined' ? 'true' : "'" + jsEncode(tv) + "'";
	fv = typeof(fv) == 'undefined' ? 'false' : "'" + jsEncode(fv) + "'";

	return (v == d) ? '' : n + (v ? ':' + tv + ',' : ':' + fv + ',');
}

function getStr(p, n, d) {
	var e = document.forms[0].elements[(p != null ? p + "_" : "") + n];
	var v = (e.type == "text" || e.type == "hidden") ? e.value : e.options[e.selectedIndex].value;

	return ((n == d || v == '') ? '' : n + ":'" + jsEncode(v) + "',");
}

function getInt(p, n, d) {
	var e = document.forms[0].elements[(p != null ? p + "_" : "") + n];
	var v = e.type == "text" ? e.value : e.options[e.selectedIndex].value;

	return ((n == d || v == '') ? '' : n + ":" + v.replace(/[^0-9]+/g, '') + ",");
}

function jsEncode(s) {
	s = s.replace(new RegExp('\\\\', 'g'), '\\\\');
	s = s.replace(new RegExp('"', 'g'), '\\"');
	s = s.replace(new RegExp("'", 'g'), "\\'");

	return s;
}

function checkTimeFormat(variable) {
	var d = document, f = d.forms[0], s;
	s = getStr(null, variable);
	if (s=="0") return;
	var sa = s.split(":");
	if (sa.length >= 3) return;
	// all other cases it is wrong	
	alert(translator.translate("olatmovieviewer.invalid_date"));
}
