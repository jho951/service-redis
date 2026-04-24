output "service_name" {
  description = "Logical service name."
  value       = var.service_name
}

output "instance_id" {
  description = "EC2 instance ID."
  value       = aws_instance.service.id
}

output "public_ip" {
  description = "Public IP of the EC2 instance when associate_public_ip is true."
  value       = var.associate_public_ip ? aws_instance.service.public_ip : null
}

output "private_ip" {
  description = "Private IP of the EC2 instance."
  value       = aws_instance.service.private_ip
}

output "private_dns" {
  description = "EC2 private DNS name."
  value       = aws_instance.service.private_dns
}

output "private_dns_name" {
  description = "Optional Route53 record name pointing to the instance."
  value       = var.private_dns_name != "" ? var.private_dns_name : null
}

output "service_url" {
  description = "Preferred Redis URL."
  value       = "redis://${var.private_dns_name != "" ? var.private_dns_name : aws_instance.service.private_ip}:${var.app_port}"
}

output "private_service_url" {
  description = "Private Redis URL for east-west traffic."
  value       = "redis://${var.private_dns_name != "" ? var.private_dns_name : aws_instance.service.private_ip}:${var.app_port}"
}

output "security_group_id" {
  description = "Security group ID attached to the EC2 host."
  value       = aws_security_group.service.id
}

output "vpc_id" {
  description = "VPC ID containing the EC2 host."
  value       = local.vpc_id
}

output "bootstrap_secret_arn" {
  description = "Secrets Manager ARN holding bootstrap secrets and secret env values."
  value       = aws_secretsmanager_secret.bootstrap.arn
}

output "app_secret_arn" {
  description = "Compatibility alias for the bootstrap secret ARN."
  value       = aws_secretsmanager_secret.bootstrap.arn
}

output "redis_primary_endpoint" {
  description = "Redis primary endpoint."
  value       = var.private_dns_name != "" ? var.private_dns_name : aws_instance.service.private_ip
}

output "redis_reader_endpoint" {
  description = "Single-node EC2 deployment uses the same endpoint for reads."
  value       = var.private_dns_name != "" ? var.private_dns_name : aws_instance.service.private_ip
}

output "redis_port" {
  description = "Redis port."
  value       = var.app_port
}

output "redis_security_group_id" {
  description = "Redis security group ID."
  value       = aws_security_group.service.id
}
