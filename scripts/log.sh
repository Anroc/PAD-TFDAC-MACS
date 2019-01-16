#!/bin/bash

ssh anrock@undo.life "tail -n +1 -f /var/lib/jenkins/deploy/logs/$1.log"
