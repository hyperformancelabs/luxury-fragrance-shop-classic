document.addEventListener('DOMContentLoaded', function() {
    // Xử lý số lượng sản phẩm
    const quantityInputs = document.querySelectorAll('.quantity-input');
    const decreaseBtns = document.querySelectorAll('.quantity-decrease');
    const increaseBtns = document.querySelectorAll('.quantity-increase');
    const removeBtns = document.querySelectorAll('.remove-btn');
    const updateForms = document.querySelectorAll('.update-form');

    // Cập nhật tổng tiền
    function updateTotals() {
        let subtotal = 0;

        document.querySelectorAll('.cart-item').forEach(item => {
            const priceText = item.querySelector('.col-md-2:nth-child(2)').textContent.trim();
            const price = parseInt(priceText.replace(/[^\d]/g, ''));
            const quantity = parseInt(item.querySelector('.quantity-input').value);
            const itemTotal = price * quantity;

            // Cập nhật tổng tiền của mỗi sản phẩm
            item.querySelector('.cart-item-price').textContent = formatPrice(itemTotal);

            subtotal += itemTotal;
        });

        // Cập nhật tổng tiền
        document.getElementById('subtotal').textContent = formatPrice(subtotal);

        // Tính phí vận chuyển (giả sử miễn phí vận chuyển cho đơn hàng trên 1.000.000đ)
        const shipping = subtotal > 1000000 ? 0 : 30000;
        document.getElementById('shipping').textContent = shipping === 0 ? 'Miễn phí' : formatPrice(shipping);

        // Cập nhật tổng cộng
        const total = subtotal + shipping;
        document.getElementById('total').textContent = formatPrice(total);
    }

    // Xử lý nút giảm số lượng
    decreaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.quantity-input');
            const currentValue = parseInt(input.value);

            if (currentValue > 1) {
                input.value = currentValue - 1;
                updateTotals();

                // Tự động gửi form cập nhật
                const form = this.closest('.update-form');
                form.submit();
            }
        });
    });

    // Xử lý nút tăng số lượng
    increaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.quantity-input');
            const currentValue = parseInt(input.value);

            input.value = currentValue + 1;
            updateTotals();

            // Tự động gửi form cập nhật
            const form = this.closest('.update-form');
            form.submit();
        });
    });

    // Xử lý thay đổi trực tiếp trong input số lượng
    quantityInputs.forEach(input => {
        input.addEventListener('change', function() {
            let value = parseInt(this.value);

            // Đảm bảo giá trị hợp lệ
            if (isNaN(value) || value < 1) {
                value = 1;
                this.value = 1;
            }

            updateTotals();

            // Tự động gửi form cập nhật
            const form = this.closest('.update-form');
            form.submit();
        });
    });

    // Xử lý nút xóa sản phẩm
    removeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const cartItem = this.closest('.cart-item');

            // Hiệu ứng xóa
            cartItem.style.opacity = '0';
            cartItem.style.height = '0';
            cartItem.style.overflow = 'hidden';
            cartItem.style.transition = 'all 0.3s ease-out';

            // Xóa phần tử sau khi hoàn thành hiệu ứng
            setTimeout(() => {
                cartItem.remove();

                // Cập nhật tổng tiền
                updateTotals();

                // Kiểm tra nếu giỏ hàng trống
                const remainingItems = document.querySelectorAll('.cart-item');
                if (remainingItems.length === 0) {
                    showEmptyCart();
                }
            }, 300);
        });
    });

    // Hiển thị giỏ hàng trống
    function showEmptyCart() {
        const cartContainer = document.querySelector('.cart-container');
        const cartSummary = document.querySelector('.cart-summary');

        // Ẩn tóm tắt giỏ hàng
        if (cartSummary) {
            cartSummary.style.display = 'none';
        }

        // Hiển thị thông báo giỏ hàng trống
        const emptyCartHTML = `
            <div class="empty-cart">
                <div class="empty-cart-icon">
                    <i class="fas fa-shopping-cart"></i>
                </div>
                <h3 class="empty-cart-message">Giỏ hàng của bạn đang trống</h3>
                <a href="/shop" class="btn btn-dark">Tiếp tục mua sắm</a>
            </div>
        `;

        cartContainer.innerHTML = emptyCartHTML;
    }

    // Xử lý nút thanh toán
    const checkoutBtn = document.getElementById('checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function() {
            window.location.href = '/checkout';
        });
    }

    // Định dạng giá tiền
    function formatPrice(price) {
        return price.toLocaleString('vi-VN') + 'đ';
    }

    // Khởi tạo tổng tiền ban đầu
    updateTotals();
});
