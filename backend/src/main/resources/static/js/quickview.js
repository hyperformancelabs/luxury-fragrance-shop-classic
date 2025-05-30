console.log('QuickView script loaded');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing QuickView');

    // Thêm CSS cho QuickView
    const quickviewCssLink = document.createElement('link');
    quickviewCssLink.rel = 'stylesheet';
    quickviewCssLink.href = '/css/quickview-custom.css';
    document.head.appendChild(quickviewCssLink);
    console.log('QuickView CSS added to head');
    
    // Kiểm tra nếu QuickView đã được khởi tạo để tránh khởi tạo lại
    if (document.getElementById('quickview-overlay')) {
        console.log('QuickView already exists, skipping initialization');
        return;
    }
    
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
                if (variantsJson && variantsJson !== '[]') {
                    try {
                        const parsedVariants = JSON.parse(variantsJson);
                        if (Array.isArray(parsedVariants)) {
                            parsedVariants.forEach(variant => {
                                if (variant.id && variant.volume && variant.price) {
                                    variantsData.push({
                                        id: variant.id,
                                        volume: variant.volume,
                                        price: variant.price,
                                        discountPrice: variant.discountPrice || 0,
                                        stock: variant.stock || 0
                                    });
                                }
                            });
                        }
                    } catch (e) {
                        console.error('Lỗi khi phân tích dữ liệu biến thể:', e);
                    }
                }

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
                    sizes: []
                };

                // Kiểm tra xem có hình ảnh bổ sung không
                const additionalImages = productCard.getAttribute('data-images');
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
                product.sizes = variantsData.map(variant => ({
                    value: variant.id.toString(),
                    label: `${variant.volume}ml`,
                    price: variant.price,
                    discountPrice: variant.discountPrice,
                    volume: variant.volume,
                    quantityInStock: variant.stock
                }));

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

    // Sử dụng event delegation để xử lý các nút "Xem nhanh" - ngăn chặn loop
    let isInitialized = false;
    
    function setupQuickViewButtons() {
        if (isInitialized) {
            console.log('Quick view buttons already initialized, skipping...');
            return;
        }
        
        console.log('Setting up quick view buttons with event delegation');
        
        // Sử dụng event delegation để bắt tất cả các click trên document
        document.addEventListener('click', function(e) {
            // Kiểm tra nếu phần tử được click có thuộc tính onclick với openQuickView
            const onclickAttr = e.target.getAttribute('onclick');
            if (onclickAttr && onclickAttr.includes('openQuickView')) {
                e.preventDefault();
                e.stopPropagation();
                
                // Trích xuất productId từ onclick attribute
                const matches = onclickAttr.match(/openQuickView\((\d+)\)/);
                if (matches && matches[1]) {
                    const productId = matches[1];
                    console.log('Quick view triggered for product ID:', productId);
                    
                    // Gọi hàm openQuickView
                    openQuickView(productId);
                }
                return;
            }
            
            // Kiểm tra các selector khác cho nút "Xem nhanh"
            if (e.target.matches('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"]') ||
                e.target.closest('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"]')) {
                
                e.preventDefault();
                e.stopPropagation();
                
                const button = e.target.matches('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"]') 
                    ? e.target 
                    : e.target.closest('.quick-view-btn, [title="Xem nhanh"], .action-btn[title="Xem nhanh"], .btn-quick-view, .quickview-button, .product-quickview, button.btn-eye, a.btn-eye, [data-action="quickview"]');
                
                const productCard = button.closest('.product-card, .product-item');
                if (!productCard) {
                    console.error('Không tìm thấy phần tử chứa sản phẩm');
                    return;
                }
                
                const productId = productCard.getAttribute('data-product-id');
                if (!productId) {
                    console.error('Không tìm thấy ID sản phẩm');
                    return;
                }
                
                console.log('Quick view triggered for product ID:', productId);
                openQuickView(productId);
            }
        });
        
        isInitialized = true;
        console.log('Quick view event delegation setup complete');
    }

    // Thiết lập event delegation
    setupQuickViewButtons();

    // Đăng ký hàm openQuickView vào window để có thể gọi từ bên ngoài
    window.openQuickView = openQuickView;

    // Log để kiểm tra
    console.log('QuickView initialized, openQuickView function is available');
});
