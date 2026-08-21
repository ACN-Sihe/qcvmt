# Backend CI/CD

`.github/workflows/backend-cicd.yml` tests and builds the application, publishes a SHA-tagged image to Alibaba Cloud Container Registry (ACR), then updates ACK using `kubectl`. It has no dependency on a visual pipeline.

Create these GitHub Actions repository secrets:

- `ACR_REGISTRY`, `ACR_USERNAME`, `ACR_PASSWORD`
- `ACK_KUBECONFIG_B64` - base64-encoded kubeconfig with namespace create, deployment, service, configmap, and secret permissions
- `MYSQL_HOST`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `N4_HOST`, `N4_SERVICE`, `N4_USER`, `N4_PASSWORD`
- `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`

Create these GitHub Actions repository variables:

- `ACR_NAMESPACE` and optional `ACR_REPOSITORY` (defaults to `qcvmt-backend`)
- `ACK_NAMESPACE` (defaults to `qcvmt`)
- `CORS_ORIGINS`, `KEYCLOAK_URL`, and optional `KEYCLOAK_REALM`
- optional `APP_PROFILE`, `MYSQL_PORT`, and `N4_PORT`

For a Windows PowerShell operator, create the ACK secret with:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\path\to\kubeconfig'))
```

Create the ACR repository before the first deployment. The ACK endpoint and the final frontend origin are needed to populate `ACK_KUBECONFIG_B64` and `CORS_ORIGINS`; no workflow code change is required when they are available.