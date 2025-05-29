# Lập trình web SPRING MVC - Bài 1: Giới thiệu Spring MVC

## Cách tính điểm

1. Chuyên cần: 10%
2. Điểm kiểm tra: 7 bài Lab
3. Thi lần 1: Báo cáo project
4. Thi lần 2: Làm bài test 60’ ở phòng thực hành

---

## Mục tiêu

* Hiểu Spring Framework
* Mô hình hoạt động Spring MVC
* Thiết lập môi trường
* Tạo dự án Spring MVC
* Làm việc với các đối tượng web
* Truyền dữ liệu từ Controller sang View

---

## 01. Giới thiệu Spring Framework

* **Spring** là một framework phát triển ứng dụng Java, mã nguồn mở, hiệu năng cao, dễ kiểm thử, tái sử dụng mã.
* **Tính năng core:** phát triển ứng dụng Java Desktop, mobile, Web (J2EE) dựa trên POJO.
* **Ra đời:** bởi Rod Johnson, tháng 6 năm 2003.

### Kiến trúc Spring Framework

* **Spring Core:** nền tảng cơ bản
* **Spring AOP:** lập trình hướng khía cạnh
* **Spring DAO:** đối tượng truy xuất dữ liệu
* **Spring ORM:** ánh xạ đối tượng - quan hệ
* **Spring Context:** dịch vụ truy cập từ xa
* **Spring Web:** tích hợp với các web framework
* **Spring MVC:** mô hình MVC cho ứng dụng web

---

## 02. Mô hình hoạt động Spring MVC

### Cấu trúc MVC

* **Model:** POJO, Service, DAO
* **View:** JSP, HTML
* **Controller:** Dispatcher Controller, Handler Mapping

### Quy trình xử lý request

1. DispatcherServlet chuyển URL cho HandlerMapping
2. Gọi action method trong Controller
3. ViewResolver xác định đường dẫn View
4. Trả kết quả HTML về client

---

## 03. Thiết lập môi trường

### Yêu cầu phần mềm

* JDK 7+
* Eclipse for Java EE
* Tomcat 8.x
* SQL Server 2008+

### Tải về và cài đặt

* [JDK](http://download.oracle.com/otn-pub/java/jdk/8u112-b15/jdk-8u112-windows-x64.exe)
* [SQL Server](http://download.microsoft.com/download/8/D/D/8DD7BDBA-CEF7-4D8E-8C16-D9F69527F909/ENU/x64/SQLManagementStudio_x64_ENU.exe)
* [Eclipse](http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/mars/R/eclipse-jee-mars-R-win32-x86_64.zip&mirror_id=448)
* [Tomcat](http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.8/bin/apache-tomcat-8.5.8-windows-x64.zip)

### Tích hợp Tomcat vào Eclipse

* Thêm Web Server
* Cấu hình port tránh xung đột
* Start Tomcat

---

## 04. Tạo dự án Spring MVC

### Yêu cầu

* Thư viện \*.jar
* Cấu hình \*.xml
* Tuân theo quy ước Spring

### Cấu trúc dự án

* **src/** chứa mã nguồn Java
* **WebContent/** chứa JSP, hình ảnh, CSS, JS
* **WEB-INF/lib/** chứa thư viện
* **web.xml** cấu hình web
* **spring-config-mvc.xml** cấu hình Spring MVC

### Các file cấu hình

* `web.xml`: khai báo DispatcherServlet, CharacterEncodingFilter
* `spring-config-mvc.xml`: Controller, ViewResolver

### Tạo Controller và View

* Controller: annotated class
* View: JSP trong `WEB-INF/views`

---

## 05. Làm việc với các đối tượng web

### Các đối tượng trong Servlet/JSP

* **HttpServletRequest**
* **HttpServletResponse**
* **HttpSession**
* **ServletContext**

### Spring MVC hỗ trợ

* Đưa trực tiếp vào phương thức xử lý như tham số
* Sử dụng `@Autowired` để tiêm `ServletContext`

---

## 06. Truyền dữ liệu từ Controller sang View

### Cách truyền dữ liệu

* Sử dụng `ModelMap` thay vì `HttpServletRequest`
* Ví dụ:

  ```java
  model.addAttribute("user", user);
  ```

### Trong JSP

* Sử dụng `${user}` hoặc `<%= request.getAttribute("user") %>`

---

## Tổng kết nội dung bài học

* Giới thiệu Spring Framework
* Xử lý request trong Spring MVC
* Thiết lập môi trường phát triển
* Tích hợp Tomcat vào Eclipse
* Tạo dự án web và Spring MVC
* Cấu hình ứng dụng
* Tạo Controller, JSP
* Làm việc với đối tượng web
* Truyền dữ liệu từ Controller sang View
