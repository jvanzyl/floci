# Service Quotas

**Protocol:** JSON 1.1
**Endpoint:** `http://localhost:4566/`
**Target prefix:** `X-Amz-Target: ServiceQuotasV20190624.*`

Floci exposes the Service Quotas read APIs needed to inspect the supported EC2
regional Elastic IP and NAT gateway quotas. The returned values are the
modeled AWS defaults, and EC2 resource creation enforces the corresponding
regional and Availability Zone limits.

## Supported Actions

<!-- floci:actions:start -->
| Action | Description |
| --- | --- |
| `GetServiceQuota` | Returns one supported quota |
| `GetAWSDefaultServiceQuota` | Returns one supported default quota |
| `ListServiceQuotas` | Lists supported quotas for a service |
| `ListAWSDefaultServiceQuotas` | Lists supported default quotas for a service |
| `ListServices` | Lists services with supported quotas |
<!-- floci:actions:end -->

## Supported EC2 Quotas

| Quota code | Name | Default |
| --- | --- | ---: |
| `L-0263D0A3` | Elastic IP addresses per Region | 5 |
| `L-5F53652F` | Elastic IP addresses per public NAT gateway | 2 |
| `L-FE5A380F` | NAT gateways per Availability Zone | 5 |
