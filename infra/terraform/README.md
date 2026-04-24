# redis-service Terraform

This stack provisions a direct EC2 deployment baseline for `redis-service`.

What Terraform creates:

- One EC2 host running the checked-out repository with Docker Compose
- One security group for Redis and optional exporter/SSH ports
- One Secrets Manager secret containing bootstrap-only secret values
- Optional dedicated VPC/public subnets when `create_vpc = true`
- Optional Route53 A record when `private_dns_zone_id` and `private_dns_name` are set

Bootstrap flow:

1. Terraform creates the EC2 instance.
2. User data installs Docker, Git, `jq`, and the Docker Compose plugin.
3. The host clones `repository_url` at `repository_ref`.
4. Terraform writes `env.docker.prod` from `app_env` + `app_secret_env`.
5. The host runs `deploy_command` from the cloned repository.

## Apply

```bash
cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
cd infra/terraform
terraform init
terraform plan
terraform apply
```

## Shared VPC Recommendation

For a real MSA EC2 topology, place the services in the same VPC or in connected VPCs. The simplest path is:

- set `create_vpc = false`
- pass `existing_vpc_id`, `existing_subnet_id`, `existing_vpc_cidr`
- create private Route53 records such as `redis.internal`

If each service stack creates its own standalone VPC, service-to-service Redis access and monitoring scrape traffic will not work until you add peering/transit routing and DNS resolution.

## Bootstrap Notes

- The default deploy command starts Redis and the Redis exporter profile together so monitoring-service can scrape `9121`.
- Use security groups to restrict `6379` and `9121`; do not rely on public exposure.
- This stack replaces the previous ElastiCache-only Terraform with a directly managed EC2 Redis host.

## Outputs

- `service_url` and `private_service_url`
- `redis_primary_endpoint`, `redis_reader_endpoint`, `redis_port`
- `public_ip`, `private_ip`, `private_dns`
- `redis_security_group_id`
- `bootstrap_secret_arn`

## Risk Notes

- `app_secret_env`, clone tokens, and registry passwords are still stored in Terraform state because Terraform provisions the bootstrap secret.
- This is a single-node EC2 Redis topology, not a managed ElastiCache replication group.
