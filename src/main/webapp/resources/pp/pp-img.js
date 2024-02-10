
var piImgCaptCount = 0;
var piImgFailedCount = 0;
var getUserMediaOk = false;
var lastGetUserMediaError = null;
var localPublisherVideoElement = null;



function piInitVideoEleForImages( maxWid, maxHgt )
{
    try
    {
        if( localPublisherVideoElement )
        {  
            getUserMediaOk=true; 
            return;
        }
        
        maxWid = (maxWid) ? maxWid : 200;
        maxHgt = (maxHgt) ? maxHgt : 150;
        
        // console.log( 'pp-img.js.piInitVideoEleForImages() localPublisherVideoElementId not found. Creating.' );
        var thumbDiv = document.getElementById('ppthumbdiv');
        if( (thumbDiv) )
        {
            thumbDiv.innerHTML='';
            
            var mrConstraints = { audio: false, video: {facingMode: "user"} };
            
            if( (selCameraDevice) )
                mrConstraints.video.deviceId={exact:selCameraDevice};
            
            var vidEle = document.createElement( 'video' );
            localPublisherVideoElement = vidEle;
            vidEle.setAttribute( 'muted','true');
            vidEle.setAttribute( 'title', keepFaceInFrontOfCameraMessage );
            vidEle.setAttribute( 'playsinline','true');
            var ves = vidEle.style;
            ves.maxWidth= maxWid + 'px';
            ves.maxHeight=maxHgt + 'px';
            ves.backgroundColor='#eaeaea';                
            vidEle.id = 'ppthumbvideoele';
            thumbDiv.appendChild( vidEle );

            navigator.mediaDevices.getUserMedia( mrConstraints ).then( function(stream) { 
                localPublisherVideoElement.srcObject = stream; 
                localPublisherVideoElement.play();
                getUserMediaOk=true;
                lastGetUserMediaError=null;
                }, 
                function(e) {
                    getUserMediaOk=false;
                    var emsg = e.name + ': ' + e.message;
                    lastGetUserMediaError = emsg;
                    ppHandleError( 'pp-img.js.piInitVideoEleForImages().getUserMedia() Error ' + emsg ); 
                    if( (emsg) && emsg.indexOf( 'NotAllowedError') )
                        showDeviceAccessDeniedMessage( e.name );
                    else if( (emsg) && emsg.indexOf( 'NotFoundError') )
                        showCameraNotFoundMessage( emsg );
                    else if( (emsg) && emsg.indexOf( 'NotReadableError') )
                        showDeviceAccessErrorMessage( emsg );                
                    else if( (emsg) && emsg.indexOf( 'OverconstrainedError') )
                        showDeviceAccessErrorMessage( emsg );                
                    else if( (emsg) && emsg.indexOf( 'SecurityError') )
                        showDeviceAccessErrorMessage( emsg );                
                    else if( (emsg) && emsg.indexOf( 'TypeError') )
                        showDeviceAccessErrorMessage( emsg );                
                    else
                        showDeviceAccessErrorMessage( emsg );                
                }            
            );                

            //localPublisherVideoElement = vidEle;
        }
    }
    catch( e )
    {
        getUserMediaOk=false;
        ppHandleError( 'pp-img.js.piInitVideoEleForImages() Error ' + (e.name ? e.name : '') + ': ' + e.message + ',\n' + e.stack );        
        showDeviceAccessErrorMessage();
    }    
}


function piUploadVideoThumbnail( uft, nthrd )
{
    try
    {
        uft = (uft) ? uft : uploadedUserFileTypeId;
        nthrd = (nthrd) ? nthrd : false;
        
        if( !(localPublisherVideoElement) )
        {
            piInitVideoEleForImages();
            return false;
        }
        else
            getUserMediaOk = true;
        
        if(  !getUserMediaOk )
        {
            piImgFailedCount++;
            doLocalErrorNotifications( 'localPublisherVideoElement is null.', false );
            console.log( 'pp-img.js.piUploadVideoThumbnail() getUserMediaOk=' + getUserMediaOk );
            return false;
        }
        
        if( !(localPublisherVideoElement) )
        {
            piImgFailedCount++;
            doLocalErrorNotifications( 'localPublisherVideoElement is null.', false );
            console.log( 'pp-img.js.piUploadVideoThumbnail() localPublisherVideoElement not found. Returning.' );
            return false;
        }
        
        var medEle = localPublisherVideoElement; // document.getElementById( localPublisherVideoElementId );
                
        //medEle.pause(); 
        var canvEle = document.createElement( 'canvas' );
        canvEle.width = medEle.videoWidth; // medEle.videoWidth;
        canvEle.height = medEle.videoHeight; //  medEle.videoHeight;

        var ctxt = canvEle.getContext( '2d' );
        ctxt.clearRect(0, 0, canvEle.width, canvEle.height);
        ctxt.drawImage( medEle,0,0,canvEle.width,canvEle.height);

        var blob = piDataURItoBlob( canvEle.toDataURL( 'image/jpeg') );
        //medEle.play(); 

        if( !(blob) )
        {
            ppSendLogToServer( 1, 'pp-img.js.piUploadVideoThumbnail() blob is null. piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount );
            console.log( 'pp-img.js.piUploadVideoThumbnail() blob not created. blob is null' );
            // let this flow through to an error.
        }
        
        else if( blob.size<4000 )
        {
            console.log( 'pp-img.js.piUploadVideoThumbnail() SMALL blob.size=' + blob.size );
            ppSendLogToServer( 1, 'pp-img.js.piUploadVideoThumbnail() blob.size is invalid. blob.size=' + blob.size + ', type=' + blob.type + ',  piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount );
            piImgFailedCount++;
            if( piImgFailedCount<10 )
            {    
                console.log( 'pp-img.js.piUploadVideoThumbnail() Trying again.' );
                setTimeout( function(){piUploadVideoThumbnail();}, 3000 );                
            }
            return false;
        }
        //if( (nthrd) )
        //    sendBlob( uft, nthrd, blob );

        //else
        setTimeout( function(){sendBlob( uft, nthrd, blob );}, 500 );

        return true;
        //if( piImgCaptCount>10 && piImgCaptCount%10===1 )
        //    piAdjImgCapInterval();
    }
    catch( e )
    {
        piImgFailedCount++;
        ppHandleError( 'pp-img.js.piUploadVideoThumbnail() BB ' + e.message + ', localPublisherVideoElementId=' + (localPublisherVideoElement ? localPublisherVideoElement.id : 'undefined' ) + ', piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount + ',\n' + e.stack );        
        return false;
    }
}

function sendBlob( uft, nthrd, blob )
{
    try
    {
        if( !(blob) )
        {
            piImgFailedCount++;
            doLocalErrorNotifications( 'collected image is null.', false );
            ppSendLogToServer( 1, 'pp-img.js.sendBlob() blob is null. piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount );
            return;
        }
        
        if( blob.size <=0 )
        {
            piImgFailedCount++;
            doLocalErrorNotifications( 'collected image is empty.', false );
            ppSendLogToServer( 1, 'pp-img.js.sendBlob() blob.size is invalid. blob.size=' + blob.size + ', type=' + blob.type + ',  piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount );
            return;
        }

        logIt( 'pp-img.js.sendBlob() blob.size=' + blob.size + ', type=' + blob.type + ',  piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount );
        
        uft = (uft) ? uft : uploadedUserFileTypeId;
        var noThread = typeof nthrd==='undefined' ? false : nthrd;

        var fd = new FormData();
        fd.append( 'rcid', rcCheckId );
        fd.append( 'rtrid', rcRaterId );

        fd.append( 'uft', uft );
        
        if( noThread )
           fd.append( 'nthrd', 'true' );
       
        fd.append( 'blobfile', blob );
        fd.append( 'blobtype', blob.type );
        fd.append( 'blobsize', blob.size );
       
        var url = '/tr/ppfupload';
        
        if( typeof acidx !=='undefined' )
        {    
            url += '?acidx=' + acidx;
            if( typeof refpagex !=='undefined')
                url += '&refpagex=' + refpagex;
        }

        var o=this;
        xhr = new XMLHttpRequest();
        xhr.open( "POST", url , true );
        xhr.addEventListener( 'load', function(e) { o.piDoOnImageUpload.call( o, e, xhr );} );
        xhr.addEventListener( 'error', function(e) { o.piDoOnImageUploadError.call( o, e ); } );
        xhr.upload.addEventListener( 'error', function(e) { o.piDoOnImageUploadError.call( o, e ); } );
        xhr.upload.addEventListener( 'abort', function(e) { o.piDoOnImageUploadError.call( o, e ); } );
        xhr.send( fd );
                
    }
    catch( e )
    {
        piImgFailedCount++;
        doLocalErrorNotifications( 'sendBlob() Error: ' + e.message, false );
        ppHandleError( 'pp-img.js.sendBlob() BB ' + e.message + ', localPublisherVideoElementId=' + (localPublisherVideoElement ? localPublisherVideoElement.id : 'undefined' ) + ', piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount + ',\n' + e.stack );        
    }
}

function doLocalErrorNotifications( msg, show )
{
    if( typeof showHideImageUploadDialog!=='undefined' )
        showHideImageUploadDialog( show );
    if( typeof localDoOnImageUploadError!=='undefined' )
        localDoOnImageUploadError( msg );    
}


function piDataURItoBlob(u) 
{
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byt;
    if (u.split(',')[0].indexOf('base64') >= 0)
        byt = atob(u.split(',')[1]);
    else
        byt = unescape(u.split(',')[1]);

    // separate out the mime component
    var mime = u.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byt.length);
    for (var i = 0; i < byt.length; i++) {
        ia[i] = byt.charCodeAt(i);
    }

    return new Blob([ia], {type:mime});
};



function piDoOnImageUploadError( e )
{
    piImgFailedCount++;
    doLocalErrorNotifications( 'piDoOnImageUploadError() Error: ' + ( (e) ? e.message : 'No error message available.' ), false );
    ppHandleError( 'pp-img.js.piDoOnImageUploadError() BB piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount + ', ' + ( (e) ? e.message + ',\n' + e.stack : ' e is not present.' ) );   
    
}


function piDoOnImageUpload( e, xhr )
{
    with( this )
    {
        // logIt( 'CUploader.onload() START ' + url );
        var rt = xhr.responseText;

        if( !(rt) ) // == null )
            rt = '';

        rt = rt.toUpperCase();

        // if( retryct<2 )
        //    rt = "FORCED ERROR RetryCt=" + retryct;

        // if( CUploadMgr.TV1>2 && xhr.status === 200 && rt === 'SUCCESS' )
        if( xhr.status === 200 && rt === 'SUCCESS' )
        {
            piImgCaptCount++;
            
            console.log( 'pp-img.js.piDoOnImageUpload() SUCCESS piImgCaptCount=' + piImgCaptCount );            

            if( piImgCaptCount>10 && piImgCaptCount%10===1 )
                piAdjImgCapInterval();

            if( typeof localDoOnImageUploadSuccess!=='undefined' )
                localDoOnImageUploadSuccess.call( this );
        }

        else
        {
            piImgFailedCount++;
            doLocalErrorNotifications( 'Upload Error Http.status=' + xhr.status + ', text=' + rt  + ', piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount, false );
            console.log( 'pp-img.js.piDoOnImageUpload() Error Http.status=' + xhr.status + ', text=' + rt  + ', piImgCaptCount=' + piImgCaptCount + ', piImgFailedCount=' + piImgFailedCount);
        }
    }
}

