#-noexit -command "&{ import-module .\SapiCli.dll -verbose}"
write-host "import-module .\SapiCli.dll -verbose"
import-module .\SapiCli.dll -verbose
write-host "Connect-VIServer -Server w1-eco-vcsa-01.eco.eng.vmware.com -user administrator@vsphere.local -Password '_Ca$hc0w'"
Connect-VIServer -Server w1-eco-vcsa-01.eco.eng.vmware.com -user administrator@vsphere.local -Password '_Ca$hc0w'
#Connect-VIServer -Server 10.161.139.74 -user administrator@vsphere.local -Password 'Admin!23'

write-host "Connect-Sapi -SapiUrl https://localhost:8443/sdk -Server w1-eco-vcsa-01.eco.eng.vmware.com -User administrator@vsphere.local -Password '_Ca$hc0w'" -IgnoreCert
Connect-Sapi -SapiUrl https://localhost:8443/sdk -Server w1-eco-vcsa-01.eco.eng.vmware.com -User administrator@vsphere.local -Password '_Ca$hc0w'  -IgnoreCert
#Connect-Sapi -SapiUrl https://localhost:8443/sdk -Server 10.161.139.74 -User administrator@vsphere.local -Password 'Admin!23'  -IgnoreCert
write-host "Connect-Repository -S3 -Region  'us-west-2' -Backet 'vmbk4/dev' -AccessKey 'AKIA3CV76DGGGU4C763N' -SecretKey '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'   -Name 'S3'"
Connect-Repository -S3 -Region  'us-west-2' -Backet 'vmbk4/dev' -AccessKey 'AKIA3CV76DGGGU4C763N' -SecretKey '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'   -Name 'S3'