# redis-server Terraform

Redis is stateful, so this service does not use ECS Blue/Green. This stack provisions central Redis with ElastiCache instead.

It creates:

- Private VPC subnets for Redis
- Security group allowing approved service CIDRs
- ElastiCache Redis replication group
- Multi-AZ automatic failover when `redis_num_cache_clusters > 1`
- Transit encryption, at-rest encryption, Redis AUTH
- Snapshot retention and slow-log delivery to CloudWatch Logs

## Apply

```bash
cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
cd infra/terraform
terraform init
terraform plan
terraform apply
```

Use the primary endpoint in application service configuration:

```bash
terraform output redis_primary_endpoint
terraform output redis_port
```

## Rollback / Recovery

- Config rollback: revert Terraform changes and run `terraform apply`.
- Data rollback: restore from an ElastiCache snapshot into a new replication group, validate clients, then cut service configuration to the restored endpoint.
- Application Blue/Green deployments must treat Redis as an external dependency and keep key formats backward-compatible during traffic shifts.
