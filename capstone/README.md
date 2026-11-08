# Capstone Assignment: FinSecure Pay
### Done by: Thong Jing Yuan

## Task 1: Application Development and Version Control

Finsecurepay application is a Spring Boot Rest API that validates transactions based on fields like transaction ID, amount, currency, source/destination accounts. It returns appropriate HTTP responses for accepted and rejected transactions.

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

Finsecurepay application was containerized using Dockerfile with Maven 3.10.0 and Eclipse Temurin 17.
The image was tagged `v1.0.0` for first release and pushed to docker hub `tjysats/nus-devops-capstone-finsecurepay`

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

Finsecurepay application was deployed in local setup using Docker-desktop Kubernetes, following which it was deployed to AWS EKS in ap-southeast-1 region.
In this deployment, 2 app replicas were created along with a LoadBalancer service for external access, and a ConfigMap for managing app settings. Deleting a pod resulted in auto creation, thereby restoring the replica count without manual intervention.

### Steps:

1. Create `k8s/finsecurepay.yaml` containing the following Kubernetes resources:
   - Namespace `finsecurepay`
   - ConfigMap for application configuration
   - Deployment with 2 replicas
   - Readiness and liveness probes
   - LoadBalancer Service for external access

2. First apply the manifest to Docker Desktop Kubernetes to verify that both application pods start successfully.

```powershell
kubectl apply -f k8s/finsecurepay.yaml
kubectl rollout status deployment/finsecurepay -n finsecurepay
kubectl get all -n finsecurepay
```

![local Kubernetes deployment](./task3_screenshots/local_setup_success.png)

3. Create an Amazon EKS cluster in the Singapore region with 2 managed worker nodes.

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

## Task 4: CI/CD Pipeline Automation

Jenkins connects to GitHub repo and runs pipelines from `Jenkinsfile`. The pipeline builds and tests the app, packages the app, builds and pushes a versioned Docker image, deploys the app to AWS EKS, and verifies the rollout.

### Steps:

1. Start Jenkins and log in at `http://localhost:8091`. Here Jenkins runs locally in a Docker container, with Docker CLI, AWS CLI, `eksctl` and `kubectl` installed inside, so that the pipeline is able to build images and deploy to EKS.

![jenkins_homepage](./task4_screenshots/jenkins_homepage.png)

2. Add the credentials needed by the pipeline under Manage Jenkins > Credentials > System > Global:
   - `dockerhub-tjysats` (username with password), used to push images to Docker Hub
   - `zscaler-root-ca` (secret file), used during Docker build

Since this laptop is behind Zscaler, Maven inside `docker build` is not able to download dependencies unless it trusts the Zscaler root CA. Instead of committing the certificate into the repo, it is stored as a Jenkins secret file and passed into the build as a BuildKit secret (`--secret id=zscaler_root_ca`), so it does not end up in Git history or in the final image.

![adding_zscaler_as_secret](./task4_screenshots/adding_zscaler_as_secret.png)

Once created, both credentials show up as follows, with the Docker Hub password masked:

![jenkins_store](./task4_screenshots/jenkins_store.png)

3. Create a Jenkins Pipeline job and configure it to use the Git repository and `capstone/Jenkinsfile`.

![Jenkins pipeline](./task4_screenshots/create_new_jenkins_pipeline.png)

Under Pipeline, select "Pipeline script from SCM", set the repository URL, branch `*/master`, and Script Path `capstone/Jenkinsfile` (since the app sits in the `capstone` subfolder of the repo):

![link_to_github_jenkinsfile](./task4_screenshots/link_to_github_jenkinsfile.png)

4. The first version of `Jenkinsfile` reused the Task 1 commands (`.\mvnw.cmd`) with `bat`. However, build #2 failed at the Build stage with "Batch scripts can only be run on Windows nodes". This is because the Jenkins container runs on Linux, even though the laptop is Windows.

![jenkins_pipeline_bat_commands_failure](./task4_screenshots/jenkins_pipeline_bat_commands_failure.png)

After changing `bat` to `sh` (and `.\mvnw.cmd` to `sh ./mvnw`), build #3 passed and pushed image `v1.0.3` to Docker Hub:

![initial_pipeline_success](./task4_screenshots/initial_pipeline_success.png)

5. Define the following stages in `Jenkinsfile`:
   - Checkout
   - Build
   - Unit Tests
   - Package
   - Build Docker Image
   - Push to Dockerhub
   - Verify AWS EKS
   - Deploy to EKS
   - Verify Rollout

The pipeline creates the EKS cluster when it does not exist and reuses it when it is available.
- As this AWS account is on the Free Tier plan. Hence, the node type should be set as `t3.small`.

![EKS verification](./task4_screenshots/create_cluster_if_not_exist.png)

Once the cluster is ready, `kubectl get nodes` shows 2 nodes in Ready state. As the deployment also did not exist yet, the pipeline applies the Task 3 YAML before setting the image to `v1.0.5`:

![eks_cluster_ready](./task4_screenshots/eks_cluster_ready.png)

The rollout completes with 2 pods running, and build #5 finishes successfully with image `v1.0.5`:

![successful_rollout](./task4_screenshots/successful_rollout.png)

6. Verify the deployment from the laptop using the following commands:

```powershell
aws eks update-kubeconfig --region ap-southeast-1 --name finsecurepay-cluster
kubectl get nodes
kubectl get all -n finsecurepay -o wide
kubectl get service finsecurepay-service -n finsecurepay -o jsonpath="{.status.loadBalancer.ingress[0].hostname}"
```

Here, the replicaset with `v1.0.5` has 2 pods, while the replicaset with `v1.0.0` (the image in the Task 3 YAML) is scaled down to 0:

![info_on_deployed_cluster_of_v1.0.5](./task4_screenshots/info_on_deployed_cluster_of_v1.0.5.png)

Using the LoadBalancer hostname, the `/health` endpoint returns `"version": "local"`, and a valid transaction is accepted:

![health_status_of_v1.0.5](./task4_screenshots/health_status_of_v1.0.5.png)

![validate_transaction_for_v1.0.5](./task4_screenshots/validate_transaction_for_v1.0.5.png)

7. Add SCM polling to `Jenkinsfile`:

```groovy
triggers {
    pollSCM('H/5 * * * *')
}
```

Since Jenkins runs on localhost, GitHub is not able to reach it through a webhook. Hence, Jenkins polls GitHub every 5 minutes instead.

Jenkins only registers the trigger after the job runs once with the updated `Jenkinsfile` (build #6). This time, the pipeline detects the existing cluster and deployment, so it skips cluster creation and only updates the image to `v1.0.6`:

![update_jenkins_job_part1](./task4_screenshots/update_jenkins_job_part1.png)

![update_jenkins_job_part2](./task4_screenshots/update_jenkins_job_part2.png)

8. Push a source code change. Here, the `/health` version in `ValidationController.java` is changed from `"local"` to `"cicd-ver2"`. Jenkins detects the change and starts a new build (#7) automatically, shown as "Started by an SCM change".

![SCM trigger](./task4_screenshots/jenkins_build_trigged_by_scm.png)

![SCM polling log](./task4_screenshots/jenkins_build_trigged_by_scm_polling_log.png)

Build #7 builds and pushes image `v1.0.7`, deploys it to EKS, and completes the rollout successfully.

![successful rollout](./task4_screenshots/successful_v1.0.7.png)

The health endpoint confirms that the new application version is running.

![updated application version](./task4_screenshots/upgrade_to_v1.0.7_correct_version.png)

9. Introduce a failing JUnit test and verify that the pipeline stops at the Unit Tests stage. The package, image, push, and deployment stages remain skipped, while EKS continues to use the previous image.

![failed test](./task4_screenshots/failed_junit_test_prevent_deployment.png)

![previous image remains deployed](./task4_screenshots/still_use_previous_image_if_failed_junit.png)

10. Verify that an unsuccessful rollout automatically returns the Deployment to the previous working revision.

To simulate a bad release, the `Dockerfile` `ENTRYPOINT` is pointed to a jar file that does not exist:

```docker
ENTRYPOINT ["java", "-jar", "/app/unknown.jar"]
```

The image still builds and pushes successfully (build #9), but the new pod crashes on startup and goes into `CrashLoopBackOff`. As the pod never becomes ready, `kubectl rollout status` times out after 300s, and the `post { failure }` block in the `Verify Rollout` stage runs `kubectl rollout undo` to return to `v1.0.7`:

Since the Task 3 YAML uses `maxUnavailable: 0`, the old `v1.0.7` pods keep serving traffic while the new pod is failing. The build is still marked as FAILURE, so the failed release remains visible in Jenkins.

![automatic rollback](./task4_screenshots/auto_rollback.png)

Lastly, revert the `Dockerfile` and push, so that the next build (#10) deploys a working image again.

11. View the rollout history and manually return the Deployment to the known-good `v1.0.5` revision using:

```powershell
kubectl rollout history deployment/finsecurepay -n finsecurepay
kubectl rollout undo deployment/finsecurepay -n finsecurepay --to-revision=2
kubectl rollout status deployment/finsecurepay -n finsecurepay --timeout=300s
```

![manual rollback](./task4_screenshots/manual_rollback.png)

## Task 5: DevSecOps and Security Integration

The Jenkins pipeline adds SpotBugs code-quality analysis and Trivy dependency and container-image scans. The pipeline archives the generated XML and JSON reports after each build.

SpotBugs is used instead of SonarQube because it runs as a Maven plugin inside the existing Jenkins container and does not require a separate SonarQube server or database on this 8 GB laptop. It still detects Java bug patterns such as `EI_EXPOSE_REP`.

### Steps:

1. Install Trivy `0.76.2` in the persistent Jenkins home volume and verify the installation:

```powershell
docker exec --user root jenkins sh -lc 'mkdir -p /var/jenkins_home/bin && curl -fsSL https://github.com/aquasecurity/trivy/releases/download/v0.76.2/trivy_0.76.2_Linux-64bit.tar.gz | tar -xz -C /var/jenkins_home/bin trivy && chown jenkins:jenkins /var/jenkins_home/bin/trivy && chmod 755 /var/jenkins_home/bin/trivy'
docker exec jenkins /var/jenkins_home/bin/trivy --version
```

`Jenkinsfile` sets `TRIVY_PATH` to `/var/jenkins_home/bin/trivy`. The binary remains available when the Jenkins container restarts because `/var/jenkins_home` uses the `jenkins_home` Docker volume.

2. Run SpotBugs during the Code Quality stage. Build #13 fails with `EI_EXPOSE_REP` and `EI_EXPOSE_REP2` findings in `ValidationResult`, so the later stages remain skipped.

![SpotBugs pipeline failure](./task5_screenshots/pipeline_view_of_spotbugs_error.png)

![SpotBugs findings](./task5_screenshots/spotbugs_error_in_jenkins_pipeline.png)

The generated SpotBugs XML report records both findings.

![SpotBugs XML report](./task5_screenshots/spotbugs_xml_report.png)

3. Copy the `reasons` list into an immutable list in the `ValidationResult` constructor. The original record stores and returns the caller's mutable list, allowing external code to change a validation result after it is created.

```java
public ValidationResult {
    reasons = List.copyOf(reasons);
}
```

SpotBugs then reports 0 bugs and "Code Quality" stage succeeds.

![successful code-quality check](./task5_screenshots/code_quality_ok.png)

4. Run Trivy against Maven dependencies. Add the log4j controlled vulnerable dependency to demonstrate security gate:

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.14.1</version>
</dependency>
```

Build #15 reports 2 critical and 1 high findings. Trivy writes the complete JSON report, and the following command returns exit code 1 when it contains a High or Critical vulnerability:

```groovy
sh '"$TRIVY_PATH" convert --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format table target/trivy-dependency-report.json'
```

This fails the Dependency Scan stage and prevents the image build, push and deployment stages from running. The red "Verify AWS EKS" icon in Build #15 pipeline view is a display quirk as console log confirms that the stage is skipped due to the earlier failure.

![blocked vulnerable dependency](./task5_screenshots/dependency_scan_critical_findings_log4j.png)

5. Remove the unused vulnerable dependency. Build #18 includes `--scanners vuln` when converting the report, so dependency scan displays a summary table with 0 vulnerabilities.

![clean dependency scan](./task5_screenshots/dependency_scan_no_errors.png)

6. Scan the built container image with Trivy. Build #18 reports 0 High or Critical vulnerabilities, so the pipeline continues.

![clean container-image scan](./task5_screenshots/container_scan_no_errors.png)

7. Store the Docker Hub password and Zscaler certificate in Jenkins Credentials.
The laptop's AWS CLI directory is mounted from `~/.aws` to `/var/jenkins_home/.aws` inside the Jenkins container. This lets Jenkins use the active AWS login session without placing AWS credentials in the repository or `Jenkinsfile`.

![Jenkins credential store](./task4_screenshots/jenkins_store.png)

8. Archive the SpotBugs, dependency-scan and image-scan reports. The remediated pipeline completes successfully through deployment and rollout.

![generated security reports](./task5_screenshots/generated_artifacts.png)

![successful DevSecOps pipeline](./task5_screenshots/final_pipeline.png)
