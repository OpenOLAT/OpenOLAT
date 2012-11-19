// test to see if the order of import is fine.
// js1.js must be imported first, that is, the var mytest1 must be defined.
var sub1 = mytest1;

function testsub1() {
	if (sub1 == "yep") {
		alert("ok: value of sub1 is :"+sub1);
	} else {
		alert("error: value should be 'yep'");
	}
}
