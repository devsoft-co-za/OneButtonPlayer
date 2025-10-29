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
document.getElementById('selectFilesBtn').addEventListener('click', selectFiles);
document.getElementById('addEventBtn').addEventListener('click', showEventModal);
document.getElementById('saveShowBtn').addEventListener('click', saveShow);
document.getElementById('clearShowBtn').addEventListener('click', clearShow);

// Event Modal
document.getElementById('cancelEventBtn').addEventListener('click', hideEventModal);
document.getElementById('saveEventBtn').addEventListener('click', saveEvent);

// Bluetooth/Media Button Event Handler
document.addEventListener('keydown', (event) => {
    if (event.key === 'MediaPlayPause') {
        event.preventDefault();
        handleButtonPress();
    }
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

// File Selection (Placeholder - would need Android WebView integration)
function selectFiles() {
    // In a real implementation, this would trigger Android file picker
    // For now, we'll simulate file selection
    const simulatedFiles = [
        { name: 'song1.mp3', path: 'file:///android_asset/song.mp3' },
        { name: 'song2.mp3', path: 'file:///storage/emulated/0/Music/song2.mp3' },
        { name: 'song3.mp3', path: 'file:///storage/emulated/0/Music/song3.mp3' }
    ];
    
    selectedFiles = simulatedFiles;
    updateSelectedFilesList();
    updateEventTrackDropdown();
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
    
    if (currentEvents.length === 0) {
        alert('Please add at least one event');
        return;
    }
    
    const show = {
        id: Date.now().toString(),
        name: showName,
        files: selectedFiles,
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
        
        statusText.textContent = 'Ready to Play';
        statusIcon.textContent = '🎵';
    } else {
        showName.textContent = 'None';
        eventCount.textContent = '0';
        playStatus.textContent = 'Stopped';
        
        statusText.textContent = 'No Show Loaded';
        statusIcon.textContent = '❌';
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
    
    statusText.textContent = 'Playing Show';
    statusIcon.textContent = '▶';
    
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
    statusText.textContent = 'Paused';
    statusIcon.textContent = '⏸';
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
    statusText.textContent = 'Stopped';
    statusIcon.textContent = '⏹';
}

// Button Press Handler
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
    
    if (!audioPlayers[trackId]) {
        audioPlayers[trackId] = new Audio(event.trackPath);
    }
    
    const player = audioPlayers[trackId];
    
    switch (event.type) {
        case 'start':
            player.currentTime = 0;
            player.play();
            break;
        case 'stop':
            player.pause();
            player.currentTime = 0;
            break;
        case 'pause':
            player.pause();
            break;
        case 'resume':
            player.play();
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
