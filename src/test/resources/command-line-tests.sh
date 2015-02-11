curl -u admin:admin -X GET -H "Accept:plication/json" http://localhost:9090/users/

curl -u admin:admin POST -d '{"email":"tim@omny.link","firstName":"Tim"}' -H "Accept: application/json" -H "Content-Type: application/json" http://localhost:9090/users/

curl -u admin:admin -X PUT -d '{"email":"tim@omny.link","firstName":"Tim"}' -H "Accept: application/json" -H "Content-Type: application/json" http://localhost:9090/users/tim

