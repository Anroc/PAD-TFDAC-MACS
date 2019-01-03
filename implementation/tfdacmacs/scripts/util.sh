openssl req -text -noout -verify -in test1.csr  
openssl x509 -in test1.crt -text -noout  


faketime '2018-12-30 00:00:00' openssl x509 -req -days 365 -in test1.csr -CA ca.crt -CAkey ca.key -set_serial 05 -out test1.crt
openssl pkcs12 -export -clcerts -in test1.crt -inkey test1.key -out test1.p12

