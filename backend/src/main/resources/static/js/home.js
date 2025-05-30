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

    // NOTE: Quick view functionality is handled by quickview.js
    // Removed conflicting event handlers to prevent loops
    
    console.log('Home page JavaScript initialized');
});