# Cấu hình Cơ Sở Dữ Liệu Luxury Fragrance Shop

**Tiếng Việt**  |  **[English](README_en.md)**

## 📋 Tổng quan

Thư mục này chứa các tập lệnh và cấu hình để quản lý cơ sở dữ liệu Microsoft SQL Server cho dự án Luxury Fragrance Shop. Cơ sở dữ liệu được đóng gói trong container Docker để đảm bảo môi trường phát triển đồng nhất và dễ dàng triển khai.

### 🔧 Công nghệ sử dụng

- **Microsoft SQL Server 2022** (phiên bản mới nhất)
- **Docker** và **Docker Compose** để quản lý container
- **Bash scripts** cho việc quản trị dễ dàng
- **sqlcmd** được tích hợp sẵn trong container

## 🚀 Bắt đầu sử dụng

### Yêu cầu hệ thống

- [Docker](https://www.docker.com/get-started) (phiên bản 20.10.0 trở lên)
- [Docker Compose](https://docs.docker.com/compose/install/) (phiên bản 2.0.0 trở lên)
- Hệ điều hành: Linux, macOS hoặc Windows với WSL
- Tối thiểu 2GB RAM cho SQL Server

### Thiết lập môi trường

1. Sao chép tệp môi trường mẫu:
   ```bash
   cp .env.example .env.local
   ```

2. Chỉnh sửa các thông số cấu hình:
   ```bash
   # Mở file để chỉnh sửa
   nano .env.local
   ```

   Các thông số quan trọng:
   - `MSSQL_SA_PASSWORD`: Mật khẩu tài khoản SA (ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số, ký tự đặc biệt)
   - `MSSQL_PORT`: Cổng để truy cập SQL Server (mặc định: 1433)
   - `CONTAINER_NAME`: Tên container Docker
   - `MSSQL_DATABASE`: Tên cơ sở dữ liệu mặc định
   - `PLATFORM`: Nền tảng Docker (linux/amd64 hoặc linux/arm64)
   - `MSSQL_PID`: Phiên bản SQL Server (Developer, Express, Standard, Enterprise, hoặc Evaluation)

## 🛠️ Quản lý cơ sở dữ liệu

### Các lệnh cơ bản

| Script | Chức năng | Cách sử dụng |
|--------|-----------|--------------|
| `./start.sh` | Khởi động container cơ sở dữ liệu | `./start.sh [env_file]` |
| `./stop.sh` | Dừng container cơ sở dữ liệu | `./stop.sh [env_file]` |
| `./restart.sh` | Khởi động lại container | `./restart.sh [env_file]` |
| `./reset.sh` | Đặt lại cơ sở dữ liệu (xóa dữ liệu) | `./reset.sh [env_file]` |
| `./delete.sh` | Xóa hoàn toàn container và volumes | `./delete.sh [env_file]` |
| `./fast-setup.sh` | Thiết lập nhanh một database và user mới | `./fast-setup.sh [env_file]` |
| `./fast-check.sh` | Kiểm tra nhanh trạng thái database | `./fast-check.sh [env_file]` |
| `./synconfigs.sh` | Đồng bộ biến môi trường giữa thư mục gốc và database | `./synconfigs.sh` |

Đối với mọi script, bạn có thể tùy chọn chỉ định đường dẫn tới file môi trường làm tham số đầu tiên. Nếu không chỉ định, script sẽ tự động tìm kiếm `.env.local` trước, sau đó là `.env`.

### Công cụ quản lý SQL Server (dbctl.sh)

Script `dbctl.sh` cung cấp giao diện toàn diện để quản lý cơ sở dữ liệu:

#### Chức năng chính:
- Tạo và xóa database
- Tạo, sửa, và xóa tài khoản người dùng
- Quản lý quyền hạn người dùng
- Thay đổi mật khẩu người dùng
- Hiển thị thông tin hệ thống và database

#### Cách sử dụng:

1. **Chế độ tương tác:**
   ```bash
   ./dbctl.sh
   ```

2. **Chế độ lệnh trực tiếp:**
   ```bash
   ./dbctl.sh --create-db
   ./dbctl.sh --create-user-login
   ./dbctl.sh --create-user-db
   ./dbctl.sh --show-info
   ```

3. **Chỉ định file môi trường:**
   ```bash
   ./dbctl.sh --env .env.custom
   ```

4. **Xem trợ giúp:**
   ```bash
   ./dbctl.sh --help
   ```

### Công cụ đồng bộ hóa cấu hình (synconfigs.sh)

Script `synconfigs.sh` giúp đồng bộ các biến môi trường giữa các file cấu hình trong dự án:

- Đồng bộ từ file môi trường ở thư mục gốc dự án đến thư mục database
- Hỗ trợ các file `.env`, `.env.example`, và `.env.local`
- Chỉ đồng bộ các biến chung giữa hai file

## 🔌 Kết nối đến cơ sở dữ liệu

Sau khi khởi động, bạn có thể kết nối đến SQL Server bằng cách sử dụng:

### Thông tin kết nối

- **Máy chủ**: localhost hoặc 127.0.0.1
- **Cổng**: Giá trị của `MSSQL_PORT` (mặc định: 1433)
- **Tài khoản**: sa hoặc tài khoản đã tạo qua `dbctl.sh`
- **Mật khẩu**: Giá trị của `MSSQL_SA_PASSWORD` hoặc mật khẩu đã đặt
- **Database**: Giá trị của `MSSQL_DATABASE` hoặc database đã tạo

### Ví dụ kết nối bằng sqlcmd

```bash
sqlcmd -S localhost,1433 -U sa -P <mật_khẩu> -d <tên_database>
```

### Kết nối từ ứng dụng

Chuỗi kết nối mẫu:
```
Server=localhost,1433;Database=<tên_database>;User Id=<tài_khoản>;Password=<mật_khẩu>;TrustServerCertificate=True;
```

## 🏗️ Cấu trúc Docker

### Dockerfile
- Sử dụng image SQL Server 2022 chính thức từ Microsoft
- Cài đặt sẵn công cụ `sqlcmd` và các tiện ích cần thiết
- Cấu hình PATH để sử dụng `sqlcmd` dễ dàng
- Tự động chấp nhận EULA của Microsoft

### docker-compose.yml
- Xây dựng container từ Dockerfile tùy chỉnh
- Cấu hình biến môi trường từ file .env
- Ánh xạ cổng để truy cập từ máy host
- Thiết lập volume cho lưu trữ dữ liệu bền vững
- Cấu hình health check để đảm bảo dịch vụ hoạt động đúng

## 🔒 Bảo mật và dữ liệu

### Lưu trữ dữ liệu
Dữ liệu được lưu trữ trong Docker volume `db_data` để đảm bảo tính bền vững giữa các lần khởi động lại container.

### Khuyến nghị bảo mật
- Thay đổi mật khẩu SA mặc định bằng mật khẩu mạnh
- Tạo tài khoản riêng cho ứng dụng thay vì sử dụng tài khoản SA
- Hạn chế quyền truy cập cổng database trong môi trường sản xuất
- Sao lưu dữ liệu thường xuyên

## 🔍 Xử lý sự cố

| Vấn đề | Giải pháp |
|--------|-----------|
| Cổng đã được sử dụng | Thay đổi `MSSQL_PORT` trong file môi trường |
| Container không khởi động | Kiểm tra logs: `docker logs <tên_container>` |
| Lỗi kết nối từ ứng dụng | Chạy `./fast-check.sh` để kiểm tra database đang hoạt động |
| "Permission denied" khi chạy script | Chạy `chmod +x *.sh` để cấp quyền thực thi |
| Không đủ bộ nhớ | Đảm bảo có ít nhất 2GB RAM trống cho SQL Server |

## 📚 Tài liệu tham khảo

- [Tài liệu chính thức của SQL Server](https://docs.microsoft.com/sql/)
- [Tài liệu Docker cho SQL Server](https://docs.microsoft.com/sql/linux/sql-server-linux-docker-container-deployment)
- [Cách sử dụng sqlcmd](https://docs.microsoft.com/sql/tools/sqlcmd-utility)