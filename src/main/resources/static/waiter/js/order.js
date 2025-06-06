async function showOrders() {
    // Update page title and toggle visibility
    document.getElementById('pageTitle').textContent = 'Quản lý đơn hàng';
    document.getElementById('dashboardContent').style.display = 'none';
    document.getElementById('dynamicContent').style.display = 'block';

    // Render the orders table HTML
    document.getElementById('dynamicContent').innerHTML = `
      <div class="container-fluid p-4">
        <div class="row mb-4">
          <div class="col-md-12">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3><i class="fas fa-clipboard-list me-2"></i>Tất cả đơn hàng</h3>
              <div class="d-flex gap-2">
                <button class="btn btn-success" onclick="takeNewOrder()">
                  <i class="fas fa-plus me-2"></i>Nhận đơn mới
                </button>
                <button class="btn btn-outline-primary" onclick="refreshOrders()">
                  <i class="fas fa-sync-alt me-2"></i>Làm mới
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <div class="card">
              <div class="card-body">
                <table class="table table-striped">
                  <thead>
                    <tr>
                      <th>Mã đơn</th>
                      <th>Bàn</th>
                      <th>Thời gian</th>
                      <th>Trạng thái</th>
                      <th>Hành động</th>
                    </tr>
                  </thead>
                  <tbody id="orderTableBody">
                    <!-- Dữ liệu đơn hàng sẽ được thêm vào đây -->
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;

    try {
        // Fetch orders using apiFetch
        const data = await apiFetch('/orders', {
            method: 'GET'
        });

        const orderTableBody = document.getElementById('orderTableBody');
        orderTableBody.innerHTML = ''; // Clear existing data

        // Adjust to access the content array inside result
        const orders = data.result.content || data.result || [];

        orders.forEach(order => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${order.id}</td>
                <td>Bàn ${order.tableNumber}</td>
                <td>${formatDateTime(order.createdAt)}</td>
                <td>
                    <span class="badge ${getStatusBadgeClass(order.status)}">
                        ${getStatusText(order.status)}
                    </span>
                </td>
                <td>
                    <div class="btn-group">
                        <button class="btn btn-sm btn-info me-1" onclick="viewOrderDetails(${order.id})">
                            <i class="fas fa-eye"></i>
                        </button>
                        ${getActionButton(order)}
                    </div>
                </td>
            `;
            orderTableBody.appendChild(row);
        });
    } catch (error) {
        console.error('Error fetching orders:', error);
        alert('Không thể tải danh sách đơn hàng: ' + error.message);
    }

    // Update navigation active state
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    const ordersLink = document.querySelector('[onclick="showOrders()"]');
    if (ordersLink) {
        ordersLink.classList.add('active');
    }
}

// Hàm định dạng thời gian
function formatDateTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
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