// API Base URLs
const API_BASE = '/server';
const CHANNELS_API_BASE = '/api/channels';

// DOM Elements - Servers
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

// DOM Elements - Server Details Modal
const serverDetailsModal = document.getElementById('serverDetailsModal');
const serverDetailsModalTitle = document.getElementById('serverDetailsModalTitle');
const closeServerDetailsModal = document.getElementById('closeServerDetailsModal');
const serverBasicInfo = document.getElementById('serverBasicInfo');
const serverChannelStats = document.getElementById('serverChannelStats');
const serverSendMessageBtn = document.getElementById('serverSendMessageBtn');
const serverClearMessagesBtn = document.getElementById('serverClearMessagesBtn');
const updateAutoReplyBtn = document.getElementById('updateAutoReplyBtn');
const serverMessagesList = document.getElementById('serverMessagesList');
const serverPrevPageBtn = document.getElementById('serverPrevPageBtn');
const serverNextPageBtn = document.getElementById('serverNextPageBtn');
const serverPageInfo = document.getElementById('serverPageInfo');

// DOM Elements - Update Auto Reply Modal
const updateAutoReplyModal = document.getElementById('updateAutoReplyModal');
const closeUpdateAutoReplyModal = document.getElementById('closeUpdateAutoReplyModal');
const updateAutoReplyForm = document.getElementById('updateAutoReplyForm');
const autoReplyServerId = document.getElementById('autoReplyServerId');
const autoReplyDefaultMap = document.getElementById('autoReplyDefaultMap');
const cancelAutoReplyBtn = document.getElementById('cancelAutoReplyBtn');
const submitAutoReplyBtn = document.getElementById('submitAutoReplyBtn');

// Send Message Modal elements
const sendMessageModal = document.getElementById('sendMessageModal');
const sendMessageForm = document.getElementById('sendMessageForm');
const closeSendMessageModal = document.getElementById('closeSendMessageModal');
const cancelSendMessageBtn = document.getElementById('cancelSendMessageBtn');
const submitSendMessageBtn = document.getElementById('submitSendMessageBtn');
const messageChannel = document.getElementById('messageChannel');
const messageContent = document.getElementById('messageContent');

// Subscription Status Modal elements
const subscriptionStatusModal = document.getElementById('subscriptionStatusModal');
const closeSubscriptionStatusModal = document.getElementById('closeSubscriptionStatusModal');
const subscriptionStatusContent = document.getElementById('subscriptionStatusContent');
const refreshSubscriptionBtn = document.getElementById('refreshSubscriptionBtn');
const subscriptionStatusBtn = document.getElementById('subscriptionStatusBtn');



// State
let servers = [];
let editingServerId = null;
let searchTimeout = null;

// Server details modal state
let currentServer = null;
let currentServerChannel = null;
let currentServerPage = 0;
let totalServerPages = 0;
let serverChannels = [];
let serverMessages = [];
let serverStats = null;

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

    // Server modal controls
    addServerBtn.addEventListener('click', openAddModal);
    closeModal.addEventListener('click', closeModalHandler);
    cancelBtn.addEventListener('click', closeModalHandler);

    // Server form submission
    serverForm.addEventListener('submit', handleFormSubmit);

    // Server details modal controls
    closeServerDetailsModal.addEventListener('click', closeServerDetailsModalHandler);
    updateAutoReplyBtn.addEventListener('click', openUpdateAutoReplyModal);
    serverSendMessageBtn.addEventListener('click', openServerSendMessageModal);
    serverClearMessagesBtn.addEventListener('click', handleServerClearMessages);
    serverPrevPageBtn.addEventListener('click', () => loadServerMessagesPage(currentServerChannel, Math.max(0, currentServerPage - 1)));
    serverNextPageBtn.addEventListener('click', () => loadServerMessagesPage(currentServerChannel, Math.min(totalServerPages - 1, currentServerPage + 1)));

    // Update auto reply modal controls
    closeUpdateAutoReplyModal.addEventListener('click', closeUpdateAutoReplyModalHandler);
    cancelAutoReplyBtn.addEventListener('click', closeUpdateAutoReplyModalHandler);
    updateAutoReplyForm.addEventListener('submit', handleUpdateAutoReply);

    // Send message modal controls
    closeSendMessageModal.addEventListener('click', closeSendMessageModalHandler);
    cancelSendMessageBtn.addEventListener('click', closeSendMessageModalHandler);
    sendMessageForm.addEventListener('submit', handleSendMessage);

    // Subscription status controls
    subscriptionStatusBtn.addEventListener('click', openSubscriptionStatusModal);
    closeSubscriptionStatusModal.addEventListener('click', closeSubscriptionStatusModalHandler);
    refreshSubscriptionBtn.addEventListener('click', handleRefreshSubscription);


    // Close modals on outside click
    serverModal.addEventListener('click', function (e) {
        if (e.target === serverModal) {
            closeModalHandler();
        }
    });

    sendMessageModal.addEventListener('click', function (e) {
        if (e.target === sendMessageModal) {
            closeSendMessageModalHandler();
        }
    });

    subscriptionStatusModal.addEventListener('click', function (e) {
        if (e.target === subscriptionStatusModal) {
            closeSubscriptionStatusModalHandler();
        }
    });


    // Close modals on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            if (serverModal.classList.contains('show')) {
                closeModalHandler();
            } else if (sendMessageModal.classList.contains('show')) {
                closeSendMessageModalHandler();
            } else if (subscriptionStatusModal.classList.contains('show')) {
                closeSubscriptionStatusModalHandler();
            }
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
        <div class="server-card" data-id="${server.id}" onclick="openServerDetailsModal(${server.id})" style="cursor: pointer;">
            <div class="server-header">
                <div>
                    <h3 class="server-title">${escapeHtml(server.displayName)}</h3>
                    <p class="server-id">ID: ${server.id}</p>
                </div>
                <div class="server-actions">
                    <button class="action-btn edit-btn" onclick="event.stopPropagation(); openEditModal(${server.id})" title="Edit Server">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete-btn" onclick="event.stopPropagation(); confirmDeleteServer(${server.id})" title="Delete Server">
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


// Server Details Modal Functions
async function openServerDetailsModal(serverId) {
    try {
        // Find server in current servers list
        currentServer = servers.find(s => s.id === serverId);
        if (!currentServer) {
            showNotification('Server not found', 'error');
            return;
        }

        // Set modal title
        serverDetailsModalTitle.textContent = `Server Details - ${currentServer.displayName}`;

        // Load server basic info
        renderServerBasicInfo();

        // Load channel stats
        await loadServerChannelStats();

        // Load initial messages
        currentServerChannel = currentServer.channelId;
        currentServerPage = 0;
        await loadServerMessagesPage(currentServerChannel, 0);

        // Show modal
        showServerDetailsModal();
    } catch (error) {
        console.error('Error opening server details modal:', error);
        showNotification('Failed to load server details', 'error');
    }
}

// 根据文本长度确定字体大小级别
function getTextLengthClass(text) {
    const length = text.length;
    if (length <= 8) return 'short';
    if (length <= 15) return 'medium';
    if (length <= 25) return 'long';
    return 'very-long';
}

function renderServerBasicInfo() {
    const serverId = currentServer.id.toString();
    const displayName = escapeHtml(currentServer.displayName);
    const channelId = escapeHtml(currentServer.channelId);
    const defaultMap = escapeHtml(currentServer.defaultMap);

    serverBasicInfo.innerHTML = `
        <div class="info-item">
            <span class="info-label">Server ID:</span>
            <span class="info-value" data-length="${getTextLengthClass(serverId)}">${serverId}</span>
        </div>
        <div class="info-item">
            <span class="info-label">Display Name:</span>
            <span class="info-value" data-length="${getTextLengthClass(currentServer.displayName)}">${displayName}</span>
        </div>
        <div class="info-item">
            <span class="info-label">Channel ID:</span>
            <span class="info-value" data-length="${getTextLengthClass(currentServer.channelId)}">${channelId}</span>
        </div>
        <div class="info-item">
            <span class="info-label">Default Map:</span>
            <span class="info-value" data-length="${getTextLengthClass(currentServer.defaultMap)}">${defaultMap}</span>
        </div>
    `;
}

async function loadServerChannelStats() {
    try {
        const response = await apiRequest(`${CHANNELS_API_BASE}/${encodeURIComponent(currentServer.channelId)}/stats`);
        serverStats = response.data;
        renderServerChannelStats(response.data);
    } catch (error) {
        console.error('Error loading server channel stats:', error);
        serverChannelStats.innerHTML = '<p class="error-message">Failed to load channel statistics</p>';
    }
}

function renderServerChannelStats(stats) {
    if (!stats) {
        serverChannelStats.innerHTML = '<p class="no-data">No statistics available</p>';
        return;
    }

    serverChannelStats.innerHTML = `
        <div class="stat-item">
            <span class="stat-label">Total Messages:</span>
            <span class="stat-value">${stats.totalMessages || 0}</span>
        </div>
        <div class="stat-item">
            <span class="stat-label">Auto Reply Messages:</span>
            <span class="stat-value">${stats.autoReplies || 0}</span>
        </div>
        <div class="stat-item">
            <span class="stat-label">Received Messages:</span>
            <span class="stat-value">${stats.receivedMessages || 0}</span>
        </div>
        <div class="stat-item">
            <span class="stat-label">Sent Messages:</span>
            <span class="stat-value">${stats.sentMessages || 0}</span>
        </div>
    `;
}

async function loadServerMessagesPage(channelName, page) {
    try {
        const response = await apiRequest(`${CHANNELS_API_BASE}/${encodeURIComponent(channelName)}/messages?page=${page}`);
        serverMessages = response.data.messages || [];
        currentServerPage = page;
        totalServerPages = response.data.totalPages || 0;

        renderServerMessages(serverMessages);
        updateServerPagination();
    } catch (error) {
        console.error('Error loading server messages:', error);
        serverMessagesList.innerHTML = '<p class="error-message">Failed to load messages</p>';
    }
}

function renderServerMessages(messages) {
    if (!messages || messages.length === 0) {
        serverMessagesList.innerHTML = '<p class="no-data">No messages found</p>';
        return;
    }

    serverMessagesList.innerHTML = messages.map(message => {
        const messageTypeClass = message.isAutoReply ? 'auto-reply' : message.messageType.toLowerCase();
        const messageTypeText = message.isAutoReply ? 'Auto Reply' : message.messageType;

        return `
            <div class="message-item ${messageTypeClass}">
                <div class="message-header">
                    <span class="message-type ${messageTypeClass}">${messageTypeText}</span>
                    <span class="message-time">${formatDateTime(message.messageTime)}</span>
                </div>
                <div class="message-content">${escapeHtml(message.messageContent)}</div>
                ${message.remark ? `<div class="message-remark">${escapeHtml(message.remark)}</div>` : ''}
            </div>
        `;
    }).join('');
}

function updateServerPagination() {
    serverPrevPageBtn.disabled = currentServerPage <= 0;
    serverNextPageBtn.disabled = currentServerPage >= totalServerPages - 1;
    serverPageInfo.textContent = `Page ${currentServerPage + 1} of ${totalServerPages}`;
}

function showServerDetailsModal() {
    serverDetailsModal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeServerDetailsModalHandler() {
    serverDetailsModal.classList.remove('show');
    document.body.style.overflow = 'auto';

    // Reset state
    currentServer = null;
    currentServerChannel = null;
    currentServerPage = 0;
    totalServerPages = 0;
    serverMessages = [];
    serverStats = null;
}

function openServerSendMessageModal() {
    if (!currentServer) return;

    messageChannel.value = currentServer.channelId;
    messageContent.value = '';
    sendMessageModal.classList.add('show');
}

async function handleServerClearMessages() {
    if (!currentServer) return;

    const confirmed = confirm(`Are you sure you want to clear all messages from channel "${currentServer.channelId}"?`);
    if (!confirmed) return;

    try {
        await clearChannelMessages(currentServer.channelId);
        showNotification('Messages cleared successfully', 'success');

        // Reload messages and stats
        await loadServerChannelStats();
        await loadServerMessagesPage(currentServerChannel, 0);
    } catch (error) {
        console.error('Error clearing messages:', error);
        showNotification('Failed to clear messages', 'error');
    }
}

function openUpdateAutoReplyModal() {
    if (!currentServer) return;

    autoReplyServerId.value = currentServer.id;
    autoReplyDefaultMap.value = currentServer.defaultMap;
    updateAutoReplyModal.classList.add('show');
}

function closeUpdateAutoReplyModalHandler() {
    updateAutoReplyModal.classList.remove('show');
    updateAutoReplyForm.reset();
}

async function handleUpdateAutoReply(e) {
    e.preventDefault();

    const serverId = autoReplyServerId.value;
    const defaultMap = autoReplyDefaultMap.value.trim();

    if (!defaultMap) {
        showNotification('Default map is required', 'error');
        return;
    }

    try {
        submitAutoReplyBtn.disabled = true;
        submitAutoReplyBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';

        const response = await updateServer(parseInt(serverId), {
            channelId: currentServer.channelId,
            displayName: currentServer.displayName,
            defaultMap: defaultMap
        });

        showNotification('Auto reply updated successfully', 'success');
        closeUpdateAutoReplyModalHandler();

        // Update current server data
        currentServer.defaultMap = defaultMap;
        renderServerBasicInfo();

        // Reload servers list
        await loadServers();
    } catch (error) {
        console.error('Error updating auto reply:', error);
        showNotification('Failed to update auto reply', 'error');
    } finally {
        submitAutoReplyBtn.disabled = false;
        submitAutoReplyBtn.innerHTML = '<i class="fas fa-save"></i> Update Auto Reply';
    }
}

// Channel API Functions
async function sendChannelMessage(channelName, message) {
    try {
        const response = await apiRequest(`${CHANNELS_API_BASE}/${encodeURIComponent(channelName)}/send`, {
            method: 'POST',
            body: JSON.stringify({message})
        });
        return response;
    } catch (error) {
        console.error('Failed to send message:', error);
        throw error;
    }
}

async function clearChannelMessages(channelName, days = null) {
    try {
        const url = days ?
            `${CHANNELS_API_BASE}/${encodeURIComponent(channelName)}/messages?days=${days}` :
            `${CHANNELS_API_BASE}/${encodeURIComponent(channelName)}/messages`;

        const response = await apiRequest(url, {
            method: 'DELETE'
        });
        return response;
    } catch (error) {
        console.error('Failed to clear messages:', error);
        throw error;
    }
}

async function getSubscriptionStatus() {
    try {
        const response = await apiRequest(`${CHANNELS_API_BASE}/subscription/status`);
        return response.data;
    } catch (error) {
        console.error('Failed to get subscription status:', error);
        throw error;
    }
}

async function refreshSubscription() {
    try {
        const response = await apiRequest(`${CHANNELS_API_BASE}/subscription/refresh`, {
            method: 'POST'
        });
        return response;
    } catch (error) {
        console.error('Failed to refresh subscription:', error);
        throw error;
    }
}

// Send Message Modal Functions
function closeSendMessageModalHandler() {
    sendMessageModal.classList.remove('show');
    sendMessageForm.reset();
}

async function handleSendMessage(e) {
    e.preventDefault();

    const channel = messageChannel.value;
    const message = messageContent.value.trim();

    if (!message) {
        showNotification('Message content is required', 'error');
        return;
    }

    try {
        submitSendMessageBtn.disabled = true;
        submitSendMessageBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';

        await sendChannelMessage(channel, message);
        showNotification('Message sent successfully', 'success');

        closeSendMessageModalHandler();

        // Refresh messages if server details modal is open
        if (currentServer && currentServer.channelId === channel) {
            await loadServerMessagesPage(currentServerChannel, 0);
        }
    } catch (error) {
        showNotification('Failed to send message: ' + error.message, 'error');
    } finally {
        submitSendMessageBtn.disabled = false;
        submitSendMessageBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Send Message';
    }
}

// Subscription Status Modal Functions
async function openSubscriptionStatusModal() {
    try {
        const status = await getSubscriptionStatus();
        renderSubscriptionStatus(status);
        subscriptionStatusModal.classList.add('show');
    } catch (error) {
        showNotification('Failed to load subscription status: ' + error.message, 'error');
    }
}

function renderSubscriptionStatus(status) {
    subscriptionStatusContent.innerHTML = `
        <div class="status-item">
            <span class="status-label">Status</span>
            <span class="status-value">${status.status}</span>
        </div>
        <div class="status-item">
            <span class="status-label">Subscribed Channels</span>
            <span class="status-value">${status.subscribedCount}</span>
        </div>
        ${status.subscribedChannels.length > 0 ? `
            <div class="subscribed-channels-list">
                ${status.subscribedChannels.map(channel =>
        `<div class="subscribed-channel-item">${escapeHtml(channel)}</div>`
    ).join('')}
            </div>
        ` : ''}
    `;
}

function closeSubscriptionStatusModalHandler() {
    subscriptionStatusModal.classList.remove('show');
}

async function handleRefreshSubscription() {
    try {
        refreshSubscriptionBtn.disabled = true;
        refreshSubscriptionBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Refreshing...';

        await refreshSubscription();
        showNotification('Subscription refreshed successfully', 'success');

        // Reload subscription status
        const status = await getSubscriptionStatus();
        renderSubscriptionStatus(status);
    } catch (error) {
        showNotification('Failed to refresh subscription: ' + error.message, 'error');
    } finally {
        refreshSubscriptionBtn.disabled = false;
        refreshSubscriptionBtn.innerHTML = '<i class="fas fa-sync-alt"></i> Refresh Subscription';
    }
}

// Utility Functions
function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString();
}

// Global functions for onclick handlers
window.openEditModal = openEditModal;
window.confirmDeleteServer = confirmDeleteServer;
window.openServerDetailsModal = openServerDetailsModal;

// Handle page refresh
window.addEventListener('beforeunload', function () {
    if (serverModal.classList.contains('show')) {
        return 'You have unsaved changes. Are you sure you want to leave?';
    }

    // Clear any pending timeouts
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }
});
