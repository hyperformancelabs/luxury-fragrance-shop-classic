FROM mcr.microsoft.com/mssql/server:2022-latest

# Đặt biến môi trường cho quá trình cài đặt
ENV ACCEPT_EULA=Y

# Cài đặt sqlcmd và các công cụ cần thiết
USER root

# Tạo thư mục home cho mssql user
RUN mkdir -p /home/mssql && chown -R mssql:mssql /home/mssql

# Cài đặt các gói cần thiết và tránh xung đột với các gói ODBC đã cài đặt 
RUN apt-get update \
    && apt-get install -y curl apt-transport-https gnupg \
    && curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - \
    && curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list > /etc/apt/sources.list.d/mssql-release.list \
    && apt-get update \
    && ACCEPT_EULA=Y apt-get install -y --no-install-recommends mssql-tools \
    && echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> /root/.bashrc \
    && echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> /home/mssql/.bashrc \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Thêm vào PATH
ENV PATH="$PATH:/opt/mssql-tools/bin"

# Chuyển về người dùng mssql để chạy SQL Server
USER mssql 