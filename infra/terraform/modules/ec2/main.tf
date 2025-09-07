terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

# IAM role for EC2 instances
resource "aws_iam_role" "ec2_role" {
  name               = "${var.name_prefix}-ec2-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action = "sts:AssumeRole"
    }]
  })
}

# Attach AWS managed policies for SSM and CloudWatch Agent (basic)
resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# ECR pull
resource "aws_iam_role_policy_attachment" "ecr_readonly" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Allow reading specified secrets (conditional attachments will be generated if ARNs provided)
resource "aws_iam_policy" "secrets_read" {
  count       = length(var.secrets_map) > 0 ? 1 : 0
  name        = "${var.name_prefix}-secrets-read"
  description = "Allows reading configured Secrets Manager secrets"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Action = [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      Resource = compact([
        var.jwt_secret_arn,
        var.db_username_secret_arn,
        var.db_password_secret_arn,
        var.smtp_username_secret_arn,
        var.smtp_password_secret_arn
      ])
    }]
  })
}

resource "aws_iam_role_policy_attachment" "secrets_read_attach" {
  count      = length(aws_iam_policy.secrets_read) > 0 ? 1 : 0
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.secrets_read[0].arn
}

# Allow instance to write logs to CloudWatch Logs
resource "aws_iam_policy" "cw_logs_write" {
  name        = "${var.name_prefix}-cw-logs-write"
  description = "Allow instance to write application logs to CloudWatch Logs"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = ["logs:CreateLogGroup"],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cw_logs_write_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.cw_logs_write.arn
}

resource "aws_iam_instance_profile" "this" {
  name = "${var.name_prefix}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# Security group for instances to allow traffic from ALB SG only
resource "aws_security_group" "ec2" {
  name        = "${var.name_prefix}-ec2-sg"
  description = "Allow app port from ALB SG"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  dynamic "ingress" {
    for_each = var.allow_ssh && var.ssh_ingress_cidr != "" ? [1] : []
    content {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = [var.ssh_ingress_cidr]
      description = "Optional SSH access"
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Latest Amazon Linux 2 AMI via SSM parameter (AL2023 can be used too)
data "aws_ssm_parameter" "ami" {
  name = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2"
}

data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

# Launch Template
resource "aws_launch_template" "this" {
  name_prefix   = "${var.name_prefix}-lt-"
  image_id      = data.aws_ssm_parameter.ami.value
  instance_type = var.instance_type
  key_name      = var.key_name != "" ? var.key_name : null
  iam_instance_profile {
    name = aws_iam_instance_profile.this.name
  }
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = base64encode(templatefile("${path.module}/user_data.sh", {
    region                   = var.region
    ecr_repo_url            = var.ecr_repository_url
    image_tag               = var.image_tag
    container_port          = var.container_port
    jwt_secret_arn          = var.jwt_secret_arn
    db_username_secret_arn  = var.db_username_secret_arn
    db_password_secret_arn  = var.db_password_secret_arn
    smtp_username_secret_arn = var.smtp_username_secret_arn
    smtp_password_secret_arn = var.smtp_password_secret_arn
    db_url_secret_arn       = var.db_url_secret_arn
    smtp_host               = var.smtp_host
    smtp_port               = var.smtp_port
    email_sender            = var.email_sender
    email_enabled           = var.email_enabled
    log_group_name          = var.log_group_name
  }))

  tag_specifications {
    resource_type = "instance"
    tags          = var.tags
  }
}

# Auto Scaling Group
resource "aws_autoscaling_group" "this" {
  name                = "${var.name_prefix}-asg"
  vpc_zone_identifier = var.private_subnet_ids
  target_group_arns   = [var.target_group_arn]
  health_check_type   = "ELB"
  health_check_grace_period = 900

  min_size         = var.min_size
  max_size         = var.max_size
  desired_capacity = var.desired_capacity

  launch_template {
    id      = aws_launch_template.this.id
    version = "$Latest"
  }

  # Instance refresh for rolling updates
  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50
    }
  }

  tag {
    key                 = "Name"
    value               = "${var.name_prefix}-instance"
    propagate_at_launch = true
  }

  dynamic "tag" {
    for_each = var.tags
    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }
}
