#-noexit -command "&{ import-module .\SapiCli.dll -verbose}"
write-host "import-module .\SapiCli.dll -verbose"
import-module .\SapiCli.dll -verbose
write-host "Connect-VIServer -Server w1-eco-vcsa-01.eco.eng.vmware.com -user administrator@vsphere.local -Password '_Ca$hc0w'"
Connect-VIServer -Server w1-eco-vcsa-01.eco.eng.vmware.com -user administrator@vsphere.local -Password '_Ca$hc0w'
write-host "Connect-Sapi -SapiUrl http://10.144.115.227:8080/sdk -Server w1-eco-vcsa-01.eco.eng.vmware.com -User administrator@vsphere.local -Password '_Ca$hc0w'"
Connect-Sapi -SapiUrl http://localhost:8080/sdk -Server w1-eco-vcsa-01.eco.eng.vmware.com -User administrator@vsphere.local -Password '_Ca$hc0w'
write-host "Connect-Target -Region  'us-west-2' -Backet 'vmbk4/dev' -AccessKey 'AKIA3CV76DGGGU4C763N' -SecretKey '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'  -Base64  -Name 'S3'"
Connect-Target -Region  'us-west-2' -Backet 'vmbk4/dev' -AccessKey 'AKIA3CV76DGGGU4C763N' -SecretKey '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'  -Base64  -Name 'S3'