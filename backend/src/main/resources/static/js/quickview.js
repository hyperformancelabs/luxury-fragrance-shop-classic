console.log('QuickView script loaded');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing QuickView');

    // Thêm CSS cho QuickView
    const quickviewCssLink = document.createElement('link');
    quickviewCssLink.rel = 'stylesheet';
    quickviewCssLink.href = '/css/quickview-custom.css';
    document.head.appendChild(quickviewCssLink);
    console.log('QuickView CSS added to head');
    // Tạo HTML cho QuickView
    const quickviewHTML = `
        <div id="quickview-overlay" class="quickview-overlay">
            <div class="quickview-container">
                <button id="quickview-close" class="quickview-close">&times;</button>
                <div class="quickview-content">
                    <div class="quickview-images">
                        <img id="quickview-main-image" class="quickview-main-image" src="" alt="Product Image">
                        <div id="quickview-thumbnails" class="quickview-thumbnails">
                            <!-- Thumbnails will be added dynamically -->
                        </div>
                    </div>
                    <div class="quickview-details">
                        <div class="quickview-brand" id="quickview-brand"></div>
                        <h2 class="quickview-title" id="quickview-title"></h2>
                        <div class="quickview-rating">
                            <div class="quickview-stars">
                                <i class="fas fa-star"></i>
                                <i class="fas fa-star"></i>
                                <i class="fas fa-star"></i>
                                <i class="fas fa-star"></i>
                                <i class="fas fa-star-half-alt"></i>
                            </div>
                            <span id="quickview-rating-text">4.5 (125 đánh giá)</span>
                        </div>
                        <div class="quickview-price">
                            <span class="quickview-original-price" id="quickview-original-price"></span>
                            <span class="quickview-sale-price" id="quickview-sale-price"></span>
                        </div>
                        <div class="quickview-description" id="quickview-description"></div>
                        <div class="quickview-sizes">
                            <label class="quickview-size-label">Dung tích:</label>
                            <div class="quickview-size-options" id="quickview-size-options">
                                <!-- Size options will be added dynamically -->
                            </div>
                        </div>
                        <div class="quickview-quantity">
                            <label class="quickview-quantity-label">Số lượng:</label>
                            <div class="quickview-quantity-selector">
                                <button class="quickview-quantity-btn quickview-quantity-decrease">-</button>
                                <input type="number" id="quickview-quantity" class="quickview-quantity-input" value="1" min="1">
                                <button class="quickview-quantity-btn quickview-quantity-increase">+</button>
                            </div>
                        </div>
                        <div class="quickview-actions">
                            <button id="quickview-add-to-cart" class="quickview-add-to-cart">
                                <i class="fas fa-shopping-cart me-2"></i>Thêm vào giỏ hàng
                            </button>
                            <button id="quickview-wishlist" class="quickview-wishlist">
                                <i class="far fa-heart"></i>
                            </button>
                        </div>
                        <div class="quickview-meta">
                            <div class="quickview-meta-item">
                                <span>Danh mục:</span> <span id="quickview-category"></span>
                            </div>
                            <div class="quickview-meta-item">
                                <span>Thương hiệu:</span> <span id="quickview-brand-meta"></span>
                            </div>
                            <div class="quickview-meta-item">
                                <span>Mã sản phẩm:</span> <span id="quickview-sku"></span>
                            </div>
                        </div>
                        <a id="quickview-view-details" class="btn btn-link ps-0">Xem chi tiết sản phẩm</a>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Thêm QuickView vào body
    const quickviewDiv = document.createElement('div');
    quickviewDiv.innerHTML = quickviewHTML;
    document.body.appendChild(quickviewDiv);

    // Lấy các phần tử DOM
    const quickviewOverlay = document.getElementById('quickview-overlay');
    const quickviewClose = document.getElementById('quickview-close');
    const quickviewMainImage = document.getElementById('quickview-main-image');
    const quickviewThumbnails = document.getElementById('quickview-thumbnails');
    const quickviewBrand = document.getElementById('quickview-brand');
    const quickviewTitle = document.getElementById('quickview-title');
    const quickviewRatingText = document.getElementById('quickview-rating-text');
    const quickviewOriginalPrice = document.getElementById('quickview-original-price');
    const quickviewSalePrice = document.getElementById('quickview-sale-price');
    const quickviewDescription = document.getElementById('quickview-description');
    const quickviewSizeOptions = document.getElementById('quickview-size-options');
    const quickviewQuantity = document.getElementById('quickview-quantity');
    const quickviewQuantityDecrease = document.querySelector('.quickview-quantity-decrease');
    const quickviewQuantityIncrease = document.querySelector('.quickview-quantity-increase');
    const quickviewAddToCart = document.getElementById('quickview-add-to-cart');
    const quickviewWishlist = document.getElementById('quickview-wishlist');
    const quickviewCategory = document.getElementById('quickview-category');
    const quickviewBrandMeta = document.getElementById('quickview-brand-meta');
    const quickviewSku = document.getElementById('quickview-sku');
    const quickviewViewDetails = document.getElementById('quickview-view-details');

    // Đóng QuickView khi nhấp vào nút đóng hoặc bên ngoài
    quickviewClose.addEventListener('click', closeQuickView);
    quickviewOverlay.addEventListener('click', function(e) {
        if (e.target === quickviewOverlay) {
            closeQuickView();
        }
    });

    // Đóng QuickView khi nhấn ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && quickviewOverlay.style.display === 'flex') {
            closeQuickView();
        }
    });

    // Xử lý số lượng
    quickviewQuantityDecrease.addEventListener('click', function() {
        const currentValue = parseInt(quickviewQuantity.value);
        if (currentValue > 1) {
            quickviewQuantity.value = currentValue - 1;
        }
    });

    quickviewQuantityIncrease.addEventListener('click', function() {
        const currentValue = parseInt(quickviewQuantity.value);
        quickviewQuantity.value = currentValue + 1;
    });

    // Xử lý thêm vào giỏ hàng
    quickviewAddToCart.addEventListener('click', function() {
        const productId = this.getAttribute('data-product-id');
        const quantity = parseInt(quickviewQuantity.value);
        const selectedSize = document.querySelector('.quickview-size.active');

        if (!selectedSize) {
            alert('Vui lòng chọn dung tích sản phẩm');
            return;
        }

        // Kiểm tra nếu sản phẩm hết hàng
        if (selectedSize.classList.contains('out-of-stock')) {
            Swal.fire({
                icon: 'error',
                title: 'Hết hàng',
                text: 'Dung tích bạn chọn hiện đã hết hàng. Vui lòng chọn dung tích khác.',
                confirmButtonText: 'Đã hiểu',
                confirmButtonColor: '#d33'
            });
            return;
        }

        // Lấy productVariantId từ selectedSize
        const variantId = selectedSize.getAttribute('data-value');
        const volume = selectedSize.getAttribute('data-volume');

        // Tạo form để submit
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/cart/add';

        // Thêm input cho productVariantId
        const productVariantIdInput = document.createElement('input');
        productVariantIdInput.type = 'hidden';
        productVariantIdInput.name = 'productVariantId';
        productVariantIdInput.value = variantId;
        form.appendChild(productVariantIdInput);

        // Thêm input cho quantity
        const quantityInput = document.createElement('input');
        quantityInput.type = 'hidden';
        quantityInput.name = 'quantity';
        quantityInput.value = quantity;
        form.appendChild(quantityInput);

        // Thêm input cho volume
        if (volume) {
            const volumeInput = document.createElement('input');
            volumeInput.type = 'hidden';
            volumeInput.name = 'volume';
            volumeInput.value = volume;
            form.appendChild(volumeInput);
        }

        // Thêm CSRF token nếu cần
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken.content;
            form.appendChild(csrfInput);
        }

        // Hiển thị trạng thái đang tải
        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        this.disabled = true;

        // Thêm form vào body và submit
        document.body.appendChild(form);
        form.submit();

        // Đóng QuickView
        closeQuickView();
    });

    // Xử lý thêm vào yêu thích
    quickviewWishlist.addEventListener('click', function () {
        const isLoggedIn = document.querySelector('meta[name="isLoggedIn"]')?.content === 'true';

        if (!isLoggedIn) {
            Swal.fire({
                icon: 'info',
                title: 'Vui lòng đăng nhập',
                text: 'Bạn cần đăng nhập để thêm sản phẩm vào mục yêu thích.',
                confirmButtonText: 'Đăng nhập',
                showCancelButton: true,
                cancelButtonText: 'Hủy',
                reverseButtons: true
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = "/auth/login";
                }
            });
            return;
        }

        const productId = quickviewAddToCart.getAttribute('data-product-id');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

        // Tạo form ẩn
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/wishlist/add';

        // Input productId
        const inputProductId = document.createElement('input');
        inputProductId.type = 'hidden';
        inputProductId.name = 'productId';
        inputProductId.value = productId;
        form.appendChild(inputProductId);

        // CSRF token
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);
        }

        // Append và submit
        document.body.appendChild(form);
        form.submit();
    });

    // Hàm mở QuickView
    function openQuickView(productId, productData) {
        console.log('openQuickView called with productId:', productId);

        // Đảm bảo đóng quickview hiện tại trước khi mở cái mới
        quickviewOverlay.style.display = 'none';
        document.body.style.overflow = '';

        // Xóa dữ liệu cũ
        quickviewThumbnails.innerHTML = '';
        quickviewSizeOptions.innerHTML = '';
        quickviewMainImage.src = '/images/placeholder.jpg';
        quickviewBrand.textContent = '';
        quickviewTitle.textContent = '';
        quickviewDescription.textContent = '';
        quickviewOriginalPrice.textContent = '';
        quickviewSalePrice.textContent = '';
        quickviewCategory.textContent = '';
        quickviewBrandMeta.textContent = '';
        quickviewSku.textContent = '';

        // Nếu đã có dữ liệu sản phẩm được truyền vào, sử dụng nó ngay
        if (productData) {
            console.log('Sử dụng dữ liệu sản phẩm được truyền vào cho sản phẩm ID:', productId);
            updateQuickViewContent(productData);
            quickviewOverlay.style.display = 'flex';
            document.body.style.overflow = 'hidden'; // Ngăn cuộn trang
            return;
        }

        // Nếu không có dữ liệu sản phẩm, lấy từ phần tử HTML
        console.log('Không có dữ liệu sản phẩm được truyền vào, lấy từ HTML cho sản phẩm ID:', productId);
        fetchProductData(productId).then(product => {
            // Cập nhật thông tin sản phẩm
            updateQuickViewContent(product);

            // Hiển thị QuickView
            quickviewOverlay.style.display = 'flex';
            document.body.style.overflow = 'hidden'; // Ngăn cuộn trang
        }).catch(error => {
            console.error('Error in openQuickView:', error);
        });
    }

    // Hàm đóng QuickView
    function closeQuickView() {
        quickviewOverlay.style.display = 'none';
        document.body.style.overflow = ''; // Cho phép cuộn trang
    }

    // Hàm lấy dữ liệu sản phẩm từ phần tử HTML
    function fetchProductData(productId) {
        return new Promise((resolve, reject) => {
            try {
                // Tìm phần tử sản phẩm dựa vào productId
                const productCard = document.querySelector(`.product-card[data-product-id="${productId}"]`);

                if (!productCard) {
                    throw new Error('Không tìm thấy sản phẩm');
                }

                // Lấy thông tin cơ bản của sản phẩm
                const productName = productCard.querySelector('.product-name, .card-title')?.textContent || 'Sản phẩm';
                const brandName = productCard.querySelector('.product-brand, .brand-name')?.textContent || productCard.getAttribute('data-brand') || 'Thương hiệu';
                const imageUrl = productCard.querySelector('.product-image, .product-img')?.src || '/images/placeholder.jpg';
                const description = productCard.getAttribute('data-description') || 'Mô tả sản phẩm';

                // Lấy thông tin giá
                const originalPrice = parseFloat(productCard.getAttribute('data-original-price') || '0');
                const salePrice = parseFloat(productCard.getAttribute('data-sale-price') || originalPrice.toString());

                // Lấy thông tin biến thể sản phẩm
                const variantsData = [];

                // Kiểm tra nếu có dữ liệu biến thể được lưu trữ trong data-variants
                const variantsJson = productCard.getAttribute('data-variants');
                if (variantsJson) {
                    try {
                        const parsedVariants = JSON.parse(variantsJson);
                        parsedVariants.forEach(variant => {
                            variantsData.push({
                                value: variant.id.toString(),
                                label: `${variant.volume}ml`,
                                price: variant.price,
                                discountPrice: variant.discountPrice,
                                volume: variant.volume,
                                quantityInStock: variant.stock
                            });
                        });
                    } catch (e) {
                        console.error('Lỗi khi phân tích dữ liệu biến thể:', e);
                    }
                }

                // Nếu không có dữ liệu biến thể, tìm các phần tử .variant-item
                if (variantsData.length === 0) {
                    const variantItems = productCard.querySelectorAll('.variant-item');
                    variantItems.forEach((item, index) => {
                        const variantId = item.getAttribute('data-variant-id') || (index + 1).toString();
                        const volume = parseInt(item.getAttribute('data-volume') || '0');
                        const price = parseFloat(item.getAttribute('data-price') || '0');
                        const discountPrice = parseFloat(item.getAttribute('data-discount-price') || '0');
                        const stock = parseInt(item.getAttribute('data-stock') || '10');

                        variantsData.push({
                            value: variantId,
                            label: `${volume}ml`,
                            price: price,
                            discountPrice: discountPrice > 0 ? discountPrice : null,
                            volume: volume,
                            quantityInStock: stock
                        });
                    });
                }

                // Nếu vẫn không có dữ liệu biến thể, tạo dữ liệu mẫu
                if (variantsData.length === 0) {
                    // Tạo các biến thể mẫu với giá dựa trên giá gốc
                    variantsData.push(
                        { value: '1', label: '30ml', price: originalPrice * 0.5, volume: 30, quantityInStock: 10 },
                        { value: '2', label: '50ml', price: originalPrice, volume: 50, quantityInStock: 8 },
                        { value: '3', label: '100ml', price: originalPrice * 1.5, volume: 100, quantityInStock: 5 }
                    );
                }

                // Tạo đối tượng sản phẩm
                const product = {
                    id: productId,
                    name: productName,
                    brand: brandName,
                    category: productCard.getAttribute('data-category') || 'Nước hoa',
                    sku: 'SKU-' + productId,
                    description: description,
                    rating: parseFloat(productCard.getAttribute('data-rating') || '4.5'),
                    reviewCount: parseInt(productCard.getAttribute('data-review-count') || '0'),
                    originalPrice: originalPrice,
                    salePrice: salePrice,
                    images: [imageUrl],
                    sizes: variantsData
                };

                // Thêm nhiều hình ảnh nếu có
                const additionalImages = productCard.getAttribute('data-additional-images');
                if (additionalImages) {
                    try {
                        const images = JSON.parse(additionalImages);
                        if (Array.isArray(images) && images.length > 0) {
                            product.images = images;
                        }
                    } catch (e) {
                        console.error('Lỗi khi phân tích dữ liệu hình ảnh bổ sung:', e);
                    }
                }

                resolve(product);
            } catch (error) {
                console.error('Lỗi khi lấy dữ liệu sản phẩm:', error);
                // Nếu có lỗi, sử dụng dữ liệu mẫu
                const product = {
                    id: productId,
                    name: 'Sản phẩm mẫu',
                    brand: 'Thương hiệu',
                    category: 'Nước hoa',
                    sku: 'SKU-' + productId,
                    description: 'Mô tả sản phẩm mẫu.',
                    rating: 4.5,
                    reviewCount: 0,
                    originalPrice: 1000000,
                    salePrice: 800000,
                    images: ['/images/placeholder.jpg'],
                    sizes: [
                        { value: '1', label: '30ml', price: 500000, volume: 30, quantityInStock: 10 },
                        { value: '2', label: '50ml', price: 800000, volume: 50, quantityInStock: 8 },
                        { value: '3', label: '100ml', price: 1200000, volume: 100, quantityInStock: 5 }
                    ]
                };
                resolve(product);
            }
        });

    }

    // Hàm cập nhật nội dung QuickView
    function updateQuickViewContent(product) {
        console.log('Cập nhật nội dung QuickView với dữ liệu:', product);

        // Xóa nội dung cũ trước khi cập nhật
        quickviewThumbnails.innerHTML = '';
        quickviewSizeOptions.innerHTML = '';

        // Cập nhật hình ảnh
        if (product.images && product.images.length > 0) {
            quickviewMainImage.src = product.images[0];
            product.images.forEach((image, index) => {
                const thumbnail = document.createElement('img');
                thumbnail.src = image;
                thumbnail.alt = 'Thumbnail';
                thumbnail.className = index === 0 ? 'quickview-thumbnail active' : 'quickview-thumbnail';
                thumbnail.addEventListener('click', function() {
                    quickviewMainImage.src = image;
                    document.querySelectorAll('.quickview-thumbnail').forEach(thumb => {
                        thumb.classList.remove('active');
                    });
                    this.classList.add('active');
                });
                quickviewThumbnails.appendChild(thumbnail);
            });
        } else {
            quickviewMainImage.src = '/images/placeholder.jpg';
        }

        // Cập nhật thông tin sản phẩm
        quickviewBrand.textContent = product.brand || 'Thương hiệu';
        quickviewTitle.textContent = product.name || 'Sản phẩm';
        quickviewRatingText.textContent = `${product.rating || 0} (${product.reviewCount || 0} đánh giá)`;

        // Cập nhật giá
        if (product.originalPrice && product.salePrice && product.originalPrice > product.salePrice) {
            quickviewOriginalPrice.textContent = formatPrice(product.originalPrice);
            quickviewSalePrice.textContent = formatPrice(product.salePrice);
            quickviewOriginalPrice.style.display = 'inline-block';
            quickviewOriginalPrice.style.textDecoration = 'line-through';
        } else if (product.originalPrice) {
            quickviewSalePrice.textContent = formatPrice(product.originalPrice);
            quickviewOriginalPrice.style.display = 'none';
        } else {
            quickviewSalePrice.textContent = '0đ';
            quickviewOriginalPrice.style.display = 'none';
        }

        // Cập nhật mô tả
        quickviewDescription.textContent = product.description || 'Không có mô tả';

        // Cập nhật kích thước
        quickviewSizeOptions.innerHTML = '';
        product.sizes.forEach((size, index) => {
            const sizeOption = document.createElement('div');
            sizeOption.className = index === 0 ? 'quickview-size active' : 'quickview-size';

            // Lưu trữ thông tin biến thể trong các thuộc tính data
            sizeOption.setAttribute('data-value', size.value); // ID của biến thể
            sizeOption.setAttribute('data-price', size.price);
            sizeOption.setAttribute('data-volume', size.volume);

            if (size.discountPrice) {
                sizeOption.setAttribute('data-discount-price', size.discountPrice);
            }

            // Thêm thông tin về số lượng tồn kho
            if (size.quantityInStock !== undefined) {
                sizeOption.setAttribute('data-stock', size.quantityInStock);

                // Hiển thị trạng thái hàng tồn kho
                if (size.quantityInStock <= 0) {
                    sizeOption.classList.add('out-of-stock');
                    sizeOption.innerHTML = `${size.label} <span class="stock-status">(Hết hàng)</span>`;
                } else if (size.quantityInStock <= 5) {
                    // Sắp hết hàng
                    sizeOption.innerHTML = `${size.label} <span class="stock-status">(Còn ${size.quantityInStock})</span>`;
                    sizeOption.classList.add('low-stock');
                } else {
                    sizeOption.innerHTML = `${size.label} <span class="stock-status">(Còn hàng)</span>`;
                }
            } else {
                // Nếu không có thông tin về số lượng tồn kho
                sizeOption.textContent = size.label;
            }

            sizeOption.addEventListener('click', function() {
                // Không cho phép chọn nếu hết hàng
                if (this.classList.contains('out-of-stock')) {
                    return;
                }

                // Bỏ chọn tất cả các kích thước khác
                document.querySelectorAll('.quickview-size').forEach(opt => {
                    opt.classList.remove('active');
                });

                // Đánh dấu kích thước này được chọn
                this.classList.add('active');

                // Lấy giá và giá khuyến mãi (nếu có)
                const price = parseFloat(this.getAttribute('data-price'));
                const discountPrice = parseFloat(this.getAttribute('data-discount-price')) || 0;

                // Cập nhật hiển thị giá
                if (discountPrice > 0 && discountPrice < price) {
                    // Nếu có giá khuyến mãi, hiển thị cả hai giá
                    quickviewOriginalPrice.textContent = formatPrice(price);
                    quickviewSalePrice.textContent = formatPrice(discountPrice);
                    quickviewOriginalPrice.style.display = 'inline-block';
                    quickviewOriginalPrice.style.textDecoration = 'line-through';
                } else {
                    // Nếu không có giá khuyến mãi, chỉ hiển thị giá gốc
                    quickviewSalePrice.textContent = formatPrice(price);
                    quickviewOriginalPrice.style.display = 'none';
                }
            });

            quickviewSizeOptions.appendChild(sizeOption);
        });

        // Tự động chọn biến thể đầu tiên có hàng
        const firstAvailableSize = quickviewSizeOptions.querySelector('.quickview-size:not(.out-of-stock)');
        if (firstAvailableSize) {
            firstAvailableSize.click();
        }

        // Cập nhật metadata
        quickviewCategory.textContent = product.category || 'Nước hoa';
        quickviewBrandMeta.textContent = product.brand || 'Thương hiệu';
        quickviewSku.textContent = product.sku || `SKU-${product.id}`;

        // Cập nhật link xem chi tiết
        quickviewViewDetails.href = `/shop/product/${product.id}`;

        // Cập nhật data-product-id cho nút thêm vào giỏ hàng
        quickviewAddToCart.setAttribute('data-product-id', product.id);

        console.log('Hoàn tất cập nhật nội dung QuickView cho sản phẩm:', product.id);
    }

    // Hàm định dạng giá
    function formatPrice(price) {
        return price.toLocaleString('vi-VN') + 'đ';
    }

    // Thêm sự kiện cho các nút "Xem nhanh" trên trang
    function setupQuickViewButtons() {
        console.log('Bắt đầu thiết lập các nút Xem nhanh');

        // Tìm tất cả các nút "Xem nhanh" trên trang với nhiều selector khác nhau
        const quickViewButtons = document.querySelectorAll(
            '.quick-view-btn, ' +
            '[title="Xem nhanh"], ' +
            '.action-btn[title="Xem nhanh"], ' +
            '.btn-quick-view, ' +
            '.quickview-button, ' +
            '.product-quickview, ' +
            'button.btn-eye, ' +
            'a.btn-eye, ' +
            '[data-action="quickview"]'
        );

        // Tìm thêm các nút có icon fa-eye
        const buttonsWithEyeIcon = Array.from(document.querySelectorAll('button i.fa-eye, a i.fa-eye'))
            .map(icon => icon.parentElement);

        // Kết hợp các nút đã tìm được
        const allQuickViewButtons = [...Array.from(quickViewButtons), ...buttonsWithEyeIcon];

        // Loại bỏ các phần tử trùng lặp
        const uniqueButtons = Array.from(new Set(allQuickViewButtons));

        // In ra tất cả các nút được tìm thấy
        console.log('Các nút Xem nhanh được tìm thấy:', uniqueButtons.map(btn => btn.outerHTML));
        console.log('Tìm thấy các nút Xem nhanh:', uniqueButtons.length);

        uniqueButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();

                // Hiển thị trạng thái đang tải
                const loadingOverlay = document.createElement('div');
                loadingOverlay.className = 'loading-overlay';
                loadingOverlay.innerHTML = '<div class="loading-spinner"></div>';
                document.body.appendChild(loadingOverlay);

                // Tìm phần tử cha chứa thông tin sản phẩm
                const productCard = this.closest('.product-card, .product-item');
                if (!productCard) {
                    console.error('Không tìm thấy phần tử chứa sản phẩm');
                    document.body.removeChild(loadingOverlay);
                    return;
                }

                // Lấy ID sản phẩm
                const productId = productCard.getAttribute('data-product-id');
                if (!productId) {
                    console.error('Không tìm thấy ID sản phẩm');
                    document.body.removeChild(loadingOverlay);
                    return;
                }

                // Lấy thông tin cơ bản của sản phẩm từ phần tử HTML
                const productName = productCard.querySelector('.product-name, .card-title')?.textContent || 'Sản phẩm';
                const brandName = productCard.querySelector('.product-brand, .brand-name')?.textContent || productCard.getAttribute('data-brand') || 'Thương hiệu';
                const imageUrl = productCard.querySelector('.product-image, .product-img')?.src || '/images/placeholder.jpg';
                const description = productCard.getAttribute('data-description') || 'Mô tả sản phẩm';
                const originalPrice = parseFloat(productCard.getAttribute('data-original-price') || '0');
                const salePrice = parseFloat(productCard.getAttribute('data-sale-price') || originalPrice.toString());

                // Lấy thông tin biến thể từ HTML thay vì gọi API
                let variantsData = [];

                // Kiểm tra nếu có dữ liệu biến thể được lưu trữ trong data-variants
                const variantsJson = productCard.getAttribute('data-variants');
                if (variantsJson) {
                    try {
                        variantsData = JSON.parse(variantsJson);
                        console.log(`Đã tìm thấy ${variantsData.length} biến thể từ data-variants cho sản phẩm ID:`, productId);
                    } catch (e) {
                        console.error('Lỗi khi phân tích dữ liệu biến thể từ data-variants:', e);
                    }
                }

                // Nếu không có dữ liệu từ data-variants, tìm các phần tử .variant-item
                if (variantsData.length === 0) {
                    const variantItems = productCard.querySelectorAll('.variant-item');
                    variantItems.forEach((item, index) => {
                        variantsData.push({
                            id: item.getAttribute('data-variant-id') || (index + 1).toString(),
                            volume: parseInt(item.getAttribute('data-volume') || '0'),
                            price: parseFloat(item.getAttribute('data-price') || '0'),
                            discountPrice: parseFloat(item.getAttribute('data-discount-price') || '0'),
                            stock: parseInt(item.getAttribute('data-stock') || '10')
                        });
                    });
                    console.log(`Đã tìm thấy ${variantItems.length} biến thể từ .variant-item cho sản phẩm ID:`, productId);
                }

                // Nếu vẫn không có dữ liệu, tạo dữ liệu mẫu dựa trên giá gốc
                if (variantsData.length === 0) {
                    variantsData = [
                        { id: '1', volume: 30, price: originalPrice * 0.5, stock: 10 },
                        { id: '2', volume: 50, price: originalPrice, stock: 8 },
                        { id: '3', volume: 100, price: originalPrice * 1.5, stock: 5 }
                    ];
                    console.log('Tạo dữ liệu biến thể mẫu cho sản phẩm ID:', productId);
                }

                // Chuyển đổi dữ liệu biến thể thành định dạng sizes
                const sizes = variantsData.map(variant => ({
                    value: variant.id.toString(),
                    label: `${variant.volume}ml`,
                    price: variant.price,
                    discountPrice: variant.discountPrice,
                    volume: variant.volume,
                    quantityInStock: variant.stock
                }));

                // Tạo đối tượng sản phẩm với ID duy nhất
                const productData = {
                    id: productId,
                    name: productName,
                    brand: brandName,
                    category: productCard.getAttribute('data-category') || 'Nước hoa',
                    sku: 'SKU-' + productId,
                    description: description,
                    rating: parseFloat(productCard.getAttribute('data-rating') || '4.5'),
                    reviewCount: parseInt(productCard.getAttribute('data-review-count') || '0'),
                    originalPrice: originalPrice,
                    salePrice: salePrice,
                    images: [imageUrl],
                    sizes: sizes,
                    // Thêm timestamp để đảm bảo mỗi lần mở là một đối tượng mới
                    timestamp: new Date().getTime()
                };

                console.log('Mở QuickView cho sản phẩm ID:', productId, 'với dữ liệu:', productData);

                // Xóa overlay đang tải
                document.body.removeChild(loadingOverlay);

                // Mở QuickView với dữ liệu đã lấy được
                openQuickView(productId, JSON.parse(JSON.stringify(productData)));
            });
        });

        console.log(`Đã thiết lập ${quickViewButtons.length} nút Xem nhanh`);
    }

    // Thiết lập các nút QuickView khi trang được tải
    setupQuickViewButtons();

    // Thiết lập lại các nút QuickView khi nội dung trang thay đổi (ví dụ: khi lọc sản phẩm)
    // Bạn có thể gọi setupQuickViewButtons() sau khi cập nhật nội dung trang

    // Sử dụng MutationObserver để tự động phát hiện khi có nút "Xem nhanh" mới được thêm vào trang
    const observer = new MutationObserver(function(mutations) {
        let shouldSetup = false;

        mutations.forEach(function(mutation) {
            // Kiểm tra nếu có phần tử mới được thêm vào DOM
            if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                for (let i = 0; i < mutation.addedNodes.length; i++) {
                    const node = mutation.addedNodes[i];
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        // Kiểm tra nếu phần tử mới có thể là nút "Xem nhanh" hoặc chứa nút "Xem nhanh"
                        if (node.matches('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"]') ||
                            node.querySelector('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"], button i.fa-eye, a i.fa-eye')) {
                            shouldSetup = true;
                            break;
                        }
                    }
                }
            }
        });

        // Nếu có nút "Xem nhanh" mới, thiết lập lại các nút
        if (shouldSetup) {
            console.log('Phát hiện thay đổi DOM, thiết lập lại các nút Xem nhanh');
            setupQuickViewButtons();
        }
    });

    // Bắt đầu theo dõi thay đổi trong DOM
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });

    console.log('Đã thiết lập MutationObserver để theo dõi các nút Xem nhanh mới');

    // Đăng ký hàm openQuickView vào window để có thể gọi từ bên ngoài
    window.openQuickView = openQuickView;

    // Log để kiểm tra
    console.log('QuickView initialized, openQuickView function is available');
});
