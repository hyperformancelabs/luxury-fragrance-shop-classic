document.addEventListener('DOMContentLoaded', function() {
    // Add to cart buttons
    const addToCartButtons = document.querySelectorAll('.add-to-cart-btn');
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            
            // Show loading state
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            this.disabled = true;
            
            // Create a form to submit
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/cart/add-by-product';
            
            // Add input for productId
            const productIdInput = document.createElement('input');
            productIdInput.type = 'hidden';
            productIdInput.name = 'productId';
            productIdInput.value = productId;
            form.appendChild(productIdInput);
            
            // Add input for quantity
            const quantityInput = document.createElement('input');
            quantityInput.type = 'hidden';
            quantityInput.name = 'quantity';
            quantityInput.value = 1; // Default quantity is 1
            form.appendChild(quantityInput);
            
            // Add form to body and submit
            document.body.appendChild(form);
            form.submit();
        });
    });
});
