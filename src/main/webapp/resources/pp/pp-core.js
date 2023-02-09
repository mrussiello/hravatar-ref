


function showDeviceInUseMessage()
{
    ppHandleError( 'pp-core.js.showDeviceInUseMessage() ' );
    showAlertMsg( errorDeviceInUseMessage, null, false, false, true );
}


function showDeviceAccessDeniedMessage( m )
{
    ppHandleError( 'pp-core.js.showDeviceAccessDeniedMessage() ' + ((m) ? m : '') );
    showAlertMsg( errorDeviceAccessDenied, m, false, false, true );
}

function showCameraNotFoundMessage( m )
{
    ppHandleError( 'pp-core.js.showCameraNotFoundMessage() ' + ((m) ? m : '') );
    showAlertMsg( errorCameraNotFound, m, false, false, true );
}

function showDeviceAccessErrorMessage( m )
{
    ppHandleError( 'pp-core.js.showDeviceAccessErrorMessage() ' + ((m) ? m : '') );
    showAlertMsg( errorDeviceAccessError, m, false, false, true );
}

function showBrowserNotSupportedMessage()
{
    ppHandleError( 'pp-core.js.showBrowserNotSupportedMessage() ' );
    showAlertMsg( errorBrowserNotSupported, null, false, false, true );
}

function showAlertMsg( msg, msg2, showClose, showRetry, showCameraHelp )
{
    showClose = (showClose) ? true : false;
    showRetry = (showRetry) ? true : false;
    showCameraHelp = (showCameraHelp) ? true : false;
    
    document.getElementById('alertmsgtxtspan').innerHTML=msg;
    document.getElementById('alertmsgtxtspan2').innerHTML=(msg2) ? msg2 : '';
    
    if( !(document.getElementById( 'alertmessageretrybut' )) )
        showRetry=false;
    
    if( !(document.getElementById( 'alertmessagehelpbut' )) )
        showCameraHelp=false;
    
    if( showRetry )
    {       
        showClose = false;
        document.getElementById( 'alertmessageretrybut' ).style.display='inline-block';
        document.getElementById( 'alertmessageclosebut' ).style.display='none';
    }
    else
    {
        if( (document.getElementById( 'alertmessageretrybut' )) )
            document.getElementById( 'alertmessageretrybut' ).style.display='none';
        document.getElementById( 'alertmessageclosebut' ).style.display='inline-block';        
    }

    document.getElementById( 'alertmessageclosebut' ).style.display= showClose ? 'inline-block' : 'none';        
        
    if( showCameraHelp )
    {
        if( (document.getElementById( 'alertmessagehelpbut' )) )
            document.getElementById( 'alertmessagehelpbut' ).style.display='inline-block';        
        if( (document.getElementById( 'alertmessagehelpbut2' )) )
            document.getElementById( 'alertmessagehelpbut2' ).style.display='inline-block';        
    }
    else
    {
        if( (document.getElementById( 'alertmessagehelpbut' )) )
            document.getElementById( 'alertmessagehelpbut' ).style.display='none';        
        if( (document.getElementById( 'alertmessagehelpbut2' )) )
            document.getElementById( 'alertmessagehelpbut2' ).style.display='none';        
    }
    
    PF( 'alertmessaggedialog' ).show();
}


function ppShowReloadDialog()
{
    PF( 'reloadrequireddialog' ).show();
}


function ppCommsError( msg )
{
    ppHandleError( 'pp-core.js.ppCommsError() ' + msg );
}


function ppHandleError( msg )
{
    if( !(msg) )
        return;

    console.log( msg );
    ppSendLogToServer( 0, msg );
    
    if( isDebugMode )
        alert( 'Error Message: ' + msg );
}

function ppSendLogToServer( level, msg )
{
    cfSendLogMessage( msg, level );
}


/*
function ppHttpGetRequest(url, errorMsg, callback, errorCallback ) 
{
    ppHttpRequest(url, null, errorMsg, callback, errorCallback );
}

function ppHttpPostRequest(url, body, errorMsg, callback, errorCallback ) 
{
    ppHttpRequest(url, body, errorMsg, callback, errorCallback ); 
}

function ppHttpRequest(url, bodyJo, errorMsg, callback, errorCallback ) 
{
    var http = new XMLHttpRequest();

    if( (typeof callback==='function') || (typeof errorCallback==='function') )
        http.addEventListener('readystatechange', processPPHttpResp, false);

    if( (bodyJo) )
    {
        console.log( 'pp-core.js.ppHttpRequest() sending POST to url=' + url + ', payload=' + JSON.stringify(bodyJo) );
        http.open('POST', url, true);
        http.setRequestHeader('Content-type', 'application/json');
        http.send(JSON.stringify(bodyJo));
    }
    else
    {
        console.log( 'pp-core.js.ppHttpRequest() sending GET to ' + url );
        http.open('GET', url, true );
        http.send();            
    }

    function processPPHttpResp() 
    {
        // console.log( 'pp-core.js.ppHttpPostRequest().processRequest() BBB' );
        if (http.readyState == 4) {
                if (http.status == 200) {
                        try 
                        {                                    
                            console.log( 'pp-core.js.ppHttpPostRequest().processRequest() response ' + http.responseText );                                    

                            if( callback )
                                callback(JSON.parse(http.responseText));
                        } 
                        catch (e) 
                        {
                            console.log( 'pp-core.js.ppHttpPostRequest().processRequest() Error ' + e.message );
                            if( callback )
                                callback();
                        }
                } 
                else 
                {
                    console.warn(errorMsg);
                    console.warn(http.responseText);

                    if( typeof errorCallback==='function' )
                        errorCallback( errorMsg +  ' (' + http.status + ')' );
                }
        }
    }
}
 * 
 */
