const state = {
    samples: [],
    maxSamples: 240,
};

const els = {
    hostLine: document.getElementById("hostLine"),
    overallStatus: document.getElementById("overallStatus"),
    overallMessage: document.getElementById("overallMessage"),
    cpuValue: document.getElementById("cpuValue"),
    loadValue: document.getElementById("loadValue"),
    memoryValue: document.getElementById("memoryValue"),
    memoryDetail: document.getElementById("memoryDetail"),
    diskValue: document.getElementById("diskValue"),
    diskDetail: document.getElementById("diskDetail"),
    socketState: document.getElementById("socketState"),
    servicesBody: document.getElementById("servicesBody"),
    eventsList: document.getElementById("eventsList"),
    historyChart: document.getElementById("historyChart"),
    dailyReport: document.getElementById("dailyReport"),
    deployState: document.getElementById("deployState"),
    alertTargets: document.getElementById("alertTargets"),
    rssFeeds: document.getElementById("rssFeeds"),
    pageWatches: document.getElementById("pageWatches"),
    runChecksButton: document.getElementById("runChecksButton"),
    testAlertButton: document.getElementById("testAlertButton"),
};

function formatPercent(value) {
    return Number.isFinite(value) ? `${value.toFixed(1)}%` : "--";
}

function formatBytes(value) {
    if (!Number.isFinite(value) || value <= 0) return "--";
    const units = ["B", "KB", "MB", "GB", "TB"];
    let size = value;
    let index = 0;
    while (size >= 1024 && index < units.length - 1) {
        size /= 1024;
        index += 1;
    }
    return `${size.toFixed(size >= 10 ? 1 : 2)} ${units[index]}`;
}

function formatTime(timestamp) {
    return new Date(timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function statusClass(status) {
    return String(status || "UNKNOWN").toLowerCase();
}

async function getJson(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
    return response.json();
}

function renderSummary(summary) {
    renderHealth(summary.health);
    renderSnapshot(summary.current);
    renderServices(summary.services);
    renderEvents(summary.events);
    renderAutomation(summary.automation);
}

function renderHealth(health) {
    els.overallStatus.textContent = health.status;
    els.overallStatus.className = statusClass(health.status);
    els.overallMessage.textContent = health.message || "ok";
}

function renderSnapshot(snapshot) {
    els.hostLine.textContent = `${snapshot.host} · uptime ${Math.floor(snapshot.uptimeSeconds / 3600)}h · ${formatTime(snapshot.timestamp)}`;
    els.cpuValue.textContent = formatPercent(snapshot.cpu.systemPercent);
    els.loadValue.textContent = `load ${Number.isFinite(snapshot.loadAverage) ? snapshot.loadAverage.toFixed(2) : "--"}`;
    els.memoryValue.textContent = formatPercent(snapshot.memory.usedPercent);
    els.memoryDetail.textContent = `${formatBytes(snapshot.memory.usedBytes)} of ${formatBytes(snapshot.memory.totalBytes)}`;

    const disk = snapshot.disks[0];
    els.diskValue.textContent = disk ? formatPercent(disk.usedPercent) : "--";
    els.diskDetail.textContent = disk ? `${formatBytes(disk.usedBytes)} of ${formatBytes(disk.totalBytes)} at ${disk.path}` : "no disk data";

    state.samples.push(snapshot);
    if (state.samples.length > state.maxSamples) state.samples.shift();
    drawChart();
}

function renderServices(services) {
    if (!services || services.length === 0) {
        els.servicesBody.innerHTML = `<tr><td colspan="5" class="empty">No checks configured yet</td></tr>`;
        return;
    }

    els.servicesBody.innerHTML = services.map((service) => `
        <tr>
            <td>${escapeHtml(service.name)}</td>
            <td>${service.kind}</td>
            <td><span class="cell-status ${statusClass(service.status)}">${service.status}</span></td>
            <td>${service.latencyMs == null ? "--" : `${service.latencyMs} ms`}</td>
            <td>${escapeHtml(service.message || "")}</td>
        </tr>
    `).join("");
}

function renderEvents(events) {
    if (!events || events.length === 0) {
        els.eventsList.innerHTML = `<li class="empty">No events yet</li>`;
        return;
    }

    els.eventsList.innerHTML = events.map((event) => `
        <li>
            <span class="event-time">${formatTime(event.timestamp)}</span>
            <span class="status-pill ${String(event.severity).toLowerCase()}">${event.severity}</span>
            <span class="event-message">
                <strong>${escapeHtml(event.message)}</strong>
                <small>${escapeHtml(event.source)}${event.details ? ` · ${escapeHtml(event.details)}` : ""}</small>
            </span>
        </li>
    `).join("");
}

function renderAutomation(automation) {
    els.dailyReport.textContent = automation.dailyReportTime;
    els.deployState.textContent = automation.deployConfigured ? "Configured" : "Not configured";
    els.alertTargets.textContent = automation.alertTargetsConfigured.length
        ? automation.alertTargetsConfigured.join(", ")
        : "In-app events only";
    els.rssFeeds.textContent = automation.rssFeeds.length
        ? automation.rssFeeds.map((feed) => feed.name).join(", ")
        : "None";
    els.pageWatches.textContent = automation.pageWatches.length
        ? automation.pageWatches.map((watch) => watch.name).join(", ")
        : "None";
}

function drawChart() {
    const canvas = els.historyChart;
    const ctx = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;
    const pad = 28;
    ctx.clearRect(0, 0, width, height);

    ctx.fillStyle = "#fbfcff";
    ctx.fillRect(0, 0, width, height);
    ctx.strokeStyle = "#e5eaf2";
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i += 1) {
        const y = pad + ((height - pad * 2) * i) / 4;
        ctx.beginPath();
        ctx.moveTo(pad, y);
        ctx.lineTo(width - pad, y);
        ctx.stroke();
    }

    drawLine(ctx, state.samples.map((sample) => sample.cpu.systemPercent), "#2563eb", width, height, pad);
    drawLine(ctx, state.samples.map((sample) => sample.memory.usedPercent), "#16a34a", width, height, pad);
}

function drawLine(ctx, values, color, width, height, pad) {
    const clean = values.map((value) => Number.isFinite(value) ? Math.max(0, Math.min(100, value)) : null);
    if (clean.filter((value) => value !== null).length < 2) return;
    const innerWidth = width - pad * 2;
    const innerHeight = height - pad * 2;
    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.beginPath();
    clean.forEach((value, index) => {
        if (value === null) return;
        const x = pad + (innerWidth * index) / Math.max(1, clean.length - 1);
        const y = height - pad - (innerHeight * value) / 100;
        if (index === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
    });
    ctx.stroke();
}

function setSocketState(status) {
    els.socketState.textContent = status;
    els.socketState.className = `status-pill ${statusClass(status === "Live" ? "UP" : status === "Offline" ? "DOWN" : "UNKNOWN")}`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function refreshSummary() {
    const summary = await getJson("/api/summary");
    renderSummary(summary);
}

async function refreshHistory() {
    const since = Date.now() - 60 * 60 * 1000;
    const history = await getJson(`/api/metrics/history?since=${since}`);
    state.samples = history.samples || [];
    drawChart();
}

function connectSocket() {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const socket = new WebSocket(`${protocol}://${window.location.host}/ws/metrics`);
    setSocketState("Connecting");

    socket.addEventListener("open", () => setSocketState("Live"));
    socket.addEventListener("message", (event) => {
        const snapshot = JSON.parse(event.data);
        renderSnapshot(snapshot);
    });
    socket.addEventListener("close", () => {
        setSocketState("Offline");
        setTimeout(connectSocket, 3000);
    });
    socket.addEventListener("error", () => {
        setSocketState("Offline");
        socket.close();
    });
}

els.runChecksButton.addEventListener("click", async () => {
    els.runChecksButton.disabled = true;
    try {
        await getJson("/api/services/run", { method: "POST", body: "{}" });
        await refreshSummary();
    } finally {
        els.runChecksButton.disabled = false;
    }
});

els.testAlertButton.addEventListener("click", async () => {
    els.testAlertButton.disabled = true;
    try {
        await getJson("/api/alerts/test", {
            method: "POST",
            body: JSON.stringify({ message: "Manual alert from dashboard" }),
        });
        await refreshSummary();
    } finally {
        els.testAlertButton.disabled = false;
    }
});

refreshHistory().catch(console.error);
refreshSummary().catch(console.error);
connectSocket();
setInterval(() => refreshSummary().catch(console.error), 15000);
