#!/bin/bash

make create-keystore KEYSTORE=ca-keystore.jks
make add-host KEYSTORE=ca-keystore.jks KEYSTORE_OTHER=ca-keystore.jks
make export-authority KEYSTORE=ca-keystore.jks
make import-ca KEYSTORE=aa-keystore.jks
make add-host HOSTNAME=aa.tu-berlin.de KEYSTORE=aa-keystore.jks KEYSTORE_OTHER=ca-keystore.jks
make create-truststore

cp truststore.jks ca-truststore.jks
mv truststore.jks aa-truststore.jks

mv aa* ../attributeAuthority/src/main/resources/
mv ca* ../centralServer/src/main/resources/