variable "aws_region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Project name used in tags."
  type        = string
  default     = "msa"
}

variable "environment" {
  description = "Environment name used in resource names."
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Logical service name from service-contract."
  type        = string
  default     = "redis-server"
}

variable "tags" {
  description = "Additional tags to apply to all taggable resources."
  type        = map(string)
  default     = {}
}

variable "vpc_cidr" {
  description = "CIDR block for the Redis VPC."
  type        = string
  default     = "10.25.0.0/16"
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDRs for ElastiCache."
  type        = list(string)
  default     = ["10.25.11.0/24", "10.25.12.0/24"]
}

variable "redis_client_cidrs" {
  description = "CIDRs allowed to connect to Redis."
  type        = list(string)
  default     = ["10.20.0.0/12"]
}

variable "redis_engine_version" {
  description = "Redis engine version."
  type        = string
  default     = "7.1"
}

variable "redis_node_type" {
  description = "ElastiCache Redis node type."
  type        = string
  default     = "cache.t4g.micro"
}

variable "redis_port" {
  description = "Redis port."
  type        = number
  default     = 6379
}

variable "redis_parameter_group_name" {
  description = "Redis parameter group name."
  type        = string
  default     = "default.redis7"
}

variable "redis_num_cache_clusters" {
  description = "Number of cache nodes. Use at least 2 for automatic failover."
  type        = number
  default     = 2
}

variable "redis_auth_token" {
  description = "Redis AUTH token. Required because transit encryption is enabled."
  type        = string
  sensitive   = true
}

variable "snapshot_retention_days" {
  description = "Redis snapshot retention in days."
  type        = number
  default     = 7
}

variable "snapshot_window" {
  description = "Daily snapshot window."
  type        = string
  default     = "18:00-19:00"
}

variable "maintenance_window" {
  description = "Weekly maintenance window."
  type        = string
  default     = "sun:19:00-sun:20:00"
}

variable "apply_immediately" {
  description = "Apply changes immediately instead of during maintenance window."
  type        = bool
  default     = false
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days."
  type        = number
  default     = 30
}
