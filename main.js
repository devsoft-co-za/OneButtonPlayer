// Media event tracker for play/pause button
const statusIndicator = document.getElementById('statusIndicator');
const statusText = statusIndicator.querySelector('.status-text');
const statusIcon = statusIndicator.querySelector('.status-icon');
const eventList = document.getElementById('eventList');

let isPlaying = false;

// Add event listener for media play/pause button
document.addEventListener('keydown', (event) => {
    if (event.key === 'MediaPlayPause') {
        event.preventDefault();
        
        // Toggle play/pause state
        isPlaying = !isPlaying;
        
        // Update visual feedback
        if (isPlaying) {
            statusText.textContent = 'Playing';
            statusIcon.textContent = '▶️';
            statusIndicator.classList.add('playing');
            statusIndicator.classList.remove('paused');
            logEvent('Play button pressed');
        } else {
            statusText.textContent = 'Paused';
            statusIcon.textContent = '⏸️';
            statusIndicator.classList.add('paused');
            statusIndicator.classList.remove('playing');
            logEvent('Pause button pressed');
        }
        
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