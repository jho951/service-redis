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
  default     = "redis-service"
}

variable "service_runtime_name" {
  description = "Runtime stack name and EC2 host bootstrap directory."
  type        = string
  default     = "redis-server"
}

variable "service_role" {
  description = "Terraform role classification for this service."
  type        = string
  default     = "cache-service"
}

variable "tags" {
  description = "Additional tags to apply to all taggable resources."
  type        = map(string)
  default     = {}
}

variable "create_vpc" {
  description = "Create a dedicated VPC and public subnets for this service. Set false to place the EC2 host in an existing shared VPC/subnet."
  type        = bool
  default     = true
}

variable "vpc_cidr" {
  description = "CIDR block for the service VPC when create_vpc is true."
  type        = string
  default     = "10.25.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs when create_vpc is true."
  type        = list(string)
  default     = ["10.25.1.0/24", "10.25.2.0/24"]
}

variable "existing_vpc_id" {
  description = "Existing shared VPC ID when create_vpc is false."
  type        = string
  default     = ""

  validation {
    condition     = var.create_vpc || var.existing_vpc_id != ""
    error_message = "existing_vpc_id is required when create_vpc is false."
  }
}

variable "existing_subnet_id" {
  description = "Existing subnet ID for the EC2 host when create_vpc is false."
  type        = string
  default     = ""

  validation {
    condition     = var.create_vpc || var.existing_subnet_id != ""
    error_message = "existing_subnet_id is required when create_vpc is false."
  }
}

variable "existing_vpc_cidr" {
  description = "CIDR block for the existing shared VPC when create_vpc is false."
  type        = string
  default     = ""

  validation {
    condition     = var.create_vpc || var.existing_vpc_cidr != ""
    error_message = "existing_vpc_cidr is required when create_vpc is false."
  }
}

variable "app_port" {
  description = "Primary Redis port exposed by the service."
  type        = number
  default     = 6379
}

variable "health_check_path" {
  description = "Compatibility field for shared service stacks. Redis uses TCP health checks inside Docker Compose."
  type        = string
  default     = "/"
}

variable "app_ingress_cidrs" {
  description = "CIDRs allowed to reach Redis. Leave empty to use the VPC CIDR."
  type        = list(string)
  default     = []
}

variable "extra_ingress_rules" {
  description = "Additional TCP ingress rules such as the Redis exporter port."
  type = list(object({
    description = string
    from_port   = number
    to_port     = number
    cidr_blocks = list(string)
  }))
  default = []
}

variable "ssh_ingress_cidrs" {
  description = "CIDRs allowed to SSH into the EC2 host. Prefer leaving this empty and using SSM Session Manager."
  type        = list(string)
  default     = []
}

variable "associate_public_ip" {
  description = "Associate a public IP to the EC2 instance."
  type        = bool
  default     = true
}

variable "ec2_key_name" {
  description = "Optional existing EC2 key pair name for SSH access."
  type        = string
  default     = ""
}

variable "ec2_ami_id" {
  description = "Optional AMI override. When empty, the latest Amazon Linux 2023 x86_64 AMI is used."
  type        = string
  default     = ""
}

variable "ec2_instance_type" {
  description = "EC2 instance type for the service host."
  type        = string
  default     = "t3.small"
}

variable "ec2_root_volume_size" {
  description = "EC2 root volume size in GiB."
  type        = number
  default     = 40
}

variable "docker_compose_version" {
  description = "Docker Compose plugin version installed by user data when it is missing."
  type        = string
  default     = "2.29.7"
}

variable "repository_url" {
  description = "Git repository URL cloned by EC2 bootstrap."
  type        = string
  default     = "https://github.com/jho951/redis-server.git"
}

variable "repository_ref" {
  description = "Git branch, tag, or commit checked out by EC2 bootstrap."
  type        = string
  default     = "main"
}

variable "repository_clone_token" {
  description = "Optional Git token used for private repository clone over HTTPS."
  type        = string
  sensitive   = true
  default     = ""
}

variable "deploy_env_file" {
  description = "Environment file path, relative to the cloned repository root."
  type        = string
  default     = "env.docker.prod"
}

variable "deploy_command" {
  description = "Command executed on the EC2 host after bootstrap writes the prod env file."
  type        = string
  default     = "./scripts/run.docker.sh up-monitoring prod"
}

variable "private_dns_zone_id" {
  description = "Optional Route53 hosted zone ID for a private DNS record pointing to this instance."
  type        = string
  default     = ""
}

variable "private_dns_name" {
  description = "Optional Route53 record name such as redis.internal."
  type        = string
  default     = ""
}

variable "registry_server" {
  description = "Optional container registry host for docker login before deployment."
  type        = string
  default     = ""
}

variable "registry_username" {
  description = "Optional container registry username for docker login before deployment."
  type        = string
  default     = ""
}

variable "registry_password" {
  description = "Optional container registry password for docker login before deployment."
  type        = string
  sensitive   = true
  default     = ""
}

variable "app_env" {
  description = "Non-secret environment variables written to the prod env file."
  type        = map(string)
  default     = {}
}

variable "app_secret_env" {
  description = "Secret environment variables written to the prod env file through Secrets Manager-backed bootstrap."
  type        = map(string)
  sensitive   = true
  default     = {}
}

variable "secret_recovery_window_days" {
  description = "Secrets Manager recovery window in days."
  type        = number
  default     = 7
}
