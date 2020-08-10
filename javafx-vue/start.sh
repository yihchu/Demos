#!/bin/sh

cd ./front && npm run build

cd ../server

mvn clean package -Pprd

cd target

java -jar server-2.0.4.RELEASE.jar



# swagger-ui: http://localhost:28080/swagger-ui.html
