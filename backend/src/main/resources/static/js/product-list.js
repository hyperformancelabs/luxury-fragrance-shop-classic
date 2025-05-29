document.addEventListener('DOMContentLoaded', function() {
    // Sort buttons
    const sortButtons = document.querySelectorAll('.sort-btn');

        sortButtons.forEach(button => {
            button.addEventListener('click', function () {
                sortButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');

                const sortOption = this.textContent.trim();

                // Tạo URL tương ứng
                let sortUrl = '/shop';
                if (sortOption === 'Giá thấp đến cao') {
                    sortUrl = '/shop/sort/min-price';
                } else if (sortOption === 'Giá cao đến thấp') {
                    sortUrl = '/shop/sort/max-price';
                } else if (sortOption === 'Bán chạy') {
                    sortUrl = '/shop/sort/top-selling';
                }

                // Lấy các tham số lọc hiện tại từ form
                const brands = document.getElementById('brandsInput')?.value || '';
                const genders = document.getElementById('gendersInput')?.value || '';
                const seasons = document.getElementById('seasonsInput')?.value || '';
                const min = document.getElementById('minRange')?.value || '';
                const max = document.getElementById('maxRange')?.value || '';

                // Xây URL đầy đủ với query string
                const queryParams = new URLSearchParams();
                if (brands) queryParams.append('brands', brands);
                if (genders) queryParams.append('genders', genders);
                if (seasons) queryParams.append('seasons', seasons);
                if (min) queryParams.append('min', min);
                if (max) queryParams.append('max', max);

                window.location.href = `${sortUrl}?${queryParams.toString()}`;
            });
        });

    // Quick view buttons - Handled by quickview.js
    // We're keeping this commented out as a reference
    /*
    const quickViewButtons = document.querySelectorAll('.quick-view-btn');
    quickViewButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            // In a real implementation, this would open a modal with product details
            // For now, we'll just log to console
            const productCard = this.closest('.product-card');
            const productTitle = productCard.querySelector('.card-title').textContent;
            console.log('Quick view for:', productTitle);
        });
    });
    */


    // Filter checkboxes
    const filterCheckboxes = document.querySelectorAll('.filter-option input[type="checkbox"]');
    filterCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            // In a real implementation, this would trigger a form submission or AJAX request
            // to filter the products based on the selected options
            console.log('Filter changed:', this.id, 'Checked:', this.checked);

            // For demo purposes, we'll just count the number of active filters
            const activeFilters = document.querySelectorAll('.filter-option input[type="checkbox"]:checked').length;
            console.log('Active filters:', activeFilters);
        });
    });

    // Price range slider
    const priceRangeSlider = document.querySelector('input[type="range"]');
    if (priceRangeSlider) {
        priceRangeSlider.addEventListener('input', function() {
            // In a real implementation, this would update the price display
            const value = this.value;
            const formattedValue = parseInt(value).toLocaleString('vi-VN') + 'đ';
            console.log('Price range changed:', formattedValue);
        });
    }
});
