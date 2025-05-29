// Countdown timer for Flash Deal
function updateCountdown() {
    const now = new Date();
    const endDate = new Date(now);
    endDate.setDate(endDate.getDate() + 2); // 2 days from now
    endDate.setHours(23, 59, 59, 0);

    const diff = endDate - now;

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    document.querySelector('.countdown-item:nth-child(1) .countdown-value').textContent = days.toString().padStart(2, '0');
    document.querySelector('.countdown-item:nth-child(2) .countdown-value').textContent = hours.toString().padStart(2, '0');
    document.querySelector('.countdown-item:nth-child(3) .countdown-value').textContent = minutes.toString().padStart(2, '0');
    document.querySelector('.countdown-item:nth-child(4) .countdown-value').textContent = seconds.toString().padStart(2, '0');
}

// Document ready function
document.addEventListener('DOMContentLoaded', function() {
    // Update countdown every second
    setInterval(updateCountdown, 1000);
    updateCountdown(); // Initial call

    // Quick view and wishlist buttons
    document.querySelectorAll('.action-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            const action = this.getAttribute('title');
            const productCard = this.closest('.product-card');
            const productName = productCard.querySelector('.card-title').textContent;

//            if (action === 'Yêu thích') {
//                console.log('Added to wishlist:', productName);
//                // In a real implementation, this would add the product to the wishlist
//            } else
            if (action === 'Xem nhanh') {
                        e.preventDefault();
                console.log('Quick view for:', productName);
                // In a real implementation, this would open a modal with product details
            }
        });
    });

    // Add to cart buttons
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');

            if (!productId) {
                alert('Không tìm thấy ID sản phẩm');
                return;
            }

            // Mở QuickView để người dùng chọn variant
            if (typeof openQuickView === 'function') {
                openQuickView(productId);
            } else {
                console.error('Hàm openQuickView không tồn tại');
                alert('Có lỗi xảy ra. Vui lòng thử lại sau.');
            }
        });
    });
});