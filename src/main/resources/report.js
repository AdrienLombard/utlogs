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

        function safeUpdate(element) {
            let updated = false;
            for (let i = 0; i < element.childNodes.length; i++) {
                const node = element.childNodes[i];
                if (node.nodeType === 3) { // Node.TEXT_NODE
                    const text = node.textContent;
                    // Handle strict match
                    if (text.trim() === ufoName) {
                        // Replace text content of the node only
                        node.textContent = text.replace(ufoName, ufoName + tentEmoji);
                        updated = true;
                    }
                    // Handle "UFO (123)" case
                    else if (text.includes(ufoName + " (")) {
                        node.textContent = text.replace(ufoName, ufoName + tentEmoji);
                        updated = true;
                    }
                }
            }
            return updated;
        }

        // Target specific elements containing player names
        const selectors = [
            '.player-name-link',     // Main table links
            '.user-tab-link',        // User tab buttons
            '.subtablinks',          // Subtab links
            'h2',                    // Headers
            '.tabcontent h2'         // Tab content headers
        ];

        selectors.forEach(selector => {
            const elements = document.querySelectorAll(selector);
            elements.forEach(el => {
                if ((el.textContent.includes(ufoName)) && !el.textContent.includes(tentEmoji)) {
                    safeUpdate(el);
                }
            });
        });

        // Also update table cells that are just the name
        const tds = document.querySelectorAll('td');
        tds.forEach(td => {
            if (td.textContent.includes(ufoName) && !td.textContent.includes(tentEmoji)) {
                safeUpdate(td);
            }
        });
    }
});

// Body Hit Diagram Interaction
document.addEventListener('DOMContentLoaded', function () {
    const interactables = document.querySelectorAll('[data-part]');

    interactables.forEach(el => {
        el.addEventListener('mouseenter', function () {
            const part = this.getAttribute('data-part');
            document.querySelectorAll(`[data-part="${part}"]`).forEach(target => {
                target.classList.add('highlight');
            });
        });

        el.addEventListener('mouseleave', function () {
            const part = this.getAttribute('data-part');
            document.querySelectorAll(`[data-part="${part}"]`).forEach(target => {
                target.classList.remove('highlight');
            });
        });
    });
});

// Update indicators on window resize to handle wrapping
window.addEventListener('resize', function () {
    const activeButtons = document.querySelectorAll('.subtab button.active');
    activeButtons.forEach(button => {
        updateSubtabIndicator(button);
    });
});
