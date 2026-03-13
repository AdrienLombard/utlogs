function openTab(evt, tabName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    var currentTab = document.getElementById(tabName);
    currentTab.style.display = "block";
    evt.currentTarget.className += " active";

    // Update subtab indicator if active button exists
    var activeSubButton = currentTab.querySelector('.subtab button.active');
    if (activeSubButton) {
        updateSubtabIndicator(activeSubButton);
    }
}
function openGame(evt, gameName) {
    var i, subtabcontent, subtablinks;
    subtabcontent = document.getElementsByClassName("game-tab-content");
    for (i = 0; i < subtabcontent.length; i++) {
        subtabcontent[i].style.display = "none";
    }
    subtablinks = document.getElementsByClassName("game-tab-link");
    for (i = 0; i < subtablinks.length; i++) {
        subtablinks[i].className = subtablinks[i].className.replace(" active", "");
    }
    document.getElementById(gameName).style.display = "block";
    evt.currentTarget.className += " active";
    updateSubtabIndicator(evt.currentTarget);
}
function openUser(evt, userName) {
    var i, subtabcontent, subtablinks;
    subtabcontent = document.getElementsByClassName("user-tab-content");
    for (i = 0; i < subtabcontent.length; i++) {
        subtabcontent[i].style.display = "none";
    }
    subtablinks = document.getElementsByClassName("user-tab-link");
    for (i = 0; i < subtablinks.length; i++) {
        subtablinks[i].className = subtablinks[i].className.replace(" active", "");
    }
    document.getElementById(userName).style.display = "block";
    evt.currentTarget.className += " active";
    updateSubtabIndicator(evt.currentTarget);
}
function openMapScore(evt, mapName) {
    var i, subtabcontent, subtablinks;
    subtabcontent = document.getElementsByClassName("map-tab-content");
    for (i = 0; i < subtabcontent.length; i++) {
        subtabcontent[i].style.display = "none";
    }
    subtablinks = document.getElementsByClassName("map-tab-link");
    for (i = 0; i < subtablinks.length; i++) {
        subtablinks[i].className = subtablinks[i].className.replace(" active", "");
    }
    document.getElementById(mapName).style.display = "block";
    evt.currentTarget.className += " active";
    updateSubtabIndicator(evt.currentTarget);
}

// Update sliding indicator position
function updateSubtabIndicator(activeButton) {
    var subtab = activeButton.closest('.subtab');
    if (!subtab) return;

    subtab.classList.add('has-active');
    var indicator = subtab;
    var buttonRect = activeButton.getBoundingClientRect();
    var containerRect = subtab.getBoundingClientRect();

    var left = buttonRect.left - containerRect.left;
    var width = buttonRect.width;

    indicator.style.setProperty('--indicator-left', left + 'px');
    indicator.style.setProperty('--indicator-width', width + 'px');
    indicator.style.setProperty('--indicator-top', (buttonRect.bottom - containerRect.top + 5) + 'px');
}

function sortTable(n, tableId) {
    var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
    table = document.getElementById(tableId);
    switching = true;
    dir = "asc";

    // Reset all arrows in this table
    var arrows = table.getElementsByClassName("sort-arrow");
    for (i = 0; i < arrows.length; i++) {
        arrows[i].innerHTML = "";
    }
    // Set initial arrow for clicked column
    var clickedHeader = table.getElementsByTagName("TH")[n];
    var clickedArrow = clickedHeader.getElementsByClassName("sort-arrow")[0];
    if (clickedArrow) clickedArrow.innerHTML = " &#9650;"; // Up arrow

    while (switching) {
        switching = false;
        rows = table.rows;
        for (i = 1; i < (rows.length - 1); i++) {
            shouldSwitch = false;
            x = rows[i].getElementsByTagName("TD")[n];
            y = rows[i + 1].getElementsByTagName("TD")[n];
            var xContent = x.textContent.toLowerCase();
            var yContent = y.textContent.toLowerCase();
            var xNum = parseFloat(xContent.replace(',', '.'));
            var yNum = parseFloat(yContent.replace(',', '.'));

            if (!isNaN(xNum) && !isNaN(yNum)) {
                if (dir == "asc") {
                    if (xNum > yNum) { shouldSwitch = true; break; }
                } else if (dir == "desc") {
                    if (xNum < yNum) { shouldSwitch = true; break; }
                }
            } else {
                if (dir == "asc") {
                    if (xContent > yContent) { shouldSwitch = true; break; }
                } else if (dir == "desc") {
                    if (xContent < yContent) { shouldSwitch = true; break; }
                }
            }
        }
        if (shouldSwitch) {
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            switchcount++;
        } else {
            if (switchcount == 0 && dir == "asc") {
                dir = "desc";
                if (clickedArrow) clickedArrow.innerHTML = " &#9660;"; // Down arrow
                switching = true;
            }
        }
    }
}

// Navigate to Users tab and select specific player
function openPlayerInUsersTab(playerName) {
    // First, open the Users tab
    const usersTab = document.getElementById('Users');
    const tablinks = document.getElementsByClassName('tablinks');
    const tabcontent = document.getElementsByClassName('tabcontent');

    // Hide all tab content
    for (let i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = 'none';
    }

    // Remove active class from all tab links
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(' active', '');
    }

    // Show Users tab and mark it as active
    usersTab.style.display = 'block';
    for (let i = 0; i < tablinks.length; i++) {
        if (tablinks[i].textContent === 'Users') {
            tablinks[i].className += ' active';
            break;
        }
    }

    // Now find and activate the specific user's subtab
    const userSubtabLinks = document.getElementsByClassName('user-tab-link');
    const userSubtabContent = document.getElementsByClassName('user-tab-content');

    // Hide all user subtab content
    for (let i = 0; i < userSubtabContent.length; i++) {
        userSubtabContent[i].style.display = 'none';
    }

    // Remove active class from all user subtab links
    for (let i = 0; i < userSubtabLinks.length; i++) {
        userSubtabLinks[i].className = userSubtabLinks[i].className.replace(' active', '');
    }

    // Find and activate the matching player's subtab
    for (let i = 0; i < userSubtabLinks.length; i++) {
        if (userSubtabLinks[i].textContent === playerName) {
            userSubtabLinks[i].className += ' active';
            const targetId = userSubtabLinks[i].getAttribute('onclick').match(/'([^']+)'/)[1];
            document.getElementById(targetId).style.display = 'block';
            break;
        }
    }
}

// Initialize all charts when the page loads
function initializeCharts() {
    if (typeof window.playerStats === 'undefined' || typeof Chart === 'undefined') {
        console.error('playerStats or Chart.js not loaded');
        return;
    }

    // Define weapon colors
    const weaponColors = [
        "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40",
        "#C9CBCF", "#7BC225", "#F25C54", "#777777", "#E6B0AA", "#D7BDE2",
        "#A9CCE3", "#A3E4D7", "#F9E79F", "#F5CBA7", "#E59866", "#D5DBDB",
        "#AEB6BF", "#566573"
    ];

    // Get all user tabs
    const userTabs = document.querySelectorAll('.user-tab-content');

    userTabs.forEach((userTab, index) => {
        const userId = 'User' + (index + 1);
        const chartCanvas = document.getElementById('chart_' + userId);
        const weaponChartCanvas = document.getElementById('weaponChart_' + userId);

        if (!chartCanvas || !weaponChartCanvas) {
            return;
        }

        // Get player name from the tab's h2 element
        const playerName = userTab.querySelector('h2').textContent;
        const playerData = window.playerStats[playerName];

        if (!playerData) {
            console.warn('No data found for player:', playerName);
            return;
        }

        // Create kills distribution chart
        const killsData = playerData.killsAgainst || {};
        const killsLabels = Object.keys(killsData);
        const killsValues = Object.values(killsData);
        const killsColors = killsLabels.map(victim =>
            window.playerColors[victim] || 'hsl(0, 0%, 50%)'
        );

        new Chart(chartCanvas, {
            type: 'pie',
            data: {
                labels: killsLabels,
                datasets: [{
                    data: killsValues,
                    backgroundColor: killsColors
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                }
            }
        });

        // Create weapon pie chart
        const weapons = playerData.weapons || [];
        const weaponLabels = weapons.map(w => w.name);
        const weaponValues = weapons.map(w => w.kills);
        // The original line `const weaponBgColors = weapons.map((_, i) => weaponColors[i % weaponColors.length]);`
        // is replaced by the new `backgroundColor: weaponColors` in the chart config.
        new Chart(weaponChartCanvas, {
            type: 'pie',
            data: {
                labels: weaponLabels,
                datasets: [{
                    data: weaponValues,
                    backgroundColor: weaponColors
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });

        // Create performance timeline chart
        const timelineChartCanvas = document.getElementById('timelineChart_' + userId);
        if (timelineChartCanvas && playerData.scoresPerGame && playerData.scoresPerGame.length > 0) {
            // Use map names if available, otherwise fall back to "Game X"
            const gameLabels = playerData.scoresPerGame.map((item, idx) => {
                if (typeof item === 'object' && item.map) {
                    return item.map;
                }
                return 'Game ' + (idx + 1);
            });

            // Extract scores (handle both array of numbers and array of objects)
            const scores = playerData.scoresPerGame.map(item =>
                typeof item === 'object' ? item.score : item
            );

            new Chart(timelineChartCanvas, {
                type: 'line',
                data: {
                    labels: gameLabels,
                    datasets: [{
                        label: 'Score',
                        data: scores,
                        borderColor: window.playerColors[playerName] || '#3f8cfb',
                        backgroundColor: 'rgba(63, 140, 251, 0.1)',
                        fill: true,
                        tension: 0.4,
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: window.playerColors[playerName] || '#3f8cfb',
                        pointBorderColor: '#fff',
                        pointBorderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false  // Hide legend
                        }
                    },
                    scales: {
                        x: {
                            grid: { color: 'rgba(255, 255, 255, 0.1)' },
                            ticks: { color: 'white' }
                        },
                        y: {
                            grid: { color: 'rgba(255, 255, 255, 0.1)' },
                            ticks: { color: 'white' },
                            beginAtZero: true
                        }
                    }
                }
            });
        }

        // Create K/D ratio progression timeline chart
        const kdTimelineChartCanvas = document.getElementById('kdTimelineChart_' + userId);
        if (kdTimelineChartCanvas && playerData.killsPerGame && playerData.deathsPerGame) {
            // Use same labels as score timeline (map names)
            const kdLabels = playerData.scoresPerGame.map((item, idx) => {
                if (typeof item === 'object' && item.map) {
                    return item.map;
                }
                return 'Game ' + (idx + 1);
            });

            // Calculate K/D ratio for each game
            const kdRatios = playerData.killsPerGame.map((kills, idx) => {
                const deaths = playerData.deathsPerGame[idx];
                return deaths > 0 ? (kills / deaths) : kills;
            });

            new Chart(kdTimelineChartCanvas, {
                type: 'line',
                data: {
                    labels: kdLabels,
                    datasets: [{
                        label: 'K/D Ratio',
                        data: kdRatios,
                        borderColor: '#a78bfa',
                        backgroundColor: 'rgba(167, 139, 250, 0.1)',
                        fill: true,
                        tension: 0.4,
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: '#a78bfa',
                        pointBorderColor: '#fff',
                        pointBorderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        x: {
                            grid: { color: 'rgba(255, 255, 255, 0.1)' },
                            ticks: { color: 'white' }
                        },
                        y: {
                            grid: { color: 'rgba(255, 255, 255, 0.1)' },
                            ticks: {
                                color: 'white',
                                callback: function (value) {
                                    return value.toFixed(2);
                                }
                            },
                            beginAtZero: true
                        }
                    }
                }
            });
        }
    });
}

// Konami Code Easter Egg
document.addEventListener('DOMContentLoaded', function () {
    const konamiCode = [
        'ArrowUp', 'ArrowUp',
        'ArrowDown', 'ArrowDown',
        'ArrowLeft', 'ArrowRight',
        'ArrowLeft', 'ArrowRight',
        'b', 'a',
        'Enter'
    ];
    let konamiIndex = 0;

    document.addEventListener('keydown', function (e) {
        if (e.key === konamiCode[konamiIndex]) {
            konamiIndex++;
            if (konamiIndex === konamiCode.length) {
                activateEasterEgg();
                konamiIndex = 0;
            }
        } else {
            konamiIndex = 0;
        }
    });

    function activateEasterEgg() {
        const ufoName = "UFO";
        const tentEmoji = " ⛺";

        const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
        let node;
        while (node = walker.nextNode()) {
            const text = node.textContent;
            if (text.includes(ufoName) && !text.includes(tentEmoji)) {
                node.textContent = text.replaceAll(ufoName, ufoName + tentEmoji);
            }
        }
    }
});

// Body Hit Diagram Interaction
document.addEventListener('DOMContentLoaded', function () {
    const interactables = document.querySelectorAll('.body-part');

    interactables.forEach(el => {
        el.addEventListener('mouseenter', function () {
            // Apply highlight
            this.classList.add('highlight');
            
            // Find the panel elements relative to this SVG's container
            const container = this.closest('.hud-container');
            if (!container) return;
            
            const panelPlaceholder = container.querySelector('.details-panel-placeholder');
            const panelStats = container.querySelector('.details-panel-stats');
            const panelCompound = container.querySelector('.details-panel-compound');
            const nameEl = container.querySelector('.details-part-name');
            const pctEl = container.querySelector('.details-part-pct');
            const hitsEl = container.querySelector('.details-part-hits');
            const compoundNameEl = container.querySelector('.details-compound-name');
            const subEntries = container.querySelectorAll('.details-sub-entry');
            
            // Check if compound element (groin/butt)
            const groinName = this.getAttribute('data-groin-name');
            
            if (groinName) {
                // Compound: show both groin and butt
                const name = this.getAttribute('data-name');
                const groinHits = this.getAttribute('data-groin-hits');
                const groinPct = this.getAttribute('data-groin-pct');
                const buttName = this.getAttribute('data-butt-name');
                const buttHits = this.getAttribute('data-butt-hits');
                const buttPct = this.getAttribute('data-butt-pct');
                
                compoundNameEl.textContent = name;
                
                if (subEntries[0]) {
                    subEntries[0].querySelector('.details-sub-name').textContent = groinName;
                    subEntries[0].querySelector('.details-sub-pct').textContent = groinPct + '%';
                    subEntries[0].querySelector('.details-sub-hits').textContent = '(' + groinHits + ' hits)';
                }
                if (subEntries[1]) {
                    subEntries[1].querySelector('.details-sub-name').textContent = buttName;
                    subEntries[1].querySelector('.details-sub-pct').textContent = buttPct + '%';
                    subEntries[1].querySelector('.details-sub-hits').textContent = '(' + buttHits + ' hits)';
                }
                
                panelPlaceholder.classList.add('hidden');
                panelStats.classList.add('hidden');
                panelCompound.classList.remove('hidden');
            } else {
                // Simple: show single body part
                const name = this.getAttribute('data-name');
                const hits = this.getAttribute('data-hits');
                const pct = this.getAttribute('data-pct');
                
                if (name && hits && pct) {
                    nameEl.textContent = name;
                    pctEl.textContent = pct + '%';
                    hitsEl.textContent = '(' + hits + ' hits)';
                    
                    panelPlaceholder.classList.add('hidden');
                    panelCompound.classList.add('hidden');
                    panelStats.classList.remove('hidden');
                }
            }
        });

        el.addEventListener('mouseleave', function () {
            // Remove highlight
            this.classList.remove('highlight');
            
            // Find the panel elements relative to this SVG's container
            const container = this.closest('.hud-container');
            if (!container) return;
            
            const panelPlaceholder = container.querySelector('.details-panel-placeholder');
            const panelStats = container.querySelector('.details-panel-stats');
            const panelCompound = container.querySelector('.details-panel-compound');
            
            // Reset panel
            panelPlaceholder.classList.remove('hidden');
            panelStats.classList.add('hidden');
            panelCompound.classList.add('hidden');
        });
    });

    initHeatmaps();
});

function initHeatmaps() {
    const svgs = document.querySelectorAll('.hud-svg');
    svgs.forEach(svg => {
        const parts = svg.querySelectorAll('.body-part');
        let maxHits = 0;
        
        // Find max hits for this specific diagram
        parts.forEach(part => {
            const hits = parseInt(part.getAttribute('data-hits')) || 0;
            if (hits > maxHits) maxHits = hits;
        });
        
        if (maxHits === 0) return;
        
        // Apply heatmap variables
        parts.forEach(part => {
            const hits = parseInt(part.getAttribute('data-hits')) || 0;
            if (hits > 0) {
                // Map [1, maxHits] to [0, 1] intensity
                const intensity = maxHits > 1 ? (hits - 1) / (maxHits - 1) : 1;
                
                // Monochromatic Cyan gradient to match the UI theme
                const hue = 190; // Cyan
                
                // Increase lightness and opacity as the body part gets hit more
                const lightness = 30 + (intensity * 50); // 30% to 80%
                const fillAlpha = 0.1 + (intensity * 0.7); // 0.1 to 0.8
                
                part.style.setProperty('--heatmap-fill', `hsla(${hue}, 100%, ${lightness}%, ${fillAlpha})`);
                part.style.setProperty('--heatmap-fill-hover', `hsla(${hue}, 100%, ${lightness}%, ${fillAlpha + 0.1})`);
                part.style.setProperty('--heatmap-glow', `hsla(${hue}, 100%, ${lightness + 10}%, 0.8)`);
            }
        });
    });
}

// Update indicators on window resize to handle wrapping
window.addEventListener('resize', function () {
    const activeButtons = document.querySelectorAll('.subtab button.active');
    activeButtons.forEach(button => {
        updateSubtabIndicator(button);
    });
});
