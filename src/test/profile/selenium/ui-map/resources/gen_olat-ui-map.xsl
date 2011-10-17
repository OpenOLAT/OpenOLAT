<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text"/>

	<xsl:template match="ui-map">
		<xsl:text>
//
// OLAT-UI-MAP.JS
// ==============
// This file contains the mappings of xpaths/links to selenium-understood javascript which can then
// be used in Selenium IDE and Selenium RC for testing.
//
//
// Note: This file is generatd using the ui-map/gen_olat-ui-map.xsl with the actual mappings stored in
//  -->  ui-map/olat-ui-map.xml
//


// INSTALL NOTE FOR SELENIUM IDE
// -----------------------------
//
// add the following line to Selenium IDE>Options>Options...>Selenium Core extensions (user-extensions.js):
//
// chrome://selenium-ide/content/ui-element.js, C:\eclipse\workspace\seleniumtesting\src\olat-ui-map.js


var myMap = new UIMap();

</xsl:text>
	    <xsl:apply-templates select="pageset"/>
	</xsl:template>


<!-- 


//
//
// ==================
// The main OLAT tabs
// ==================
//
//
myMap.addPageset({
    name: 'tabs'
    , description: 'main OLAT tabs'
    , pathRegexp: '.*'
});

 -->
	<xsl:template match="pageset">
		<xsl:if test="count(ancestor::pageset)=0">
	   		<xsl:if test="count(@description)=0 or @description=''">
	   			<xsl:message terminate="no">
   					<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
				</xsl:message>
	      			<xsl:message terminate="yes">
	      			<xsl:text>you must specify a description! Pageset=</xsl:text>
	      			<xsl:value-of select="@name"/>
	       		</xsl:message>
	       	</xsl:if>
			<xsl:text>
//
//
// ======
// PAGESET: </xsl:text>
			<xsl:value-of select="@name"/>
			<xsl:text>
// ======
//
//
myMap.addPageset({
    name: '</xsl:text><xsl:value-of select="@name"/><xsl:text>'
</xsl:text>
			<xsl:if test="@description">
				<xsl:text>    , description: "</xsl:text>
				<xsl:value-of select="@description"/>
			<xsl:text>"
</xsl:text>
			</xsl:if>
			<xsl:text>    , pathRegexp: '.*'
});

</xsl:text>

		</xsl:if>
		<xsl:apply-templates select="pageset"/>
		<xsl:apply-templates select="xpath-ui-element"/>
		<xsl:apply-templates select="link-ui-element"/>
		<xsl:apply-templates select="var-link-ui-element"/>
	</xsl:template>
	
<!-- 

// ADMINISTRATION
myMap.addElement('tabs', {
    name: 'administration'
    , description: 'main tab "Administration"'
    , xpath: "//a[span/text()='Administration']"
	, testcase1: {
	        xhtml: '<a expected-result="1"><span>Administration</span></a>'
	    }
});

 -->
<!-- 

// CLOSE AN OPENED COURSE
myMap.addElement('tabs', {
    name: 'closeCourse'
    , description: 'close a course'
    , args: [
        {
            name: 'nameOfCourse'
            , description: 'the name of the course'
            , defaultValues: [ 'Demo Course', 'Demo course wiki' ]
        }
    ]
    , getLocator: function(args) {
        var nameOfCourse = args['nameOfCourse'];
        return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
    }
});


 -->	
	<xsl:template match="xpath-ui-element">
		<xsl:variable name="name">
			<xsl:for-each select="ancestor::pageset[count(ancestor::pageset)!=0]">
				<xsl:value-of select="@name"/>
				<xsl:text>_</xsl:text>
			</xsl:for-each>
			<xsl:value-of select="@name"/>
		</xsl:variable>
   		<xsl:if test="count(@description)=0 or @description=''">
   			<xsl:message terminate="no">
   				<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
			</xsl:message>
      		<xsl:message terminate="yes">
      			<xsl:text>you must specify a description! Element=</xsl:text>
				<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
				<xsl:text>::</xsl:text>
      			<xsl:value-of select="$name"/>
       		</xsl:message>
       	</xsl:if>
		<!-- a newline first -->
		<xsl:text>
</xsl:text>

		<!-- // USERNAME input field -->
		<xsl:if test="@description">
			<xsl:text>// </xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>
</xsl:text>
		</xsl:if>
		
		<!-- myMap.addElement('dialog', { -->
		<xsl:text>myMap.addElement('</xsl:text>
		<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
		<xsl:text>', {
</xsl:text> 

		<!-- name: 'OK' -->
		<xsl:text>    name: '</xsl:text>
		<xsl:value-of select="$name"/>
		<xsl:text>'
</xsl:text>

		<!-- , description: 'OK Button in Dialog Popup Window' -->
		<xsl:if test="@description">
			<xsl:text>    , description: "</xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>"
</xsl:text>
		</xsl:if>
		
		<xsl:choose>
			<xsl:when test="count(parameter)>0">
<!-- 
    , args: [
        {
            name: 'nameOfCourse'
            , description: 'the name of the course'
            , defaultValues: [ 'Demo Course', 'Demo course wiki' ]
        }
    ]
    , getLocator: function(args) {
        var nameOfCourse = args['nameOfCourse'];
        return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
    }
 -->			
				<xsl:text>    , args: [
        {</xsl:text>
        		<xsl:for-each select="parameter">
        			<xsl:text>
            name: '</xsl:text>
	            	<xsl:value-of select="@name"/>
	            	<xsl:text>'
            , defaultValues: [ </xsl:text>
            		<xsl:if test="count(defaultValue)=0">
            			<xsl:text> "Foo", "Bar" </xsl:text>
	            	</xsl:if>
	           		<xsl:for-each select="defaultValue">
	           			<xsl:if test="position()!=1">
	           				<xsl:text>, </xsl:text>
	           			</xsl:if>
	           			<xsl:text>"</xsl:text>
	           			<xsl:value-of select="@value"/>
	           			<xsl:text>"</xsl:text>
	           		</xsl:for-each>
				<xsl:text> ]
</xsl:text>
					<xsl:if test="@description">
						<xsl:text>            , description: "</xsl:text>
						<xsl:value-of select="@description"/>
						<xsl:text>"
</xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:text>        }
    ]
    , getLocator: function(args) {
</xsl:text>
			<!--         var nameOfCourse = args['nameOfCourse'];
			 -->
			 	<xsl:for-each select="parameter">
			 		<xsl:text>         var </xsl:text>
			 		<xsl:value-of select="@name"/>
			 		<xsl:text> = args['</xsl:text>
			 		<xsl:value-of select="@name"/>
			 		<xsl:text>'];
</xsl:text>
			 	</xsl:for-each>
				
				<!--         return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
				 -->
				 <xsl:text>         return "</xsl:text>
				 <xsl:value-of select="@xpath"/>
				 <xsl:text>";
    }
});
</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<!--  , xpath: '//div[contains(@class, "x-window")]//button[text()="OK"]' -->
				<xsl:text>    , xpath: "</xsl:text>
				<xsl:value-of select="@xpath"/>
				<xsl:text>"
});
</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	
	
<!-- 

// ADMINISTRATION
myMap.addElement('tabs', {
    name: 'administration'
    , description: 'main tab "Administration"'
    , xpath: "//a[span/text()='Administration']"
	, testcase1: {
	        xhtml: '<a expected-result="1"><span>Administration</span></a>'
	    }
});

 -->
	<xsl:template match="link-ui-element">
		<xsl:variable name="name">
			<xsl:for-each select="ancestor::pageset[count(ancestor::pageset)!=0]">
				<xsl:value-of select="@name"/>
				<xsl:text>_</xsl:text>
			</xsl:for-each>
			<xsl:value-of select="@name"/>
		</xsl:variable>
   		<xsl:if test="count(@description)=0 or @description=''">
   			<xsl:message terminate="no">
   				<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
			</xsl:message>
      		<xsl:message terminate="yes">
      			<xsl:text>you must specify a description! Element=</xsl:text>
				<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
				<xsl:text>::</xsl:text>
      			<xsl:value-of select="$name"/>
       		</xsl:message>
       	</xsl:if>
		<!-- a newline first -->
		<xsl:text>
</xsl:text>

		<!-- // USERNAME input field -->
		<xsl:if test="@description">
			<xsl:text>// </xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>
</xsl:text>
		</xsl:if>
		
		<!-- myMap.addElement('dialog', { -->
		<xsl:text>myMap.addElement('</xsl:text>
		<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
		<xsl:text>', {
</xsl:text> 

		<!-- name: 'OK' -->
		<xsl:text>    name: '</xsl:text>
		<xsl:value-of select="$name"/>
		<xsl:text>'
</xsl:text>

		<!-- , description: 'OK Button in Dialog Popup Window' -->
		<xsl:if test="@description">
			<xsl:text>    , description: "</xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>"
</xsl:text>
		</xsl:if>
		
		<!--  , xpath: '//a[.//text()="Close detailed view"]' -->
		<xsl:text>    , xpath: "//a[.//text()='</xsl:text>
		<xsl:value-of select="@link"/>
		<xsl:text>']"
});
</xsl:text>
	</xsl:template>
	
	
<!-- 

// CLOSE AN OPENED COURSE
myMap.addElement('tabs', {
    name: 'closeCourse'
    , description: 'close a course'
    , args: [
        {
            name: 'nameOfCourse'
            , description: 'the name of the course'
            , defaultValues: [ 'Demo Course', 'Demo course wiki' ]
        }
    ]
    , getLocator: function(args) {
        var nameOfCourse = args['nameOfCourse'];
        return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
    }
});


 -->	
	<xsl:template match="var-link-ui-element">
		<xsl:variable name="name">
			<xsl:for-each select="ancestor::pageset[count(ancestor::pageset)!=0]">
				<xsl:value-of select="@name"/>
				<xsl:text>_</xsl:text>
			</xsl:for-each>
			<xsl:value-of select="@name"/>
		</xsl:variable>
   		<xsl:if test="count(@description)=0 or @description=''">
   			<xsl:message terminate="no">
   				<xsl:text>

-------------------------------
Syntax Error in olat-ui-map.xml
-------------------------------


</xsl:text>
			</xsl:message>
      		<xsl:message terminate="yes">
      			<xsl:text>you must specify a description! Element=</xsl:text>
				<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
				<xsl:text>::</xsl:text>
      			<xsl:value-of select="$name"/>
       		</xsl:message>
       	</xsl:if>
		<!-- a newline first -->
		<xsl:text>
</xsl:text>

		<!-- // USERNAME input field -->
		<xsl:if test="@description">
			<xsl:text>// </xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>
</xsl:text>
		</xsl:if>
		
		<!-- myMap.addElement('dialog', { -->
		<xsl:text>myMap.addElement('</xsl:text>
		<xsl:value-of select="ancestor-or-self::pageset[count(ancestor::pageset)=0]/@name"/>
		<xsl:text>', {
</xsl:text> 

		<!-- name: 'OK' -->
		<xsl:text>    name: '</xsl:text>
		<xsl:value-of select="$name"/>
		<xsl:text>'
</xsl:text>

		<!-- , description: 'OK Button in Dialog Popup Window' -->
		<xsl:if test="@description">
			<xsl:text>    , description: "</xsl:text>
			<xsl:value-of select="@description"/>
			<xsl:text>"
</xsl:text>
		</xsl:if>
		
<!-- 
    , args: [
        {
            name: 'nameOfCourse'
            , description: 'the name of the course'
            , defaultValues: [ 'Demo Course', 'Demo course wiki' ]
        }
    ]
    , getLocator: function(args) {
        var nameOfCourse = args['nameOfCourse'];
        return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
    }
 -->			
		<xsl:text>    , args: [
        {</xsl:text>
		<xsl:text>
            name: '</xsl:text>
       	<xsl:value-of select="@linkparam"/>
       	<xsl:text>'
            , defaultValues: [ "none" ]
            , description: "the link parameter"
        }
    ]
    , getLocator: function(args) {
</xsl:text>
		<!--         var nameOfCourse = args['nameOfCourse'];
		 -->
 		<xsl:text>         var linkparam = args['</xsl:text>
 		<xsl:value-of select="@linkparam"/>
 		<xsl:text>'];
</xsl:text>
		
		<!--         return "//a[contains(@class, 'b_nav_tab_close') and ../..//@title='"+nameOfCourse+"']";
		 -->
		 <xsl:text>         return "link="+linkparam;
    }
});
</xsl:text>
	</xsl:template>


</xsl:stylesheet>