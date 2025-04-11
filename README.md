# exception-retry

A sample code to explore resilience4j and rxJava3
Howto upload to central.sonatype.org
1. do ```mvn clean install```
2. create the sha1, md5 on all artifacts such as using the script below(md5sha1sum)
```shell
#!/bin/bash
if [ -z "$1" ]; then
    echo "variable tag is empty (should be of the form \"ls exception-retry-1.0*\") so program will exit now!"
    exit 1
fi
list=`$1`
for entry in $list; do
    md5sum  $entry | cut -d ' ' -f 1 > $entry.md5;
    sha1sum $entry | cut -d ' ' -f 1 > $entry.sha1;
done
```
3. do ```mvn site``` to generate exception-retry-1.0-zip.zip
4. Goto https://central.sonatype.com/publishing and upload the component
5. deployment name : io.github.venkateshamurthy:exception-retry:1.0 and choose the file exception-retry-1.0-zip.zip