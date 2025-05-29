document.addEventListener('DOMContentLoaded', function () {
    // Xử lý chọn phương thức thanh toán
    document.querySelectorAll('.payment-method').forEach(method => {
        method.addEventListener('click', function () {
            const radio = this.querySelector('input[type="radio"]');
            radio.checked = true;
            document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Submit form
    const checkoutForm = document.getElementById('checkout-form');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function (e) {
            if (!checkoutForm.checkValidity()) {

                e.preventDefault();
                e.stopPropagation();
                checkoutForm.classList.add('was-validated');
                return;
            }
            const btn = document.getElementById('place-order-btn');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';
        });
    }

    // Toggle shipping address section
    const sameAddress = document.getElementById('sameAddress');
    const shippingSection = document.getElementById('shipping-address-section');
    if (sameAddress && shippingSection) {
        const toggleShipping = () => shippingSection.style.display = sameAddress.checked ? 'none' : 'block';
        sameAddress.addEventListener('change', toggleShipping);
        toggleShipping();
    }

    // Maps
    const districtMap = {
        hanoi: ['Hoàn Kiếm', 'Ba Đình', 'Đống Đa'],
        hcm: ['Quận 1', 'Quận 3', 'Quận 5'],
        danang: ['Hải Châu', 'Thanh Khê', 'Liên Chiểu']
    };
    const wardMap = {
        hoankiem: ['Hàng Bông', 'Hàng Trống'],
        badinh: ['Ngọc Hà', 'Kim Mã'],
        dongda: ['Láng Hạ', 'Khâm Thiên'],
        quan1: ['Bến Nghé', 'Bến Thành'],
        quan3: ['Phường 1', 'Phường 2'],
        quan5: ['Phường 6', 'Phường 8'],
        haichau: ['Hải Châu 1', 'Hải Châu 2'],
        thankhe: ['Thanh Khê Đông', 'Thanh Khê Tây'],
        lienchieu: ['Hoà Minh', 'Hoà Khánh']
    };

    function handleLocationChange(provinceId, districtId, wardId) {
        const provinceSelect = document.getElementById(provinceId);
        const districtSelect = document.getElementById(districtId);
        const wardSelect = document.getElementById(wardId);

        if (!provinceSelect || !districtSelect || !wardSelect) return;

        provinceSelect.addEventListener('change', function () {
            const provinceValue = this.value;
            districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
            wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            wardSelect.disabled = true;

            if (districtMap[provinceValue]) {
                districtMap[provinceValue].forEach(d => {
                    const opt = document.createElement('option');
                    opt.value = d.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, '').replace(/\s+/g, '');

                    opt.textContent = d;
                    districtSelect.appendChild(opt);
                });
                districtSelect.disabled = false;
            } else {
                districtSelect.disabled = true;
            }
        });

        districtSelect.addEventListener('change', function () {
            const districtValue = this.value;
            wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            if (wardMap[districtValue]) {
                wardMap[districtValue].forEach(w => {
                    const opt = document.createElement('option');
                    opt.value = w.toLowerCase().replace(/\s+/g, '');
                    opt.textContent = w;
                    wardSelect.appendChild(opt);
                });
                wardSelect.disabled = false;
            } else {
                wardSelect.disabled = true;
            }
        });
    }

    // Áp dụng cho cả billing và shipping
    handleLocationChange('province', 'district', 'ward');
    handleLocationChange('shippingProvince', 'shippingDistrict', 'shippingWard');
});
