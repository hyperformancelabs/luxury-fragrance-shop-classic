# Lập trình web SPRING MVC - Bài 6: Tích hợp Hibernate

## Mục tiêu

1. Hiểu Hibernate
2. Cấu hình tích hợp Hibernate
3. Ánh xạ thực thể
4. Lập trình Hibernate

   * Truy vấn
   * Thao tác
5. Hiểu thêm ngôn ngữ HQL

---

## 1. Hiểu Hibernate

### Giới thiệu Hibernate

* Hibernate là framework hỗ trợ lập trình với CSDL trong các ứng dụng Java được ưa chuộng nhất hiện nay.
* Hibernate đóng vai trò là tầng trung gian giữa các đối tượng và CSDL để điều khiển việc lưu trữ trạng thái của đối tượng dựa trên ánh xạ.

### ORM/Hibernate

* Hibernate ánh xạ các lớp thực thể vào các bảng của CSDL quan hệ qua XML hoặc annotation.
* Sử dụng HQL để truy vấn đối tượng.
* Hỗ trợ truy vấn dễ dàng giữa các thực thể.
* Ổn định, tin cậy và giúp giảm công việc của lập trình viên.

### Hỗ trợ CSDL

Hibernate hỗ trợ nhiều CSDL phổ biến như:

* HSQL, DB2, MySQL, PostgreSQL, Oracle, SQL Server, Sybase, Informix...

### Các thành phần Hibernate

* `Configuration`
* `SessionFactory`
* `Session`
* `Transaction`
* `Query`
* `Criteria`

### Thư viện

* `sqljdbc4.jar` dùng cho SQL Server.
* Cần thêm JDBC driver tương ứng cho CSDL khác như Oracle, MySQL...

---

## 2. Cấu hình tích hợp Hibernate

### Các bean cần cấu hình

* `DriverManagerDataSource`
* `LocalSessionFactoryBean`
* `HibernateTransactionManager`

### Cấu hình `DataSource`

* Chứa thông tin kết nối CSDL (driver, server, user/password).
* Lưu ý bật TCP/IP cho SQL Server.

### Cấu hình `SessionFactory`

* Được tiêm vào Controller.
* Tiêm `DataSource` và khai báo package chứa entity class.

### Cấu hình `Transaction`

* Quản lý tự động transaction.
* Sử dụng `<tx:annotation>` và `@Transactional`.

---

## 3. Ánh xạ thực thể

### Mô hình thực thể

#### User

* id, fullname, password, photo, email

#### Major

* id, name, students

#### Student

* id, fullname, gender, birthday, mark, major

### Annotation chính

* `@Entity`, `@Table`, `@Column`, `@Id`
* Có thể bỏ `@Table` và `@Column` nếu trùng tên.
* Dùng `transient` nếu không muốn ánh xạ.
* Phải có getter/setter.

### Ánh xạ quan hệ

* `@OneToMany`
* `@ManyToOne`
* `@JoinColumn`
* `fetch = FetchType.LAZY` hoặc `EAGER`

---

## 4. Lập trình Hibernate

### Tiêm `SessionFactory`

* Qua `@Autowired` trong `@Controller`.

### Sử dụng `Session`

* `factory.getCurrentSession()` (được Spring quản lý).
* `factory.openSession()` (tự quản lý commit/rollback).

### Truy vấn thực thể

```java
Session session = factory.getCurrentSession();
String hql = "FROM Major";
Query query = session.createQuery(hql);
List<Major> list = query.list();
```

### Truy vấn có tham số

```java
query.setParameter("name", value);
```

### Truy vấn phân trang

```java
query.setFirstResult(startIndex);
query.setMaxResults(size);
```

### Truy vấn một số thuộc tính

* Dùng `SELECT` → kết quả là mảng object.

### Truy vấn một giá trị

```java
query.uniqueResult();
```

### Truy vấn thực thể theo ID

```java
session.get(Class, Id);
session.refresh(Object);
```

### Thao tác thực thể (CRUD)

* Mở session mới
* Bắt đầu transaction
* Thêm, cập nhật, xóa
* Commit hoặc rollback
* Đóng session

---

## 5. Ngôn ngữ HQL

### Đặc điểm

* Truy vấn đối tượng thay vì bảng.
* Không phụ thuộc CSDL cụ thể.
* Sử dụng thực thể và thuộc tính thay vì bảng/cột.

### Câu lệnh cơ bản

```sql
SELECT ... FROM ... WHERE ... GROUP BY ... HAVING ... ORDER BY ...
```

### FROM

```sql
FROM Course
FROM Course as c
FROM Course c
```

### WHERE

```sql
FROM Course WHERE name LIKE 'Nguyễn%'
```

### SELECT

```sql
SELECT name, schoolfee FROM Course
```

### ORDER BY

```sql
FROM Course ORDER BY startDate DESC, schoolfee
```

### Aggregate

```sql
SELECT AVG(unitPrice), MAX(discount) FROM Product GROUP BY category
```

---

### Toán tử

* Số học: `+`, `-`, `*`, `/`
* So sánh: `=`, `>=`, `<=`, `<>`, `!=`
* Logic: `AND`, `OR`, `NOT`
* Đặc biệt: `IN`, `BETWEEN`, `IS NULL`, `LIKE`, `IS EMPTY`

### Hàm HQL

#### Thống kê

* `avg()`, `sum()`, `min()`, `max()`, `count(*)`, ...

#### Chuỗi

* `concat()`, `substring()`, `trim()`, `lower()`, `upper()`, ...

#### Thời gian

* `current_date()`, `current_time()`, `current_timestamp()`, `year()`, ...

#### Khác

* `abs()`, `sqrt()`, `str()`, `cast(...)`

---

## Transaction (Giao dịch)

### Quy trình

* Bắt đầu → Nhiều thao tác dữ liệu → Commit / Rollback

### Tính chất ACID

1. **Atomicity**: tất cả hoặc không gì cả
2. **Consistency**: dữ liệu phải đúng
3. **Isolation**: giao dịch tách biệt
4. **Durability**: giữ dữ liệu dù mất điện/sự cố
