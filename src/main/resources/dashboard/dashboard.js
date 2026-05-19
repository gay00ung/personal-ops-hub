const LOCALE_KEY = "personal-ops-hub-locale";

const translations = {
    en: {
        brandSubtitle: "personal server",
        languageLabel: "Language",
        navOverview: "Overview",
        navServices: "Services",
        navEvents: "Events",
        navAutomation: "Automation",
        waitingTelemetry: "Waiting for server telemetry...",
        runChecks: "Run checks",
        testAlert: "Test alert",
        metricOverall: "Overall",
        metricMemory: "Memory",
        metricDisk: "Disk",
        noStatusYet: "No status yet",
        load: "load",
        usedOf: "{used} of {total}",
        diskAt: "{used} of {total} at {path}",
        noDiskData: "no disk data",
        hostLine: "{host} · uptime {uptime} · {time}",
        hourShort: "h",
        minuteShort: "m",
        lastHour: "Last Hour",
        lastHourDesc: "CPU and memory trend from stored samples.",
        serviceHealth: "Service Health",
        serviceHealthDesc: "HTTP, TCP, Docker, and backup checks.",
        tableName: "Name",
        tableKind: "Kind",
        tableStatus: "Status",
        tableLatency: "Latency",
        tableMessage: "Message",
        noChecksYet: "No checks yet",
        noChecksConfigured: "No checks configured yet",
        automation: "Automation",
        automationDesc: "Reports, webhooks, RSS, and page watches.",
        dailyReport: "Daily report",
        deployWebhook: "Deploy webhook",
        alertTargets: "Alert targets",
        rssFeeds: "RSS feeds",
        pageWatches: "Page watches",
        configured: "Configured",
        notConfigured: "Not configured",
        inAppEventsOnly: "In-app events only",
        none: "None",
        recentEvents: "Recent Events",
        recentEventsDesc: "Incidents, recoveries, reports, deploys, and backup results.",
        noEventsYet: "No events yet",
        websocket: "WebSocket",
        socketConnecting: "Connecting",
        socketLive: "Live",
        socketOffline: "Offline",
        ok: "ok",
        serviceChecksFailing: "{count} service checks failing",
        statusUP: "UP",
        statusDEGRADED: "DEGRADED",
        statusDOWN: "DOWN",
        statusUNKNOWN: "UNKNOWN",
        severityINFO: "INFO",
        severityWARNING: "WARNING",
        severityCRITICAL: "CRITICAL",
        kindHTTP: "HTTP",
        kindTCP: "TCP",
        kindDOCKER: "Docker",
        kindBACKUP: "Backup",
        manualAlertMessage: "Manual alert from dashboard",
    },
    ko: {
        brandSubtitle: "개인 서버",
        languageLabel: "언어",
        navOverview: "개요",
        navServices: "서비스",
        navEvents: "이벤트",
        navAutomation: "자동화",
        waitingTelemetry: "서버 상태를 기다리는 중...",
        runChecks: "검사 실행",
        testAlert: "알림 테스트",
        metricOverall: "전체 상태",
        metricMemory: "메모리",
        metricDisk: "디스크",
        noStatusYet: "아직 상태 없음",
        load: "부하",
        usedOf: "{total} 중 {used}",
        diskAt: "{path} 경로, {total} 중 {used}",
        noDiskData: "디스크 데이터 없음",
        hostLine: "{host} · 가동 {uptime} · {time}",
        hourShort: "시간",
        minuteShort: "분",
        lastHour: "최근 1시간",
        lastHourDesc: "저장된 샘플 기준 CPU와 메모리 추이입니다.",
        serviceHealth: "서비스 상태",
        serviceHealthDesc: "HTTP, TCP, Docker, 백업 검사를 확인합니다.",
        tableName: "이름",
        tableKind: "종류",
        tableStatus: "상태",
        tableLatency: "응답 시간",
        tableMessage: "메시지",
        noChecksYet: "아직 검사 없음",
        noChecksConfigured: "설정된 검사가 없습니다",
        automation: "자동화",
        automationDesc: "리포트, 웹훅, RSS, 페이지 변경 감지입니다.",
        dailyReport: "일일 리포트",
        deployWebhook: "배포 웹훅",
        alertTargets: "알림 대상",
        rssFeeds: "RSS 피드",
        pageWatches: "페이지 감지",
        configured: "설정됨",
        notConfigured: "설정 안 됨",
        inAppEventsOnly: "앱 이벤트만 기록",
        none: "없음",
        recentEvents: "최근 이벤트",
        recentEventsDesc: "장애, 복구, 리포트, 배포, 백업 결과입니다.",
        noEventsYet: "아직 이벤트 없음",
        websocket: "웹소켓",
        socketConnecting: "연결 중",
        socketLive: "실시간",
        socketOffline: "오프라인",
        ok: "정상",
        serviceChecksFailing: "서비스 검사 {count}개 실패",
        statusUP: "정상",
        statusDEGRADED: "주의",
        statusDOWN: "장애",
        statusUNKNOWN: "알 수 없음",
        severityINFO: "정보",
        severityWARNING: "경고",
        severityCRITICAL: "긴급",
        kindHTTP: "HTTP",
        kindTCP: "TCP",
        kindDOCKER: "Docker",
        kindBACKUP: "백업",
        manualAlertMessage: "대시보드 수동 알림",
    },
};

const state = {
    samples: [],
    maxSamples: 240,
    locale: getInitialLocale(),
    latestSummary: null,
    latestSnapshot: null,
    latestSocketState: "websocket",
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
    languageToggle: document.querySelector(".language-toggle"),
    localeButtons: document.querySelectorAll("[data-locale]"),
};

function getInitialLocale() {
    const saved = localStorage.getItem(LOCALE_KEY);
    if (saved === "ko" || saved === "en") return saved;
    return navigator.language?.toLowerCase().startsWith("ko") ? "ko" : "en";
}

function t(key, vars = {}) {
    const template = translations[state.locale][key] || translations.en[key] || key;
    return Object.entries(vars).reduce((text, [name, value]) => {
        return text.replaceAll(`{${name}}`, String(value));
    }, template);
}

function setLocale(locale) {
    state.locale = locale;
    localStorage.setItem(LOCALE_KEY, locale);
    applyLocale();
}

function applyLocale() {
    document.documentElement.lang = state.locale;
    document.querySelectorAll("[data-i18n]").forEach((node) => {
        node.textContent = t(node.dataset.i18n);
    });
    els.languageToggle.setAttribute("aria-label", t("languageLabel"));
    els.localeButtons.forEach((button) => {
        const active = button.dataset.locale === state.locale;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", String(active));
    });
    setSocketState(state.latestSocketState);
    if (state.latestSummary) renderSummary(state.latestSummary, false);
    else if (state.latestSnapshot) renderSnapshot(state.latestSnapshot, false);
    drawChart();
}

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
    const locale = state.locale === "ko" ? "ko-KR" : "en-US";
    return new Date(timestamp).toLocaleTimeString(locale, { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function formatUptime(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    if (state.locale === "ko") return `${hours}${t("hourShort")} ${minutes}${t("minuteShort")}`;
    return `${hours}${t("hourShort")} ${minutes}${t("minuteShort")}`;
}

function statusClass(status) {
    return String(status || "UNKNOWN").toLowerCase();
}

function statusLabel(status) {
    return t(`status${String(status || "UNKNOWN").toUpperCase()}`);
}

function severityLabel(severity) {
    return t(`severity${String(severity || "INFO").toUpperCase()}`);
}

function kindLabel(kind) {
    return t(`kind${String(kind || "UNKNOWN").toUpperCase()}`);
}

async function getJson(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
    return response.json();
}

function renderSummary(summary, remember = true) {
    if (remember) state.latestSummary = summary;
    renderHealth(summary.health);
    renderSnapshot(summary.current, remember);
    renderServices(summary.services);
    renderEvents(summary.events);
    renderAutomation(summary.automation);
}

function renderHealth(health) {
    els.overallStatus.textContent = statusLabel(health.status);
    els.overallStatus.className = statusClass(health.status);
    els.overallMessage.textContent = localizeHealthMessage(health.message);
}

function renderSnapshot(snapshot, remember = true) {
    if (remember) {
        state.latestSnapshot = snapshot;
        state.samples.push(snapshot);
        if (state.samples.length > state.maxSamples) state.samples.shift();
    }

    els.hostLine.textContent = t("hostLine", {
        host: snapshot.host,
        uptime: formatUptime(snapshot.uptimeSeconds),
        time: formatTime(snapshot.timestamp),
    });
    els.cpuValue.textContent = formatPercent(snapshot.cpu.systemPercent);
    els.loadValue.textContent = `${t("load")} ${Number.isFinite(snapshot.loadAverage) ? snapshot.loadAverage.toFixed(2) : "--"}`;
    els.memoryValue.textContent = formatPercent(snapshot.memory.usedPercent);
    els.memoryDetail.textContent = t("usedOf", {
        used: formatBytes(snapshot.memory.usedBytes),
        total: formatBytes(snapshot.memory.totalBytes),
    });

    const disk = snapshot.disks[0];
    els.diskValue.textContent = disk ? formatPercent(disk.usedPercent) : "--";
    els.diskDetail.textContent = disk
        ? t("diskAt", { used: formatBytes(disk.usedBytes), total: formatBytes(disk.totalBytes), path: disk.path })
        : t("noDiskData");

    drawChart();
}

function renderServices(services) {
    if (!services || services.length === 0) {
        els.servicesBody.innerHTML = `<tr><td colspan="5" class="empty">${escapeHtml(t("noChecksConfigured"))}</td></tr>`;
        return;
    }

    els.servicesBody.innerHTML = services.map((service) => `
        <tr>
            <td>${escapeHtml(service.name)}</td>
            <td>${escapeHtml(kindLabel(service.kind))}</td>
            <td><span class="cell-status ${statusClass(service.status)}">${escapeHtml(statusLabel(service.status))}</span></td>
            <td>${service.latencyMs == null ? "--" : `${service.latencyMs} ms`}</td>
            <td>${escapeHtml(localizeServiceMessage(service.message || ""))}</td>
        </tr>
    `).join("");
}

function renderEvents(events) {
    if (!events || events.length === 0) {
        els.eventsList.innerHTML = `<li class="empty">${escapeHtml(t("noEventsYet"))}</li>`;
        return;
    }

    els.eventsList.innerHTML = events.map((event) => `
        <li>
            <span class="event-time">${formatTime(event.timestamp)}</span>
            <span class="status-pill ${String(event.severity).toLowerCase()}">${escapeHtml(severityLabel(event.severity))}</span>
            <span class="event-message">
                <strong>${escapeHtml(localizeEventMessage(event.message))}</strong>
                <small>${escapeHtml(localizeSource(event.source))}${event.details ? ` · ${escapeHtml(localizeServiceMessage(event.details))}` : ""}</small>
            </span>
        </li>
    `).join("");
}

function renderAutomation(automation) {
    els.dailyReport.textContent = automation.dailyReportTime;
    els.deployState.textContent = automation.deployConfigured ? t("configured") : t("notConfigured");
    els.alertTargets.textContent = automation.alertTargetsConfigured.length
        ? automation.alertTargetsConfigured.join(", ")
        : t("inAppEventsOnly");
    els.rssFeeds.textContent = automation.rssFeeds.length
        ? automation.rssFeeds.map((feed) => feed.name).join(", ")
        : t("none");
    els.pageWatches.textContent = automation.pageWatches.length
        ? automation.pageWatches.map((watch) => watch.name).join(", ")
        : t("none");
}

function localizeHealthMessage(message) {
    if (!message || message === "ok") return t("ok");
    const failedMatch = message.match(/^(\d+) service checks failing$/);
    if (failedMatch) return t("serviceChecksFailing", { count: failedMatch[1] });
    return replaceMetricWords(message);
}

function localizeServiceMessage(message) {
    if (state.locale !== "ko") return message;
    return message
        .replace(/^container running$/i, "컨테이너 실행 중")
        .replace(/^container not running$/i, "컨테이너가 실행 중이 아님")
        .replace(/^backup marker missing:/i, "백업 마커 없음:")
        .replace(/^last success (\d+)m ago$/i, "마지막 성공 $1분 전")
        .replace(/^port (\d+) open$/i, "$1 포트 열림")
        .replace(/^request failed$/i, "요청 실패")
        .replace(/^connection failed$/i, "연결 실패");
}

function localizeEventMessage(message) {
    if (state.locale !== "ko") return message;

    const diskMatch = message.match(/^Disk (.+) is at ([\d.]+)% \(threshold ([\d.]+)%\)$/);
    if (diskMatch) return `디스크 ${diskMatch[1]} 사용률 ${diskMatch[2]}% (기준 ${diskMatch[3]}%)`;

    const memoryMatch = message.match(/^Memory usage is at ([\d.]+)% \(threshold ([\d.]+)%\)$/);
    if (memoryMatch) return `메모리 사용률 ${memoryMatch[1]}% (기준 ${memoryMatch[2]}%)`;

    const cpuMatch = message.match(/^CPU usage is at ([\d.]+)% \(threshold ([\d.]+)%\)$/);
    if (cpuMatch) return `CPU 사용률 ${cpuMatch[1]}% (기준 ${cpuMatch[2]}%)`;

    const serviceDownMatch = message.match(/^(.+) is down$/);
    if (serviceDownMatch) return `${serviceDownMatch[1]} 장애`;

    const recoveredMatch = message.match(/^(.+) recovered$/);
    if (recoveredMatch) return `${recoveredMatch[1]} 복구됨`;

    const exact = {
        "application started": "앱 시작됨",
        "application stopped": "앱 중지됨",
        "Manual alert from dashboard": "대시보드 수동 알림",
        "deploy command succeeded": "배포 명령 성공",
        "deploy command failed": "배포 명령 실패",
        "GitHub webhook accepted": "GitHub 웹훅 수신됨",
        "GitHub webhook rejected: invalid signature": "GitHub 웹훅 거부됨: 서명 오류",
    };
    return exact[message] || replaceMetricWords(message);
}

function localizeSource(source) {
    if (state.locale !== "ko") return source;
    return source
        .replace(/^metric:memory$/, "지표:메모리")
        .replace(/^metric:cpu$/, "지표:CPU")
        .replace(/^metric:disk:/, "지표:디스크:")
        .replace(/^service:/, "서비스:")
        .replace(/^backup:/, "백업:")
        .replace(/^daily-report$/, "일일 리포트")
        .replace(/^alert-test$/, "알림 테스트")
        .replace(/^ops-hub$/, "Ops Hub");
}

function replaceMetricWords(message) {
    if (state.locale !== "ko") return message;
    return message
        .replace(/\bmemory\b/gi, "메모리")
        .replace(/\bdisk\b/gi, "디스크")
        .replace(/\bservice checks failing\b/gi, "서비스 검사 실패");
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
    state.latestSocketState = status;
    const labelKey = {
        websocket: "websocket",
        connecting: "socketConnecting",
        live: "socketLive",
        offline: "socketOffline",
    }[status] || "websocket";
    els.socketState.textContent = t(labelKey);
    els.socketState.className = `status-pill ${status === "live" ? "up" : status === "offline" ? "down" : "unknown"}`;
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
    setSocketState("connecting");

    socket.addEventListener("open", () => setSocketState("live"));
    socket.addEventListener("message", (event) => {
        const snapshot = JSON.parse(event.data);
        renderSnapshot(snapshot);
    });
    socket.addEventListener("close", () => {
        setSocketState("offline");
        setTimeout(connectSocket, 3000);
    });
    socket.addEventListener("error", () => {
        setSocketState("offline");
        socket.close();
    });
}

els.localeButtons.forEach((button) => {
    button.addEventListener("click", () => setLocale(button.dataset.locale));
});

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

applyLocale();
refreshHistory().catch(console.error);
refreshSummary().catch(console.error);
connectSocket();
setInterval(() => refreshSummary().catch(console.error), 15000);
