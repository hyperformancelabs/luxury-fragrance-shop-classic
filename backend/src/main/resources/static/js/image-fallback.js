/**
 * Image Fallback Handler
 * Xử lý fallback cho ảnh sản phẩm bị lỗi
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Image fallback handler initialized');
    
    // Function để setup fallback cho một ảnh
    function setupImageFallback(img) {
        // Skip nếu đã được setup
        if (img.dataset.fallbackSetup) return;
        
        // Lưu URL gốc để debug
        if (img.src && !img.dataset.originalSrc) {
            img.dataset.originalSrc = img.src;
        }
        
        // Thêm event listener cho lỗi load ảnh
        img.addEventListener('error', function() {
            console.log('Image error detected for:', this.src);
            
            // Kiểm tra nếu chưa fallback để tránh loop vô hạn
            if (!this.src.includes('product-default.png')) {
                console.log('Applying fallback for:', this.dataset.originalSrc || this.src);
                this.src = '/images/product-default.png';
                this.alt = 'Ảnh sản phẩm mặc định';
                
                // Thêm class để có thể style khác biệt nếu cần
                this.classList.add('fallback-image');
                
                console.log('Fallback image loaded for:', this.dataset.originalSrc || 'unknown product');
            }
        });
        
        // Đánh dấu đã setup
        img.dataset.fallbackSetup = 'true';
        
        // Kiểm tra ngay lập tức nếu ảnh đã bị lỗi
        if (img.complete && img.naturalHeight === 0 && img.src) {
            console.log('Image already broken, applying fallback immediately');
            img.dispatchEvent(new Event('error'));
        }
    }
    
    // Function để force check tất cả ảnh
    function forceCheckAllImages() {
        const selectors = [
            'img[alt*="Product"]',
            'img.product-img', 
            'img.card-img-top', 
            'img.product-main-image', 
            'img.related-product-image', 
            'img.cart-item-image', 
            'img.wishlist-item-image', 
            'img.product-image', 
            'img.search-suggestion-image'
        ];
        
        const allImages = document.querySelectorAll(selectors.join(', '));
        console.log('Force checking', allImages.length, 'images for broken state');
        
        allImages.forEach(function(img) {
            // Setup fallback trước
            setupImageFallback(img);
            
            // Force check broken state
            if (img.complete && img.naturalHeight === 0 && img.src && !img.src.includes('product-default.png')) {
                console.log('Found broken image, applying fallback:', img.src);
                img.dispatchEvent(new Event('error'));
            }
        });
    }
    
    // Tìm tất cả ảnh sản phẩm và thiết lập fallback
    function initializeImageFallbacks() {
        const selectors = [
            'img[alt*="Product"]',
            'img.product-img', 
            'img.card-img-top', 
            'img.product-main-image', 
            'img.related-product-image', 
            'img.cart-item-image', 
            'img.wishlist-item-image', 
            'img.product-image', 
            'img.search-suggestion-image'
        ];
        
        const productImages = document.querySelectorAll(selectors.join(', '));
        console.log('Found', productImages.length, 'product images to setup fallback');
        
        productImages.forEach(setupImageFallback);
        
        // Force check sau một khoảng delay để chắc chắn images đã load xong
        setTimeout(forceCheckAllImages, 1000);
        
        // Thêm một lần check nữa sau 3 giây cho chắc
        setTimeout(forceCheckAllImages, 3000);
    }
    
    // Khởi tạo ban đầu
    initializeImageFallbacks();
    
    // Thêm observer để xử lý ảnh được load động (như search suggestions)
    const observer = new MutationObserver(function(mutations) {
        let hasNewImages = false;
        
        mutations.forEach(function(mutation) {
            if (mutation.type === 'childList') {
                mutation.addedNodes.forEach(function(node) {
                    if (node.nodeType === 1) { // Element node
                        // Nếu chính node này là ảnh sản phẩm
                        if (node.tagName === 'IMG' && isProductImage(node)) {
                            setupImageFallback(node);
                            hasNewImages = true;
                        }
                        
                        // Tìm ảnh sản phẩm trong node mới
                        if (node.querySelectorAll) {
                            const newImages = node.querySelectorAll([
                                'img[alt*="Product"]',
                                'img.product-img', 
                                'img.card-img-top', 
                                'img.product-main-image', 
                                'img.related-product-image', 
                                'img.cart-item-image', 
                                'img.wishlist-item-image', 
                                'img.product-image', 
                                'img.search-suggestion-image'
                            ].join(', '));
                            
                            if (newImages.length > 0) {
                                newImages.forEach(setupImageFallback);
                                hasNewImages = true;
                            }
                        }
                    }
                });
            }
        });
        
        if (hasNewImages) {
            console.log('Detected new images, setup fallback completed');
        }
    });

    // Helper function để kiểm tra xem có phải ảnh sản phẩm không
    function isProductImage(img) {
        return img.alt.includes('Product') || 
               img.classList.contains('product-img') ||
               img.classList.contains('card-img-top') ||
               img.classList.contains('product-main-image') ||
               img.classList.contains('related-product-image') ||
               img.classList.contains('cart-item-image') ||
               img.classList.contains('wishlist-item-image') ||
               img.classList.contains('product-image') ||
               img.classList.contains('search-suggestion-image');
    }

    // Observe changes to handle dynamically added images
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
    
    // Export function để sử dụng global
    window.setupImageFallback = setupImageFallback;
    window.initializeImageFallbacks = initializeImageFallbacks;
});

/**
 * Function để thêm fallback cho ảnh được load động
 * @param {HTMLImageElement} imgElement - Element ảnh cần thêm fallback
 */
function addImageFallback(imgElement) {
    if (imgElement && imgElement.tagName === 'IMG') {
        if (window.setupImageFallback) {
            window.setupImageFallback(imgElement);
        } else {
            // Fallback nếu function chưa available
            imgElement.addEventListener('error', function() {
                if (!this.src.includes('product-default.png')) {
                    this.src = '/images/product-default.png';
                    this.alt = 'Ảnh sản phẩm mặc định';
                    this.classList.add('fallback-image');
                }
            });
        }
    }
}

// Export function để sử dụng ở các file khác
window.addImageFallback = addImageFallback; 