# Terraform MCP Assistant

A powerful macro-based infrastructure automation tool that generates Terraform code using Model Context Protocol (MCP) servers for AWS and HashiCorp resources.

## Overview

Terraform MCP Assistant is an intelligent infrastructure automation platform that combines **DeepSeek AI** with the **AWS Labs Terraform MCP Server** to generate and directly execute Terraform code.

### How It Works

1. **User Request**: Specify infrastructure needs (e.g., "Create VPC with EC2 instance, RDS database, and S3 bucket")
2. **AI Processing**: DeepSeek Chat API understands requirements and generates Terraform specifications
3. **MCP Interaction**: AWS Labs Terraform MCP Server provides real-time AWS resource schemas and best practices
4. **Code Generation**: System generates production-grade Terraform configurations
5. **File Creation**: Actual `.tf` files and directory structure are created (`terraform/generated/`)
6. **Terraform Execution**: Automatically runs `terraform plan` and `terraform apply` (if configured)
7. **Resource Provisioning**: AWS resources are created and managed via Terraform state

### Key Advantages

- **Intelligent Generation**: AI understands complex infrastructure requirements
- **AWS Best Practices**: MCP server ensures configurations follow AWS guidelines
- **Automated Execution**: No manual `terraform apply` needed - automatic provisioning
- **Complex Deployments**: Handles multi-resource, multi-tier infrastructure
- **Scalable**: Async operations for high-performance concurrent requests

## Features

- **DeepSeek AI Integration**: Uses DeepSeek Chat API for intelligent Terraform code generation
- **AWS Labs MCP Server**: Connects to official AWS Terraform MCP server (awslabs.terraform-mcp-server) for real-time resource discovery
- **Dynamic Terraform Generation**: Automatically generates production-grade Terraform configurations
- **Direct Terraform Execution**: Creates actual `.tf` files and directories, then executes `terraform apply`
- **Complex Infrastructure Support**: Handles multi-resource deployments (EC2, RDS, S3, EKS, VPC, IAM, etc.)
- **Async MCP Client**: Non-blocking MCP operations for optimal performance
- **Intelligent Tool Callbacks**: MCP tools are automatically parsed and executed

## Tech Stack

- **Backend**: Java, Spring Boot 3.x, Spring AI
- **AI Integration**: DeepSeek Chat API (AI-powered code generation)
- **MCP (Model Context Protocol)**: AWS Labs Terraform MCP Server
- **Infrastructure**: Terraform (IaC generation and execution)
- **Cloud Platform**: AWS
- **Build Tool**: Maven
- **Async Operations**: Async MCP Client for non-blocking calls
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
- DeepSeek API Key (free at https://platform.deepseek.com)
- Terraform 1.0+
- AWS Account with valid credentials
- `uvx` installed (for MCP server execution)
- Git

## Installation & Setup

### 1. Get DeepSeek API Key

- Go to https://platform.deepseek.com
- Sign up for free account
- Generate API key
- Note down your API key

### 2. Clone Repository

```bash
git clone https://github.com/sudarshanj01/terraform-mcp-assistant.git
cd terraform-mcp-assistant
```

### 3. Set Environment Variables

```bash
# DeepSeek API Key
export DEEPSEEK_API_KEY=your_deepseek_api_key_here

# AWS Credentials
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Optional: Terraform directory
export TERRAFORM_OUTPUT_PATH=./terraform/generated
```

### 4. Build Project

```bash
# Using Maven wrapper (Unix/Linux/Mac)
./mvnw clean install

# Using Maven wrapper (Windows)
mvnw.cmd clean install

# Or using system Maven
mvn clean install
```

### 5. Run Application

```bash
./mvnw spring-boot:run
# Or
mvn spring-boot:run
```

Application starts at `http://localhost:8080`

## Usage

### API Endpoint: Generate & Execute Terraform

```bash
POST /api/terraform/generate-and-apply
Content-Type: application/json

{
  "infrastructure_request": "Create a VPC with 2 public subnets, 1 private subnet, 1 NAT gateway, 1 EC2 t3.micro instance in public subnet, and 1 RDS MySQL database in private subnet",
  "region": "us-east-1",
  "environment": "development",
  "auto_apply": true
}
```

### What Happens Automatically

1. **AI Parses Request**: DeepSeek understands the infrastructure needs
2. **MCP Server Queries**: AWS Terraform MCP server provides resource schemas
3. **Terraform Files Created**: 
   ```
   terraform/generated/
   ├── main.tf
   ├── variables.tf
   ├── outputs.tf
   ├── vpc.tf
   ├── compute.tf
   ├── database.tf
   └── terraform.tfvars
   ```
4. **Validation**: `terraform validate` is run automatically
5. **Execution**: `terraform plan` and `terraform apply` execute (if auto_apply=true)
6. **Resources Provisioned**: AWS resources are created and managed
7. **Response**: Returns Terraform output, resource IDs, and deployment status

### Example Response

```json
{
  "status": "success",
  "terraform_files_created": 6,
  "resources_provisioned": 8,
  "vpc_id": "vpc-0abc123def456",
  "ec2_instance_id": "i-0xyz789abc123",
  "rds_endpoint": "mydb.c9akciq32.us-east-1.rds.amazonaws.com",
  "terraform_state": "terraform.tfstate",
  "execution_time_ms": 45000
}
```

## MCP Server Integration

This project integrates with:
- **AWS Labs Terraform MCP Server** (`awslabs.terraform-mcp-server@latest`): Official Terraform resource schemas, AWS best practices, and infrastructure patterns

### How MCP Integration Works

1. **Connection**: Spring AI MCP Client connects to AWS Labs Terraform MCP Server via STDIO
2. **Tool Discovery**: Server exposes Terraform tools for resource creation and validation
3. **Async Execution**: Non-blocking async operations prevent timeouts on large deployments
4. **Tool Callbacks**: MCP tools are automatically parsed and executed
5. **Error Handling**: STDERR is redirected to logs (FASTMCP_LOG_LEVEL=ERROR) to avoid STDIO pollution

### Supported AWS Resources (via MCP)

- EC2 (instances, security groups, key pairs)
- VPC (subnets, internet gateways, route tables, NAT gateways)
- RDS (databases, parameter groups, security groups)
- S3 (buckets, policies, versioning)
- EKS (clusters, node groups)
- IAM (roles, policies, users)
- Lambda (functions, layers, permissions)
- CloudWatch (log groups, alarms)
- And many more...

## Key Components

### DeepSeek AI Service
- Processes natural language infrastructure requests
- Generates Terraform variable specifications
- Understands complex multi-resource deployments
- Uses 4000 token context window for detailed configurations

### MCP Client Service
- Manages async STDIO connections to AWS Labs Terraform MCP Server
- Executes Terraform tools discovered from MCP server
- Handles tool callbacks and result parsing
- Implements 120-second timeout for long-running operations

### Terraform Code Generator
- Creates directory structure (`terraform/generated/`)
- Generates `.tf` files (main.tf, variables.tf, outputs.tf, resource files)
- Implements Terraform best practices
- Handles variable interpolation and resource dependencies

### Terraform Executor
- Runs `terraform validate`
- Executes `terraform plan` with state locking
- Applies changes via `terraform apply`
- Manages Terraform state files
- Captures execution logs and resource IDs

### AWS Integration Layer
- AWS credentials management (IAM roles, access keys)
- Region configuration and multi-AZ support
- Security group and VPC networking
- Cross-resource dependency management

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

### Environment Variables

```bash
# DeepSeek API Key (required)
export DEEPSEEK_API_KEY=your_deepseek_api_key_here

# AWS Credentials (for actual Terraform deployment)
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

### Application Configuration (application.properties)

```properties
# Application
spring.application.name=mcp

# DeepSeek API Configuration
spring.ai.openai.api-key=${DEEPSEEK_API_KEY}
spring.ai.openai.base-url=https://api.deepseek.com
spring.ai.openai.chat.options.model=deepseek-chat
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=4000

# MCP Client Configuration
spring.ai.mcp.client.request-timeout=120s
spring.ai.mcp.client.type=ASYNC
spring.ai.mcp.client.toolcallback.enabled=true

# AWS Labs Terraform MCP Server Configuration
spring.ai.mcp.client.stdio.connections.terraform-mcp-server.command=uvx
spring.ai.mcp.client.stdio.connections.terraform-mcp-server.args=awslabs.terraform-mcp-server@latest
spring.ai.mcp.client.stdio.connections.terraform-mcp-server.env.FASTMCP_LOG_LEVEL=ERROR

# Terraform Configuration
terraform.output.path=./terraform/generated
terraform.auto.execute=true
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

- [ ] Terraform state management dashboard
- [ ] Cost estimation before apply (AWS Pricing API integration)
- [ ] Compliance scanning (checkov integration)
- [ ] Multi-cloud support (Azure ARM, Google Cloud)
- [ ] GitOps integration (automatic commits and PR creation)
- [ ] Terraform module versioning and rollback
- [ ] Infrastructure drift detection
- [ ] Custom MCP server integration
- [ ] Web UI for infrastructure visualization
- [ ] Slack/Teams notifications for deployments

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
