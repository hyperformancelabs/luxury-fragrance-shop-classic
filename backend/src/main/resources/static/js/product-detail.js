document.addEventListener('DOMContentLoaded', function() {
    // Xử lý chọn hình ảnh
    const mainImage = document.getElementById('main-product-image');
    const thumbnails = document.querySelectorAll('.product-thumbnail');

    // Thêm fallback cho ảnh chính
    if (mainImage) {
        mainImage.onerror = function() {
            if (!this.src.includes('product-default.png')) {
                this.src = '/images/product-default.png';
                this.alt = 'Ảnh sản phẩm mặc định';
                this.classList.add('fallback-image');
            }
        };
    }

    // Thêm fallback cho thumbnails
    thumbnails.forEach(thumbnail => {
        thumbnail.onerror = function() {
            if (!this.src.includes('product-default.png')) {
                this.src = '/images/product-default.png';
                this.alt = 'Ảnh sản phẩm mặc định';
                this.classList.add('fallback-image');
            }
        };
    });

    // Xử lý chọn variant (dung tích)
    const variantOptions = document.querySelectorAll('.quickview-size');
    
    variantOptions.forEach(option => {
        option.addEventListener('click', function() {
            if (this.classList.contains('out-of-stock')) return;

            // Cập nhật trạng thái active
            variantOptions.forEach(o => o.classList.remove('active'));
            this.classList.add('active');

            // Cập nhật giá dựa trên variant đã chọn
            const variantId = this.getAttribute('data-value');
            const price = this.getAttribute('data-price');

            if (variantId) {
                const selectedVariantIdInput = document.getElementById('selectedVariantId');
                if (selectedVariantIdInput) {
                    selectedVariantIdInput.value = variantId;
                }
            }

            if (price) {
                const salePriceElement = document.querySelector('.sale-price');
                if (salePriceElement) {
                    salePriceElement.textContent = new Intl.NumberFormat('vi-VN').format(price) + 'đ';
                }
            }
        });
    });

    // Xử lý số lượng
    const quantityInput = document.getElementById('quantity');
    const decreaseBtn = document.querySelector('.quantity-decrease');
    const increaseBtn = document.querySelector('.quantity-increase');
    const selectedQuantityInput = document.getElementById('selectedQuantity');

    if (decreaseBtn) {
        decreaseBtn.addEventListener('click', function() {
            if (quantityInput) {
                const currentValue = parseInt(quantityInput.value);
                if (currentValue > 1) {
                    quantityInput.value = currentValue - 1;
                    if (selectedQuantityInput) {
                        selectedQuantityInput.value = quantityInput.value;
                    }
                }
            }
        });
    }

    if (increaseBtn) {
        increaseBtn.addEventListener('click', function() {
            if (quantityInput) {
                const currentValue = parseInt(quantityInput.value);
                quantityInput.value = currentValue + 1;
                if (selectedQuantityInput) {
                    selectedQuantityInput.value = quantityInput.value;
                }
            }
        });
    }

    if (quantityInput) {
        quantityInput.addEventListener('input', function() {
            if (selectedQuantityInput) {
                selectedQuantityInput.value = this.value;
            }
        });
    }

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

    // Khởi tạo ban đầu
    if (tabButtons.length > 0) {
        tabButtons[0].click(); // Chọn tab đầu tiên
    }

    // Khởi tạo thumbnail đầu tiên
    if (thumbnails.length > 0) {
        thumbnails[0].classList.add('active');
    }
});
