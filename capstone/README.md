# Capstone Assignment: FinSecure Pay
### Done by: Thong Jing Yuan

## Task 1: Application Development and Version Control

### Steps:

1. Create a Spring Boot application using Java 17 and Maven. The application provides a `/health` endpoint and a `/validate` endpoint for validating payment transactions.

2. Add validation rules for the transaction ID, amount, currency, source account, and destination account. Test both accepted and rejected transactions successfully.

![successful transaction](./task1_screenshots/successful_transaction.png)

![invalid transaction](./task1_screenshots/invalid_transaction.png)

3. Add JUnit 5 tests for the validation service and controller. Build and test the application using:

```powershell
.\mvnw.cmd clean test
```

The tests and Maven build complete successfully.

![JUnit tests](./task1_screenshots/junit_tests_pass.png)

![build success](./task1_screenshots/build_success.png)

4. Run the application locally using:

```powershell
.\mvnw.cmd spring-boot:run
```

The `/health` endpoint returns a successful response.

![health endpoint](./task1_screenshots/health_status_success.png)

## Task 2: Containerisation

### Steps:

1. Create a multi-stage Dockerfile. Maven and Eclipse Temurin 17 build the application, while Eclipse Temurin 17 JRE runs the final container.

2. Build and tag the first application image as `v1.0.0` using:

```powershell
docker build -t tjysats/nus-devops-capstone-finsecurepay:v1.0.0 .
```

![Docker build](./task2_screenshots/docker_build_part5.png)

3. Run the image locally and map container port `8080` to the host:

```powershell
docker run -d --name finsecurepay -p 8080:8080 tjysats/nus-devops-capstone-finsecurepay:v1.0.0
```

![running container](./task2_screenshots/start_docker_container.png)

4. Test the health and transaction validation endpoints through the running container.

![container health](./task2_screenshots/docker_container_health.png)

![container validation](./task2_screenshots/docker_container_validation_success.png)

5. Push the versioned image to Docker Hub using:

```powershell
docker push tjysats/nus-devops-capstone-finsecurepay:v1.0.0
```

![Docker Hub registry](./task2_screenshots/docker_hub_container_registry.png)

## Task 3: Kubernetes and Cloud Deployment

### Steps:

1. Create `k8s/finsecurepay.yaml` containing the following Kubernetes resources:
   - Namespace `finsecurepay`
   - ConfigMap for application configuration
   - Deployment with two replicas
   - Readiness and liveness probes
   - LoadBalancer Service for external access

2. First apply the manifest to Docker Desktop Kubernetes to verify that both application pods start successfully.

```powershell
kubectl apply -f k8s/finsecurepay.yaml
kubectl rollout status deployment/finsecurepay -n finsecurepay
kubectl get all -n finsecurepay
```

![local Kubernetes deployment](./task3_screenshots/local_setup_success.png)

3. Create an Amazon EKS cluster in the Singapore region with two managed worker nodes.

![Amazon EKS cluster](./task3_screenshots/aws_console_cluster.png)

4. Update the kubeconfig for the EKS cluster, then apply the same Kubernetes manifest. Both replicas complete their rollout and the LoadBalancer Service receives an external address.

```powershell
aws eks update-kubeconfig --region ap-southeast-1 --name finsecurepay-cluster
kubectl apply -f k8s/finsecurepay.yaml
kubectl rollout status deployment/finsecurepay -n finsecurepay --timeout=300s
kubectl get all -n finsecurepay
```

![EKS rollout](./task3_screenshots/eks_rollout.png)

5. Verify external access through the LoadBalancer. The application health, readiness, and liveness endpoints return successful responses.

![EKS health endpoint](./task3_screenshots/eks_health.png)

![EKS readiness probe](./task3_screenshots/eks_readiness.png)

![EKS liveness probe](./task3_screenshots/eks_liveness.png)

6. Test both successful and rejected payment transactions through the externally accessible application.

![valid EKS transaction](./task3_screenshots/eks_valid_transaction.png)

![invalid EKS transaction](./task3_screenshots/eks_invalid_transaction.png)

7. Delete one application pod and verify that the Deployment automatically creates a replacement pod while maintaining two replicas.

![automatic pod recreation](./task3_screenshots/eks_delete_pod.png)
