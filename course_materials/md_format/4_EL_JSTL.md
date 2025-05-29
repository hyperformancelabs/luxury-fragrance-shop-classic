# Lập trình web SPRING MVC - BÀI 4: EL và JSTL

### 🎯 Mục tiêu

Nắm vững kỹ thuật lập trình giao diện trong JSP:

1. **Expression Language (EL)**
2. **Java Standard Tag Library (JSTL)**

---

## 1. Expression Language (EL)

### Tổng quan

* EL giúp viết mã ngắn gọn khi làm việc với attribute trong các scope: `page`, `request`, `session`, `application`.
* Xuất hiện từ JSP 2.0.
* Cho phép truy xuất:

  * Attribute trong scope
  * Thuộc tính của bean
  * Phần tử trong Collection, Map
  * Parameter, Cookie, Header

### Cú pháp:

```jsp
${<biểu thức>}
```

Ví dụ:

```jsp
${salary * 2}
${sessionScope['salary']}
${param.salary}
```

### Ví dụ EL:

```java
@RequestMapping("/el/demo1")
public String sayHello(ModelMap model, HttpSession session){
    session.setAttribute("name", "Tèo");
    model.addAttribute("salary", 2000);
    return "el/demo";
}
```

HTML:

```html
<li>name: ${name}</li>
<li>salary: ${salary}</li>
<li>requestScope.name: ${requestScope.name}</li>
<li>sessionScope.name: ${sessionScope.name}</li>
```

---

## 2. SCOPE API

### 4 Scope chính:

* `pageScope`
* `requestScope`
* `sessionScope`
* `applicationScope`

### Phương thức:

```java
setAttribute(name, value)
getAttribute(name)
removeAttribute(name)
getAttributeNames()
```

---

## 3. Truy xuất thuộc tính bean

* Bean phải là `public`, có constructor mặc định và getter/setter.
* Cú pháp:

```jsp
${bean.property}
```

Ví dụ:

```java
@RequestMapping("/el/demo2")
public String demo2(ModelMap model) {
    Student student = new Student("Phương", 10.0, "APP");
    model.addAttribute("student", student);
    return "el/demo2";
}
```

HTML:

```html
<li>name: ${student.name}</li>
<li>mark: ${student.mark}</li>
```

---

## 4. Truy xuất Collection, Array

```java
@RequestMapping("/el/demo3")
public String demo3(ModelMap model) {
    List<String> list = new ArrayList<>();
    list.add("Phương");
    list.add("Cường");
    model.addAttribute("items", list);
    return "el/demo3";
}
```

HTML:

```html
<li>${items[0]}</li>
<li>${items[1]}</li>
```

---

## 5. Truy xuất Map

```java
Map<String, Object> map = new HashMap<>();
map.put("name", "Phương");
map.put("mark", 9.5);
model.addAttribute("student", map);
```

Truy xuất:

```jsp
${student['name']}
${student.mark}
```

---

## 6. Truy xuất Parameter, Cookie

### Parameter

```jsp
${param['salary']}
${param.salary}
```

### Cookie

```jsp
${cookie['userid'].value}
${cookie.userid.value}
```

---

## 7. Java Standard Tag Library (JSTL)

### Thư viện cần:

* `jstl-api.jar`
* `jstl-impl.jar`

### Các bộ thẻ:

* Core
* Format
* Xml
* Sql
* Function

### Khai báo taglib:

```jsp
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
```

---

## 8. Thẻ Core

### `<c:if>`

```jsp
<c:if test="${condition}">
    Nội dung
</c:if>
```

---

### `<c:choose>`

```jsp
<c:choose>
    <c:when test="${condition1}">...</c:when>
    <c:otherwise>...</c:otherwise>
</c:choose>
```

---

### `<c:forEach>`

```jsp
<c:forEach var="item" items="${items}" begin="0" end="10" varStatus="status">
    Nội dung
</c:forEach>
```

---

### `<c:set>` & `<c:remove>`

```jsp
<c:set var="name" value="value" scope="session" />
<c:remove var="name" scope="session" />
```

---

## 9. Thẻ Format

### Định dạng số:

```jsp
<fmt:formatNumber value="1000000" type="currency" />
<fmt:formatNumber value="0.51" type="percent" />
```

### Định dạng thời gian:

```jsp
<fmt:formatDate value="${date}" pattern="dd-MM-yyyy" />
```

---

## 10. Thư viện Hàm

### Ví dụ:

```jsp
${fn:substring(0, 100, description)}
<c:if test="${fn:startsWith('Nguyễn ', fullname)}">...</c:if>
```

---

## ✅ Tổng kết

### EL:

* Truy xuất attribute trong các scope
* Truy xuất thuộc tính bean
* Truy xuất phần tử mảng, tập hợp, map
* Truy xuất parameter, cookie

### JSTL:

* Core
* Format
* Function
