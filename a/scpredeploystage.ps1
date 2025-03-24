
ant copystaticfilesfordeploy

$app="tm2ref2"
$contextroot="tr"
$baseurl="stage1.hravatar.com"


$prefix="${app}-1.0"
$identityfile="C:/dev/AmazonAWS/HRA-Stage-Oregon.pem"
$glassfishlocation="/usr/pgms/payara6.2025.2/payara6/glassfish"
$basedir="c:/work/${app}"
$targetdir="$basedir/target"

$DateTime = (Get-Date -Format "MM-dd-yyyy")
$proddistdir="/backup/dist/$DateTime"

# make dist dir
ssh -o "StrictHostKeyChecking=accept-new" -i "${identityfile}" ec2-user@${baseurl} "mkdir $proddistdir"


# Copy static files
scp -i "${identityfile}" -r "${basedir}/deploytemp/webmod"  ec2-user@${baseurl}:/work/${app}


# copy war file

# copy file to dist dir
scp -i "${identityfile}"  "${targetdir}/${prefix}.war"  ec2-user@${baseurl}:/backup/dist/$DateTime

# undeploy


ssh -i "${identityfile}" ec2-user@${baseurl} "$glassfishlocation/bin/asadmin  --user admin --passwordfile /home/payara/passwd.gf undeploy ${prefix}"

# deploy
ssh -i "${identityfile}" ec2-user@${baseurl} "$glassfishlocation/bin/asadmin --user admin --passwordfile /home/payara/passwd.gf deploy --virtualservers server --contextroot ${contextroot} --force=true ${proddistdir}/${prefix}.war"




