# SparkTransformer
StreamSets Pipeline


Commands to run Docker container and access the container terminal: 
'''bash
docker run -d -p 18630:18630 -v /tmp:/tmp --name streamsets-spark1 otp/streamsets_spark:1.0

danielsef$ docker exec -ti streamsets-spark1 /bin/bash
'''
