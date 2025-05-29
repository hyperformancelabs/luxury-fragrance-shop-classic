# Lập trình web SPRING MVC - BÀI 2: CONTROLLER

## Mục tiêu:

* Sử dụng thành thạo `@RequestMapping`:

  * Ánh xạ nhiều action
  * Ánh xạ phân biệt POST | GET
  * Ánh xạ phân biệt tham số
* Nắm vững phương pháp nhận tham số:

  * Sử dụng `HttpServletRequest`
  * Sử dụng `@RequestParam`
  * Sử dụng `JavaBean`
  * Sử dụng `@PathVariable` để nhận dữ liệu từ URL
* Sử dụng `@CookieValue` để nhận cookie
* Hiểu rõ kết quả của phương thức action

---

## PHẦN 1

### `@RequestMapping` (1)

* Annotation `@RequestMapping` được sử dụng để ánh xạ một action đến một phương thức trong Controller.
* Khi người dùng truy cập `say-hello.htm`, phương thức `sayHello()` sẽ thực hiện.
* Một lớp `@Controller` có thể chứa nhiều phương thức action.

### `@RequestMapping` (2)

* `@RequestMapping("say-hello")` là cách viết rút gọn của `@RequestMapping(value="say-hello")`.
* Có thể đặt `@RequestMapping()` trên lớp Controller để ánh xạ chung cho nhiều action method.

### `@RequestMapping` (3)

* Hai cách ánh xạ trên là tương đương nhau.

---

## PHÂN BIỆT POST | GET

### POST | GET (1)

* Dùng để xử lý gửi nhận thông tin từ form người dùng.
* **GET**: dữ liệu gửi qua URL.
* **POST**: dữ liệu gửi qua form HTML và ẩn khỏi URL.

### POST | GET (2)

**Ví dụ GET**:

```url
https://shop.vnexpress.net/giay-da...?src=category-page-27&position=top#c:1_Đen
```

### POST | GET (3)

* POST bảo mật hơn GET vì dữ liệu không hiện trên URL.
* Dữ liệu POST được gửi qua form HTML: textbox, radio, etc.

### POST | GET (5 → 8)

**So sánh chi tiết giữa GET và POST**:

| GET                       | POST                      |
| ------------------------- | ------------------------- |
| Gửi dữ liệu qua URL       | Gửi qua HTTP header       |
| Dữ liệu hiển thị trên URL | Dữ liệu ẩn                |
| Dễ bị cache               | Không cache               |
| Có thể bookmark           | Không bookmark            |
| Không bảo mật             | Bảo mật hơn               |
| Gửi lại dễ dàng (F5)      | Có cảnh báo khi gửi lại   |
| Lưu lịch sử               | Không lưu lịch sử         |
| Không gửi nhị phân        | Gửi được dữ liệu nhị phân |
| Giới hạn 2048 ký tự       | Không giới hạn            |

### Trong Servlet

* `doGet()` xử lý GET
* `doPost()` xử lý POST
* Trong Spring MVC: phân biệt qua thuộc tính `method` của `@RequestMapping`.

---

## PHÂN BIỆT THAM SỐ

### (1)

* Cho phép phân biệt action dựa vào tham số truyền đến.
* VD: `say-hello.htm?mvc` mới thực hiện phương thức `sayHello()`.

### (2)

* `student.htm` gọi các phương thức tùy thuộc tham số:

  * `btnInsert` → `insert()`
  * `btnUpdate` → `update()`
  * `btnDelete` → `delete()`
  * `lnkEdit` → `edit()`
  * Không có → `index()`

---

## PHẦN 2: XỬ LÝ THAM SỐ NGƯỜI DÙNG

### Ví dụ:

```html
<a href="say-hello.htm?mark=5&name=Phương">Hello</a>
```

```html
<form action="say-hello.htm">
  <input name="mark">
  <input name="name">
  <button>Hello</button>
</form>
```

---

### CÁC CÁCH NHẬN THAM SỐ

1. **HttpServletRequest**
2. **@RequestParam**
3. **JavaBean**
4. **@PathVariable**

---

### SỬ DỤNG `HttpServletRequest`

* Thêm đối số `HttpServletRequest` vào phương thức để lấy tham số.

### SỬ DỤNG `@RequestParam`

* Chuyển đổi tự động kiểu dữ liệu.
* Cú pháp đầy đủ:

  ```java
  @RequestParam(value="tuoi", defaultValue="20", required=false) Integer age
  ```

---

### SỬ DỤNG `JavaBean`

* Yêu cầu lớp phải:

  * `public`
  * Có constructor không tham số
  * Có getter/setter
* Spring tự động gán tham số cho các thuộc tính trùng tên.

---

### SỬ DỤNG `@PathVariable`

* Nhận dữ liệu trực tiếp từ URL.
* Ví dụ:

  ```html
  <a href="student/Nguyễn Văn Tèo.htm?lnkEdit">Sửa</a>
  ```

---

## NHẬN GIÁ TRỊ COOKIE

### So sánh Cookie vs Session

| Cookie             | Session                       |
| ------------------ | ----------------------------- |
| Lưu ở client       | Lưu ở server                  |
| Có thể bị sửa đổi  | Không dễ bị sửa đổi           |
| Có thể tồn tại lâu | Kết thúc khi đóng trình duyệt |

### Trong Spring MVC

* Dùng `@CookieValue` để nhận cookie:

```java
@CookieValue(value="userid", defaultValue="poly", required=false) String id
```

---

## ĐẦU RA CỦA PHƯƠNG THỨC ACTION

1. Trả về **View**:

   ```java
   return "home";
   ```
2. Trả về **nội dung trực tiếp**:

   ```java
   @ResponseBody
   return "Hello";
   ```
3. Chuyển hướng action khác:

   ```java
   return "redirect:/login";
   ```

---

## TỔNG KẾT NỘI DUNG BÀI HỌC

✅ Sử dụng `@RequestMapping`
✅ Ánh xạ nhiều action
✅ Phân biệt POST | GET
✅ Phân biệt tham số
✅ Nhận tham số qua:

* `HttpServletRequest`
* `@RequestParam`
* `JavaBean`
* `@PathVariable`
  ✅ Nhận Cookie bằng `@CookieValue`
  ✅ Hiểu rõ return của phương thức action
