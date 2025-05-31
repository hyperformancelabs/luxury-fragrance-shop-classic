/**
 * Debug utilities for Image Fallback
 * Các function tiện ích để debug fallback trong browser console
 */

window.ImageFallbackDebug = {
    // Kiểm tra tất cả ảnh broken
    checkBrokenImages: function() {
        const allImages = document.querySelectorAll('img');
        const brokenImages = [];
        
        allImages.forEach(function(img) {
            if (img.complete && img.naturalHeight === 0 && img.src) {
                brokenImages.push({
                    element: img,
                    src: img.src,
                    classes: Array.from(img.classList),
                    alt: img.alt
                });
            }
        });
        
        console.log('=== BROKEN IMAGES REPORT ===');
        console.log('Found', brokenImages.length, 'broken images');
        brokenImages.forEach(function(item, index) {
            console.log(`${index + 1}.`, item);
        });
        
        return brokenImages;
    },
    
    // Force apply fallback cho tất cả ảnh broken
    fixAllBrokenImages: function() {
        const brokenImages = this.checkBrokenImages();
        let fixed = 0;
        
        brokenImages.forEach(function(item) {
            const img = item.element;
            if (!img.src.includes('product-default.png')) {
                img.src = '/images/product-default.png';
                img.classList.add('fallback-image');
                fixed++;
            }
        });
        
        console.log('Fixed', fixed, 'broken images');
        return fixed;
    },
    
    // Kiểm tra các product images
    checkProductImages: function() {
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
        const report = {
            total: productImages.length,
            working: 0,
            broken: 0,
            fallback: 0,
            details: []
        };
        
        productImages.forEach(function(img) {
            const status = {
                element: img,
                src: img.src,
                classes: Array.from(img.classList),
                isComplete: img.complete,
                naturalHeight: img.naturalHeight,
                hasFallbackSetup: !!img.dataset.fallbackSetup
            };
            
            if (img.src.includes('product-default.png')) {
                status.type = 'fallback';
                report.fallback++;
            } else if (img.complete && img.naturalHeight === 0 && img.src) {
                status.type = 'broken';
                report.broken++;
            } else {
                status.type = 'working';
                report.working++;
            }
            
            report.details.push(status);
        });
        
        console.log('=== PRODUCT IMAGES REPORT ===');
        console.log('Total:', report.total);
        console.log('Working:', report.working);
        console.log('Broken:', report.broken);
        console.log('Fallback:', report.fallback);
        console.log('Details:', report.details);
        
        return report;
    },
    
    // Test thêm ảnh broken
    addTestBrokenImage: function() {
        const img = document.createElement('img');
        img.src = 'https://broken-test-' + Date.now() + '.jpg';
        img.className = 'product-img test-broken-image';
        img.alt = 'Test Product';
        img.style.width = '200px';
        img.style.height = '200px';
        img.style.border = '2px solid red';
        img.style.margin = '10px';
        
        document.body.appendChild(img);
        
        console.log('Added test broken image:', img);
        
        // Thử apply fallback
        if (window.setupImageFallback) {
            window.setupImageFallback(img);
        }
        
        return img;
    },
    
    // Reinitialize fallback system
    reinitialize: function() {
        if (window.initializeImageFallbacks) {
            console.log('Reinitializing image fallback system...');
            window.initializeImageFallbacks();
        } else {
            console.log('initializeImageFallbacks function not available');
        }
    }
};

// Make available globally
console.log('Image Fallback Debug utilities loaded. Use ImageFallbackDebug.* functions');
console.log('Available functions:');
console.log('- ImageFallbackDebug.checkBrokenImages()');
console.log('- ImageFallbackDebug.fixAllBrokenImages()');
console.log('- ImageFallbackDebug.checkProductImages()');
console.log('- ImageFallbackDebug.addTestBrokenImage()');
console.log('- ImageFallbackDebug.reinitialize()'); 