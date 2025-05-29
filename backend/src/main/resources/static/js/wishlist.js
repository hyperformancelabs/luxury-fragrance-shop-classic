document.addEventListener('DOMContentLoaded', function() {
    // Xử lý số lượng sản phẩm
    const quantityInputs = document.querySelectorAll('.quantity-input');
    const decreaseBtns = document.querySelectorAll('.quantity-decrease');
    const increaseBtns = document.querySelectorAll('.quantity-increase');
    const removeBtns = document.querySelectorAll('.remove-btn');
    const sizeOptions = document.querySelectorAll('.size-option');

    // Xử lý nút giảm số lượng
    decreaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.quantity-input');
            const currentValue = parseInt(input.value);

            if (currentValue > 1) {
                input.value = currentValue - 1;
            }
        });
    });

    // Xử lý nút tăng số lượng
    increaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.quantity-input');
            const currentValue = parseInt(input.value);

            input.value = currentValue + 1;
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
        });
    });

    // Xử lý chọn kích thước
    sizeOptions.forEach(option => {
        option.addEventListener('click', function() {
            // Lấy tất cả các kích thước trong cùng một sản phẩm
            const productId = this.closest('.wishlist-item').getAttribute('data-product-id');
            const sizeOptionsInProduct = document.querySelectorAll(`.wishlist-item[data-product-id="${productId}"] .size-option`);

            // Xóa trạng thái active cho tất cả các kích thước
            sizeOptionsInProduct.forEach(opt => opt.classList.remove('active'));

            // Thêm trạng thái active cho kích thước được chọn
            this.classList.add('active');

            // Cập nhật giá hiển thị nếu cần
            const price = this.getAttribute('data-price');
            if (price) {
                const priceElement = document.querySelector(`.wishlist-item[data-product-id="${productId}"] .wishlist-item-price`);
                if (priceElement) {
                    priceElement.textContent = formatPrice(price);
                }
            }
        });
    });

    // Xử lý nút xóa sản phẩm
    removeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const wishlistItem = this.closest('.wishlist-item');

            // Hiệu ứng xóa
            wishlistItem.style.opacity = '0';
            wishlistItem.style.height = '0';
            wishlistItem.style.overflow = 'hidden';
            wishlistItem.style.transition = 'all 0.3s ease-out';

            // Xóa phần tử sau khi hoàn thành hiệu ứng
            setTimeout(() => {
                wishlistItem.remove();

                // Kiểm tra nếu danh sách yêu thích trống
                const remainingItems = document.querySelectorAll('.wishlist-item');
                if (remainingItems.length === 0) {
                    showEmptyWishlist();
                }

                // Cập nhật số lượng sản phẩm
                updateItemCount();
            }, 300);
        });
    });

    // Xử lý nút xóa tất cả
    const clearAllBtn = document.getElementById('clear-all-btn');
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', function() {
            const wishlistItems = document.querySelectorAll('.wishlist-item');

            // Xóa tất cả các sản phẩm
            wishlistItems.forEach(item => {
                item.style.opacity = '0';
                item.style.height = '0';
                item.style.overflow = 'hidden';
                item.style.transition = 'all 0.3s ease-out';
            });

            // Hiển thị danh sách trống sau khi hoàn thành hiệu ứng
            setTimeout(() => {
                showEmptyWishlist();
            }, 300);
        });
    }

//    const addAllToCartBtn = document.getElementById('add-all-to-cart-btn');
//    if (addAllToCartBtn) {
//        addAllToCartBtn.addEventListener('click', function() {
//            const wishlistItems = document.querySelectorAll('.wishlist-item');
//
//            wishlistItems.forEach(item => {
//                const productId = item.getAttribute('data-product-id');
//                const quantity = item.querySelector('.quantity-input').value;
//                const selectedSize = item.querySelector('.size-option.active');
//
//                if (selectedSize) {
//                    const size = selectedSize.getAttribute('data-value');
//                    console.log(`Thêm sản phẩm ${productId} vào giỏ hàng, kích thước: ${size}, số lượng: ${quantity}`);
//                } else {
//                    console.log(`Vui lòng chọn kích thước cho sản phẩm ${productId}`);
//                }
//            });
//
//            // Hiển thị thông báo thành công
//            alert('Đã thêm tất cả sản phẩm vào giỏ hàng!');
//        });
//    }

    // Hiển thị danh sách yêu thích trống
    function showEmptyWishlist() {
        const wishlistContainer = document.querySelector('.wishlist-container');
        const wishlistFooter = document.querySelector('.wishlist-footer');

        // Ẩn footer
        if (wishlistFooter) {
            wishlistFooter.style.display = 'none';
        }

        // Hiển thị thông báo danh sách trống
        const emptyWishlistHTML = `
            <div class="empty-wishlist">
                <div class="empty-wishlist-icon">
                    <i class="fas fa-heart"></i>
                </div>
                <h3 class="empty-wishlist-message">Danh sách yêu thích trống</h3>
                <p class="text-muted mb-4">Bạn chưa thêm sản phẩm nào vào danh sách yêu thích</p>
                <a href="/shop" class="btn btn-dark">Tiếp tục mua sắm</a>
            </div>
        `;

        wishlistContainer.innerHTML = emptyWishlistHTML;
    }

    // Cập nhật số lượng sản phẩm
    function updateItemCount() {
        const itemCount = document.querySelectorAll('.wishlist-item').length;
        const itemCountElement = document.querySelector('.wishlist-count');

        if (itemCountElement) {
            itemCountElement.textContent = itemCount + ' sản phẩm';
        }
    }

    // Định dạng giá tiền
    function formatPrice(price) {
        return parseInt(price).toLocaleString('vi-VN') + 'đ';
    }

    // Khởi tạo kích thước mặc định cho mỗi sản phẩm
    document.querySelectorAll('.wishlist-item').forEach(item => {
        const firstSizeOption = item.querySelector('.size-option');
        if (firstSizeOption) {
            firstSizeOption.click();
        }
    });
});