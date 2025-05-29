document.addEventListener('DOMContentLoaded', function() {
    // Xử lý chọn hình ảnh
    const mainImage = document.getElementById('main-product-image');
    const thumbnails = document.querySelectorAll('.product-thumbnail');

    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            // Cập nhật hình ảnh chính
            mainImage.src = this.src;

            // Cập nhật trạng thái active cho thumbnail
            thumbnails.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Xử lý chọn kích thước
    const sizeOptions = document.querySelectorAll('.size-option');
    const priceElement = document.querySelector('.sale-price');
    const originalPriceElement = document.querySelector('.original-price');

    sizeOptions.forEach(option => {
        option.addEventListener('click', function() {
            // Cập nhật trạng thái active cho kích thước
            sizeOptions.forEach(o => o.classList.remove('active'));
            this.classList.add('active');

            // Cập nhật giá dựa trên kích thước đã chọn
            const price = this.getAttribute('data-price');
            if (price) {
                priceElement.textContent = formatPrice(price);

                // Cập nhật giá gốc (nếu có)
                const originalPrice = parseInt(price) * 1.2; // Giả sử giá gốc cao hơn 20%
                if (originalPriceElement) {
                    originalPriceElement.textContent = formatPrice(originalPrice);
                }
            }
        });
    });

    // Xử lý số lượng
    const quantityInput = document.getElementById('quantity');
    const decreaseBtn = document.querySelector('.quantity-decrease');
    const increaseBtn = document.querySelector('.quantity-increase');

    decreaseBtn.addEventListener('click', function() {
        const currentValue = parseInt(quantityInput.value);
        if (currentValue > 1) {
            quantityInput.value = currentValue - 1;
        }
    });

    increaseBtn.addEventListener('click', function() {
        const currentValue = parseInt(quantityInput.value);
        quantityInput.value = currentValue + 1;
    });

    // Xử lý tabs
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');

            // Cập nhật trạng thái active cho tab
            tabButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // Hiển thị nội dung tab tương ứng
            tabContents.forEach(content => {
                if (content.getAttribute('data-tab') === tabId) {
                    content.style.display = 'block';
                } else {
                    content.style.display = 'none';
                }
            });
        });
    });

    // Xử lý thêm vào giỏ hàng
    const addToCartBtn = document.getElementById('add-to-cart-btn');

    addToCartBtn.addEventListener('click', function() {
        const productId = this.getAttribute('data-product-id');
        const quantity = parseInt(quantityInput.value);
        const selectedSize = document.querySelector('.size-option.active');

        if (!selectedSize) {
            alert('Vui lòng chọn kích thước sản phẩm');
            return;
        }

        const productVariantId = selectedSize.getAttribute('data-value');

        // Gửi yêu cầu AJAX để thêm sản phẩm vào giỏ hàng
        fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
            },
            body: JSON.stringify({
                productVariantId: productVariantId,
                quantity: quantity
            })
        })
        .then(response => {
            if (!response.ok) {
                console.error('Lỗi khi thêm vào giỏ hàng:', response.statusText);
                throw new Error('Lỗi khi thêm vào giỏ hàng');
            }
            return response.json();
        })
        .then(data => {
            // Hiển thị thông báo thành công
            alert('Đã thêm sản phẩm vào giỏ hàng!');

            // Cập nhật số lượng sản phẩm trong giỏ hàng (nếu có)
            const cartCountElement = document.querySelector('.badge');
            if (cartCountElement && data.itemCount) {
                cartCountElement.textContent = data.itemCount;
            }
        })
        .catch(error => {
            console.error('Lỗi:', error);
            alert('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng!');
        });
    });

    // Hàm định dạng giá
    function formatPrice(price) {
        return parseInt(price).toLocaleString('vi-VN') + 'đ';
    }

    // Khởi tạo ban đầu
    if (sizeOptions.length > 0) {
        sizeOptions[0].click(); // Chọn kích thước đầu tiên
    }

    if (tabButtons.length > 0) {
        tabButtons[0].click(); // Chọn tab đầu tiên
    }
});
