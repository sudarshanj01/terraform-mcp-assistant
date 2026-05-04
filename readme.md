# Terraform MCP Assistant

A powerful macro-based infrastructure automation tool that generates Terraform code using Model Context Protocol (MCP) servers for AWS and HashiCorp resources.

## Overview

Terraform MCP Assistant leverages MCP servers to dynamically search, fetch, and integrate official AWS and HashiCorp Terraform modules. This tool streamlines infrastructure-as-code generation, enabling rapid provisioning of cloud resources with minimal manual configuration.

## Features

- **MCP Server Integration**: Connects to official AWS and HashiCorp MCP servers for resource discovery
- **Dynamic Terraform Generation**: Automatically generates Terraform code based on infrastructure requirements
- **Multi-Cloud Support**: Supports AWS resources (EC2, RDS, S3, EKS, VPC, IAM, etc.)
- **Module Management**: Searches and integrates reusable Terraform modules
- **Infrastructure as Code**: Full Infrastructure-as-Code approach for reproducible deployments

## Tech Stack

- **Backend**: Java, Spring Boot, Spring MVC
- **Infrastructure**: Terraform, MCP (Model Context Protocol)
- **Cloud Platform**: AWS
- **Build Tool**: Maven
- **Version Control**: Git

## Project Structure

```
terraform-mcp-assistant/
├── src/
│   ├── main/
│   │   ├── java/          # Java source code
│   │   └── resources/     # Application properties
│   └── test/              # Unit tests
├── target/                # Compiled artifacts
├── pom.xml               # Maven configuration
├── mvnw                  # Maven wrapper (Unix)
├── mvnw.cmd             # Maven wrapper (Windows)
└── README.md            # This file
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- AWS Account (for testing)
- Git

## Installation & Setup

### 1. Clone Repository
```bash
git clone https://github.com/sudarshanj01/terraform-mcp-assistant.git
cd terraform-mcp-assistant
```

### 2. Build Project
```bash
# Using Maven wrapper (Unix/Linux/Mac)
./mvnw clean install

# Using Maven wrapper (Windows)
mvnw.cmd clean install

# Or using system Maven
mvn clean install
```

### 3. Configure AWS Credentials
```bash
# Set AWS credentials (choose one method)
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

### 4. Run Application
```bash
./mvnw spring-boot:run
# Or
mvn spring-boot:run
```

## Usage

### Generate Terraform Code for AWS Resources

1. Access the application (typically at `http://localhost:8080`)
2. Specify infrastructure requirements (resource type, region, configuration)
3. System queries MCP servers to fetch available modules
4. Generated Terraform code is displayed and ready to deploy

### Example: Generate EC2 Instance Configuration

```bash
# API request to generate Terraform code
POST /api/terraform/generate
{
  "resource_type": "ec2",
  "region": "us-east-1",
  "instance_type": "t3.micro",
  "availability_zone": "us-east-1a"
}
```

## MCP Server Integration

This project integrates with:
- **AWS MCP Server**: Official AWS Terraform modules and documentation
- **HashiCorp MCP Server**: HashiCorp Terraform modules and best practices

### How It Works

1. User specifies infrastructure needs
2. MCP servers are queried for matching Terraform modules
3. Relevant modules are fetched and analyzed
4. Terraform code is generated with best practices
5. Output is validated and ready for deployment

## Key Components

### Terraform Service
- Handles communication with MCP servers
- Generates Terraform configurations
- Manages module dependencies

### AWS Integration
- EC2, RDS, S3, EKS, VPC, IAM resources
- Security group and networking configurations
- Multi-AZ and high-availability support

### Code Generation Engine
- Parses infrastructure requirements
- Generates optimized Terraform code
- Implements infrastructure best practices

## Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Run with coverage
./mvnw clean test jacoco:report
```

## Deployment

### Local Development
```bash
./mvnw spring-boot:run
```

### Docker Deployment
```bash
# Build Docker image
docker build -t terraform-mcp-assistant .

# Run container
docker run -p 8080:8080 terraform-mcp-assistant
```

### AWS Deployment
- Deploy to AWS Elastic Beanstalk or ECS
- Configure environment variables for AWS credentials
- Use RDS for persistent storage (if needed)

## Configuration

Configure via `application.properties`:

```properties
# Server
server.port=8080

# AWS
aws.region=us-east-1

# MCP Servers
mcp.aws.endpoint=https://...
mcp.hashicorp.endpoint=https://...

# Terraform
terraform.output.path=/terraform/generated
```

## Troubleshooting

### MCP Server Connection Issues
- Verify network connectivity to MCP servers
- Check API credentials and tokens
- Review logs in `target/` directory

### Terraform Generation Errors
- Validate resource requirements
- Check AWS credentials and permissions
- Ensure correct region configuration

## Performance

- **Module Search**: < 2 seconds (with MCP caching)
- **Code Generation**: < 1 second for standard resources
- **Multi-resource Batches**: Scales linearly with complexity

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## Future Enhancements

- [ ] Multi-cloud support (Azure, GCP)
- [ ] Terraform state management UI
- [ ] Cost estimation integration
- [ ] Compliance and security scanning
- [ ] Workflow automation for infrastructure changes
- [ ] Advanced module customization options

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

**Sudarshan Jadhav**
- GitHub: [@sudarshanj01](https://github.com/sudarshanj01)
- Email: sudarshan08062001@gmail.com
- LinkedIn: [sudarshan-jadhav-8a3982199](https://linkedin.com/in/sudarshan-jadhav-8a3982199)

## Acknowledgments

- AWS for Terraform modules and documentation
- HashiCorp for Terraform and MCP specifications
- Spring Boot community for excellent framework

## Support

For issues, questions, or suggestions:
1. Open an issue on GitHub
2. Contact via email
3. Check existing issues for solutions

---

**Built with ☕ and ☁️ by Sudarshan Jadhav**