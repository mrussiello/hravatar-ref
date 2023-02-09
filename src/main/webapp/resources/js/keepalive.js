

function hra_keepalivestart()
{
   //  hra_keepalive();
   setTimeout( hra_keepalive, 1000*60*15 );
}

function hra_keepalive()
{
    try
    {
        var r = new XMLHttpRequest();
        r.open( "GET", '/tr/misc/keepalive.xhtml', true );
        r.send();
        hra_keepalivestart();
    }

    catch( e )
    {
        cfLogErr( e, 'hra_keepalive() ' );
    }
}

if (window.addEventListener) // W3C standard
    window.addEventListener( 'load', hra_keepalivestart, false );

else if (window.attachEvent) // Microsoft
    window.attachEvent( 'load', hra_keepalivestart );



