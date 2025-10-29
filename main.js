// Media event tracker for play/pause button
const statusIndicator = document.getElementById('statusIndicator');
const statusText = statusIndicator.querySelector('.status-text');
const statusIcon = statusIndicator.querySelector('.status-icon');
const eventList = document.getElementById('eventList');

let pressCount = 0;

// Add event listener for media play/pause button
document.addEventListener('keydown', (event) => {
    if (event.key === 'MediaPlayPause') {
        event.preventDefault();
        
        // Increment counter
        pressCount++;
        
        // Update visual feedback
        statusText.textContent = `Button Pressed: ${pressCount}`;
        statusIcon.textContent = '🎵';
        
        // Generate random color
        const randomColor = `#${Math.floor(Math.random()*16777215).toString(16)}`;
        statusIndicator.style.background = `linear-gradient(135deg, ${randomColor}, ${randomColor}99)`;
        statusIndicator.style.borderColor = randomColor;
        
        logEvent(`Play/Pause button pressed ${pressCount}`);
        
        // Add visual feedback animation
        statusIndicator.classList.add('active');
        setTimeout(() => {
            statusIndicator.classList.remove('active');
        }, 300);
    }
});

// Log events to the event list
function logEvent(message) {
    const timestamp = new Date().toLocaleTimeString();
    const listItem = document.createElement('li');
    listItem.innerHTML = `<span class="timestamp">[${timestamp}]</span> ${message}`;
    
    // Add to top of list
    eventList.insertBefore(listItem, eventList.firstChild);
    
    // Limit to 10 events
    if (eventList.children.length > 10) {
        eventList.removeChild(eventList.lastChild);
    }
}
