// API Base URL
const API_BASE = '/server';

// DOM Elements
const serversGrid = document.getElementById('serversGrid');
const loadingSpinner = document.getElementById('loadingSpinner');
const emptyState = document.getElementById('emptyState');
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const addServerBtn = document.getElementById('addServerBtn');
const serverModal = document.getElementById('serverModal');
const serverForm = document.getElementById('serverForm');
const modalTitle = document.getElementById('modalTitle');
const closeModal = document.getElementById('closeModal');
const cancelBtn = document.getElementById('cancelBtn');
const submitBtn = document.getElementById('submitBtn');
const notification = document.getElementById('notification');

// State
let servers = [];
let editingServerId = null;
let searchTimeout = null;

// Initialize the application
document.addEventListener('DOMContentLoaded', function () {
    loadServers();
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    // Search functionality
    searchInput.addEventListener('input', handleSearch);
    searchBtn.addEventListener('click', handleSearch);

    // Modal controls
    addServerBtn.addEventListener('click', openAddModal);
    closeModal.addEventListener('click', closeModalHandler);
    cancelBtn.addEventListener('click', closeModalHandler);

    // Form submission
    serverForm.addEventListener('submit', handleFormSubmit);

    // Close modal on outside click
    serverModal.addEventListener('click', function (e) {
        if (e.target === serverModal) {
            closeModalHandler();
        }
    });

    // Close modal on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && serverModal.classList.contains('show')) {
            closeModalHandler();
        }
    });
}

// API Functions
async function apiRequest(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

async function loadServers() {
    try {
        showLoading(true);
        const response = await apiRequest(API_BASE);
        servers = response.data || [];
        renderServers(servers);
    } catch (error) {
        showNotification('Failed to load servers: ' + error.message, 'error');
        servers = [];
        renderServers([]);
    } finally {
        showLoading(false);
    }
}

async function createServer(serverData) {
    const response = await apiRequest(API_BASE, {
        method: 'POST',
        body: JSON.stringify(serverData)
    });
    return response.data;
}

async function updateServer(id, serverData) {
    const response = await apiRequest(`${API_BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(serverData)
    });
    return response.data;
}

async function deleteServer(id) {
    await apiRequest(`${API_BASE}/${id}`, {
        method: 'DELETE'
    });
}

async function searchServerByName(displayName) {
    const response = await apiRequest(`${API_BASE}/name/${encodeURIComponent(displayName)}`);
    return response.data;
}

// UI Functions
function showLoading(show) {
    loadingSpinner.style.display = show ? 'block' : 'none';
    serversGrid.style.display = show ? 'none' : 'grid';
}

function renderServers(serverList) {
    if (serverList.length === 0) {
        serversGrid.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    serversGrid.innerHTML = serverList.map(server => `
        <div class="server-card" data-id="${server.id}">
            <div class="server-header">
                <div>
                    <h3 class="server-title">${escapeHtml(server.displayName)}</h3>
                    <p class="server-id">ID: ${server.id}</p>
                </div>
                <div class="server-actions">
                    <button class="action-btn edit-btn" onclick="openEditModal(${server.id})" title="Edit Server">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete-btn" onclick="confirmDeleteServer(${server.id})" title="Delete Server">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
            <div class="server-info">
                <div class="info-item">
                    <span class="info-label">Channel:</span>
                    <span class="info-value">${escapeHtml(server.channelId)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Map:</span>
                    <span class="info-value">${escapeHtml(server.defaultMap)}</span>
                </div>
            </div>
        </div>
    `).join('');
}

function openAddModal() {
    editingServerId = null;
    modalTitle.textContent = 'Add New Server';
    submitBtn.innerHTML = '<i class="fas fa-plus"></i> Add Server';
    serverForm.reset();
    showModal();
}

function openEditModal(serverId) {
    const server = servers.find(s => s.id === serverId);
    if (!server) {
        showNotification('Server not found', 'error');
        return;
    }

    editingServerId = serverId;
    modalTitle.textContent = 'Edit Server';
    submitBtn.innerHTML = '<i class="fas fa-save"></i> Update Server';

    // Populate form
    document.getElementById('channelId').value = server.channelId;
    document.getElementById('displayName').value = server.displayName;
    document.getElementById('defaultMap').value = server.defaultMap;

    showModal();
}

function showModal() {
    serverModal.classList.add('show');
    document.body.style.overflow = 'hidden';

    // Focus first input
    setTimeout(() => {
        document.getElementById('channelId').focus();
    }, 100);
}

function closeModalHandler() {
    serverModal.classList.remove('show');
    document.body.style.overflow = '';
    editingServerId = null;
    serverForm.reset();
}

async function handleFormSubmit(e) {
    e.preventDefault();

    const formData = new FormData(serverForm);
    const serverData = {
        channelId: formData.get('channelId').trim(),
        displayName: formData.get('displayName').trim(),
        defaultMap: formData.get('defaultMap').trim()
    };

    // Validation
    if (!serverData.channelId || !serverData.displayName || !serverData.defaultMap) {
        showNotification('Please fill in all fields', 'error');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

        if (editingServerId) {
            // Update existing server
            const updatedServer = await updateServer(editingServerId, serverData);
            const index = servers.findIndex(s => s.id === editingServerId);
            if (index !== -1) {
                servers[index] = updatedServer;
            }
            showNotification('Server updated successfully', 'success');
        } else {
            // Create new server
            const newServer = await createServer(serverData);
            servers.push(newServer);
            showNotification('Server created successfully', 'success');
        }

        renderServers(servers);
        closeModalHandler();

    } catch (error) {
        showNotification('Failed to save server: ' + error.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = editingServerId ?
            '<i class="fas fa-save"></i> Update Server' :
            '<i class="fas fa-plus"></i> Add Server';
    }
}

function confirmDeleteServer(serverId) {
    const server = servers.find(s => s.id === serverId);
    if (!server) return;

    if (confirm(`Are you sure you want to delete server "${server.displayName}"?\n\nThis action cannot be undone.`)) {
        handleDeleteServer(serverId);
    }
}

async function handleDeleteServer(serverId) {
    try {
        await deleteServer(serverId);
        servers = servers.filter(s => s.id !== serverId);
        renderServers(servers);
        showNotification('Server deleted successfully', 'success');
    } catch (error) {
        showNotification('Failed to delete server: ' + error.message, 'error');
    }
}

function handleSearch() {
    clearTimeout(searchTimeout);

    searchTimeout = setTimeout(async () => {
        const query = searchInput.value.trim();

        if (!query) {
            renderServers(servers);
            return;
        }

        try {
            // First try exact search by display name
            const exactMatch = await searchServerByName(query);
            renderServers([exactMatch]);
        } catch (error) {
            // If exact match fails, do local filtering
            const filtered = servers.filter(server =>
                server.displayName.toLowerCase().includes(query.toLowerCase()) ||
                server.channelId.toLowerCase().includes(query.toLowerCase()) ||
                server.defaultMap.toLowerCase().includes(query.toLowerCase())
            );
            renderServers(filtered);
        }
    }, 300);
}

function showNotification(message, type = 'info') {
    const notificationIcon = notification.querySelector('.notification-icon');
    const notificationMessage = notification.querySelector('.notification-message');

    // Set icon based on type
    switch (type) {
        case 'success':
            notificationIcon.className = 'notification-icon fas fa-check-circle';
            break;
        case 'error':
            notificationIcon.className = 'notification-icon fas fa-exclamation-circle';
            break;
        default:
            notificationIcon.className = 'notification-icon fas fa-info-circle';
    }

    notificationMessage.textContent = message;
    notification.className = `notification ${type} show`;

    // Auto hide after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
    }, 5000);
}

// Utility Functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Global functions for onclick handlers
window.openEditModal = openEditModal;
window.confirmDeleteServer = confirmDeleteServer;

// Handle page refresh
window.addEventListener('beforeunload', function () {
    if (serverModal.classList.contains('show')) {
        return 'You have unsaved changes. Are you sure you want to leave?';
    }
});
