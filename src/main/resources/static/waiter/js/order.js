async function showOrders() {
    // Update page title and toggle visibility
    document.getElementById('pageTitle').textContent = 'Quản lý đơn hàng';
    document.getElementById('dashboardContent').style.display = 'none';
    document.getElementById('dynamicContent').style.display = 'block';

    // Render the enhanced orders table HTML with filters and sorting
    document.getElementById('dynamicContent').innerHTML = `
      <div class="orders-container">
        <!-- Enhanced Header Section -->
        <div class="orders-header">
          <div class="d-flex justify-content-between align-items-center w-100 flex-wrap">
            <h3>
              <i class="fas fa-clipboard-list"></i>
              Tất cả đơn hàng
            </h3>
            <div class="orders-actions">
              <button class="btn-orders-action btn-success" onclick="takeNewOrder()">
                <i class="fas fa-plus"></i>
                Nhận đơn mới
              </button>
              <button class="btn-orders-action btn-outline-primary" onclick="refreshOrders()">
                <i class="fas fa-sync-alt"></i>
                Làm mới
              </button>
            </div>
          </div>
        </div>

        <!-- Filter and Sort Section -->
        <div class="orders-filter-section card mb-3">
          <div class="card-body">
            <div class="row g-3">
              <!-- Status Filter -->
              <div class="col-md-3">
                <label class="form-label">
                  <i class="fas fa-filter me-1"></i>
                  Trạng thái
                </label>
                <select class="form-select" id="statusFilter" onchange="applyFilters()">
                  <option value="">Tất cả trạng thái</option>
                  <option value="PENDING">Chờ xử lý</option>
                  <option value="CONFIRMED">Đã xác nhận</option>
                  <option value="READY">Sẵn sàng</option>
                  <option value="COMPLETED">Hoàn thành</option>
                  <option value="CANCELLED">Đã hủy</option>
                </select>
              </div>

              <!-- Table Filter -->
              <div class="col-md-3">
                <label class="form-label">
                  <i class="fas fa-chair me-1"></i>
                  Bàn số
                </label>
                <input type="number" class="form-control" id="tableIdFilter" 
                       placeholder="Chọn bàn" onchange="applyFilters()">
              </div>

              <!-- Min Price Filter -->
              <div class="col-md-3">
                <label class="form-label">
                  <i class="fas fa-money-bill me-1"></i>
                  Giá tối thiểu (VNĐ)
                </label>
                <input type="number" class="form-control" id="minPriceFilter" 
                       placeholder="0" onchange="applyFilters()">
              </div>

              <!-- Max Price Filter -->
              <div class="col-md-3">
                <label class="form-label">
                  <i class="fas fa-money-bill me-1"></i>
                  Giá tối đa (VNĐ)
                </label>
                <input type="number" class="form-control" id="maxPriceFilter" 
                       placeholder="1000000" onchange="applyFilters()">
              </div>

              <!-- Sort Field -->
              <div class="col-md-4">
                <label class="form-label">
                  <i class="fas fa-sort me-1"></i>
                  Sắp xếp theo
                </label>
                <select class="form-select" id="sortByFilter" onchange="applyFilters()">
                  <option value="createdAt">Thời gian tạo</option>
                  <option value="totalAmount">Tổng tiền</option>
                  <option value="id">Mã đơn hàng</option>
                  <option value="status">Trạng thái</option>
                </select>
              </div>

              <!-- Sort Direction -->
              <div class="col-md-4">
                <label class="form-label">
                  <i class="fas fa-sort-amount-down me-1"></i>
                  Thứ tự
                </label>
                <select class="form-select" id="sortDirectionFilter" onchange="applyFilters()">
                  <option value="DESC">Giảm dần</option>
                  <option value="ASC">Tăng dần</option>
                </select>
              </div>

              <!-- Page Size -->
              <div class="col-md-4">
                <label class="form-label">
                  <i class="fas fa-list me-1"></i>
                  Số đơn/trang
                </label>
                <select class="form-select" id="pageSizeFilter" onchange="applyFilters()">
                  <option value="5">5 đơn</option>
                  <option value="10" selected>10 đơn</option>
                  <option value="20">20 đơn</option>
                  <option value="50">50 đơn</option>
                </select>
              </div>
            </div>

            <!-- Filter Actions -->
            <div class="row mt-3">
              <div class="col-12">
                <div class="d-flex gap-2 justify-content-end">
                  <button class="btn btn-outline-secondary" onclick="clearFilters()">
                    <i class="fas fa-times me-1"></i>
                    Xóa bộ lọc
                  </button>
                  <button class="btn btn-primary" onclick="applyFilters()">
                    <i class="fas fa-search me-1"></i>
                    Áp dụng
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Results Summary -->
        <div class="orders-summary mb-3">
          <div class="alert alert-info d-flex align-items-center">
            <i class="fas fa-info-circle me-2"></i>
            <span id="ordersSummary">Đang tải...</span>
          </div>
        </div>

        <!-- Enhanced Orders Table -->
        <div class="orders-table-card card">
          <div class="card-body">
            <div class="orders-table-wrapper">
              <table class="orders-table table">
                <thead>
                  <tr>
                    <th><i class="fas fa-hashtag me-2"></i>Mã đơn</th>
                    <th><i class="fas fa-chair me-2"></i>Bàn</th>
                    <th><i class="fas fa-clock me-2"></i>Thời gian</th>
                    <th><i class="fas fa-money-bill me-2"></i>Tổng tiền</th>
                    <th><i class="fas fa-info-circle me-2"></i>Trạng thái</th>
                    <th><i class="fas fa-utensils me-2"></i>Loại</th>
                    <th><i class="fas fa-cogs me-2"></i>Hành động</th>
                  </tr>
                </thead>
                <tbody id="orderTableBody">
                  <!-- Loading state -->
                  <tr>
                    <td colspan="7">
                      <div class="orders-loading">
                        <div class="spinner"></div>
                        <p>Đang tải danh sách đơn hàng...</p>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Enhanced Pagination - Only pagination controls -->
        <div class="pagination-container mt-3">
          <div class="d-flex justify-content-center">
            <div class="pagination-controls">
              <nav aria-label="Phân trang đơn hàng">
                <ul class="pagination mb-0" id="paginationList">
                  <!-- Pagination buttons will be generated here -->
                </ul>
              </nav>
            </div>
          </div>
          
          <!-- Quick Jump (only show when total pages > 10) -->
          <div class="pagination-jump mt-2" id="paginationJump" style="display: none;">
            <div class="d-flex justify-content-center align-items-center gap-2">
              <span class="text-muted">Chuyển đến trang:</span>
              <input type="number" class="form-control form-control-sm" id="jumpToPage" 
                     style="width: 80px;" min="1" max="1" placeholder="1">
              <button class="btn btn-sm btn-outline-primary" onclick="jumpToPage()">
                <i class="fas fa-arrow-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    // Initialize and load orders
    await loadOrders();

    // Update navigation active state
    updateActiveNavigation('showOrders()');
}

// Global variables for pagination
let currentPage = 0; // Backend uses 0-based pagination
let totalPages = 1;
let totalElements = 0;

// Load orders from API with filters and pagination
async function loadOrders() {
    try {
        // Get filter values
        const status = document.getElementById('statusFilter')?.value || '';
        const tableId = document.getElementById('tableIdFilter')?.value || '';
        const minPrice = document.getElementById('minPriceFilter')?.value || '';
        const maxPrice = document.getElementById('maxPriceFilter')?.value || '';
        const sortBy = document.getElementById('sortByFilter')?.value || 'createdAt';
        const sortDirection = document.getElementById('sortDirectionFilter')?.value || 'DESC';
        const pageSize = document.getElementById('pageSizeFilter')?.value || '10';

        // Build query parameters
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        if (tableId) params.append('tableId', tableId);
        if (minPrice) params.append('minPrice', minPrice);
        if (maxPrice) params.append('maxPrice', maxPrice);
        params.append('page', currentPage.toString());
        params.append('size', pageSize);
        params.append('SorderBy', sortBy); // Note: backend uses 'SorderBy'
        params.append('sort', sortDirection);

        // Fetch orders with filters
        const data = await apiFetch(`/orders?${params.toString()}`, {
            method: 'GET'
        });

        const orderPage = data.result;
        const orders = orderPage.content || [];

        // Update pagination info
        totalPages = orderPage.totalPages;
        totalElements = orderPage.totalElements;
        currentPage = orderPage.number;

        // Render orders
        renderOrders(orders);
        updateSummary(orderPage);
        updatePagination();

    } catch (error) {
        console.error('Error fetching orders:', error);
        showErrorState(error.message);
    }
}

// Apply filters and reload data
function applyFilters() {
    currentPage = 0; // Reset to first page when applying filters
    loadOrders();
}

// Clear all filters
function clearFilters() {
    document.getElementById('statusFilter').value = '';
    document.getElementById('tableIdFilter').value = '';
    document.getElementById('minPriceFilter').value = '';
    document.getElementById('maxPriceFilter').value = '';
    document.getElementById('sortByFilter').value = 'createdAt';
    document.getElementById('sortDirectionFilter').value = 'DESC';
    document.getElementById('pageSizeFilter').value = '10';

    applyFilters();
}

// Render orders table
function renderOrders(orders) {
    const orderTableBody = document.getElementById('orderTableBody');

    if (orders.length === 0) {
        // Show empty state
        orderTableBody.innerHTML = `
            <tr>
                <td colspan="7">
                    <div class="empty-orders-state">
                        <i class="fas fa-clipboard-list"></i>
                        <h4>Không tìm thấy đơn hàng nào</h4>
                        <p>Thử thay đổi bộ lọc để xem thêm kết quả.</p>
                        <button class="btn-orders-action btn-outline-secondary" onclick="clearFilters()">
                            <i class="fas fa-times me-2"></i>Xóa bộ lọc
                        </button>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    // Clear table body
    orderTableBody.innerHTML = '';

    // Populate orders
    orders.forEach(order => {
        const row = document.createElement('tr');
        row.onclick = () => viewOrderDetails(order.id);
        row.className = 'order-row';

        row.innerHTML = `
            <td>
                <strong>#${order.id}</strong>
            </td>
            <td>
                <span class="table-number-badge">
                    <i class="fas fa-chair me-1"></i>
                    ${order.tableNumber ? `Bàn ${order.tableNumber}` : 'Mang về'}
                </span>
            </td>
            <td>
                <div class="order-time">
                    <i class="fas fa-clock"></i>
                    ${formatDateTime(order.createdAt)}
                </div>
            </td>
            <td>
                <div class="order-amount">
                    <i class="fas fa-money-bill"></i>
                    ${formatCurrency(order.totalAmount)}
                </div>
            </td>
            <td>
                <span class="status-badge-enhanced ${getStatusBadgeClass(order.status)}">
                    ${getStatusIcon(order.status)}
                    ${getStatusText(order.status)}
                </span>
            </td>
            <td>
                <span class="order-type-badge ${getOrderTypeBadgeClass(order.orderType)}">
                    ${getOrderTypeIcon(order.orderType)}
                    ${getOrderTypeText(order.orderType)}
                </span>
            </td>
            <td>
                <div class="action-btn-group">
                    <button class="action-btn btn-info" 
                            onclick="event.stopPropagation(); viewOrderDetails(${order.id})"
                            title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${getActionButton(order)}
                </div>
            </td>
        `;
        orderTableBody.appendChild(row);
    });
}

// Update summary information
function updateSummary(orderPage) {
    const summaryElement = document.getElementById('ordersSummary');
    const pageSize = orderPage.size;
    const currentPageNum = orderPage.number + 1; // Display 1-based page number
    const startItem = orderPage.number * pageSize + 1;
    const endItem = Math.min((orderPage.number + 1) * pageSize, totalElements);

    summaryElement.innerHTML = `
        <strong>Tìm thấy ${totalElements} đơn hàng</strong> 
        - Hiển thị ${startItem} - ${endItem} 
        (Trang ${currentPageNum}/${totalPages})
    `;
}

// Update pagination controls with smart pagination - Removed pagination info display
function updatePagination() {
    // Generate pagination buttons
    generatePaginationButtons();

    // Show/hide quick jump feature for large page counts
    const jumpContainer = document.getElementById('paginationJump');
    const jumpInput = document.getElementById('jumpToPage');

    if (totalPages > 10) {
        jumpContainer.style.display = 'block';
        jumpInput.max = totalPages;
        jumpInput.placeholder = `1-${totalPages}`;
    } else {
        jumpContainer.style.display = 'none';
    }
}

// Generate smart pagination buttons (max 10 visible pages)
function generatePaginationButtons() {
    const paginationList = document.getElementById('paginationList');
    paginationList.innerHTML = '';

    if (totalPages <= 1) return;

    const currentPageDisplay = currentPage + 1; // Convert to 1-based for display
    const maxVisiblePages = 10;

    // Calculate page range to display
    let startPage, endPage;

    if (totalPages <= maxVisiblePages) {
        // Show all pages if total is <= 10
        startPage = 1;
        endPage = totalPages;
    } else {
        // Smart pagination logic
        const halfVisible = Math.floor(maxVisiblePages / 2);

        if (currentPageDisplay <= halfVisible + 1) {
            // Near the beginning
            startPage = 1;
            endPage = maxVisiblePages;
        } else if (currentPageDisplay >= totalPages - halfVisible) {
            // Near the end
            startPage = totalPages - maxVisiblePages + 1;
            endPage = totalPages;
        } else {
            // In the middle
            startPage = currentPageDisplay - halfVisible;
            endPage = currentPageDisplay + halfVisible;
        }
    }

    // Previous button
    const prevBtn = createPaginationButton('‹', currentPage - 1, currentPage <= 0, 'Trang trước');
    paginationList.appendChild(prevBtn);

    // First page + ellipsis (if needed)
    if (startPage > 1) {
        paginationList.appendChild(createPaginationButton('1', 0));
        if (startPage > 2) {
            paginationList.appendChild(createPaginationEllipsis('start'));
        }
    }

    // Page number buttons
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPaginationButton(
            i.toString(),
            i - 1, // Convert to 0-based for backend
            false,
            `Trang ${i}`,
            i === currentPageDisplay
        );
        paginationList.appendChild(pageBtn);
    }

    // Last page + ellipsis (if needed)
    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            paginationList.appendChild(createPaginationEllipsis('end'));
        }
        paginationList.appendChild(createPaginationButton(totalPages.toString(), totalPages - 1));
    }

    // Next button
    const nextBtn = createPaginationButton('›', currentPage + 1, currentPage >= totalPages - 1, 'Trang sau');
    paginationList.appendChild(nextBtn);
}

// Create pagination button element
function createPaginationButton(text, pageIndex, disabled = false, title = '', active = false) {
    const li = document.createElement('li');
    li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;

    const button = document.createElement('button');
    button.className = 'page-link';
    button.innerHTML = text;
    button.title = title;
    button.disabled = disabled;

    if (!disabled) {
        button.onclick = () => goToPage(pageIndex);
    }

    li.appendChild(button);
    return li;
}

// Create ellipsis element for pagination
function createPaginationEllipsis(position) {
    const li = document.createElement('li');
    li.className = 'page-item disabled';

    const span = document.createElement('span');
    span.className = 'page-link';
    span.innerHTML = '...';
    span.title = position === 'start' ? 'Các trang trước' : 'Các trang sau';

    li.appendChild(span);
    return li;
}

// Go to specific page
function goToPage(pageIndex) {
    if (pageIndex >= 0 && pageIndex < totalPages && pageIndex !== currentPage) {
        currentPage = pageIndex;
        loadOrders();
    }
}

// Jump to page functionality
function jumpToPage() {
    const jumpInput = document.getElementById('jumpToPage');
    const pageNumber = parseInt(jumpInput.value);

    if (pageNumber && pageNumber >= 1 && pageNumber <= totalPages) {
        goToPage(pageNumber - 1); // Convert to 0-based
        jumpInput.value = ''; // Clear input
    } else {
        // Show error feedback
        jumpInput.classList.add('is-invalid');
        setTimeout(() => {
            jumpInput.classList.remove('is-invalid');
        }, 2000);
    }
}

// Allow Enter key in jump input
document.addEventListener('DOMContentLoaded', function() {
    // This will be called when the page is ready
    setTimeout(() => {
        const jumpInput = document.getElementById('jumpToPage');
        if (jumpInput) {
            jumpInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    jumpToPage();
                }
            });
        }
    }, 100);
});

// Change page (legacy support)
function changePage(direction) {
    const newPage = currentPage + direction;
    goToPage(newPage);
}

// Refresh orders
function refreshOrders() {
    loadOrders();
}

// Show error state
function showErrorState(message) {
    document.getElementById('orderTableBody').innerHTML = `
        <tr>
            <td colspan="7">
                <div class="empty-orders-state">
                    <i class="fas fa-exclamation-triangle" style="color: var(--danger);"></i>
                    <h4 style="color: var(--danger);">Không thể tải danh sách đơn hàng</h4>
                    <p>${message}</p>
                    <button class="btn-orders-action btn-outline-primary" onclick="refreshOrders()">
                        <i class="fas fa-sync-alt me-2"></i>Thử lại
                    </button>
                </div>
            </td>
        </tr>
    `;
}

// Helper functions for order type
function getOrderTypeText(orderType) {
    const types = {
        'DINE_IN': 'Tại chỗ',
        'TAKEAWAY': 'Mang về',
        'DELIVERY': 'Giao hàng'
    };
    return types[orderType] || orderType;
}

function getOrderTypeIcon(orderType) {
    const icons = {
        'DINE_IN': '<i class="fas fa-utensils"></i>',
        'TAKEAWAY': '<i class="fas fa-shopping-bag"></i>',
        'DELIVERY': '<i class="fas fa-truck"></i>'
    };
    return icons[orderType] || '<i class="fas fa-question"></i>';
}

function getOrderTypeBadgeClass(orderType) {
    const classes = {
        'DINE_IN': 'order-type-dine-in',
        'TAKEAWAY': 'order-type-takeaway',
        'DELIVERY': 'order-type-delivery'
    };
    return classes[orderType] || 'order-type-default';
}

// Format currency helper
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}
// Helper function to get enhanced status badge classes
function getStatusBadgeClass(status) {
    const statusMap = {
        'PENDING': 'badge-pending',
        'CONFIRMED': 'badge-confirmed',
        'PREPARING': 'badge-preparing',
        'READY': 'badge-ready',
        'COMPLETED': 'badge-completed'
    };
    return statusMap[status] || 'badge-pending';
}

// Helper function to get status icons
function getStatusIcon(status) {
    const iconMap = {
        'PENDING': '<i class="fas fa-hourglass-start me-1"></i>',
        'CONFIRMED': '<i class="fas fa-check-circle me-1"></i>',
        'PREPARING': '<i class="fas fa-utensils me-1"></i>',
        'READY': '<i class="fas fa-bell me-1"></i>',
        'COMPLETED': '<i class="fas fa-check-double me-1"></i>'
    };
    return iconMap[status] || '<i class="fas fa-clock me-1"></i>';
}

// Helper function to get Vietnamese status text
function getStatusText(status) {
    const textMap = {
        'PENDING': 'Chờ xác nhận',
        'CONFIRMED': 'Đã xác nhận',
        'PREPARING': 'Đang chuẩn bị',
        'READY': 'Sẵn sàng',
        'COMPLETED': 'Hoàn thành'
    };
    return textMap[status] || 'Không xác định';
}

// Enhanced action button generator
function getActionButton(order) {
    const actionMap = {
        'PENDING': `
            <button class="action-btn btn-success" 
                    onclick="event.stopPropagation(); confirmOrder(${order.id})"
                    title="Xác nhận đơn hàng">
                <i class="fas fa-check"></i>
            </button>`,
        'CONFIRMED': `
            <button class="action-btn btn-warning" 
                    onclick="event.stopPropagation(); startPreparing(${order.id})"
                    title="Bắt đầu chuẩn bị">
                <i class="fas fa-play"></i>
            </button>`,
        'PREPARING': `
            <button class="action-btn btn-success" 
                    onclick="event.stopPropagation(); markReady(${order.id})"
                    title="Đánh dấu hoàn thành">
                <i class="fas fa-flag-checkered"></i>
            </button>`,
        'READY': `
            <button class="action-btn btn-info" 
                    onclick="event.stopPropagation(); completeOrder(${order.id})"
                    title="Hoàn thành giao hàng">
                <i class="fas fa-truck"></i>
            </button>`,
        'COMPLETED': `
            <button class="action-btn btn-secondary" 
                    onclick="event.stopPropagation(); viewOrderDetails(${order.id})"
                    title="Xem lại" disabled>
                <i class="fas fa-archive"></i>
            </button>`
    };
    return actionMap[order.status] || '';
}

// Helper function to update navigation active state
function updateActiveNavigation(currentFunction) {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    const activeLink = document.querySelector(`[onclick="${currentFunction}"]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }
}

// Refresh orders function
async function refreshOrders() {
    const refreshBtn = document.querySelector('[onclick="refreshOrders()"]');
    if (refreshBtn) {
        const originalHTML = refreshBtn.innerHTML;
        refreshBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang tải...';
        refreshBtn.disabled = true;

        try {
            await showOrders();
        } finally {
            refreshBtn.innerHTML = originalHTML;
            refreshBtn.disabled = false;
        }
    } else {
        await showOrders();
    }
}

// Hàm định dạng thời gian
function formatDateTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    // Format time part
    const timeStr = date.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });

    if (diffMins < 60) {
        return `${diffMins} phút trước`;
    } else if (diffHours < 24) {
        return `${diffHours} giờ trước (${timeStr})`;
    } else if (diffDays < 7) {
        return `${diffDays} ngày trước (${timeStr})`;
    } else {
        return date.toLocaleDateString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        }) + ` ${timeStr}`;
    }
}
// Hàm xác định class cho badge trạng thái
function getStatusBadgeClass(status) {
    const statusClasses = {
        'PENDING': 'bg-warning',
        'CONFIRMED': 'bg-info',
        'READY': 'bg-success',
        'COMPLETED': 'bg-secondary',
        'CANCELLED': 'bg-danger'
    };
    return statusClasses[status] || 'bg-secondary';
}

// Hàm chuyển đổi trạng thái sang tiếng Việt
function getStatusText(status) {
    const statusTexts = {
        'PENDING': 'Chờ xác nhận',
        'CONFIRMED': 'Đã xác nhận',
        'READY': 'Sẵn sàng phục vụ',
        'COMPLETED': 'Hoàn thành',
        'CANCELLED': 'Đã hủy'
    };
    return statusTexts[status] || status;
}

// Hàm tạo nút hành động dựa trên trạng thái đơn hàng
function getActionButton(order) {
    switch (order.status) {
        case 'PENDING':
            return `
                <button class="btn btn-sm btn-success" onclick="confirmOrder(${order.id})">
                    <i class="fas fa-check me-1"></i>Xác nhận
                </button>`;
        case 'READY':
            return `
                <button class="btn btn-sm btn-primary" onclick="serveOrder(${order.id})">
                    <i class="fas fa-utensils me-1"></i>Phục vụ
                </button>`;
        case 'COMPLETED':
            return `
                <button class="btn btn-sm btn-secondary" disabled>
                    <i class="fas fa-check-circle me-1"></i>Đã hoàn thành
                </button>`;
        default:
            return '';
    }
}

// Hàm xem chi tiết đơn hàng
function viewOrderDetails(orderId) {
    // TODO: Implement view order details
    alert('Chức năng xem chi tiết đơn hàng sẽ được cập nhật sau!');
}

// Hàm xác nhận đơn hàng
function confirmOrder(orderId) {
    if (confirm('Xác nhận đơn hàng này?')) {
        fetch(`/api/orders/${orderId}/confirm`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    showOrders(); // Refresh danh sách
                    alert('Đã xác nhận đơn hàng thành công!');
                } else {
                    throw new Error('Không thể xác nhận đơn hàng');
                }
            })
            .catch(error => {
                console.error('Error confirming order:', error);
                alert('Không thể xác nhận đơn hàng. Vui lòng thử lại sau.');
            });
    }
}

// Hàm phục vụ đơn hàng
function serveOrder(orderId) {
    if (confirm('Xác nhận phục vụ đơn hàng này?')) {
        fetch(`/api/orders/${orderId}/serve`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    showOrders(); // Refresh danh sách
                    alert('Đã cập nhật trạng thái phục vụ thành công!');
                } else {
                    throw new Error('Không thể cập nhật trạng thái phục vụ');
                }
            })
            .catch(error => {
                console.error('Error serving order:', error);
                alert('Không thể cập nhật trạng thái phục vụ. Vui lòng thử lại sau.');
            });
    }
}

// Hàm làm mới danh sách đơn hàng
function refreshOrders() {
    showOrders();
}

// Hàm nhận đơn mới
function takeNewOrder() {
    // TODO: Implement take new order functionality
    alert('Chức năng nhận đơn mới sẽ được cập nhật sau!');
}