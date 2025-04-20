# exception-retry

A sample code to explore resilience4j and rxJava3
Howto upload to central.sonatype.org
1. do ```mvn clean install```
   2. It will ask for the passphrase so please be ready with. You can just use mvn clean test (without passphrase)
2. do ```mvn site``` to generate exception-retry-1.0-zip.zip
3. Goto https://central.sonatype.com/publishing and upload the component 
   1. deployment name : io.github.venkateshamurthy:exception-retry:1.0 
   2. and choose the file exception-retry-1.0-zip.zip
   3. Click validate and post validation click publish