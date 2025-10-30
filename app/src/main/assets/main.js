// One Button Player - Main Application Logic

// Global state
let currentShow = null;
let shows = JSON.parse(localStorage.getItem('oneButtonShows') || '[]');
let selectedFiles = [];
let currentEvents = [];
let audioPlayers = {};
let currentEventIndex = 0;
let isPlaying = false;

// DOM Elements
const screens = {
    play: document.getElementById('play-screen'),
    configure: document.getElementById('configure-screen'),
    load: document.getElementById('load-screen')
};

const statusIndicator = document.getElementById('statusIndicator');
const statusText = statusIndicator.querySelector('.status-text');
const statusIcon = statusIndicator.querySelector('.status-icon');

// Navigation
document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const screen = btn.dataset.screen;
        
        // Update active nav button
        document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        // Show selected screen
        Object.values(screens).forEach(s => s.classList.remove('active'));
        screens[screen].classList.add('active');
        
        // Load screen-specific data
        if (screen === 'load') {
            loadShowsList();
        } else if (screen === 'play') {
            updatePlayScreen();
        }
    });
});

// Play Screen Controls
document.getElementById('playBtn').addEventListener('click', startShow);
document.getElementById('pauseBtn').addEventListener('click', pauseShow);
document.getElementById('stopBtn').addEventListener('click', stopShow);

// Configure Screen
document.getElementById('selectFilesBtn').addEventListener('click', function() {
    console.log('Select Files button clicked');
    selectFiles();
});
document.getElementById('addEventBtn').addEventListener('click', showEventModal);
document.getElementById('saveShowBtn').addEventListener('click', saveShow);
document.getElementById('clearShowBtn').addEventListener('click', clearShow);

// Event Modal
document.getElementById('cancelEventBtn').addEventListener('click', hideEventModal);
document.getElementById('saveEventBtn').addEventListener('click', saveEvent);

// Bluetooth/Media Button Event Handler
document.addEventListener('keydown', (event) => {
    console.log('Key pressed:', event.key, event.code, event.keyCode);
    
    // Handle various media button codes
    if (event.key === 'MediaPlayPause' || 
        event.code === 'MediaPlayPause' || 
        event.keyCode === 179 || // Standard media play/pause key code
        event.keyCode === 85 || // Additional media button code
        event.code === 'KeyM' || // Fallback for testing
        event.key === ' ' || // Space bar for testing
        event.code === 'Space') {
        
        console.log('Media button detected, handling button press...');
        event.preventDefault();
        event.stopPropagation();
        handleButtonPress();
    }
});

// Alternative event listener for Android WebView
window.addEventListener('keydown', (event) => {
    console.log('Window keydown:', event.key, event.code, event.keyCode);
    
    if (event.key === 'MediaPlayPause' || 
        event.code === 'MediaPlayPause' || 
        event.keyCode === 179 ||
        event.keyCode === 85) {
        
        console.log('Window media button detected');
        event.preventDefault();
        event.stopPropagation();
        handleButtonPress();
    }
});

// Add focus to the document to ensure key events are captured
document.addEventListener('DOMContentLoaded', () => {
    document.body.focus();
    console.log('Document focused for key events');
});

// Initialize
function init() {
    // Load last used show if available
    const lastShowId = localStorage.getItem('lastShowId');
    if (lastShowId) {
        currentShow = shows.find(show => show.id === lastShowId);
        updatePlayScreen();
    }
}

// File Selection - Triggers Android file picker via WebView
function selectFiles() {
    console.log('Opening Android file picker...');
    
    // Create a hidden file input element to trigger the Android file picker
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'audio/*,.mp3,.m4a,.wav,.ogg';
    fileInput.multiple = true;
    fileInput.setAttribute('multiple', 'multiple');
    
    fileInput.addEventListener('change', function(event) {
        const files = event.target.files;
        console.log('Files selected:', files.length);
        
        if (files.length > 0) {
            // Clear existing files and add new ones
            selectedFiles = [];
            
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const fileInfo = {
                    name: file.name,
                    path: file.webkitRelativePath || file.name, // Use file name or relative path
                    uri: file.name, // Store the file name for reference
                    file: file, // Keep File object for current session
                    index: i,
                    // Store the actual file URI for persistent access
                    persistentUri: file.webkitRelativePath || file.name
                };
                selectedFiles.push(fileInfo);
            }
            
            updateSelectedFilesList();
            updateEventTrackDropdown();
            
            alert(`Selected ${files.length} audio file(s) from Android file system`);
        }
    });
    
    // Trigger the file picker
    fileInput.click();
}

function updateSelectedFilesList() {
    const container = document.getElementById('selectedFiles');
    container.innerHTML = '';
    
    selectedFiles.forEach(file => {
        const div = document.createElement('div');
        div.className = 'file-item';
        div.textContent = file.name;
        container.appendChild(div);
    });
}

// Event Management
function showEventModal() {
    if (selectedFiles.length === 0) {
        alert('Please select MP3 files first');
        return;
    }
    
    document.getElementById('eventModal').classList.add('active');
}

function hideEventModal() {
    document.getElementById('eventModal').classList.remove('active');
}

function updateEventTrackDropdown() {
    const select = document.getElementById('eventTrack');
    select.innerHTML = '';
    
    selectedFiles.forEach((file, index) => {
        const option = document.createElement('option');
        option.value = index;
        option.textContent = file.name;
        select.appendChild(option);
    });
}

function saveEvent() {
    const eventType = document.getElementById('eventType').value;
    const trackIndex = parseInt(document.getElementById('eventTrack').value);
    const track = selectedFiles[trackIndex];
    
    const event = {
        type: eventType,
        trackIndex: trackIndex,
        trackName: track.name,
        trackPath: track.path
    };
    
    currentEvents.push(event);
    updateEventsList();
    hideEventModal();
}

function updateEventsList() {
    const container = document.getElementById('eventsList');
    container.innerHTML = '';
    
    currentEvents.forEach((event, index) => {
        const div = document.createElement('div');
        div.className = 'event-item';
        div.innerHTML = `
            <strong>${event.type.toUpperCase()}</strong> - ${event.trackName}
            <button onclick="removeEvent(${index})" style="float: right; background: #dc3545; color: white; border: none; border-radius: 5px; padding: 5px 10px; cursor: pointer;">×</button>
        `;
        container.appendChild(div);
    });
}

function removeEvent(index) {
    currentEvents.splice(index, 1);
    updateEventsList();
}

// Show Management
function saveShow() {
    const showName = document.getElementById('showNameInput').value.trim();
    
    if (!showName) {
        alert('Please enter a show name');
        return;
    }
    
    if (selectedFiles.length === 0) {
        alert('Please select at least one MP3 file');
        return;
    }
    
    if (currentEvents.length === 0) {
        alert('Please add at least one event');
        return;
    }
    
    // Create a clean copy without File objects (which don't persist)
    const filesForStorage = selectedFiles.map(file => ({
        name: file.name,
        path: file.path,
        uri: file.uri,
        index: file.index,
        persistentUri: file.persistentUri
        // Note: file.file (File object) is intentionally omitted as it doesn't persist
    }));
    
    const show = {
        id: Date.now().toString(),
        name: showName,
        files: filesForStorage,
        events: [...currentEvents],
        created: new Date().toISOString()
    };
    
    // Remove existing show with same name
    shows = shows.filter(s => s.name !== showName);
    shows.push(show);
    
    localStorage.setItem('oneButtonShows', JSON.stringify(shows));
    alert('Show saved successfully!');
    clearShow();
}

function clearShow() {
    document.getElementById('showNameInput').value = '';
    selectedFiles = [];
    currentEvents = [];
    updateSelectedFilesList();
    updateEventsList();
}

function loadShowsList() {
    const container = document.getElementById('showsList');
    const noShowsMessage = document.getElementById('noShowsMessage');
    
    if (shows.length === 0) {
        noShowsMessage.style.display = 'block';
        container.innerHTML = '';
        return;
    }
    
    noShowsMessage.style.display = 'none';
    container.innerHTML = '';
    
    shows.forEach(show => {
        const div = document.createElement('div');
        div.className = 'show-item';
        div.innerHTML = `
            <h4>${show.name}</h4>
            <p>Events: ${show.events.length}</p>
            <p>Created: ${new Date(show.created).toLocaleDateString()}</p>
        `;
        div.addEventListener('click', () => loadShow(show));
        container.appendChild(div);
    });
}

function loadShow(show) {
    currentShow = show;
    localStorage.setItem('lastShowId', show.id);
    
    // Reset selectedFiles to match the loaded show
    selectedFiles = show.files || [];
    
    // Clear existing audio players when loading a new show
    Object.values(audioPlayers).forEach(player => {
        if (player) {
            player.pause();
            player.src = '';
        }
    });
    audioPlayers = {};
    
    updatePlayScreen();
    
    // Switch to play screen
    document.querySelector('.nav-btn[data-screen="play"]').click();
}

function updatePlayScreen() {
    const showName = document.getElementById('showName');
    const eventCount = document.getElementById('eventCount');
    const playStatus = document.getElementById('playStatus');
    
    if (currentShow) {
        showName.textContent = currentShow.name;
        eventCount.textContent = currentShow.events.length;
        playStatus.textContent = isPlaying ? 'Playing' : 'Stopped';
        
        // Update status indicator based on play state
        if (isPlaying) {
            statusText.textContent = 'Playing Show';
            statusIcon.textContent = '▶';
            statusIndicator.style.cursor = 'pointer';
            statusIndicator.addEventListener('click', handleStatusClick);
        } else {
            statusText.textContent = 'Ready to Play';
            statusIcon.textContent = '🎵';
            statusIndicator.style.cursor = 'default';
            statusIndicator.removeEventListener('click', handleStatusClick);
        }
    } else {
        showName.textContent = 'None';
        eventCount.textContent = '0';
        playStatus.textContent = 'Stopped';
        
        statusText.textContent = 'No Show Loaded';
        statusIcon.textContent = '❌';
        statusIndicator.style.cursor = 'default';
        statusIndicator.removeEventListener('click', handleStatusClick);
    }
}

// Playback Functions
function startShow() {
    if (!currentShow || currentShow.events.length === 0) {
        alert('No show loaded or no events configured');
        return;
    }
    
    isPlaying = true;
    currentEventIndex = 0;
    updatePlayScreen();
    
    // Visual feedback
    statusIndicator.classList.add('active');
    setTimeout(() => statusIndicator.classList.remove('active'), 300);
}

function pauseShow() {
    isPlaying = false;
    
    // Stop all audio players
    Object.values(audioPlayers).forEach(player => {
        if (player && !player.paused) {
            player.pause();
        }
    });
    
    updatePlayScreen();
}

function stopShow() {
    isPlaying = false;
    currentEventIndex = 0;
    
    // Stop all audio players
    Object.values(audioPlayers).forEach(player => {
        if (player) {
            player.pause();
            player.currentTime = 0;
        }
    });
    
    updatePlayScreen();
}

// Button Press Handler
// Status Indicator Click Handler
function handleStatusClick() {
    if (isPlaying && currentShow) {
        console.log('Status indicator clicked - triggering next event');
        handleButtonPress();
    }
}
function handleButtonPress() {
    if (!isPlaying || !currentShow) return;
    
    if (currentEventIndex >= currentShow.events.length) {
        // Show completed
        statusText.textContent = 'Show Complete!';
        statusIcon.textContent = '🎉';
        isPlaying = false;
        updatePlayScreen();
        return;
    }
    
    const event = currentShow.events[currentEventIndex];
    executeEvent(event);
    currentEventIndex++;
    
    // Update status
    statusText.textContent = `Event ${currentEventIndex}/${currentShow.events.length}`;
    statusIcon.textContent = getEventIcon(event.type);
    
    // Visual feedback
    statusIndicator.classList.add('active');
    setTimeout(() => statusIndicator.classList.remove('active'), 300);
}

function executeEvent(event) {
    const trackId = event.trackIndex;
    
    // Check if we have the track in selectedFiles (for current session)
    // or if we need to use the show's stored file data
    let track = selectedFiles[trackId];
    
    if (!track && currentShow && currentShow.files && currentShow.files[trackId]) {
        // Use the stored file data from the show
        track = currentShow.files[trackId];
    }
    
    if (!track) {
        console.error('Track not found for event:', event);
        alert(`Error: Audio file "${event.trackName}" not found. Please reload the show.`);
        return;
    }
    
    if (!audioPlayers[trackId]) {
        // Try to create audio player with the file
        try {
            if (track.file) {
                // Use the actual File object for playback (current session)
                audioPlayers[trackId] = new Audio(URL.createObjectURL(track.file));
            } else if (track.persistentUri) {
                // Try to use persistent URI for loaded shows
                console.log('Attempting to play track using persistent URI:', track.persistentUri);
                audioPlayers[trackId] = new Audio(track.persistentUri);
            } else {
                // For loaded shows, we need to re-select the files
                console.warn('File object not available for track:', track.name);
                alert(`Cannot play "${event.trackName}". Please re-select the audio files for this show.`);
                return;
            }
            
            // Handle audio loading errors
            audioPlayers[trackId].addEventListener('error', (e) => {
                console.error('Audio loading error:', e);
                alert(`Error loading audio file: ${event.trackName}. Please check file permissions.`);
            });
        } catch (error) {
            console.error('Error creating audio player:', error);
            alert(`Cannot play ${event.trackName}. File may be corrupted or inaccessible.`);
            return;
        }
    }
    
    const player = audioPlayers[trackId];
    
    // Check if player is ready
    if (!player || !player.src) {
        console.error('Audio player not ready for track:', track.name);
        alert(`Cannot play ${event.trackName}. Audio player not ready.`);
        return;
    }
    
    switch (event.type) {
        case 'start':
            player.currentTime = 0;
            player.play().catch(e => {
                console.error('Play error:', e);
                alert(`Cannot play ${event.trackName}. File may be corrupted or inaccessible.`);
            });
            break;
        case 'stop':
            player.pause();
            player.currentTime = 0;
            break;
        case 'pause':
            player.pause();
            break;
        case 'resume':
            player.play().catch(e => {
                console.error('Play error:', e);
                alert(`Cannot play ${event.trackName}. File may be corrupted or inaccessible.`);
            });
            break;
    }
}

function getEventIcon(eventType) {
    const icons = {
        'start': '▶',
        'stop': '⏹',
        'pause': '⏸',
        'resume': '▶'
    };
    return icons[eventType] || '🎵';
}

// Initialize the app
init();