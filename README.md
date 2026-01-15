# hytale-test-plugin

**Installing the Hytale Dependencies**

Example: ```mvn install:install-file ^
  -Dfile="C:\Users\15055\Downloads\HytaleDownloader\Server\HytaleServer.jar" ^
  -DgroupId=io.hytale ^
  -DartifactId=hytale-api ^
  -Dversion=early-access ^
  -Dpackaging=jar ^
  -DgeneratePom=true```


**Building Plugin JAR**

`mvn -U clean package`

**Starting Hytale Server**

`java -jar HytaleServer.jar --assets ../Assets.zip`

**Opening Port 5520**

Linux: `sudo iptables -A INPUT -p udp --dport 5520 -j ACCEPT`

Windows: `New-NetFirewallRule -DisplayName "Hytale Server" -Direction Inbound -Protocol UDP -LocalPort 5520 -Action Allow`
