# webapp
Tianqi Zhang 001056916

assignment 6 for CSYE 6225

Now this webapp springboot application uses external application.properties

To use this application, maven build it and get the .jar, use the following command to run this application:
```
sudo java -jar webapp-0.0.1-SNAPSHOT.jar --server.port=${port} --spring.config.location=${location}
```

Attention, in order to make sure every api could work, you should have appropriate IAM role with policies attached to the EC2 instance where the application is on.

Then just start the app and test the apis in postman!

