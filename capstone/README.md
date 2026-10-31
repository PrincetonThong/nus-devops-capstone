## NUS DevOps and Cloud with AI program

### Capstone Assignment: FinSecure Pay

#### Task 1: App development

To build and test the Spring Boot app, run
```powershell
.\mvnw.cmd clean test
```

To run the app, use
```powershell
.\mvnw.cmd spring-boot:run
```

#### Task 2: Containerization

The finsecurepay app was containerized using Dockerfile with Maven 3.10.0 and Eclipse Temurin 17.
The image was tagged `v1.0.0` for first release and pushed to docker hub `tjysats/nus-devops-capstone-finsecurepay`
