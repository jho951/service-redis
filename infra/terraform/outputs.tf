output "service_name" {
  description = "Logical service name."
  value       = var.service_name
}

output "redis_primary_endpoint" {
  description = "Primary Redis endpoint."
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "redis_reader_endpoint" {
  description = "Reader Redis endpoint."
  value       = aws_elasticache_replication_group.redis.reader_endpoint_address
}

output "redis_port" {
  description = "Redis port."
  value       = var.redis_port
}

output "redis_security_group_id" {
  description = "Redis security group ID."
  value       = aws_security_group.redis.id
}
