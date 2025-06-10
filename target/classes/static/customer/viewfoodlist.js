let cart = [];
let selectedCategory = 'all';
let searchTerm = '';
let menuItems = [];
let allMenuItems = [];
let categories = [];
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let pageSize = 10; // Match backend default page size
let searchTimeout = null;
let isLoadingMore = false;

const API_BASE_URL = 'http://localhost:8080/foodhub';

const categoryEmojis = {
    'Lẩu nước': '🍲',
    'Nguyên liệu nhúng lẩu': '🥩',
    'Món nướng thịt': '🥓',
    'Món nướng hải sản': '🦐',
    'Món nướng rau củ': '🥬',
    'Món khai vị': '🥗',
    'Món ăn kèm': '🍚',
    'Nước chấm': '🥄',
    'Đồ uống không cồn': '🥤',
    'Đồ uống có cồn': '🍺'
};

async function apiFetch(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    console.log(`Fetching: ${url}`); // Debug API call
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('API response:', data); // Debug response
        return data;
    } catch (error) {
        console.error('API fetch error:', error);
        throw error;
    }
}

function getPlaceholderImage(categoryName) {
    const emoji = categoryEmojis[categoryName] || '🍽️';
    return `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96"><rect width="96" height="96" fill="%23f8f9fa"/><text x="50%" y="50%" text-anchor="middle" dy="0.3em" font-size="32">${emoji}</text></svg>`;
}

async function loadMenuData(page = 0, append = false) {
    try {
        if (!append) {
            showLoading(true);
        } else {
            isLoadingMore = true;
            updateLoadMoreButton();
        }
        hideError();

        let endpoint = '/menu';
        const params = new URLSearchParams({
            page: page.toString(),
            size: pageSize.toString()
        });

        if (selectedCategory !== 'all') {
            endpoint = '/menu/by-category';
            params.append('name', selectedCategory);
        }

        const data = await apiFetch(`${endpoint}?${params}`);

        if (data.code === 0 && data.result) {
            const pageData = data.result;

            const transformedItems = pageData.content.map((item, index) => ({
                id: item.id || (page * pageSize + index + 1),
                name: item.name,
                category: item.categoryNames[0] || 'Khác',
                price: item.price,
                image: item.imageUrl || getPlaceholderImage(item.categoryNames[0]),
                description: item.description,
                status: item.status,
                rating: 4.5 + Math.random() * 0.5,
                time: Math.floor(5 + Math.random() * 15) + ' phút'
            }));

            currentPage = pageData.number;
            totalPages = pageData.totalPages;
            totalElements = pageData.totalElements;

            if (append) {
                menuItems = [...menuItems, ...transformedItems];
            } else {
                menuItems = transformedItems;
            }

            const newCategories = [...new Set(transformedItems.map(item => item.category))];
            categories = [...new Set([...categories, ...newCategories])];

            if (!append) {
                renderCategories();
            }
            renderMenuItems();
            updatePagination();

        } else {
            throw new Error('Invalid API response format');
        }

    } catch (error) {
        console.error('Error loading menu data:', error);
        showError();
    } finally {
        showLoading(false);
        isLoadingMore = false;
        updateLoadMoreButton();
    }
}

async function loadMoreItems() {
    if (currentPage < totalPages - 1 && !isLoadingMore) {
        await loadMenuData(currentPage + 1, true);
    }
}

function updateLoadMoreButton() {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (currentPage < totalPages - 1) {
        loadMoreBtn.classList.remove('d-none');
        loadMoreBtn.disabled = isLoadingMore;
        loadMoreBtn.innerHTML = isLoadingMore ?
            '<i class="fas fa-spinner fa-spin me-2"></i>Đang tải...' :
            '<i class="fas fa-plus me-2"></i>Xem thêm món ăn';
    } else {
        loadMoreBtn.classList.add('d-none');
    }
}

function showLoading(show) {
    const spinner = document.getElementById('loadingSpinner');
    spinner.style.display = show ? 'flex' : 'none';
}

function showError() {
    document.getElementById('errorMessage').classList.remove('d-none');
}

function hideError() {
    document.getElementById('errorMessage').classList.add('d-none');
}

function renderCategories() {
    const container = document.getElementById('categoriesContainer');
    const allButton = container.querySelector('[data-category="all"]');
    const existingCategories = container.querySelectorAll('.category-btn:not([data-category="all"])');
    existingCategories.forEach(btn => btn.remove());

    categories.forEach(category => {
        const emoji = categoryEmojis[category] || '🍽️';
        const button = document.createElement('button');
        button.className = `category-btn ${selectedCategory === category ? 'active' : ''}`;
        button.setAttribute('data-category', category);
        button.onclick = () => selectCategory(category, button);
        button.innerHTML = `${emoji} ${category}`;
        container.appendChild(button);
    });
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(price);
}

function getFilteredItems() {
    if (!searchTerm) return menuItems;

    return menuItems.filter(item => {
        const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.description.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesSearch;
    });
}

function renderMenuItems() {
    const container = document.getElementById('menuContainer');
    const filteredItems = getFilteredItems();

    if (filteredItems.length === 0) {
        container.innerHTML = `
            <div class="text-center py-5">
                <i class="fas fa-search text-muted" style="font-size: 3rem;"></i>
                <p class="text-muted mt-2">${searchTerm ? 'Không tìm thấy món ăn nào' : 'Không có món ăn nào'}</p>
            </div>
        `;
        return;
    }

    container.innerHTML = filteredItems.map(item => `
        <div class="menu-item">
            <div class="d-flex">
                ${item.image.startsWith('data:') ?
        `<div class="placeholder-image">${categoryEmojis[item.category] || '🍽️'}</div>` :
        `<img src="${item.image}" alt="${item.name}" onerror="this.outerHTML='<div class=\\'placeholder-image\\'>${categoryEmojis[item.category] || '🍽️'}</div>'" />`
    }
                <div class="flex-fill p-3">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h6 class="fw-semibold mb-0">${item.name}</h6>
                        <span class="price-text">${formatPrice(item.price)}</span>
                    </div>
                    <p class="text-muted small mb-2 line-clamp-2">${item.description}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex gap-3">
                            <small class="text-muted">
                                <i class="fas fa-star rating-star me-1"></i>
                                ${item.rating.toFixed(1)}
                            </small>
                            <small class="text-muted">
                                <i class="fas fa-clock me-1"></i>
                                ${item.time}
                            </small>
                        </div>
                        <button class="btn btn-primary btn-sm px-3" onclick="addToCart(${item.id})">
                            Thêm
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

function updatePagination() {
    const paginationContainer = document.getElementById('paginationContainer');
    const pagination = document.getElementById('pagination');
    const itemsRange = document.getElementById('itemsRange');
    const totalItemsSpan = document.getElementById('totalItems');

    if (searchTerm || totalPages <= 1) {
        paginationContainer.classList.add('d-none');
        return;
    }

    paginationContainer.classList.remove('d-none');

    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
    itemsRange.textContent = `${startItem}-${endItem}`;
    totalItemsSpan.textContent = totalElements;

    let paginationHTML = '';

    paginationHTML += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="goToPage(${currentPage - 1}); return false;" aria-label="Previous">
                <span aria-hidden="true">«</span>
            </a>
        </li>
    `;

    const maxPagesToShow = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage < maxPagesToShow - 1) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    if (startPage > 0) {
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="goToPage(0); return false;">1</a></li>`;
        if (startPage > 1) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="goToPage(${i}); return false;">${i + 1}</a>
            </li>
        `;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
        paginationHTML += `<li class="page-item"><a class="page-link" href="#" onclick="goToPage(${totalPages - 1}); return false;">${totalPages}</a></li>`;
    }

    paginationHTML +=
        `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="goToPage(${currentPage + 1}); return false;" aria-label="Next">
            <span aria-hidden="true">»</span>
        </a>
    </li>`;

    pagination.innerHTML = paginationHTML;
}

async function goToPage(page) {
    if (page >= 0 && page < totalPages && page !== currentPage) {
        console.log(`Navigating to page ${page}`); // Debug page navigation
        currentPage = page; // Update currentPage before fetching
        await loadMenuData(page);
        document.getElementById('menuContainer').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

function handleSearch() {
    const newSearchTerm = document.getElementById('searchInput').value.trim();
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }
    searchTimeout = setTimeout(() => {
        searchTerm = newSearchTerm;
        renderMenuItems();
        updatePagination();
    }, 300);
}

function addToCart(itemId) {
    const item = menuItems.find(item => item.id === itemId);
    if (!item) return;

    const existingItem = cart.find(cartItem => cartItem.id === itemId);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ ...item, quantity: 1 });
    }
    updateCartUI();
}

function updateQuantity(itemId, change) {
    const itemIndex = cart.findIndex(item => item.id === itemId);
    if (itemIndex > -1) {
        cart[itemIndex].quantity += change;
        if (cart[itemIndex].quantity <= 0) {
            cart.splice(itemIndex, 1);
        }
    }
    updateCartUI();
}

function getTotalPrice() {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
}

function getTotalItemsCount() {
    return cart.reduce((total, item) => total + item.quantity, 0);
}

function updateCartUI() {
    const totalItems = getTotalItemsCount();
    const totalPrice = getTotalPrice();

    const cartBadge = document.getElementById('cartBadge');
    if (totalItems > 0) {
        cartBadge.textContent = totalItems;
        cartBadge.classList.remove('d-none');
    } else {
        cartBadge.classList.add('d-none');
    }

    const floatingCart = document.getElementById('floatingCart');
    if (totalItems > 0) {
        document.getElementById('floatingTotal').textContent = formatPrice(totalPrice);
        document.getElementById('floatingCount').textContent = totalItems;
        floatingCart.classList.remove('d-none');
    } else {
        floatingCart.classList.add('d-none');
    }

    document.getElementById('cartItemCount').textContent = totalItems;

    const emptyCart = document.getElementById('emptyCart');
    const cartItems = document.getElementById('cartItems');
    const cartFooter = document.getElementById('cartFooter');

    if (cart.length === 0) {
        emptyCart.classList.remove('d-none');
        cartItems.innerHTML = '';
        cartFooter.classList.add('d-none');
    } else {
        emptyCart.classList.add('d-none');
        cartFooter.classList.remove('d-none');
        document.getElementById('totalPrice').textContent = formatPrice(totalPrice);

        cartItems.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="d-flex align-items-center gap-3">
                    ${item.image && !item.image.startsWith('data:') ?
            `<img src="${item.image}" alt="${item.name}" onerror="this.outerHTML='<div class=\\'placeholder-image\\' style=\\'width: 48px; height: 48px; font-size: 1rem;\\'>${categoryEmojis[item.category] || '🍽️'}</div>'" />` :
            `<div class="placeholder-image" style="width: 48px; height: 48px; font-size: 1rem;">${categoryEmojis[item.category] || '🍽️'}</div>`
        }
                    <div class="flex-fill">
                        <h6 class="small fw-medium mb-0">${item.name}</h6>
                        <p class="price-text small fw-semibold mb-0">${formatPrice(item.price)}</p>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <button class="quantity-btn quantity-minus" onclick="updateQuantity(${item.id}, -1)">
                            <i class="fas fa-minus"></i>
                        </button>
                        <span class="fw-medium" style="min-width: 32px; text-align: center;">${item.quantity}</span>
                        <button class="quantity-btn quantity-plus" onclick="updateQuantity(${item.id}, 1)">
                            <i class="fas fa-plus"></i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }
}

function toggleCart() {
    const cartModal = document.getElementById('cartModal');
    cartModal.classList.toggle('show');
}

async function selectCategory(category, button) {
    if (selectedCategory === category) return;

    selectedCategory = category;
    searchTerm = '';
    document.getElementById('searchInput').value = '';

    document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');

    currentPage = 0;
    console.log(`Selecting category: ${category}`); // Debug category selection
    await loadMenuData(0);
}

document.getElementById('cartModal').addEventListener('click', function(e) {
    if (e.target === this) {
        toggleCart();
    }
});

document.addEventListener('click', function(e) {
    if (e.target.matches('.page-link')) {
        e.preventDefault();
    }
});

function setupInfiniteScroll() {
    let isNearBottom = false;

    window.addEventListener('scroll', () => {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;

        const nearBottom = scrollTop + windowHeight >= documentHeight - 200;

        if (nearBottom && !isNearBottom && !isLoadingMore && currentPage < totalPages - 1) {
            isNearBottom = true;
            loadMoreItems().then(() => {
                setTimeout(() => {
                    isNearBottom = false;
                }, 1000);
            });
        } else if (!nearBottom) {
            isNearBottom = false;
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    loadMenuData();
    updateCartUI();
    // setupInfiniteScroll();
});