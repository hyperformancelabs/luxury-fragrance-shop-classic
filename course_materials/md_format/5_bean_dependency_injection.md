# Lập trình web SPRING MVC - BÀI 5: Bean và Dependency Injection

### Mục tiêu:

Nắm vững kỹ thuật lập trình giao diện trong JSP:

1. Hiểu Dependency Injection (DI) là gì?
2. Xây dựng và sử dụng Bean
3. Sử dụng `@Autowired` và `@Qualifier`
4. Sử dụng bean `CommonsMultipartResolver` để upload file lên server
5. Sử dụng bean `JavaMailSender` để gửi email
6. Xây dựng bean gửi email

---

## Dependency Injection (DI)

### Xét tình huống về Dependency

* Có lớp `Company` lưu thông tin doanh nghiệp như tên, khẩu hiệu, logo.
* Website sử dụng lớp này để hiển thị thông tin doanh nghiệp.
* Các lớp trong website phụ thuộc vào `Company`. Nếu thay đổi thông tin, phải sửa mã.
* Cần một cách thay đổi thông tin doanh nghiệp mà không phải sửa mã website.

### Dependency Injection là gì?

* Là cách truyền một module vào một module khác qua XML hoặc code với DI container.
* Spring framework hỗ trợ DI container giúp DI dễ dàng.
* Mục tiêu: giảm phụ thuộc giữa các module, dễ bảo trì, dễ test.

### Cụ thể hóa DI với bean `Company`

Thuộc tính:

* `Name`: tên công ty
* `Slogan`: khẩu hiệu
* `Logo`: ảnh logo

### Khai báo bean

* Mục tiêu: tạo đối tượng `Company` có thể sử dụng lại trong website.
* Khai báo trong file cấu hình Spring, DI container sẽ tạo đối tượng khi khởi động.

### Injection (Tiêm)

* Sau khi khai báo bean, có thể sử dụng `@Autowired` và `@Qualifier` để tiêm bean vào thành phần khác.

### Hiển thị thông tin doanh nghiệp

* `index.jsp` hiển thị thông tin doanh nghiệp.

### Các cách tiêm:

* Tiêm vào field
* Tiêm qua constructor
* Tiêm qua setter

### Xác định bean cần tiêm:

* `@Autowired` dùng kiểu dữ liệu để tìm bean.
* Nếu có nhiều bean cùng kiểu, dùng thêm `@Qualifier(id)`.

### Bean tự khai báo:

* Sử dụng `@Component`, `@Service`, `@Repository`.
* Cần khai báo package chứa bean:

```xml
<context:component-scan base-package="ptithcm.controller,ptithcm.components"/>
```

---

## Upload File

### Giới thiệu

* Chức năng quan trọng trong web: gửi mail, upload ảnh, video, nộp bài,...

### Thư viện & cấu hình

* Khai báo bean `CommonsMultipartResolver`
* Mặc định file tối đa 2MB. Có thể cấu hình `maxUploadSize`.

**Thư viện cần thiết:**

* `commons-fileupload-1.2.2.jar`
* `commons-io-1.3.2.jar`

### Form upload:

```html
<form method="POST" enctype="multipart/form-data">
```

### MultipartFile API:

| Phương thức             | Công dụng                      |
| ----------------------- | ------------------------------ |
| `isEmpty()`             | Kiểm tra file có tồn tại không |
| `getOriginalFilename()` | Lấy tên gốc file               |
| `transferTo(File)`      | Lưu file tới đường dẫn mới     |
| `getContentType()`      | Lấy kiểu file                  |
| `getSize()`             | Kích thước file                |
| `getBytes()`            | Nội dung file                  |
| `getInputStream()`      | Lấy stream dữ liệu             |

---

## Gửi Email

### Vai trò:

* Kích hoạt tài khoản
* Xác nhận đơn hàng
* Gửi mật khẩu mới
* Chia sẻ sản phẩm với bạn bè

### Bean `JavaMailSender`

* Dễ sử dụng, cần thư viện:

  * `mail.jar`
  * `activation.jar`

### Mô hình:

* Gmail đóng vai trò như SMTP server.

### Cấu hình `JavaMailSender`:

* Gửi mail thông qua Gmail.
* Phải bật tính năng:

  * [Turn on Less Secure Apps](https://www.google.com/settings/security/lesssecureapps)

### Tạo password ứng dụng:

* Truy cập quản lý tài khoản Google.
* Tạo mật khẩu ứng dụng riêng để dùng trong cấu hình SMTP.

### Case Study: Gửi email

* Form gửi mail nhập thông tin hợp lệ.
* Gửi file đính kèm thông qua:

```java
@RequestParam("attach") MultipartFile attach;
```

```java
String fileName = attach.getOriginalFilename();
String path = context.getRealPath("/images/" + fileName);
helper.addAttachment(fileName, new File(path));
```

### JavaMailSender API:

| Phương thức                 | Công dụng       |
| --------------------------- | --------------- |
| `setFrom(email, name)`      | Người gửi       |
| `setTo(email)`              | Người nhận      |
| `setCc(emails)`             | Email cùng nhận |
| `setBcc(emails)`            | Email ẩn danh   |
| `setReplyTo(email, name)`   | Email phản hồi  |
| `setSubject(subject)`       | Tiêu đề         |
| `setText(body, isHtml)`     | Nội dung        |
| `addAttachment(name, file)` | Đính kèm file   |
| `createMimeMessage()`       | Tạo email       |
| `send(mail)`                | Gửi email       |

---

## Tổng kết nội dung bài học

* ✅ Tìm hiểu Dependency Injection
* ✅ Xây dựng, khai báo và sử dụng bean
* ✅ Upload file
* ✅ Gửi email
* ✅ Xây dựng bean Mailer
