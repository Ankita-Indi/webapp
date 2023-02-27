variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-0dfcb1ef8550277af" # Ubuntu 22.04 LTS
}

variable "ssh_username" {
  type    = string
  default = "ec2-user"
}

#variable "subnet_id" {
#  type    = string
#  default = "subnet-0efede05bd423a4a7"
#}

variable "profile" {
  type    = string
  default = "dev"
}

variable "aws_demouser" {
  type    = string
  default = "719338688171"
}

variable "aws_devuser" {
  type    = string
  default = "966364109421"
}




# https://www.packer.io/plugins/builders/amazon/ebs
source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
  profile         = "${var.profile}"
  ami_name        = "csye6225_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for CSYE 6225"
  ami_regions = [
    "${var.aws_region}"
  ]
  ami_users = [
    "${var.aws_devuser}",
    "${var.aws_demouser}",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
#  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 50
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "file" {
    sources     = ["./target/health-check-api-0.0.1-SNAPSHOT.jar"]
    destination = "/tmp/"
  }

  provisioner "file" {
    sources     = ["health-check-api.service"]
    destination = "/tmp/"
  }

  provisioner "shell" {
    # environment_vars = [
    #   "DEBIAN_FRONTEND=noninteractive",
    #   "CHECKPOINT_DISABLE=1"
    # ]
    scripts = [
      "setup.sh"
    ]
  }
}
