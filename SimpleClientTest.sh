#!/bin/bash
#Get Token Key
curl -i -u crowd-auth-webapp:password http://localhost:8095/crowd/rest/usermanagement/1/config/cookie --header 'Content-Type: application/json' --header 'Accept: application/json'
#Get Token
curl -i -u crowd-auth-webapp:password --data '{"password": "test", "username": "test"}' http://localhost:8095/crowd/rest/usermanagement/1/session --header 'Content-Type: application/json' --header 'Accept: application/json'
