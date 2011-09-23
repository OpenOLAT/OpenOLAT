// filler text on demand
// http://web-graphics.com/mtarchive/001667.php
//
// added methods to fill large tables and images to force 
// overflow situation in most layouts
// florian gnaegi http://www.frentix.com

var words=new Array('lorem','ipsum','dolor','sit','amet','consectetuer','adipiscing','elit','suspendisse','eget','diam','quis','diam','consequat','interdum');

function AddFillerLink(){
if(!document.getElementById || !document.createElement) return;
var i,l;
for(i=0;i<arguments.length;i++){
	
	if (document.getElementById(arguments[i])) { /* Check elements exists - add Reinhard Hiebl */
		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("Add Text"));
		l.onclick=function(){AddText();return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);
		
		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("M table"));
		l.onclick=function(){AddTable(5);return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("L table"));
		l.onclick=function(){AddTable(10);return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("XL table"));
		l.onclick=function(){AddTable(15);return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);


		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("XS img"));
		l.onclick=function(){AddImage("100x100.jpg");return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("S img"));
		l.onclick=function(){AddImage("100x200.jpg");return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("M img"));
		l.onclick=function(){AddImage("200x400.jpg");return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("L img"));
		l.onclick=function(){AddImage("200x600.jpg");return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		l=document.createElement("a");
		l.href="#";
		l.appendChild(document.createTextNode("XL img"));
		l.onclick=function(){AddImage("200x800.jpg");return(false)};
		document.getElementById(arguments[i]).appendChild(l);
		b=document.createTextNode(" | ");
		document.getElementById(arguments[i]).appendChild(b);

		r=document.createElement("a");
		r.href="#";
		r.id="removeLink";
		r.appendChild(document.createTextNode("Remove last step"));
		r.onclick=function(){RemoveLastStep(this);return(false)};
			document.getElementById(arguments[i]).appendChild(r);
	}
}
}

function AddText(){
	var el = document.getElementById("removeLink");
	var s="",n,i;
	n=RandomNumber(20,80);
	for(i=0;i<n;i++)
    	s+=words[RandomNumber(0,words.length-1)]+" ";
	var t=document.createElement("p");
	t.setAttribute('class','added');
	t.appendChild(document.createTextNode(s));
	el.parentNode.insertBefore(t,el);
}
function AddTable(cols){
	var el = document.getElementById("removeLink");
	var s="<table border='1'>";
	for(j=0;j<5;j++) {
		s+= "<tr>";
		for(i=0;i<cols;i++) {
    		s+= "<td>" + words[RandomNumber(0,words.length-1)] + "</td>";
    	}
		s+= "</tr>";
    }
    s+="</table>";
	var t=document.createElement("p");
	t.setAttribute('class','added');
	t.innerHTML = s;
	el.parentNode.insertBefore(t,el);
}
function AddImage(img){
	var el = document.getElementById("removeLink");
	var s="<img src='http://devel.frentix.com/filler/";
	s+= img;
	s+= "' />";
	var t=document.createElement("p");
	t.setAttribute('class','added');
	t.innerHTML = s;
	el.parentNode.insertBefore(t,el);
}
function RemoveLastStep(el){
	var parent = el.parentNode;
	for(var i=parent.childNodes.length-1; i>0;i--) {
		var para = parent.childNodes[i];
		if(para.nodeName == "P" && para.getAttribute('class')=='added') {
			parent.removeChild(para);
			break;
		}
	}
}
function RandomNumber(n1,n2){
return(Math.floor(Math.random()*(n2-n1))+n1);
}
