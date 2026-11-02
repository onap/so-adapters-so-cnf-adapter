# SO CNF Adapter

## Outbound MultiCloud Authentication

The adapter supports optional preemptive HTTP Basic authentication for its
outbound MultiCloud requests. Both MultiCloud client implementations apply it
to all operations, including JSON requests, multipart uploads, status and
health-check polling, and deletes.

| Spring property | Environment variable | Default |
| --- | --- | --- |
| `multicloud.auth.username` | `MULTICLOUD_AUTH_USERNAME` | Empty |
| `multicloud.auth.password` | `MULTICLOUD_AUTH_PASSWORD` | Empty |

Both credentials absent or empty retains anonymous access to existing
MultiCloud deployments. Configure both to enable authentication. A missing
partner, whitespace-only value, or username containing a colon prevents startup
with an error naming the configuration, without including credential values.
Passwords may contain colons, special characters, and significant whitespace;
credentials are encoded using UTF-8 and are not trimmed.

Supply credentials at runtime, for example by mapping both environment variables
to separate keys in a Kubernetes Secret using `env[].valueFrom.secretKeyRef`.
Use the deployment's secret-management mechanism to provision that Secret; do
not put credentials in the image, source control, ConfigMaps, or command-line
arguments. Restart the adapter after changing a Secret-backed environment value.

Basic authentication encodes credentials but does not encrypt them. Protect
the connection with HTTPS (`multicloud.endpoint`) or an appropriately secured
service-mesh TLS/mTLS transport, and configure certificate trust and client
certificates through the existing runtime transport configuration. Basic auth
does not enable TLS/mTLS, replace certificate validation, or require disabling
it. Avoid logging Authorization headers or enabling HTTP wire logging that
exposes them.

This setting only authenticates calls to MultiCloud. It does not configure
inbound adapter authentication, SO callbacks, AAI credentials, or shared
`RestTemplate`/`RestTemplateBuilder` authentication. Existing HTTP instrumentation
and each client's response/error handling remain in place. Configure matching
credentials on the MultiCloud service before enabling this option; older
anonymous deployments require no configuration changes.