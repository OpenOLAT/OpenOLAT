// test to see if the order of import is fine.
// js2.js must be imported first, that is, the var mytest1 must be defined.
var sub2 = mytest2;

function testsub2() {
	if (sub2 == "yep2") {
		alert("ok: value of sub1 is :"+sub2);
	} else {
		alert("error: value should be 'yep2'");
	}
}
