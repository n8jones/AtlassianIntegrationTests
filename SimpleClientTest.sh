#!/bin/bash
#Get Token Key
curl -i -u test-app:test http://127.0.0.1:8095/crowd/rest/usermanagement/1/config/cookie --header 'Content-Type: application/json' --header 'Accept: application/json';
#Get Token
curl -i -u test-app:test\
	--data '{"password": "test", "username": "test", "validation-factors" : {"validationFactors" : [{"name" : "remote_address","value" : "127.0.0.1"}]}}'\
	http://127.0.0.1:8095/crowd/rest/usermanagement/1/session --header 'Content-Type: application/json' --header 'Accept: application/json';
