
function _gel( id )
{
	return document.getElementById( id );
}


String.prototype.trim=function()
{
    return this.replace(/^\s*|\s*$/g,'');
}

String.prototype.replaceAll=function(s1, s2)
{
    return this.replace(new RegExp(s1,"g"), s2);
}


function initLog()
{
	if (!window.console)
		console = {};

	console.log = console.log || function(){};
}


function logIt( m )
{
    try
    {
	console.log( m );
    }
    catch( e )
    {}
}




