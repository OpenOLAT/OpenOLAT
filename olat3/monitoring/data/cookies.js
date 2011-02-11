/*==============================================================================

    Routines written by John Gardner - 2003 - 2005

    See www.braemoor.co.uk/software for information about more freeware
    available.

/*==============================================================================

Routine to write a session cookie

    Parameters:
        cookieName        Cookie name
        cookieValue       Cookie Value
    
    Return value:
        true              Session cookie written successfullly
        false             Failed - persistent cookies are not enabled

   e.g. if (writeSessionCookie("pans","drizzle") then
           alert ("Session cookie written");
        else
           alert ("Sorry - Session cookies not enabled");
*/

function writeSessionCookie (cookieName, cookieValue) {
  if (testSessionCookie()) {
    document.cookie = escape(cookieName) + "=" + escape(cookieValue) + "; path=/";
    return true;
  }
  else return false;
}

/*==============================================================================

Routine to get the current value of a cookie

    Parameters:
        cookieName        Cookie name
    
    Return value:
        false             Failed - no such cookie
        value             Value of the retrieved cookie

   e.g. if (!getCookieValue("pans") then  {
           cookieValue = getCoookieValue ("pans2);
        }
*/

function getCookieValue (cookieName) {
  var exp = new RegExp (escape(cookieName) + "=([^;]+)");
  if (exp.test (document.cookie + ";")) {
    exp.exec (document.cookie + ";");
    return unescape(RegExp.$1);
  }
  else return false;
}

/*==============================================================================

Routine to see if session cookies are enabled

    Parameters:
        None
    
    Return value:
        true              Session cookies are enabled
        false             Session cookies are not enabled

   e.g. if (testSessionCookie())
           alert ("Session coookies are enabled");
        else
           alert ("Session coookies are not enabled");
*/

function testSessionCookie () {
  document.cookie ="testSessionCookie=Enabled";
  if (getCookieValue ("testSessionCookie")=="Enabled")
    return true 
  else
    return false;
}

/*==============================================================================

Routine to see of persistent cookies are allowed:

    Parameters:
        None
    
    Return value:
        true              Session cookies are enabled
        false             Session cookies are not enabled

   e.g. if (testPersistentCookie()) then
           alert ("Persistent coookies are enabled");
        else
           alert ("Persistent coookies are not enabled");
*/

function testPersistentCookie () {
  writePersistentCookie ("testPersistentCookie", "Enabled", "minutes", 1);
  if (getCookieValue ("testPersistentCookie")=="Enabled")
    return true  
  else 
    return false;
}

/*==============================================================================

Routine to write a persistent cookie

    Parameters:
        CookieName        Cookie name
        CookieValue       Cookie Value
        periodType        "years","months","days","hours", "minutes"
        offset            Number of units specified in periodType
    
    Return value:
        true              Persistent cookie written successfullly
        false             Failed - persistent cookies are not enabled
    
    e.g. writePersistentCookie ("Session", id, "years", 1);
*/       

function writePersistentCookie (CookieName, CookieValue, periodType, offset) {

  var expireDate = new Date ();
  offset = offset / 1;
  
  var myPeriodType = periodType;
  switch (myPeriodType.toLowerCase()) {
    case "years":
      expireDate.setYear(expireDate.getFullYear()+offset);
      break;
    case "months":
      expireDate.setMonth(expireDate.getMonth()+offset);
      break;
    case "days":
      expireDate.setDate(expireDate.getDate()+offset);
      break;
    case "hours":
      expireDate.setHours(expireDate.getHours()+offset);
      break;
    case "minutes":
      expireDate.setMinutes(expireDate.getMinutes()+offset);
      break;
    default:
      alert ("Invalid periodType parameter for writePersistentCookie()");
      break;
  } 
  
  document.cookie = escape(CookieName ) + "=" + escape(CookieValue) + "; expires=" + expireDate.toGMTString() + "; path=/";
}  

/*==============================================================================

Routine to delete a persistent cookie

    Parameters:
        CookieName        Cookie name
    
    Return value:
        true              Persistent cookie marked for deletion
    
    e.g. deleteCookie ("Session");
*/    

function deleteCookie (cookieName) {

  if (getCookieValue (cookieName)) writePersistentCookie (cookieName,"Pending delete","years", -1);  
  return true;     
}
