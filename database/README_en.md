# Database Configuration for Luxury Fragrance Shop

**[Tiếng Việt](README.md)**  |  **English**

## 📋 Overview

This directory contains scripts and configuration for managing the Microsoft SQL Server database used in the Luxury Fragrance Shop project. The database runs in a Docker container for ease of deployment and consistent development environments.

### 🔧 Technologies Used

- **Microsoft SQL Server 2022** (latest version)
- **Docker** and **Docker Compose** for container management
- **Bash scripts** for easy administration
- **sqlcmd** pre-installed in the container

## 🚀 Getting Started

### System Requirements

- [Docker](https://www.docker.com/get-started) (version 20.10.0 or higher)
- [Docker Compose](https://docs.docker.com/compose/install/) (version 2.0.0 or higher)
- Operating System: Linux, macOS, or Windows with WSL
- Minimum 2GB RAM for SQL Server

### Environment Setup

1. Copy the example environment file:
   ```bash
   cp .env.example .env.local
   ```

2. Edit the configuration parameters:
   ```bash
   # Open file for editing
   nano .env.local
   ```

   Important parameters:
   - `MSSQL_SA_PASSWORD`: SA account password (minimum 8 characters, including uppercase, lowercase, digits, special characters)
   - `MSSQL_PORT`: Port to access SQL Server (default: 1433)
   - `CONTAINER_NAME`: Docker container name
   - `MSSQL_DATABASE`: Default database name
   - `PLATFORM`: Docker platform (linux/amd64 or linux/arm64)
   - `MSSQL_PID`: SQL Server edition (Developer, Express, Standard, Enterprise, or Evaluation)

## 🛠️ Database Management

### Basic Commands

| Script | Function | Usage |
|--------|----------|-------|
| `./start.sh` | Starts the database container | `./start.sh [env_file]` |
| `./stop.sh` | Stops the database container | `./stop.sh [env_file]` |
| `./restart.sh` | Restarts the container | `./restart.sh [env_file]` |
| `./reset.sh` | Resets the database (data will be lost) | `./reset.sh [env_file]` |
| `./delete.sh` | Completely removes container and volumes | `./delete.sh [env_file]` |
| `./fast-setup.sh` | Quickly sets up a new database and user | `./fast-setup.sh [env_file]` |
| `./fast-check.sh` | Quick check of database status | `./fast-check.sh [env_file]` |
| `./synconfigs.sh` | Synchronizes environment variables between root and database directory | `./synconfigs.sh` |

For all scripts, you can optionally specify a custom environment file path as the first argument. If not specified, the scripts will automatically look for `.env.local` first, then `.env`.

### SQL Server Management Tool (dbctl.sh)

The `dbctl.sh` script provides a comprehensive interface for managing your database:

#### Key Features:
- Create and delete databases
- Create, modify, and delete user accounts
- Manage user permissions
- Change user passwords
- Display system and database information

#### Usage:

1. **Interactive mode:**
   ```bash
   ./dbctl.sh
   ```

2. **Direct command mode:**
   ```bash
   ./dbctl.sh --create-db
   ./dbctl.sh --create-user-login
   ./dbctl.sh --create-user-db
   ./dbctl.sh --show-info
   ```

3. **Environment file option:**
   ```bash
   ./dbctl.sh --env .env.custom
   ```

4. **View help:**
   ```bash
   ./dbctl.sh --help
   ```

### Configuration Sync Tool (synconfigs.sh)

The `synconfigs.sh` script helps synchronize environment variables between configuration files in the project:

- Synchronizes from root environment files to the database directory
- Supports `.env`, `.env.example`, and `.env.local` files
- Only synchronizes variables common to both files

## 🔌 Connecting to the Database

After starting, you can connect to SQL Server using:

### Connection Information

- **Server**: localhost or 127.0.0.1
- **Port**: Value of `MSSQL_PORT` (default: 1433)
- **Username**: sa or an account created via `dbctl.sh`
- **Password**: Value of `MSSQL_SA_PASSWORD` or the password you set
- **Database**: Value of `MSSQL_DATABASE` or a database you've created

### Connection Example with sqlcmd

```bash
sqlcmd -S localhost,1433 -U sa -P <password> -d <database_name>
```

### Connection from Applications

Sample connection string:
```
Server=localhost,1433;Database=<database_name>;User Id=<username>;Password=<password>;TrustServerCertificate=True;
```

## 🏗️ Docker Configuration

### Dockerfile
- Based on the official Microsoft SQL Server 2022 image
- Pre-installs `sqlcmd` and related utilities
- Configures PATH for easy `sqlcmd` usage
- Automatically accepts Microsoft's EULA

### docker-compose.yml
- Builds container from the custom Dockerfile
- Configures environment variables from .env file
- Maps ports for access from the host machine
- Sets up volume for persistent data storage
- Configures health check to ensure the service is working properly

## 🔒 Security and Data

### Data Persistence
Database files are stored in a Docker volume named `db_data` to ensure data persists between container restarts.

### Security Recommendations
- Change the default SA password to a strong password
- Create separate accounts for applications instead of using the SA account
- Restrict database port access in production environments
- Back up your data regularly

## 🔍 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port already in use | Change `MSSQL_PORT` in the environment file |
| Container won't start | Check logs: `docker logs <container_name>` |
| Connection error from application | Run `./fast-check.sh` to verify the database is running |
| "Permission denied" when running scripts | Run `chmod +x *.sh` to make scripts executable |
| Not enough memory | Ensure at least 2GB RAM is available for SQL Server |

## 📚 References

- [Official SQL Server Documentation](https://docs.microsoft.com/sql/)
- [Docker Documentation for SQL Server](https://docs.microsoft.com/sql/linux/sql-server-linux-docker-container-deployment)
- [How to use sqlcmd](https://docs.microsoft.com/sql/tools/sqlcmd-utility)