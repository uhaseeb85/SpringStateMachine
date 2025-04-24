// IVR System Client JavaScript

// Global variables
let sessionId = null;
let currentState = null;
let selectedOption = null;

// DOM Elements
const sessionIdElement = document.getElementById('session-id');
const currentStateElement = document.getElementById('current-state');
const promptMessageElement = document.getElementById('prompt-message');
const startCallButton = document.getElementById('start-call-btn');
const endCallButton = document.getElementById('end-call-btn');
const inputSection = document.getElementById('input-section');
const userInputField = document.getElementById('user-input');
const submitInputButton = document.getElementById('submit-input');
const dynamicButtonsContainer = document.getElementById('dynamic-buttons');
const callHistoryContainer = document.getElementById('call-history');
const numericKeypad = document.getElementById('numeric-keypad');

// Map between button actions and actual input values
const buttonActionMap = {
    // Authentication methods
    'SELECT_SSN_AUTH': '1',
    'SELECT_CARD_AUTH': '2',
    
    // Main menu
    'SELECT_ACCOUNT_SERVICES': '1',
    'END_CALL': '0',
    
    // Account services
    'SELECT_BALANCE_INQUIRY': '1',
    'SELECT_TRANSACTION_HISTORY': '2',
    'SELECT_TRANSFER_FUNDS': '3',
    'BACK': '9',
    
    // Error state
    'BACK': '1',
    'END_CALL': '0'
};

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    initializeNumericKeypad();
});

// Initialize numeric keypad
function initializeNumericKeypad() {
    const keypadButtons = document.querySelectorAll('.keypad-btn');
    keypadButtons.forEach(button => {
        button.addEventListener('click', function() {
            const value = this.getAttribute('data-value');
            userInputField.value += value;
            
            // Add active class for visual feedback
            this.classList.add('btn-primary');
            this.classList.remove('btn-outline');
            
            setTimeout(() => {
                this.classList.remove('btn-primary');
                this.classList.add('btn-outline');
            }, 200);
        });
    });
}

// Initialize event listeners
function initializeEventListeners() {
    // Start call button
    startCallButton.addEventListener('click', startCall);
    
    // End call button
    endCallButton.addEventListener('click', endCall);
    
    // Submit input button
    submitInputButton.addEventListener('click', submitUserInput);
    
    // Allow submission with Enter key
    userInputField.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            submitUserInput();
        }
    });
}

// Start a new call
async function startCall() {
    try {
        resetUI();
        
        // Display connecting message
        promptMessageElement.textContent = "Connecting...";
        addToCallHistory('system', "Connecting to IVR system...");
        
        // Create a new session
        const response = await fetch('/api/ivr/session', {
            method: 'POST'
        });
        
        if (!response.ok) {
            throw new Error('Failed to connect to IVR system');
        }
        
        const data = await response.json();
        sessionId = data.sessionId;
        currentState = data.currentState;
        
        // Update UI
        sessionIdElement.textContent = sessionId;
        sessionIdElement.classList.remove('badge-neutral');
        sessionIdElement.classList.add('badge-success');
        
        currentStateElement.textContent = currentState;
        promptMessageElement.textContent = data.promptMessage;
        
        // Add to call history
        addToCallHistory('system', data.promptMessage);
        
        // Update buttons
        startCallButton.disabled = true;
        endCallButton.disabled = false;
        
        // Show input section if needed
        if (data.nextAction !== 'CONNECT_CALL') {
            inputSection.classList.remove('hidden');
        }
        
        // Handle the next action
        handleNextAction(data);
        
    } catch (error) {
        console.error('Error starting call:', error);
        showToast('Failed to connect: ' + error.message, 'error');
    }
}

// End the current call
async function endCall() {
    try {
        if (!sessionId) return;
        
        // Send end call request
        const response = await fetch(`/api/ivr/session/${sessionId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Failed to end call');
        }
        
        // Update UI
        promptMessageElement.textContent = "Call ended. Thank you for using our service.";
        addToCallHistory('system', "Call ended");
        
        // Reset session
        sessionId = null;
        currentState = null;
        
        // Update UI elements
        sessionIdElement.textContent = "Not Started";
        sessionIdElement.classList.remove('badge-success');
        sessionIdElement.classList.add('badge-neutral');
        
        currentStateElement.textContent = "N/A";
        
        // Update buttons
        startCallButton.disabled = false;
        endCallButton.disabled = true;
        inputSection.classList.add('hidden');
        
        // Show toast
        showToast('Call ended successfully', 'success');
        
    } catch (error) {
        console.error('Error ending call:', error);
        showToast('Failed to end call: ' + error.message, 'error');
    }
}

// Submit user input
async function submitUserInput() {
    // Get the input value (either from text field or selected option)
    const inputType = "text";
    let userInput = userInputField.value.trim();
    
    // If a dynamic button was selected, use its value instead
    if (selectedOption) {
        userInput = buttonActionMap[selectedOption] || selectedOption;
        selectedOption = null;
    }
    
    // Validate input
    if (!userInput) {
        showToast('Please enter a value', 'warning');
        return;
    }
    
    // Add to call history
    addToCallHistory('user', userInput);
    
    // Clear input field
    userInputField.value = '';
    
    // Clear dynamic buttons
    dynamicButtonsContainer.innerHTML = '';
    
    // Send the request
    await sendRequest(inputType, userInput);
}

// Send request to the server
async function sendRequest(inputType, userInput) {
    try {
        if (!sessionId) return;
        
        // Show waiting message
        promptMessageElement.textContent = "Processing...";
        
        // Prepare request body
        const requestBody = {
            sessionId: sessionId,
            inputType: inputType,
            userInput: userInput
        };
        
        // Send request
        const response = await fetch('/api/ivr/process', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to process input');
        }
        
        const data = await response.json();
        
        // Update current state
        currentState = data.currentState;
        currentStateElement.textContent = currentState;
        
        // Update prompt message
        promptMessageElement.textContent = data.promptMessage;
        
        // Add to call history
        addToCallHistory('system', data.promptMessage);
        
        // Handle the next action
        handleNextAction(data);
        
        // If call ended, update UI
        if (data.callEnded) {
            sessionId = null;
            startCallButton.disabled = false;
            endCallButton.disabled = true;
            inputSection.classList.add('hidden');
            
            sessionIdElement.textContent = "Not Started";
            sessionIdElement.classList.remove('badge-success');
            sessionIdElement.classList.add('badge-neutral');
        }
        
    } catch (error) {
        console.error('Error processing input:', error);
        promptMessageElement.textContent = "An error occurred. Please try again.";
        showToast('Error: ' + error.message, 'error');
    }
}

// Determine input type based on state
function determineInputType(state) {
    // Could implement more sophisticated input type determination based on state
    return "text";
}

// Handle the next action based on response
function handleNextAction(response) {
    // Clear any existing buttons
    dynamicButtonsContainer.innerHTML = '';
    
    // Determine which property to use for action handling (some responses may use currentState, others nextAction)
    const actionType = response.nextAction || determineActionFromState(response.currentState);
    
    // Handle different actions
    switch (actionType) {
        case 'COLLECT_AUTH_METHOD':
            createOptionButton('Use SSN (1)', 'SELECT_SSN_AUTH', '<i class="fas fa-id-card mr-1"></i>');
            createOptionButton('Use Card (2)', 'SELECT_CARD_AUTH', '<i class="fas fa-credit-card mr-1"></i>');
            break;
            
        case 'MAIN_MENU':
        case 'COLLECT_MENU_SELECTION':
            createOptionButton('Account Services (1)', 'SELECT_ACCOUNT_SERVICES', '<i class="fas fa-university mr-1"></i>');
            createOptionButton('End Call (0)', 'END_CALL', '<i class="fas fa-phone-slash mr-1"></i>');
            break;
            
        case 'ACCOUNT_SERVICES':
        case 'COLLECT_SERVICE_SELECTION':
            createOptionButton('Balance Inquiry (1)', 'SELECT_BALANCE_INQUIRY', '<i class="fas fa-balance-scale mr-1"></i>');
            createOptionButton('Transaction History (2)', 'SELECT_TRANSACTION_HISTORY', '<i class="fas fa-history mr-1"></i>');
            createOptionButton('Transfer Funds (3)', 'SELECT_TRANSFER_FUNDS', '<i class="fas fa-exchange-alt mr-1"></i>');
            createOptionButton('Back to Main Menu (9)', 'BACK', '<i class="fas fa-arrow-left mr-1"></i>');
            break;
            
        case 'BALANCE_INQUIRY':
        case 'TRANSACTION_HISTORY':
        case 'TRANSFER_FUNDS':
        case 'PRESENT_BALANCE':
        case 'PRESENT_TRANSACTIONS':
        case 'PRESENT_TRANSFER_OPTIONS':
            createOptionButton('Complete Transaction', 'COMPLETE_TRANSACTION', '<i class="fas fa-check-circle mr-1"></i>');
            break;
            
        case 'ERROR':
        case 'COLLECT_ERROR_RESPONSE':
            createOptionButton('Try Again (1)', 'BACK', '<i class="fas fa-redo mr-1"></i>');
            createOptionButton('End Call (0)', 'END_CALL', '<i class="fas fa-phone-slash mr-1"></i>');
            break;
    }
}

// Helper function to determine action based on current state
function determineActionFromState(state) {
    if (!state) return null;
    
    switch (state) {
        case 'AUTHENTICATION_METHOD': return 'COLLECT_AUTH_METHOD';
        case 'MAIN_MENU': return 'COLLECT_MENU_SELECTION';
        case 'ACCOUNT_SERVICES': return 'COLLECT_SERVICE_SELECTION';
        case 'BALANCE_INQUIRY': return 'PRESENT_BALANCE';
        case 'TRANSACTION_HISTORY': return 'PRESENT_TRANSACTIONS';
        case 'TRANSFER_FUNDS': return 'PRESENT_TRANSFER_OPTIONS';
        case 'ERROR': return 'COLLECT_ERROR_RESPONSE';
        default: return state;
    }
}

// Create an option button for the user to select
function createOptionButton(label, action, icon = '') {
    // Fix any FontAwesome margin classes in the icon string
    const fixedIcon = icon.replace(/me-(\d+)/g, 'mr-$1');
    
    const button = document.createElement('button');
    button.innerHTML = `${fixedIcon} ${label}`;
    button.className = 'btn btn-outline btn-primary m-1';
    
    button.addEventListener('click', function() {
        selectedOption = action;
        
        // Visual feedback - highlight all buttons and emphasize the selected one
        document.querySelectorAll('#dynamic-buttons button').forEach(btn => {
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-outline');
        });
        
        this.classList.remove('btn-outline');
        this.classList.add('btn-primary');
        
        // Submit automatically after selection
        submitUserInput();
    });
    
    dynamicButtonsContainer.appendChild(button);
}

// Add entry to call history
function addToCallHistory(source, message) {
    // Create history entry element
    const entryDiv = document.createElement('div');
    entryDiv.className = source === 'user' 
        ? 'bg-blue-50 border-l-4 border-blue-500 p-3 mb-2' 
        : 'bg-gray-50 border-l-4 border-gray-500 p-3 mb-2';
    
    // Create header with icon
    const header = document.createElement('div');
    header.className = 'font-semibold text-sm flex items-center mb-1';
    
    // Set icon and text based on source
    if (source === 'user') {
        header.innerHTML = '<i class="fas fa-user text-blue-500 mr-2"></i> You';
    } else {
        header.innerHTML = '<i class="fas fa-robot text-gray-500 mr-2"></i> System';
    }
    
    // Create message body
    const messageBody = document.createElement('div');
    messageBody.className = 'text-gray-700';
    messageBody.textContent = message;
    
    // Assemble entry
    entryDiv.appendChild(header);
    entryDiv.appendChild(messageBody);
    
    // Add to container at the top
    callHistoryContainer.insertBefore(entryDiv, callHistoryContainer.firstChild);
    
    // Remove placeholder if it exists
    const placeholder = callHistoryContainer.querySelector('.text-gray-400');
    if (placeholder) {
        placeholder.remove();
    }
}

// Show toast notification
function showToast(message, type = 'info') {
    // Check if toast container exists, create if not
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container fixed bottom-4 right-4 z-50 flex flex-col gap-2';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toast = document.createElement('div');
    toast.className = 'toast shadow-lg opacity-0 transform translate-x-full transition-all duration-300';
    
    // Set toast type styling
    switch (type) {
        case 'success':
            toast.classList.add('bg-green-100', 'border-l-4', 'border-green-500', 'text-green-700');
            break;
        case 'error':
            toast.classList.add('bg-red-100', 'border-l-4', 'border-red-500', 'text-red-700');
            break;
        case 'warning':
            toast.classList.add('bg-yellow-100', 'border-l-4', 'border-yellow-500', 'text-yellow-700');
            break;
        default: // info
            toast.classList.add('bg-blue-100', 'border-l-4', 'border-blue-500', 'text-blue-700');
    }
    
    // Set icon based on type
    let icon = 'info-circle';
    if (type === 'success') icon = 'check-circle';
    if (type === 'error') icon = 'exclamation-circle';
    if (type === 'warning') icon = 'exclamation-triangle';
    
    // Set toast content
    toast.innerHTML = `
        <div class="p-4 flex items-start">
            <div class="flex-shrink-0">
                <i class="fas fa-${icon} text-lg"></i>
            </div>
            <div class="ml-3">
                <p class="text-sm">${message}</p>
            </div>
            <div class="ml-auto pl-3">
                <button class="inline-flex text-gray-400 hover:text-gray-500">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
    `;
    
    // Add to container
    toastContainer.appendChild(toast);
    
    // Add close functionality
    toast.querySelector('button').addEventListener('click', () => {
        toast.classList.add('opacity-0', 'translate-x-full');
        setTimeout(() => {
            toast.remove();
        }, 300);
    });
    
    // Show toast with animation
    setTimeout(() => {
        toast.classList.remove('opacity-0', 'translate-x-full');
    }, 10);
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-x-full');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

// Reset UI to initial state
function resetUI() {
    // Clear all input fields and history
    userInputField.value = '';
    
    // Clear call history while keeping the container
    callHistoryContainer.innerHTML = `
        <div class="text-gray-400 text-center py-4">
            <i class="fas fa-info-circle mb-2 text-xl"></i>
            <p>Call history will appear here</p>
        </div>
    `;
    
    // Reset dynamic buttons
    dynamicButtonsContainer.innerHTML = '';
    
    // Hide input section
    inputSection.classList.add('hidden');
    
    // Reset display
    promptMessageElement.textContent = "Press \"Start Call\" to begin";
} 