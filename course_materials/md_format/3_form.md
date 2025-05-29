# Lập trình web SPRING MVC - BÀI 3: LÀM VIỆC VỚI FORM

### Mục tiêu
1. Hiểu cơ chế buộc dữ liệu
2. Xây dựng form trong Spring
3. Sử dụng `@ModelAttribute`

---

## Giới thiệu Databinding?

### Databinding là gì?
- Databinding là sự kết nối dữ liệu của bean đặt trong model đến các điều khiển trên form.
- Khi thay đổi dữ liệu trong bean thì dữ liệu trên các điều khiển cũng thay đổi theo.
- Ràng buộc dữ liệu có thể là:
  - **Một chiều**:
    - **Chiều lên**: chuyển dữ liệu từ điều khiển vào bean.
    - **Chiều về**: hiển thị dữ liệu từ bean lên form.

---

## Buộc dữ liệu với các thẻ HTML?

- Có thể buộc dữ liệu từ thuộc tính của bean vào các điều khiển HTML bằng cách dùng biểu thức EL.
- Hạn chế:
  - Mã dài dòng, khó quản lý.
  - Khó khăn khi đổ dữ liệu vào các List Control như:
    - Combobox
    - Listbox
    - Radiobuttons
    - Checkboxes
  - Kiểm tra và thông báo lỗi phức tạp.

---

## Spring Form

- Spring MVC cung cấp thư viện thẻ để buộc dữ liệu từ bean vào điều khiển dễ dàng hơn.
- Sau khi khai báo thư viện thẻ ở đầu trang JSP, có thể:
  - Tạo form.
  - Ràng buộc dữ liệu từ bean trong model.

---

## Ưu điểm của Form Spring

- Cơ chế buộc dữ liệu tự động.
- Form rõ ràng, dễ hiểu.
- Thay đổi dữ liệu trong bean thì giao diện cũng thay đổi theo.
- Cấp dữ liệu vào List Control đơn giản.
- Kiểm lỗi và hiển thị lỗi dễ dàng.

---

## Tình huống buộc dữ liệu

- Người dùng truy cập `student/edit.htm`.
- Phương thức `edit()` tạo bean và đặt vào model.
- View chứa form buộc dữ liệu từ bean lên form.

---

## Lớp Bean

- Trường chứa dữ liệu.
- Constructor.
- Getter/Setter.

---

## Lớp `StudentController`

- Khi gọi `student/edit.htm`, phương thức `edit()` chạy.
- Tạo đối tượng `sv` và đưa vào model với tên `student`.
- View: `student.jsp`.

---

## Thiết kế form có ràng buộc dữ liệu

- View `student.jsp` chứa form ràng buộc thuộc tính bean vào điều khiển.

---

## Buộc dữ liệu lên form

- Thuộc tính của bean `student` được buộc với điều khiển form.
- Thay đổi dữ liệu trên form = thay đổi dữ liệu trong model.

---

## Buộc dữ liệu chiều lên

- Form submit đến action `update.htm`.
- Phương thức `update()` trong `StudentController` xử lý nút Update.
- Dữ liệu từ form gán vào thuộc tính của tham số `student`.
- `@ModelAttribute("student")` đưa `student` vào model để sử dụng lại.

---

## Các điều khiển form của Spring

| Điều khiển Spring        | Điều khiển HTML           |
|-------------------------|---------------------------|
| `<form:form>`           | `<form>`                  |
| `<form:input/>`         | `<input type="text"/>`    |
| `<form:textarea/>`      | `<textarea/>`             |
| `<form:checkbox/>`      | `<input type="checkbox"/>`|
| `<form:radiobutton/>`   | `<input type="radio"/>`   |
| `<form:hidden/>`        | `<input type="hidden"/>`  |
| `<form:password/>`      | `<input type="password"/>`|
| `<form:button/>`        | `<button/>`               |
| `<form:select/>`        | `<select/>`               |
| `<form:radiobuttons/>`  | Nhóm radio                |
| `<form:checkboxes/>`    | Nhóm checkbox             |

**Lưu ý:** List Control cần cấp dữ liệu từ `Collection`, `Array`, hoặc `Map`.

---

## Sử dụng List Control

- Điều khiển dùng để tạo các List Control như `Combobox`.

### Combobox
- Cần:
  - Trong `StudentController`: cung cấp dữ liệu dạng `Array`, `Collection`, hoặc `Map`.
  - Trong `student.jsp`: thay đổi điều khiển và đổ dữ liệu vào.

---

## Đổ dữ liệu vào Combobox

1. Trong `StudentController`:
   - Thêm phương thức `getMajors()`.
   - `@ModelAttribute("majors")` đưa kết quả phương thức vào model.
2. Trong view:
   - Đổi `<form:input path="major"/>` thành `<form:select path="major" items="${majors}"/>`.

---

## @ModelAttribute

- Dùng để bổ sung attribute vào model trong 2 trường hợp:

1. `@ModelAttribute(name)` ở **tham số**:
   - Thêm attribute tên `name`, giá trị là tham số.
   - Tương đương: `model.addAttribute(name, argument)`

2. `@ModelAttribute(name)` ở **phương thức**:
   - Thêm attribute tên `name`, giá trị là kết quả phương thức.
   - Tương đương: `model.addAttribute(name, method())`

- Dùng như attribute bình thường: buộc vào form, EL, đổ vào List Control.

---

## Đổ dữ liệu vào List Control

```jsp
<form:select path="property" items="{items}" itemValue="prop1" itemLabel="prop2"/>
````

* `items`: dữ liệu đưa vào ComboBox.
* `itemValue`/`itemLabel`: dùng khi `items` là `Collection<Bean>`.

Tương tự với:

```jsp
<form:radiobuttons path="property" items="{items}" itemValue="prop1" itemLabel="prop2"/>
<form:checkboxes path="property" items="{items}" itemValue="prop1" itemLabel="prop2"/>
```

---

## Các thuộc tính thường dùng trong `<form:tag>`

* `cssClass`: tương đương với `class` HTML.
* `disabled`
* `readonly`
* `cssErrorClass`: class định dạng thông báo lỗi.

### Ví dụ:

```jsp
<form:input path="id" readonly="true"/>
<form:input path="name" cssClass="form-control"/>
```

---

## Tổng kết nội dung bài học

* Tìm hiểu cơ chế buộc dữ liệu 2 chiều.
* Dùng `modelAttribute` để kết nối model với form.
* Dùng `path="property"` để buộc dữ liệu bean.
* Đổ dữ liệu vào List Control.
* Sử dụng `@ModelAttribute`.
* Khai thác các thuộc tính trong các điều khiển Spring.