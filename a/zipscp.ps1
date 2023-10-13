
$app="tm2ref2"
$identityfile="C:/dev/AmazonAWS/ClicFlicKeyPair2-ps.pem"
$baseurl="imo1.hravatar.com"
$basedir="c:/work/${app}"
$backupdir="${basedir}/backup"
$DateTime = (Get-Date -Format "MM-dd-yyyy")

ant zipit

ssh -i "${identityfile}" ec2-user@${baseurl} "mkdir /backup/sw/${DateTime}"

scp -i "${identityfile}"  "${backupdir}/${app}.zip"  ec2-user@${baseurl}:/backup/sw/${DateTime}


