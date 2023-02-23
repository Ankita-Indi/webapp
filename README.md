CSYE6225 Network Structures and Cloud Computing

Web Application Development
Create a web application using a technology stack that meets Cloud-Native Web Application Requirements. Start implementing APIs for the web application. Features of the web application will be split among various applications. For this assignment, we will focus on the backend API (no UI) service. Additional features of the web application will be implemented in future assignments. We will also build the infrastructure on the cloud to host the application. This assignment will focus on the user management aspect of the application. You will implement RESTful APIs based on user stories you will find below.

**API Documentation**

Assignment #01 - https://app.swaggerhub.com/apis-docs/csye6225-webapp/cloud-native-webapp/spring2023-a1 Links to an external site.(Links to an external site.)

Assignment #02 - https://app.swaggerhub.com/apis-docs/csye6225-webapp/cloud-native-webapp/spring2023-a2#/(Links to an external site.)


**Spring Boot Application.**
This Standalone Spring Boot Project is a simple health check REST API.


**Prerequisite and Technology:**
JAVA 1.8
Maven
Spring boot setup
git and github
MYSQL
IntelliJ IDE


**Assignment01:**
Developed rest apis to create users.
Also added a test case which is invoked before merging with the organisation main branch.

Once the project is running you will be able to use following endpoints:

GET: localhost:8080/healthz

POST: localhost:8080/v1/user

PUT: localhost:8080/v1/user/{userid}

GET: localhost:8080/v1/user/{userid}

**Assignment02**

GET: localhost:8080/v1/product/{productId}

POST: localhost:8080/v1/product

PUT: localhost:8080/v1/product/{productId}

PATCH: localhost:8080/v1/product/{productId}

DELETE:  localhost:8080/v1/product/{productId} 



