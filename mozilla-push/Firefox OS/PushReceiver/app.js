/* This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *   * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

var endpoint;
var msgCount=0;

/** Stupid logging function
 */
function Log(s, cls) {
  console.log(s)
  var txt = document.createTextNode(s);
  var p = document.createElement("p");
  p.className=cls;
  p.appendChild(txt);
  var stat = document.getElementById("status");
  if (msgCount > 10) {
      stat.removeChild(stat.firstChild);
  }else {
      msgCount = msgCount + 1;
  }
  document.getElementById("status").appendChild(p);
}

/** Set the message handler for the push events
 */
function setMessageHandler() {
  navigator.mozSetMessageHandler('push', function(e) {
    Log("last notification: \n" + e.version)
    
    if(e.data != undefined && typeof e.data.txt == 'function')
    {
      Log("data = " + e.data.txt())
    }
    else
    {
      Log("e.data.txt does not exist")
    }
  })
  navigator.mozSetMessageHandler("push-register", function(){
    Log("Recv'd re-registration ")
    doRegister()
  })
  Log("Message Handler Set.")
}


/** Register the endpoint with the server
 */
function doRegister() {
  Log("Registering...");
  
  var req = navigator.push.register();
  req.onsuccess = function(e) {
    endpoint = req.result;
    Log("Endpoint:" + endpoint)
  }
  req.onerror = function(e) {
      Log("Registration error: " + JSON.stringify(e), "error");
      return;
  }
}

// main
if (!navigator.push && !navigator.mozSetMessageHandler) {
  document.getElementById("config").style.display="none";
  Log("No push service.")
} else {

    setMessageHandler();
    document.getElementById("go").addEventListener("click", doRegister, true)
}

