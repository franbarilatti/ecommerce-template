// FILE: admin/scripts/admin.js
// Propósito: Lógica del panel de administración
// Descripción: CRUD de productos, gestión de pedidos y usuarios
// Uso: Importar en admin/index.html después de main.js

(function () {
  "use strict";

  // ===== VERIFICACIÓN DE ACCESO =====
  function checkAdminAccess() {
    const user = JSON.parse(localStorage.getItem("aguardi_user") || "null");

    if (!user || !user.logged || user.role !== "admin") {
      alert(
        "Acceso denegado. Solo administradores pueden acceder a esta página."
      );
      window.location.href = "../login.html?redirect=admin/index.html";
      return false;
    }

    return true;
  }

  // Verificar acceso inmediatamente
  if (!checkAdminAccess()) return;

  // ===== ESTADO =====
  let state = {
    currentSection: "dashboard",
    products: [],
    orders: [],
    users: [],
    currentProduct: null,
    orderFilter: "all",
  };

  // ===== ELEMENTOS DEL DOM =====
  const elements = {
    // Navigation
    sidebarLinks: document.querySelectorAll(".sidebar-link"),
    sections: document.querySelectorAll(".admin-section"),
    adminUserName: document.getElementById("adminUserName"),
    adminLogout: document.getElementById("adminLogout"),

    // Stats
    statProducts: document.getElementById("statProducts"),
    statOrders: document.getElementById("statOrders"),
    statUsers: document.getElementById("statUsers"),
    statRevenue: document.getElementById("statRevenue"),
    ordersBadge: document.getElementById("ordersBadge"),

    // Quick Actions
    quickActions: document.querySelectorAll("[data-action]"),

    // Products
    addProductBtn: document.getElementById("addProductBtn"),
    productsTableBody: document.getElementById("productsTableBody"),
    productModal: document.getElementById("productModal"),
    productForm: document.getElementById("productForm"),
    productModalSave: document.getElementById("productModalSave"),
    productModalCancel: document.getElementById("productModalCancel"),
    productModalClose: document.getElementById("productModalClose"),

    // Orders
    filterTabs: document.querySelectorAll(".filter-tab"),
    ordersTableBody: document.getElementById("ordersTableBody"),
    orderModal: document.getElementById("orderModal"),
    orderModalBody: document.getElementById("orderModalBody"),
    orderModalClose: document.getElementById("orderModalClose"),
    orderModalClose2: document.getElementById("orderModalClose2"),

    // Users
    usersTableBody: document.getElementById("usersTableBody"),
  };

  // ===== UTILIDADES =====

  function formatPrice(price) {
    return `$${price.toLocaleString("es-AR")}`;
  }

  function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString("es-AR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  function getCategoryName(category) {
    const names = {
      baby: "Bebé",
      boy: "Niño",
      girl: "Niña",
      party: "Fiesta",
      accessories: "Accesorios",
    };
    return names[category] || category;
  }

  // ===== NAVEGACIÓN =====

  function switchSection(sectionName) {
    // Actualizar sidebar
    elements.sidebarLinks.forEach((link) => {
      link.classList.remove("active");
      if (link.dataset.section === sectionName) {
        link.classList.add("active");
      }
    });

    // Actualizar secciones
    elements.sections.forEach((section) => {
      section.classList.remove("active");
      if (section.id === `${sectionName}-section`) {
        section.classList.add("active");
      }
    });

    state.currentSection = sectionName;

    // Cargar datos de la sección
    if (sectionName === "products") {
      renderProductsTable();
    } else if (sectionName === "orders") {
      renderOrdersTable();
    } else if (sectionName === "users") {
      renderUsersTable();
    }
  }

  // ===== DATOS =====

  function loadData() {
    // Cargar productos
    const productsData = localStorage.getItem("aguardi_products");
    state.products = productsData
      ? JSON.parse(productsData)
      : getMockProducts();

    // Cargar pedidos
    const ordersData = localStorage.getItem("aguardi_orders");
    state.orders = ordersData ? JSON.parse(ordersData) : [];

    // Cargar usuarios
    const usersData = localStorage.getItem("aguardi_users");
    state.users = usersData ? JSON.parse(usersData) : [];

    // Guardar productos mock si no existen
    if (!productsData) {
      localStorage.setItem("aguardi_products", JSON.stringify(state.products));
    }

    updateStats();
  }

  function getMockProducts() {
    return [
      {
        id: 1,
        name: "Traje Elegante Niño",
        description: "Traje completo perfecto para bodas y eventos especiales",
        price: 12990,
        category: "boy",
        isNew: true,
        onSale: false,
        stock: 15,
      },
      {
        id: 2,
        name: "Vestido de Fiesta Niña",
        description: "Elegante vestido para cualquier ocasión especial",
        price: 15990,
        category: "girl",
        isNew: false,
        onSale: true,
        stock: 8,
      },
      {
        id: 3,
        name: "Set de Accesorios Premium",
        description: "Moños, tiradores y corbatas de alta calidad",
        price: 3990,
        category: "accessories",
        isNew: false,
        onSale: false,
        stock: 25,
      },
    ];
  }

  function saveProducts() {
    localStorage.setItem("aguardi_products", JSON.stringify(state.products));
    updateStats();
    renderProductsTable();
  }

  // ===== STATS =====

  function updateStats() {
    // Total productos
    elements.statProducts.textContent = state.products.length;

    // Pedidos pendientes
    const pendingOrders = state.orders.filter(
      (o) => o.status === "pending"
    ).length;
    elements.statOrders.textContent = pendingOrders;
    elements.ordersBadge.textContent = pendingOrders;

    // Total usuarios
    elements.statUsers.textContent = state.users.length;

    // Ventas totales
    const totalRevenue = state.orders.reduce((sum, order) => {
      return sum + (order.totals?.total || 0);
    }, 0);
    elements.statRevenue.textContent = formatPrice(totalRevenue);
  }

  // ===== PRODUCTOS =====

  function renderProductsTable() {
    elements.productsTableBody.innerHTML = "";

    if (state.products.length === 0) {
      elements.productsTableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 2rem; color: #6B7280;">
                        No hay productos. Agrega tu primer producto.
                    </td>
                </tr>
            `;
      return;
    }

    state.products.forEach((product) => {
      const row = document.createElement("tr");

      const status = product.stock > 0 ? "active" : "inactive";
      const statusText = product.stock > 0 ? "Activo" : "Agotado";

      row.innerHTML = `
                <td>${product.id}</td>
                <td><strong>${product.name}</strong></td>
                <td>${getCategoryName(product.category)}</td>
                <td>${formatPrice(product.price)}</td>
                <td>${product.stock}</td>
                <td><span class="status-badge status-${status}">${statusText}</span></td>
                <td>
                    <div class="table-actions">
                        <button class="btn-icon-sm btn-edit" onclick="window.adminEditProduct(${
                          product.id
                        })">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                            </svg>
                        </button>
                        <button class="btn-icon-sm btn-delete" onclick="window.adminDeleteProduct(${
                          product.id
                        })">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                    </div>
                </td>
            `;

      elements.productsTableBody.appendChild(row);
    });
  }

  function openProductModal(product = null) {
    state.currentProduct = product;

    if (product) {
      // Editar producto
      document.getElementById("productModalTitle").textContent =
        "Editar Producto";
      document.getElementById("productId").value = product.id;
      document.getElementById("productName").value = product.name;
      document.getElementById("productCategory").value = product.category;
      document.getElementById("productPrice").value = product.price;
      document.getElementById("productStock").value = product.stock;
      document.getElementById("productDescription").value =
        product.description || "";
      document.getElementById("productIsNew").checked = product.isNew || false;
      document.getElementById("productOnSale").checked =
        product.onSale || false;
    } else {
      // Nuevo producto
      document.getElementById("productModalTitle").textContent =
        "Agregar Producto";
      elements.productForm.reset();
      document.getElementById("productId").value = "";
    }

    elements.productModal.classList.add("active");
  }

  function closeProductModal() {
    elements.productModal.classList.remove("active");
    elements.productForm.reset();
    state.currentProduct = null;
  }

  function saveProduct() {
    const id = document.getElementById("productId").value;
    const name = document.getElementById("productName").value.trim();
    const category = document.getElementById("productCategory").value;
    const price = parseFloat(document.getElementById("productPrice").value);
    const stock = parseInt(document.getElementById("productStock").value);
    const description = document
      .getElementById("productDescription")
      .value.trim();
    const isNew = document.getElementById("productIsNew").checked;
    const onSale = document.getElementById("productOnSale").checked;

    if (!name || !category || !price || stock < 0) {
      alert("Por favor completa todos los campos obligatorios");
      return;
    }

    if (id) {
      // Editar producto existente
      const index = state.products.findIndex((p) => p.id === parseInt(id));
      if (index !== -1) {
        state.products[index] = {
          ...state.products[index],
          name,
          category,
          price,
          stock,
          description,
          isNew,
          onSale,
        };
      }
    } else {
      // Crear nuevo producto
      const newProduct = {
        id: Date.now(),
        name,
        category,
        price,
        stock,
        description,
        isNew,
        onSale,
      };
      state.products.push(newProduct);
    }

    saveProducts();
    closeProductModal();

    if (window.AGUARDI && window.AGUARDI.showNotification) {
      window.AGUARDI.showNotification(
        "Producto guardado correctamente",
        "success"
      );
    }
  }

  function deleteProduct(id) {
    if (!confirm("¿Estás seguro de eliminar este producto?")) {
      return;
    }

    state.products = state.products.filter((p) => p.id !== id);
    saveProducts();

    if (window.AGUARDI && window.AGUARDI.showNotification) {
      window.AGUARDI.showNotification("Producto eliminado", "success");
    }
  }
  // ===== PEDIDOS =====
  function renderOrdersTable() {
    // Filtrar pedidos según tab activo
    let filteredOrders = state.orders;
    if (state.orderFilter !== "all") {
      filteredOrders = state.orders.filter(
        (o) => o.status === state.orderFilter
      );
    }

    elements.ordersTableBody.innerHTML = "";

    if (filteredOrders.length === 0) {
      elements.ordersTableBody.innerHTML = `
            <tr><td colspan="6" style="text-align: center; padding: 2rem;">
                No hay pedidos
            </td></tr>
        `;
      return;
    }

    filteredOrders.forEach((order) => {
      const row = document.createElement("tr");
      row.innerHTML = `
            <td>#${order.id.split("-")[1]}</td>
            <td>${order.customer.fullName}</td>
            <td>${formatDate(order.date)}</td>
            <td>${formatPrice(order.totals.total)}</td>
            <td><span class="status-badge status-${order.status}">${
        order.status
      }</span></td>
            <td>
                <button class="btn-icon-sm btn-view" onclick="window.adminViewOrder('${
                  order.id
                }')">
                    Ver
                </button>
            </td>
        `;
      elements.ordersTableBody.appendChild(row);
    });
  }

  function viewOrder(orderId) {
    const order = state.orders.find((o) => o.id === orderId);
    if (!order) return;

    elements.orderModalBody.innerHTML = `
        <div><strong>Cliente:</strong> ${order.customer.fullName}</div>
        <div><strong>Email:</strong> ${order.customer.email}</div>
        <div><strong>Teléfono:</strong> ${order.customer.phone}</div>
        <hr>
        <h4>Productos:</h4>
        ${order.items
          .map(
            (item) => `
            <div>${item.name} x${item.quantity} - ${formatPrice(
              item.price * item.quantity
            )}</div>
        `
          )
          .join("")}
        <hr>
        <div><strong>Total:</strong> ${formatPrice(order.totals.total)}</div>
    `;

    elements.orderModal.classList.add("active");
  }

  // ===== USUARIOS =====
  function renderUsersTable() {
    elements.usersTableBody.innerHTML = "";

    state.users.forEach((user) => {
      const row = document.createElement("tr");
      row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.fullName}</td>
            <td>${user.email}</td>
            <td>${user.phone}</td>
            <td><span class="status-badge status-${user.role}">${
        user.role
      }</span></td>
            <td>${formatDate(user.createdAt)}</td>
            <td>
                <button class="btn-icon-sm btn-delete" onclick="if(confirm('¿Eliminar usuario?')) window.adminDeleteUser(${
                  user.id
                })">
                    Eliminar
                </button>
            </td>
        `;
      elements.usersTableBody.appendChild(row);
    });
  }

  function deleteUser(userId) {
    state.users = state.users.filter((u) => u.id !== userId);
    localStorage.setItem("aguardi_users", JSON.stringify(state.users));
    updateStats();
    renderUsersTable();
  }

  // ===== EVENT LISTENERS =====
  function initEventListeners() {
    // Sidebar navigation
    elements.sidebarLinks.forEach((link) => {
      link.addEventListener("click", (e) => {
        e.preventDefault();
        switchSection(link.dataset.section);
      });
    });

    // Logout
    elements.adminLogout.addEventListener("click", () => {
      if (window.AGUARDI && window.AGUARDI.logout) {
        window.AGUARDI.logout();
      }
    });

    // Quick actions
    elements.quickActions.forEach((btn) => {
      btn.addEventListener("click", () => {
        const action = btn.dataset.action;
        if (action === "add-product") {
          openProductModal();
        } else if (action === "view-orders") {
          switchSection("orders");
        } else if (action === "export-data") {
          exportData();
        }
      });
    });

    // Product modal
    elements.addProductBtn.addEventListener("click", () => openProductModal());
    elements.productModalSave.addEventListener("click", saveProduct);
    elements.productModalCancel.addEventListener("click", closeProductModal);
    elements.productModalClose.addEventListener("click", closeProductModal);

    // Order modal
    elements.orderModalClose.addEventListener("click", () => {
      elements.orderModal.classList.remove("active");
    });
    elements.orderModalClose2.addEventListener("click", () => {
      elements.orderModal.classList.remove("active");
    });

    // Order filters
    elements.filterTabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        elements.filterTabs.forEach((t) => t.classList.remove("active"));
        tab.classList.add("active");
        state.orderFilter = tab.dataset.filter;
        renderOrdersTable();
      });
    });
  }

  function exportData() {
    const data = {
      products: state.products,
      orders: state.orders,
      users: state.users.map((u) => ({ ...u, password: undefined })),
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `aguardi-backup-${Date.now()}.json`;
    a.click();
  }

  // ===== FUNCIONES GLOBALES =====
  window.adminEditProduct = (id) => {
    const product = state.products.find((p) => p.id === id);
    if (product) openProductModal(product);
  };

  window.adminDeleteProduct = deleteProduct;
  window.adminViewOrder = viewOrder;
  window.adminDeleteUser = deleteUser;

  // ===== INICIALIZACIÓN =====
  function init() {
    const user = JSON.parse(localStorage.getItem("aguardi_user"));
    if (user) {
      elements.adminUserName.textContent = user.fullName;
    }

    loadData();
    initEventListeners();
    renderProductsTable();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
