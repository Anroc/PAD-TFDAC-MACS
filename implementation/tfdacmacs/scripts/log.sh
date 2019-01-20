#!/bin/bash

ssh anrock@undo.life "less +F /var/lib/jenkins/deploy/logs/$1.log"
