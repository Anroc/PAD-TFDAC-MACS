#!/bin/bash

docker kill couchbase
docker rm couchbase
docker run -d --name couchbase -p 8091-8094:8091-8094 -p 11210:11210 -v couchbase-volume:/opt/couchbase/var couchbase/server

