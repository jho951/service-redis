locals {
  resource_prefix = "${var.environment}-${var.service_name}"

  common_tags = merge(var.tags, {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Service     = var.service_name
    Role        = var.service_role
  })

  vpc_id                    = var.create_vpc ? aws_vpc.main[0].id : var.existing_vpc_id
  primary_subnet_id         = var.create_vpc ? aws_subnet.public[0].id : var.existing_subnet_id
  network_cidr              = var.create_vpc ? var.vpc_cidr : var.existing_vpc_cidr
  default_app_ingress_cidrs = compact([local.network_cidr])
  effective_app_ingress     = length(var.app_ingress_cidrs) > 0 ? var.app_ingress_cidrs : local.default_app_ingress_cidrs
}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-kernel-6.1-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_vpc" "main" {
  count = var.create_vpc ? 1 : 0

  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-vpc"
  })
}

resource "aws_internet_gateway" "main" {
  count = var.create_vpc ? 1 : 0

  vpc_id = aws_vpc.main[0].id

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-igw"
  })
}

resource "aws_subnet" "public" {
  count = var.create_vpc ? length(var.public_subnet_cidrs) : 0

  vpc_id                  = aws_vpc.main[0].id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-public-${count.index + 1}"
    Tier = "public"
  })
}

resource "aws_route_table" "public" {
  count = var.create_vpc ? 1 : 0

  vpc_id = aws_vpc.main[0].id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main[0].id
  }

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-public-rt"
  })
}

resource "aws_route_table_association" "public" {
  count = var.create_vpc ? length(aws_subnet.public) : 0

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public[0].id
}

resource "aws_security_group" "service" {
  name        = "${local.resource_prefix}-sg"
  description = "Ingress for ${var.service_name} EC2 host"
  vpc_id      = local.vpc_id

  dynamic "ingress" {
    for_each = length(local.effective_app_ingress) > 0 ? [1] : []
    content {
      description = "Service port"
      from_port   = var.app_port
      to_port     = var.app_port
      protocol    = "tcp"
      cidr_blocks = local.effective_app_ingress
    }
  }

  dynamic "ingress" {
    for_each = var.extra_ingress_rules
    content {
      description = ingress.value.description
      from_port   = ingress.value.from_port
      to_port     = ingress.value.to_port
      protocol    = "tcp"
      cidr_blocks = ingress.value.cidr_blocks
    }
  }

  dynamic "ingress" {
    for_each = var.ssh_ingress_cidrs
    content {
      description = "SSH"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = [ingress.value]
    }
  }

  egress {
    description = "Outbound internet for bootstrap, Git clone, and image pulls"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-sg"
  })
}

resource "aws_secretsmanager_secret" "bootstrap" {
  name                    = "${local.resource_prefix}-bootstrap"
  recovery_window_in_days = var.secret_recovery_window_days

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-bootstrap"
  })
}

resource "aws_secretsmanager_secret_version" "bootstrap" {
  secret_id = aws_secretsmanager_secret.bootstrap.id
  secret_string = jsonencode({
    app_secret_env         = var.app_secret_env
    repository_clone_token = var.repository_clone_token
    registry_password      = var.registry_password
  })
}

resource "aws_iam_role" "ec2" {
  name = "${local.resource_prefix}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "bootstrap_secret_access" {
  name = "${local.resource_prefix}-bootstrap-secret"
  role = aws_iam_role.ec2.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = [aws_secretsmanager_secret.bootstrap.arn]
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${local.resource_prefix}-ec2-profile"
  role = aws_iam_role.ec2.name
}

resource "aws_instance" "service" {
  ami                         = var.ec2_ami_id != "" ? var.ec2_ami_id : data.aws_ami.al2023.id
  instance_type               = var.ec2_instance_type
  subnet_id                   = local.primary_subnet_id
  vpc_security_group_ids      = [aws_security_group.service.id]
  associate_public_ip_address = var.associate_public_ip
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  key_name                    = var.ec2_key_name == "" ? null : var.ec2_key_name

  metadata_options {
    http_tokens = "required"
  }

  user_data_replace_on_change = true
  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    aws_region             = var.aws_region
    stack_name             = var.service_runtime_name
    docker_compose_version = var.docker_compose_version
    bootstrap_secret_arn   = aws_secretsmanager_secret.bootstrap.arn
    repository_url_json    = jsonencode(var.repository_url)
    repository_ref_json    = jsonencode(var.repository_ref)
    deploy_env_file_json   = jsonencode(var.deploy_env_file)
    deploy_command_json    = jsonencode(var.deploy_command)
    registry_server_json   = jsonencode(var.registry_server)
    registry_username_json = jsonencode(var.registry_username)
    plain_env_json         = jsonencode(var.app_env)
  })

  root_block_device {
    volume_size = var.ec2_root_volume_size
    volume_type = "gp3"
    encrypted   = true
  }

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-ec2"
  })

  depends_on = [
    aws_iam_role_policy_attachment.ssm,
    aws_iam_role_policy.bootstrap_secret_access,
    aws_secretsmanager_secret_version.bootstrap,
  ]
}

resource "aws_route53_record" "private" {
  count = var.private_dns_zone_id != "" && var.private_dns_name != "" ? 1 : 0

  zone_id = var.private_dns_zone_id
  name    = var.private_dns_name
  type    = "A"
  ttl     = 60
  records = [aws_instance.service.private_ip]
}
