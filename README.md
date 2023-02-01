CSYE6225 CLoud Computing 

Web Application Development
Create a web application using a technology stack that meets Cloud-Native Web Application Requirements. Start implementing APIs for the web application. Features of the web application will be split among various applications. For this assignment, we will focus on the backend API (no UI) service. Additional features of the web application will be implemented in future assignments. We will also build the infrastructure on the cloud to host the application. This assignment will focus on the user management aspect of the application. You will implement RESTful APIs based on user stories you will find below.

**API Documentation**

Assignment #01 - https://app.swaggerhub.com/apis-docs/csye6225-webapp/cloud-native-webapp/spring2023-a1#/ (Links to an external site.) 


**Spring Boot Application.**
This Standalone Spring Boot Project is a simple health check REST API.


**Prerequisite and Technology:**
JAVA 1.8
Maven
Spring boot setup
git and github
Postman
IntelliJ IDE


Select the project on IDE and Run.
Once the application is started you can access following APIs through postman.
1. GET: http://localhost:8080/healthz
2. POST: http://localhost:8080/v1/user
3. GET: http://localhost:8080/v1/user/{userId}
4. PUT: http://localhost:8080/v1/user/{userId}