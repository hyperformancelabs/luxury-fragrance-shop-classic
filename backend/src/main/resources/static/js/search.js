/**
 * Advanced Search Component
 * Real-time search with suggestions, keyboard navigation, and optimized performance
 */

class AdvancedSearch {
    constructor() {
        this.searchInput = document.getElementById('search-input');
        this.searchForm = document.getElementById('search-form');
        this.searchSuggestions = document.getElementById('search-suggestions');
        this.suggestionsContainer = this.searchSuggestions?.querySelector('.search-suggestions-list');
        this.searchClear = document.getElementById('search-clear');
        this.searchLoading = document.getElementById('search-loading');
        
        this.currentQuery = '';
        this.selectedIndex = -1;
        this.suggestions = [];
        this.debounceTimer = null;
        this.isLoading = false;
        
        // Configuration
        this.config = {
            debounceTime: 300,
            minQueryLength: 2,
            maxSuggestions: 8,
            apiEndpoint: '/search/api/suggestions'
        };
        
        this.init();
    }
    
    init() {
        if (!this.searchInput) return;
        
        this.bindEvents();
        this.initializeFromURL();
    }
    
    bindEvents() {
        // Input events
        this.searchInput.addEventListener('input', (e) => this.handleInput(e));
        this.searchInput.addEventListener('focus', (e) => this.handleFocus(e));
        this.searchInput.addEventListener('blur', (e) => this.handleBlur(e));
        this.searchInput.addEventListener('keydown', (e) => this.handleKeydown(e));
        
        // Form submission
        this.searchForm.addEventListener('submit', (e) => this.handleSubmit(e));
        
        // Clear button
        if (this.searchClear) {
            this.searchClear.addEventListener('click', () => this.clearSearch());
        }
        
        // Close suggestions when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.searchInput.contains(e.target) && !this.searchSuggestions?.contains(e.target)) {
                this.hideSuggestions();
            }
        });
        
        // Close suggestions on escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideSuggestions();
                this.searchInput.blur();
            }
        });
    }
    
    initializeFromURL() {
        // Set search input value from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        const query = urlParams.get('q');
        if (query && this.searchInput) {
            this.searchInput.value = query;
            this.updateClearButton();
        }
    }
    
    handleInput(e) {
        const query = e.target.value.trim();
        this.currentQuery = query;
        
        this.updateClearButton();
        
        // Clear previous timer
        if (this.debounceTimer) {
            clearTimeout(this.debounceTimer);
        }
        
        if (query.length >= this.config.minQueryLength) {
            // Debounce the search
            this.debounceTimer = setTimeout(() => {
                this.fetchSuggestions(query);
            }, this.config.debounceTime);
        } else {
            this.hideSuggestions();
        }
    }
    
    handleFocus(e) {
        const query = e.target.value.trim();
        if (query.length >= this.config.minQueryLength && this.suggestions.length > 0) {
            this.showSuggestions();
        }
    }
    
    handleBlur(e) {
        // Delay hiding to allow clicking on suggestions
        setTimeout(() => {
            if (!this.searchSuggestions?.contains(document.activeElement)) {
                this.hideSuggestions();
            }
        }, 150);
    }
    
    handleKeydown(e) {
        if (!this.suggestions.length || !this.isVisible()) return;
        
        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                this.navigateDown();
                break;
            case 'ArrowUp':
                e.preventDefault();
                this.navigateUp();
                break;
            case 'Enter':
                e.preventDefault();
                if (this.selectedIndex >= 0) {
                    this.selectSuggestion(this.suggestions[this.selectedIndex]);
                } else {
                    this.searchForm.submit();
                }
                break;
            case 'Tab':
                if (this.selectedIndex >= 0) {
                    e.preventDefault();
                    this.selectSuggestion(this.suggestions[this.selectedIndex]);
                }
                break;
        }
    }
    
    handleSubmit(e) {
        const query = this.searchInput.value.trim();
        if (!query) {
            e.preventDefault();
            this.showToast('Vui lòng nhập từ khóa tìm kiếm', 'warning');
            this.searchInput.focus();
        } else {
            this.hideSuggestions();
        }
    }
    
    async fetchSuggestions(query) {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();
        
        try {
            const response = await fetch(`${this.config.apiEndpoint}?q=${encodeURIComponent(query)}&limit=${this.config.maxSuggestions}`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            
            if (data.success && data.suggestions) {
                this.suggestions = data.suggestions;
                this.renderSuggestions();
                this.showSuggestions();
            } else {
                this.suggestions = [];
                this.showEmptyState();
            }
        } catch (error) {
            console.error('Search error:', error);
            this.suggestions = [];
            this.showErrorState();
        } finally {
            this.isLoading = false;
            this.hideLoading();
        }
    }
    
    renderSuggestions() {
        if (!this.suggestionsContainer) return;
        
        if (this.suggestions.length === 0) {
            this.showEmptyState();
            return;
        }
        
        const html = this.suggestions.map((item, index) => `
            <a href="${item.url}" class="search-suggestion-item" data-index="${index}">
                <img src="${item.image || '/images/product-default.png'}" 
                     alt="${item.name}" 
                     class="search-suggestion-image"
                     onerror="this.src='/images/product-default.png'">
                <div class="search-suggestion-content">
                    <div class="search-suggestion-name">${this.highlightQuery(item.name)}</div>
                    <div class="search-suggestion-brand">${item.brand}</div>
                </div>
                <i class="fas fa-arrow-right search-suggestion-icon"></i>
            </a>
        `).join('');
        
        this.suggestionsContainer.innerHTML = html;
        
        // Add click events
        this.suggestionsContainer.querySelectorAll('.search-suggestion-item').forEach((item, index) => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                this.selectSuggestion(this.suggestions[index]);
            });
            
            item.addEventListener('mouseenter', () => {
                this.setSelectedIndex(index);
            });
        });
    }
    
    showEmptyState() {
        if (!this.suggestionsContainer) return;
        
        this.suggestionsContainer.innerHTML = `
            <div class="search-suggestions-empty">
                <i class="fas fa-search"></i>
                <div>Không tìm thấy sản phẩm nào</div>
                <small>Thử tìm kiếm với từ khóa khác</small>
            </div>
        `;
        this.showSuggestions();
    }
    
    showErrorState() {
        if (!this.suggestionsContainer) return;
        
        this.suggestionsContainer.innerHTML = `
            <div class="search-suggestions-empty">
                <i class="fas fa-exclamation-triangle"></i>
                <div>Có lỗi xảy ra</div>
                <small>Vui lòng thử lại sau</small>
            </div>
        `;
        this.showSuggestions();
    }
    
    highlightQuery(text) {
        if (!this.currentQuery) return text;
        
        const regex = new RegExp(`(${this.escapeRegex(this.currentQuery)})`, 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    }
    
    escapeRegex(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
    
    navigateDown() {
        if (this.selectedIndex < this.suggestions.length - 1) {
            this.setSelectedIndex(this.selectedIndex + 1);
        }
    }
    
    navigateUp() {
        if (this.selectedIndex > 0) {
            this.setSelectedIndex(this.selectedIndex - 1);
        } else {
            this.setSelectedIndex(-1);
        }
    }
    
    setSelectedIndex(index) {
        this.selectedIndex = index;
        this.updateVisualSelection();
    }
    
    updateVisualSelection() {
        if (!this.suggestionsContainer) return;
        
        const items = this.suggestionsContainer.querySelectorAll('.search-suggestion-item');
        items.forEach((item, index) => {
            item.classList.toggle('active', index === this.selectedIndex);
        });
    }
    
    selectSuggestion(suggestion) {
        if (suggestion && suggestion.url) {
            window.location.href = suggestion.url;
        }
    }
    
    showSuggestions() {
        if (this.searchSuggestions) {
            this.searchSuggestions.style.display = 'block';
        }
    }
    
    hideSuggestions() {
        if (this.searchSuggestions) {
            this.searchSuggestions.style.display = 'none';
        }
        this.selectedIndex = -1;
    }
    
    isVisible() {
        return this.searchSuggestions && this.searchSuggestions.style.display !== 'none';
    }
    
    showLoading() {
        if (this.searchLoading) {
            this.searchLoading.style.display = 'block';
        }
    }
    
    hideLoading() {
        if (this.searchLoading) {
            this.searchLoading.style.display = 'none';
        }
    }
    
    updateClearButton() {
        if (!this.searchClear) return;
        
        const hasValue = this.searchInput.value.trim().length > 0;
        this.searchClear.style.display = hasValue ? 'flex' : 'none';
    }
    
    clearSearch() {
        this.searchInput.value = '';
        this.currentQuery = '';
        this.hideSuggestions();
        this.updateClearButton();
        this.searchInput.focus();
    }
    
    showToast(message, type = 'info') {
        // Using SweetAlert2 if available, otherwise console
        if (typeof Swal !== 'undefined') {
            const icon = type === 'warning' ? 'warning' : type === 'error' ? 'error' : 'info';
            Swal.fire({
                text: message,
                icon: icon,
                timer: 3000,
                showConfirmButton: false,
                toast: true,
                position: 'top-end'
            });
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new AdvancedSearch();
});

// Add CSS for highlighted text
const style = document.createElement('style');
style.textContent = `
    .search-suggestions mark {
        background-color: rgba(220, 53, 69, 0.15);
        color: var(--primary);
        padding: 1px 2px;
        border-radius: 2px;
        font-weight: 600;
    }
`;
document.head.appendChild(style); 