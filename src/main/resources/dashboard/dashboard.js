const LOCALE_KEY = "personal-ops-hub-locale";
const THEME_KEY = "personal-ops-hub-theme";

const translations = {
    en: {
        brandSubtitle: "personal server",
        languageLabel: "Language",
        themeLabel: "Theme",
        themeSystem: "System",
        themeLight: "Light",
        themeDark: "Dark",
        navOverview: "Overview",
        navServices: "Services",
        navJobs: "Jobs",
        navEvents: "Events",
        navAutomation: "Automation",
        navApiDocs: "API Docs",
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
        automationStatusOK: "OK",
        automationStatusWARNING: "Attention",
        automationStatusUNKNOWN: "Waiting",
        automationLastChecked: "Checked {time}",
        automationNeverChecked: "Not checked yet",
        jobsInventory: "Jobs & Inventory",
        jobsInventoryDesc: "Read-only view of cron, systemd timers, services, containers, and listening ports.",
        refreshJobs: "Refresh",
        inventoryLoading: "Loading inventory...",
        inventoryUpdated: "Updated {time}",
        inventoryProblemCount: "{count} problems",
        noInventoryProblems: "No failed services or timers",
        inventoryUnavailable: "Unavailable",
        inventoryEmpty: "No entries",
        inventoryColumnName: "Name",
        inventoryColumnStatus: "Status",
        inventoryColumnSchedule: "Schedule",
        inventoryColumnDetail: "Detail",
        inventoryColumnCommand: "Command",
        inventoryColumnActions: "Actions",
        inventorySectionUSER_CRONTAB: "User crontab",
        inventorySectionSYSTEM_CRONTAB: "System crontab",
        inventorySectionCRON_D: "Cron drop-ins",
        inventorySectionCRON_PERIODIC: "Periodic cron scripts",
        inventorySectionSYSTEMD_TIMERS: "Systemd timers",
        inventorySectionFAILED_SERVICES: "Failed services",
        inventorySectionFAILED_TIMERS: "Failed timers",
        inventorySectionRUNNING_SERVICES: "Running services",
        inventorySectionDOCKER_CONTAINERS: "Docker containers",
        inventorySectionLISTENING_PORTS: "Listening ports",
        inventorySectionMANAGED_SYSTEMD_UNITS: "Managed systemd units",
        inventorySectionMANAGED_DOCKER_CONTAINERS: "Managed Docker containers",
        manageActionSTART: "Start",
        manageActionSTOP: "Stop",
        manageActionRESTART: "Restart",
        managementRunning: "Running {action} on {name}...",
        managementSucceeded: "{action} completed for {name}",
        managementFailed: "{action} failed for {name}",
        confirmStop: "Stop {name}?",
        viewLogs: "Logs",
        logsTitle: "Logs",
        logsEmpty: "Select a log-enabled systemd unit or Docker container.",
        logsLoading: "Loading logs for {name}...",
        logsUpdated: "Updated {time} · last {lines} lines",
        logsFailed: "Failed to load logs · exit {exitCode}",
        closeLogs: "Close",
        quickJump: "Search",
        quickJumpTitle: "Search dashboard",
        quickJumpClose: "Close",
        quickJumpInputLabel: "Search",
        quickJumpPlaceholder: "Search sections, services, containers, ports, events",
        quickJumpNoResults: "No matching results",
        quickJumpGroupSection: "Section",
        quickJumpGroupService: "Service",
        quickJumpGroupInventory: "Inventory",
        quickJumpGroupEvent: "Event",
        recentEvents: "Recent Events",
        recentEventsDesc: "Incidents, recoveries, reports, deploys, and backup results. Recoveries can close incidents automatically.",
        eventSearchLabel: "Search",
        eventSearchPlaceholder: "Search message, source, or details",
        eventSeverityFilter: "Severity",
        eventStateFilter: "Response status",
        filterAll: "All",
        eventStateOPEN: "Needs action",
        eventStateACKNOWLEDGED: "Acknowledged",
        eventStateRESOLVED: "Done",
        eventActionLog: "Log",
        selectEventPrompt: "Select an event to inspect it.",
        eventDetails: "Event Details",
        eventMessage: "Message",
        eventSource: "Source",
        eventTimestamp: "Time",
        eventStatus: "Response status",
        eventStateHelp: "Needs action means the alert is still open. Recoveries can close incidents automatically; use these buttons only for manual triage.",
        eventDetailsLabel: "Details",
        markOpen: "Mark needs action",
        markAcknowledged: "Mark acknowledged",
        markResolved: "Mark done",
        noOpenEvents: "No action items need attention",
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
        themeLabel: "테마",
        themeSystem: "시스템",
        themeLight: "라이트",
        themeDark: "다크",
        navOverview: "개요",
        navServices: "서비스",
        navJobs: "작업",
        navEvents: "이벤트",
        navAutomation: "자동화",
        navApiDocs: "API 문서",
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
        automationStatusOK: "정상",
        automationStatusWARNING: "확인 필요",
        automationStatusUNKNOWN: "대기 중",
        automationLastChecked: "{time} 확인",
        automationNeverChecked: "아직 확인 전",
        jobsInventory: "작업 및 인벤토리",
        jobsInventoryDesc: "cron, systemd timer, 서비스, 컨테이너, 열린 포트를 읽기 전용으로 확인합니다.",
        refreshJobs: "새로고침",
        inventoryLoading: "인벤토리 불러오는 중...",
        inventoryUpdated: "{time} 갱신",
        inventoryProblemCount: "문제 {count}개",
        noInventoryProblems: "실패한 서비스나 타이머 없음",
        inventoryUnavailable: "사용 불가",
        inventoryEmpty: "항목 없음",
        inventoryColumnName: "이름",
        inventoryColumnStatus: "상태",
        inventoryColumnSchedule: "스케줄",
        inventoryColumnDetail: "상세",
        inventoryColumnCommand: "명령",
        inventoryColumnActions: "작업",
        inventorySectionUSER_CRONTAB: "사용자 crontab",
        inventorySectionSYSTEM_CRONTAB: "시스템 crontab",
        inventorySectionCRON_D: "cron.d 작업",
        inventorySectionCRON_PERIODIC: "주기별 cron 스크립트",
        inventorySectionSYSTEMD_TIMERS: "systemd 타이머",
        inventorySectionFAILED_SERVICES: "실패한 서비스",
        inventorySectionFAILED_TIMERS: "실패한 타이머",
        inventorySectionRUNNING_SERVICES: "실행 중인 서비스",
        inventorySectionDOCKER_CONTAINERS: "Docker 컨테이너",
        inventorySectionLISTENING_PORTS: "열린 포트",
        inventorySectionMANAGED_SYSTEMD_UNITS: "관리 허용 systemd 유닛",
        inventorySectionMANAGED_DOCKER_CONTAINERS: "관리 허용 Docker 컨테이너",
        manageActionSTART: "시작",
        manageActionSTOP: "중지",
        manageActionRESTART: "재시작",
        managementRunning: "{name}에 {action} 실행 중...",
        managementSucceeded: "{name} {action} 완료",
        managementFailed: "{name} {action} 실패",
        confirmStop: "{name}을(를) 중지할까요?",
        viewLogs: "로그",
        logsTitle: "로그",
        logsEmpty: "로그를 볼 수 있는 systemd 유닛이나 Docker 컨테이너를 선택하세요.",
        logsLoading: "{name} 로그 불러오는 중...",
        logsUpdated: "{time} 갱신 · 최근 {lines}줄",
        logsFailed: "로그 불러오기 실패 · 종료 코드 {exitCode}",
        closeLogs: "닫기",
        quickJump: "검색",
        quickJumpTitle: "대시보드 검색",
        quickJumpClose: "닫기",
        quickJumpInputLabel: "검색",
        quickJumpPlaceholder: "섹션, 서비스, 컨테이너, 포트, 이벤트 검색",
        quickJumpNoResults: "일치하는 결과 없음",
        quickJumpGroupSection: "섹션",
        quickJumpGroupService: "서비스",
        quickJumpGroupInventory: "인벤토리",
        quickJumpGroupEvent: "이벤트",
        recentEvents: "최근 이벤트",
        recentEventsDesc: "장애, 복구, 리포트, 배포, 백업 결과입니다. 복구가 감지되면 관련 장애는 자동으로 완료될 수 있습니다.",
        eventSearchLabel: "검색",
        eventSearchPlaceholder: "메시지, 출처, 상세 내용 검색",
        eventSeverityFilter: "심각도",
        eventStateFilter: "대응 상태",
        filterAll: "전체",
        eventStateOPEN: "조치 필요",
        eventStateACKNOWLEDGED: "확인 중",
        eventStateRESOLVED: "완료",
        eventActionLog: "기록",
        selectEventPrompt: "이벤트를 선택하면 상세 내용을 볼 수 있습니다.",
        eventDetails: "이벤트 상세",
        eventMessage: "메시지",
        eventSource: "출처",
        eventTimestamp: "시간",
        eventStatus: "대응 상태",
        eventStateHelp: "조치 필요는 아직 열린 알림입니다. 복구가 감지되면 자동으로 완료될 수 있고, 버튼은 직접 확인하거나 정리할 때만 사용합니다.",
        eventDetailsLabel: "상세",
        markOpen: "조치 필요로 되돌리기",
        markAcknowledged: "확인 중으로 표시",
        markResolved: "조치 완료로 표시",
        noOpenEvents: "조치 필요한 항목이 없습니다",
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
    theme: getInitialTheme(),
    latestSummary: null,
    latestSnapshot: null,
    latestInventory: null,
    latestLog: null,
    latestSocketState: "websocket",
    navLockUntil: 0,
    quickJumpOpen: false,
    quickJumpQuery: "",
    quickJumpIndex: 0,
    events: [],
    selectedEventId: null,
    eventSearchTimer: null,
    eventFilters: {
        severity: "ALL",
        state: "OPEN",
        query: "",
    },
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
    eventDetail: document.getElementById("eventDetail"),
    eventsSearchInput: document.getElementById("eventsSearchInput"),
    eventSeverityButtons: document.querySelectorAll("[data-event-severity-filter]"),
    eventStateButtons: document.querySelectorAll("[data-event-state-filter]"),
    historyChart: document.getElementById("historyChart"),
    dailyReport: document.getElementById("dailyReport"),
    deployState: document.getElementById("deployState"),
    alertTargets: document.getElementById("alertTargets"),
    rssFeeds: document.getElementById("rssFeeds"),
    pageWatches: document.getElementById("pageWatches"),
    inventoryUpdated: document.getElementById("inventoryUpdated"),
    inventoryActionStatus: document.getElementById("inventoryActionStatus"),
    inventoryProblems: document.getElementById("inventoryProblems"),
    inventorySections: document.getElementById("inventorySections"),
    logPanel: document.getElementById("logPanel"),
    logTitle: document.getElementById("logTitle"),
    logMeta: document.getElementById("logMeta"),
    logOutput: document.getElementById("logOutput"),
    closeLogPanelButton: document.getElementById("closeLogPanelButton"),
    quickJumpButton: document.getElementById("quickJumpButton"),
    quickJumpOverlay: document.getElementById("quickJumpOverlay"),
    quickJumpInput: document.getElementById("quickJumpInput"),
    quickJumpResults: document.getElementById("quickJumpResults"),
    closeQuickJumpButton: document.getElementById("closeQuickJumpButton"),
    refreshInventoryButton: document.getElementById("refreshInventoryButton"),
    runChecksButton: document.getElementById("runChecksButton"),
    testAlertButton: document.getElementById("testAlertButton"),
    languageToggle: document.querySelector(".language-toggle"),
    localeButtons: document.querySelectorAll("[data-locale]"),
    themeToggle: document.querySelector(".theme-toggle"),
    themeButtons: document.querySelectorAll("[data-theme-choice]"),
    navLinks: document.querySelectorAll(".nav a[href^='#']"),
};

function getInitialLocale() {
    const saved = localStorage.getItem(LOCALE_KEY);
    if (saved === "ko" || saved === "en") return saved;
    return navigator.language?.toLowerCase().startsWith("ko") ? "ko" : "en";
}

function getInitialTheme() {
    const saved = localStorage.getItem(THEME_KEY);
    return ["system", "light", "dark"].includes(saved) ? saved : "system";
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

function setTheme(theme) {
    state.theme = theme;
    localStorage.setItem(THEME_KEY, theme);
    applyTheme();
}

function applyTheme() {
    document.documentElement.dataset.theme = state.theme;
    els.themeButtons.forEach((button) => {
        const active = button.dataset.themeChoice === state.theme;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", String(active));
    });
    drawChart();
}

function cssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function setActiveNav(sectionId) {
    if (!sectionId) return;
    els.navLinks.forEach((link) => {
        const active = link.getAttribute("href") === `#${sectionId}`;
        link.classList.toggle("active", active);
        if (active) link.setAttribute("aria-current", "location");
        else link.removeAttribute("aria-current");
    });
}

function syncActiveNavFromScroll() {
    if (Date.now() < state.navLockUntil) return;

    const hashId = window.location.hash.replace(/^#/, "");
    const hashTarget = hashId ? document.getElementById(hashId) : null;
    if (hashTarget && isMostlyVisible(hashTarget)) {
        setActiveNav(hashId);
        return;
    }

    const scrollTargets = Array.from(els.navLinks)
        .map((link) => link.getAttribute("href")?.replace(/^#/, ""))
        .map((id) => document.getElementById(id))
        .filter(Boolean);
    const offset = 160;
    const current = scrollTargets.reduce((active, section) => {
        return section.getBoundingClientRect().top <= offset ? section : active;
    }, scrollTargets[0]);

    setActiveNav(current?.id);
}

function isMostlyVisible(element) {
    const rect = element.getBoundingClientRect();
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
    return rect.top < viewportHeight * 0.75 && rect.bottom > 80;
}

function bindNavigation() {
    els.navLinks.forEach((link) => {
        link.addEventListener("click", () => {
            const sectionId = link.getAttribute("href")?.replace(/^#/, "");
            state.navLockUntil = Date.now() + 900;
            setActiveNav(sectionId);
        });
    });

    window.addEventListener("hashchange", () => {
        const sectionId = window.location.hash.replace(/^#/, "");
        if (document.getElementById(sectionId)) setActiveNav(sectionId);
    });
    window.addEventListener("scroll", syncActiveNavFromScroll, { passive: true });
    window.addEventListener("resize", syncActiveNavFromScroll);

    setActiveNav(window.location.hash.replace(/^#/, "") || "overview");
    syncActiveNavFromScroll();
}

function applyLocale() {
    document.documentElement.lang = state.locale;
    document.querySelectorAll("[data-i18n]").forEach((node) => {
        node.textContent = t(node.dataset.i18n);
    });
    document.querySelectorAll("[data-i18n-placeholder]").forEach((node) => {
        node.setAttribute("placeholder", t(node.dataset.i18nPlaceholder));
    });
    els.languageToggle.setAttribute("aria-label", t("languageLabel"));
    els.themeToggle.setAttribute("aria-label", t("themeLabel"));
    els.quickJumpResults.setAttribute("aria-label", t("quickJumpTitle"));
    els.localeButtons.forEach((button) => {
        const active = button.dataset.locale === state.locale;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", String(active));
    });
    applyTheme();
    setSocketState(state.latestSocketState);
    if (state.latestSummary) renderSummary(state.latestSummary, false);
    else if (state.latestSnapshot) renderSnapshot(state.latestSnapshot, false);
    if (state.latestInventory) renderInventory(state.latestInventory, false);
    if (state.latestLog) renderLogResult(state.latestLog);
    if (state.quickJumpOpen) renderQuickJumpResults();
    syncEventFilterButtons();
    renderEventDetail();
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

function eventStateLabel(event) {
    if (event && event.actionRequired === false) return t("eventActionLog");
    return t(`eventState${String(event?.state || "OPEN").toUpperCase()}`);
}

function eventStateClass(event) {
    if (event && event.actionRequired === false) return "log";
    return String(event?.state || "OPEN").toLowerCase();
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
    if (hasActiveEventFilters()) {
        refreshEvents().catch(console.error);
    } else {
        renderEvents(summary.events);
    }
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

    els.servicesBody.innerHTML = services.map((service, index) => `
        <tr data-service-target="${escapeHtml(serviceTargetId(service, index))}">
            <td>${escapeHtml(service.name)}</td>
            <td>${escapeHtml(kindLabel(service.kind))}</td>
            <td><span class="cell-status ${statusClass(service.status)}">${escapeHtml(statusLabel(service.status))}</span></td>
            <td>${service.latencyMs == null ? "--" : `${service.latencyMs} ms`}</td>
            <td>${escapeHtml(localizeServiceMessage(service.message || ""))}</td>
        </tr>
    `).join("");
}

function serviceTargetId(service, index) {
    return stableTargetId(`service:${index}:${service.kind}:${service.name}`);
}

function renderEvents(events) {
    state.events = events || [];
    if (!state.events.some((event) => event.id === state.selectedEventId)) {
        state.selectedEventId = state.events[0]?.id || null;
    }

    if (state.events.length === 0) {
        els.eventsList.innerHTML = `<li class="empty">${escapeHtml(emptyEventsText())}</li>`;
        renderEventDetail();
        return;
    }

    els.eventsList.innerHTML = state.events.map((event) => `
        <li>
            <button type="button" class="event-row ${event.id === state.selectedEventId ? "active" : ""}" data-event-id="${event.id}" aria-pressed="${event.id === state.selectedEventId}">
                <span class="event-time">${formatTime(event.timestamp)}</span>
                <span class="status-pill ${String(event.severity).toLowerCase()}">${escapeHtml(severityLabel(event.severity))}</span>
                <span class="event-state ${eventStateClass(event)}">${escapeHtml(eventStateLabel(event))}</span>
                <span class="event-message">
                    <strong>${escapeHtml(localizeEventMessage(event.message))}</strong>
                    <small>${escapeHtml(localizeSource(event.source))}${event.details ? ` · ${escapeHtml(localizeServiceMessage(event.details))}` : ""}</small>
                </span>
            </button>
        </li>
    `).join("");
    renderEventDetail();
}

function renderEventDetail() {
    const event = state.events.find((item) => item.id === state.selectedEventId);
    if (!event) {
        els.eventDetail.innerHTML = `<p class="empty">${escapeHtml(t("selectEventPrompt"))}</p>`;
        return;
    }

    const actions = event.actionRequired === false ? [] : [
        ["OPEN", "markOpen", "secondary"],
        ["ACKNOWLEDGED", "markAcknowledged", "secondary"],
        ["RESOLVED", "markResolved", "primary"],
    ].filter(([eventState]) => eventState !== event.state);

    els.eventDetail.innerHTML = `
        <h3>${escapeHtml(t("eventDetails"))}</h3>
        <dl>
            <div>
                <dt>${escapeHtml(t("eventMessage"))}</dt>
                <dd>${escapeHtml(localizeEventMessage(event.message))}</dd>
            </div>
            <div>
                <dt>${escapeHtml(t("eventStatus"))}</dt>
                <dd>
                    <span class="event-state ${eventStateClass(event)}">${escapeHtml(eventStateLabel(event))}</span>
                    <p class="event-state-note">${escapeHtml(t("eventStateHelp"))}</p>
                </dd>
            </div>
            <div>
                <dt>${escapeHtml(t("eventTimestamp"))}</dt>
                <dd>${escapeHtml(new Date(event.timestamp).toLocaleString(state.locale === "ko" ? "ko-KR" : "en-US"))}</dd>
            </div>
            <div>
                <dt>${escapeHtml(t("eventSource"))}</dt>
                <dd>${escapeHtml(localizeSource(event.source))}</dd>
            </div>
            <div>
                <dt>${escapeHtml(t("eventDetailsLabel"))}</dt>
                <dd>${escapeHtml(event.details ? localizeServiceMessage(event.details) : t("none"))}</dd>
            </div>
        </dl>
        <div class="detail-actions">
            ${actions.map(([eventState, labelKey, style]) => `
                <button type="button" class="button compact ${style}" data-event-state-action="${eventState}">
                    ${escapeHtml(t(labelKey))}
                </button>
            `).join("")}
        </div>
    `;
}

function renderAutomation(automation) {
    els.dailyReport.textContent = automation.dailyReportTime;
    els.deployState.textContent = automation.deployConfigured ? t("configured") : t("notConfigured");
    els.alertTargets.textContent = automation.alertTargetsConfigured.length
        ? automation.alertTargetsConfigured.join(", ")
        : t("inAppEventsOnly");
    els.rssFeeds.innerHTML = renderAutomationTargets(automation.rssFeeds);
    els.pageWatches.innerHTML = renderAutomationTargets(automation.pageWatches);
}

function renderAutomationTargets(targets) {
    if (!targets || targets.length === 0) return escapeHtml(t("none"));
    return `
        <ol class="automation-targets">
            ${targets.map((target) => {
                const status = String(target.status || "UNKNOWN").toUpperCase();
                const checkedAt = target.checkedAt ? t("automationLastChecked", { time: formatTime(target.checkedAt) }) : t("automationNeverChecked");
                return `
                    <li>
                        <span class="automation-target-status ${status.toLowerCase()}">${escapeHtml(t(`automationStatus${status}`))}</span>
                        <span class="automation-target-copy">
                            <strong>${escapeHtml(target.name)}</strong>
                            <small>${escapeHtml(target.message || target.url)}</small>
                            <time>${escapeHtml(checkedAt)}</time>
                        </span>
                    </li>
                `;
            }).join("")}
        </ol>
    `;
}

function renderInventory(inventory, remember = true) {
    if (remember) state.latestInventory = inventory;
    els.inventoryUpdated.textContent = t("inventoryUpdated", { time: formatTime(inventory.timestamp) });

    if (inventory.problems?.length) {
        els.inventoryProblems.innerHTML = `
            <span class="status-pill warning">${escapeHtml(t("inventoryProblemCount", { count: inventory.problems.length }))}</span>
            ${inventory.problems.slice(0, 3).map((problem) => `
                <span class="inventory-problem">${escapeHtml(localizeEventMessage(problem.message))}</span>
            `).join("")}
        `;
    } else {
        els.inventoryProblems.innerHTML = `<span class="cell-status up">${escapeHtml(t("noInventoryProblems"))}</span>`;
    }

    const sections = inventory.sections || [];
    if (sections.length === 0) {
        els.inventorySections.innerHTML = `<p class="empty">${escapeHtml(t("inventoryEmpty"))}</p>`;
        return;
    }

    els.inventorySections.innerHTML = sections.map(renderInventorySection).join("");
}

function renderInventorySection(section) {
    const title = inventorySectionTitle(section);
    const state = section.available ? `${section.items.length}` : t("inventoryUnavailable");
    const sectionClass = `inventory-section-${String(section.key || "").toLowerCase().replaceAll("_", "-")}`;
    const rows = section.available
        ? renderInventoryRows(section.items, section.key)
        : `<tr><td colspan="6" class="empty">${escapeHtml(section.message || t("inventoryUnavailable"))}</td></tr>`;

    return `
        <article class="inventory-section ${escapeHtml(sectionClass)}" data-inventory-section-target="${escapeHtml(inventorySectionTargetId(section.key))}">
            <div class="inventory-section-head">
                <div>
                    <h3>${escapeHtml(title)}</h3>
                    <small>${escapeHtml(section.source)}</small>
                </div>
                <span class="inventory-count ${section.available ? "" : "unavailable"}">${escapeHtml(state)}</span>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>${escapeHtml(t("inventoryColumnName"))}</th>
                        <th>${escapeHtml(t("inventoryColumnActions"))}</th>
                        <th>${escapeHtml(t("inventoryColumnStatus"))}</th>
                        <th>${escapeHtml(t("inventoryColumnSchedule"))}</th>
                        <th>${escapeHtml(t("inventoryColumnDetail"))}</th>
                        <th>${escapeHtml(t("inventoryColumnCommand"))}</th>
                    </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        </article>
    `;
}

function renderInventoryRows(items, sectionKey) {
    if (!items || items.length === 0) {
        return `<tr><td colspan="6" class="empty">${escapeHtml(t("inventoryEmpty"))}</td></tr>`;
    }
    return items.map((item, index) => `
        <tr data-inventory-target="${escapeHtml(inventoryItemTargetId(sectionKey, item, index))}">
            <td data-label="${escapeHtml(t("inventoryColumnName"))}">
                <strong class="inventory-name">${escapeHtml(item.name)}</strong>
                <small>${escapeHtml(item.kind || "")}</small>
            </td>
            <td data-label="${escapeHtml(t("inventoryColumnActions"))}">${renderManagementActions(item)}</td>
            <td data-label="${escapeHtml(t("inventoryColumnStatus"))}">${escapeHtml(item.status || "--")}</td>
            <td data-label="${escapeHtml(t("inventoryColumnSchedule"))}">${escapeHtml(item.schedule || "--")}</td>
            <td data-label="${escapeHtml(t("inventoryColumnDetail"))}">${escapeHtml(item.detail || "--")}</td>
            <td data-label="${escapeHtml(t("inventoryColumnCommand"))}"><code>${escapeHtml(item.command || item.raw || "--")}</code></td>
        </tr>
    `).join("");
}

function inventorySectionTitle(section) {
    const key = `inventorySection${section.key}`;
    const label = t(key);
    return label === key ? section.title : label;
}

function inventorySectionTargetId(sectionKey) {
    return stableTargetId(`inventory-section:${sectionKey}`);
}

function inventoryItemTargetId(sectionKey, item, index) {
    return stableTargetId(`inventory:${sectionKey}:${index}:${item.kind}:${item.name}`);
}

function stableTargetId(value) {
    let hash = 0;
    for (let index = 0; index < value.length; index += 1) {
        hash = ((hash << 5) - hash + value.charCodeAt(index)) | 0;
    }
    return `target-${Math.abs(hash).toString(36)}`;
}

function renderManagementActions(item) {
    const targetType = managementTargetType(item.kind);
    const managementButtons = targetType && item.actions?.length
        ? item.actions.map((action) => `
            <button
                type="button"
                class="mini-action ${action === "STOP" ? "danger" : ""}"
                data-manage-target-type="${escapeHtml(targetType)}"
                data-manage-name="${escapeHtml(item.name)}"
                data-manage-action="${escapeHtml(action)}"
            >${escapeHtml(t(`manageAction${action}`))}</button>
        `)
        : [];
    const logButtons = (item.logs || []).map((logTargetType) => `
        <button
            type="button"
            class="mini-action"
            data-log-target-type="${escapeHtml(logTargetType)}"
            data-log-name="${escapeHtml(item.name)}"
        >${escapeHtml(t("viewLogs"))}</button>
    `);

    if (managementButtons.length === 0 && logButtons.length === 0) return "--";
    return `
        <div class="management-actions">
            ${[...managementButtons, ...logButtons].join("")}
        </div>
    `;
}

function managementTargetType(kind) {
    if (kind === "managed-systemd-unit") return "SYSTEMD_UNIT";
    if (kind === "docker") return "DOCKER_CONTAINER";
    if (kind === "managed-docker-container") return "DOCKER_CONTAINER";
    return null;
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

    const managementMatch = message.match(/^(systemd unit|docker container) (start|stop|restart) (.+) (succeeded|failed)$/);
    if (managementMatch) {
        const target = managementMatch[1] === "systemd unit" ? "systemd 유닛" : "Docker 컨테이너";
        const action = { start: "시작", stop: "중지", restart: "재시작" }[managementMatch[2]];
        const result = managementMatch[4] === "succeeded" ? "성공" : "실패";
        return `${target} ${managementMatch[3]} ${action} ${result}`;
    }

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
        .replace(/^management:systemd_unit:/, "관리:systemd:")
        .replace(/^management:docker_container:/, "관리:Docker:")
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
    const chartBg = cssVar("--chart-bg") || "#ffffff";
    const chartGrid = cssVar("--chart-grid") || "#eef2f7";
    const cpuColor = cssVar("--chart-cpu") || "#2563eb";
    const memoryColor = cssVar("--chart-memory") || "#16a34a";
    ctx.clearRect(0, 0, width, height);

    ctx.fillStyle = chartBg;
    ctx.fillRect(0, 0, width, height);
    ctx.strokeStyle = chartGrid;
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i += 1) {
        const y = pad + ((height - pad * 2) * i) / 4;
        ctx.beginPath();
        ctx.moveTo(pad, y);
        ctx.lineTo(width - pad, y);
        ctx.stroke();
    }

    drawLine(ctx, state.samples.map((sample) => sample.cpu.systemPercent), cpuColor, width, height, pad);
    drawLine(ctx, state.samples.map((sample) => sample.memory.usedPercent), memoryColor, width, height, pad);
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

function hasActiveEventFilters() {
    return state.eventFilters.severity !== "ALL" ||
        state.eventFilters.state !== "ALL" ||
        state.eventFilters.query.trim() !== "";
}

function emptyEventsText() {
    if (state.eventFilters.state === "OPEN" && !state.eventFilters.query.trim()) return t("noOpenEvents");
    return t("noEventsYet");
}

function buildEventsUrl() {
    const params = new URLSearchParams({ limit: "100" });
    if (state.eventFilters.severity !== "ALL") params.set("severity", state.eventFilters.severity);
    if (state.eventFilters.state !== "ALL") params.set("state", state.eventFilters.state);
    if (state.eventFilters.query.trim()) params.set("q", state.eventFilters.query.trim());
    return `/api/events?${params.toString()}`;
}

function eventMatchesFilters(event) {
    const query = state.eventFilters.query.trim().toLowerCase();
    if (state.eventFilters.severity !== "ALL" && event.severity !== state.eventFilters.severity) return false;
    if (state.eventFilters.state !== "ALL" && event.actionRequired === false) return false;
    if (state.eventFilters.state !== "ALL" && event.state !== state.eventFilters.state) return false;
    if (!query) return true;
    return [event.message, event.source, event.details]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(query));
}

function syncEventFilterButtons() {
    els.eventSeverityButtons.forEach((button) => {
        const active = button.dataset.eventSeverityFilter === state.eventFilters.severity;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", String(active));
    });
    els.eventStateButtons.forEach((button) => {
        const active = button.dataset.eventStateFilter === state.eventFilters.state;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", String(active));
    });
}

function setEventFilter(kind, value) {
    state.eventFilters[kind] = value;
    syncEventFilterButtons();
    refreshEvents().catch(console.error);
}

async function refreshEvents() {
    const events = await getJson(buildEventsUrl());
    renderEvents(events);
}

async function updateSelectedEventState(eventState) {
    const eventId = state.selectedEventId;
    if (!eventId) return;

    els.eventDetail.querySelectorAll("button").forEach((button) => {
        button.disabled = true;
    });

    const updated = await getJson(`/api/events/${eventId}/state`, {
        method: "POST",
        body: JSON.stringify({ state: eventState }),
    });

    if (eventMatchesFilters(updated)) {
        state.events = state.events.map((event) => event.id === updated.id ? updated : event);
        state.selectedEventId = updated.id;
        renderEvents(state.events);
    } else {
        await refreshEvents();
    }
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

async function refreshInventory() {
    els.refreshInventoryButton.disabled = true;
    try {
        const inventory = await getJson("/api/inventory");
        renderInventory(inventory);
        await refreshEvents();
    } finally {
        els.refreshInventoryButton.disabled = false;
    }
}

async function runManagementAction(button) {
    const targetType = button.dataset.manageTargetType;
    const name = button.dataset.manageName;
    const action = button.dataset.manageAction;
    const actionLabel = t(`manageAction${action}`);

    if (action === "STOP" && !window.confirm(t("confirmStop", { name }))) return;

    setInventoryActionStatus("running", t("managementRunning", { action: actionLabel, name }));
    els.inventorySections.querySelectorAll("[data-manage-action]").forEach((item) => {
        item.disabled = true;
    });

    try {
        const result = await getJson("/api/manage/actions", {
            method: "POST",
            body: JSON.stringify({ targetType, name, action }),
        });
        setInventoryActionStatus(
            result.success ? "success" : "error",
            t(result.success ? "managementSucceeded" : "managementFailed", { action: actionLabel, name }),
        );
        await refreshInventory();
        await refreshSummary();
    } catch (error) {
        setInventoryActionStatus("error", t("managementFailed", { action: actionLabel, name }));
        els.inventorySections.querySelectorAll("[data-manage-action]").forEach((item) => {
            item.disabled = false;
        });
    }
}

async function readLogs(button) {
    const targetType = button.dataset.logTargetType;
    const name = button.dataset.logName;
    const url = logUrl(targetType, name, 100);
    if (!url) return;

    els.logPanel.classList.remove("hidden");
    els.logTitle.textContent = `${name} ${t("logsTitle")}`;
    els.logMeta.textContent = t("logsLoading", { name });
    els.logOutput.textContent = t("logsLoading", { name });
    els.inventorySections.querySelectorAll("[data-log-target-type]").forEach((item) => {
        item.disabled = true;
    });

    try {
        const result = await getJson(url);
        state.latestLog = result;
        renderLogResult(result);
    } catch (error) {
        els.logMeta.textContent = t("logsFailed", { exitCode: "--" });
        els.logOutput.textContent = error.message;
    } finally {
        els.inventorySections.querySelectorAll("[data-log-target-type]").forEach((item) => {
            item.disabled = false;
        });
    }
}

function logUrl(targetType, name, lines) {
    if (targetType === "SYSTEMD_UNIT") {
        const params = new URLSearchParams({ unit: name, lines: String(lines) });
        return `/api/logs/systemd?${params.toString()}`;
    }
    if (targetType === "DOCKER_CONTAINER") {
        const params = new URLSearchParams({ container: name, lines: String(lines) });
        return `/api/logs/docker?${params.toString()}`;
    }
    return null;
}

function renderLogResult(result) {
    els.logPanel.classList.remove("hidden");
    els.logTitle.textContent = `${result.name} ${t("logsTitle")}`;
    els.logMeta.textContent = result.success
        ? t("logsUpdated", { time: formatTime(result.timestamp), lines: result.lines })
        : t("logsFailed", { exitCode: result.exitCode ?? "--" });
    els.logOutput.textContent = result.output || t("inventoryEmpty");
}

function openQuickJump(query = "") {
    state.quickJumpOpen = true;
    state.quickJumpQuery = query;
    state.quickJumpIndex = 0;
    els.quickJumpOverlay.classList.remove("hidden");
    document.body.classList.add("quick-jump-active");
    els.quickJumpInput.value = query;
    renderQuickJumpResults();
    requestAnimationFrame(() => {
        els.quickJumpInput.focus();
        els.quickJumpInput.select();
    });
}

function closeQuickJump() {
    state.quickJumpOpen = false;
    els.quickJumpOverlay.classList.add("hidden");
    document.body.classList.remove("quick-jump-active");
    els.quickJumpInput.value = "";
    state.quickJumpQuery = "";
    state.quickJumpIndex = 0;
    els.quickJumpInput.removeAttribute("aria-activedescendant");
}

function renderQuickJumpResults() {
    const items = visibleQuickJumpItems();
    if (items.length === 0) {
        els.quickJumpResults.innerHTML = `<li class="quick-jump-empty">${escapeHtml(t("quickJumpNoResults"))}</li>`;
        els.quickJumpInput.removeAttribute("aria-activedescendant");
        return;
    }

    state.quickJumpIndex = Math.max(0, Math.min(state.quickJumpIndex, items.length - 1));
    els.quickJumpResults.innerHTML = items.map((item, index) => `
        <li>
            <button
                id="quickJumpResult-${index}"
                type="button"
                class="quick-jump-result ${index === state.quickJumpIndex ? "active" : ""}"
                data-quick-jump-index="${index}"
                aria-selected="${index === state.quickJumpIndex}"
            >
                <span class="quick-jump-kind">${escapeHtml(t(item.groupKey))}</span>
                <span class="quick-jump-copy">
                    <strong>${escapeHtml(item.title)}</strong>
                    <small>${escapeHtml(item.subtitle || "")}</small>
                </span>
            </button>
        </li>
    `).join("");
    els.quickJumpInput.setAttribute("aria-activedescendant", `quickJumpResult-${state.quickJumpIndex}`);
}

function visibleQuickJumpItems() {
    const query = normalizeSearch(state.quickJumpQuery);
    const items = buildQuickJumpItems();
    const filtered = query
        ? items
            .filter((item) => normalizeSearch(item.keywords).includes(query))
            .map((item, index) => ({ item, index, score: quickJumpScore(item, query) }))
            .sort((a, b) => a.score - b.score || a.index - b.index)
            .map(({ item }) => item)
        : items;
    return filtered.slice(0, 12);
}

function buildQuickJumpItems() {
    const sectionItems = [
        quickSectionItem("overview", t("navOverview"), t("lastHourDesc")),
        quickSectionItem("services", t("navServices"), t("serviceHealthDesc")),
        quickSectionItem("jobs", t("navJobs"), t("jobsInventoryDesc")),
        quickSectionItem("events", t("navEvents"), t("recentEventsDesc")),
        quickSectionItem("automation", t("navAutomation"), t("automationDesc")),
    ];

    const serviceItems = (state.latestSummary?.services || []).map((service, index) => ({
        groupKey: "quickJumpGroupService",
        title: service.name,
        subtitle: [kindLabel(service.kind), statusLabel(service.status), localizeServiceMessage(service.message || "")]
            .filter(Boolean)
            .join(" · "),
        keywords: [service.name, service.kind, service.status, service.message, kindLabel(service.kind), statusLabel(service.status)].join(" "),
        action: "target",
        sectionId: "services",
        selector: `[data-service-target="${serviceTargetId(service, index)}"]`,
    }));

    const inventorySections = (state.latestInventory?.sections || []).map((section) => ({
        groupKey: "quickJumpGroupInventory",
        title: inventorySectionTitle(section),
        subtitle: [section.source, section.available ? `${section.items.length}` : t("inventoryUnavailable")]
            .filter(Boolean)
            .join(" · "),
        keywords: [inventorySectionTitle(section), section.title, section.source, section.message]
            .filter(Boolean)
            .join(" "),
        action: "target",
        sectionId: "jobs",
        selector: `[data-inventory-section-target="${inventorySectionTargetId(section.key)}"]`,
    }));

    const inventoryItems = (state.latestInventory?.sections || []).flatMap((section) => {
        if (!section.available) return [];
        return (section.items || []).map((item, index) => ({
            groupKey: "quickJumpGroupInventory",
            title: item.name,
            subtitle: [inventorySectionTitle(section), item.kind, item.status, item.schedule, item.detail]
                .filter(Boolean)
                .join(" · "),
            keywords: [
                inventorySectionTitle(section),
                section.source,
                item.kind,
                item.name,
                item.status,
                item.schedule,
                item.detail,
                item.command,
                item.raw,
            ].filter(Boolean).join(" "),
            action: "target",
            sectionId: "jobs",
            selector: `[data-inventory-target="${inventoryItemTargetId(section.key, item, index)}"]`,
        }));
    });

    const eventItems = (state.events.length ? state.events : state.latestSummary?.events || []).map((event) => ({
        groupKey: "quickJumpGroupEvent",
        title: localizeEventMessage(event.message),
        subtitle: [severityLabel(event.severity), eventStateLabel(event), localizeSource(event.source)]
            .filter(Boolean)
            .join(" · "),
        keywords: [event.message, event.details, event.source, event.severity, event.state, localizeEventMessage(event.message)]
            .filter(Boolean)
            .join(" "),
        action: "event",
        sectionId: "events",
        eventId: event.id,
    }));

    return [...sectionItems, ...serviceItems, ...inventorySections, ...inventoryItems, ...eventItems];
}

function quickSectionItem(sectionId, title, subtitle) {
    return {
        groupKey: "quickJumpGroupSection",
        title,
        subtitle,
        keywords: `${sectionId} ${title} ${subtitle}`,
        action: "section",
        sectionId,
    };
}

function normalizeSearch(value) {
    return String(value || "")
        .toLowerCase()
        .normalize("NFKC")
        .replace(/\s+/g, " ")
        .trim();
}

function quickJumpScore(item, query) {
    const title = normalizeSearch(item.title);
    const subtitle = normalizeSearch(item.subtitle);
    if (title === query) return 0;
    if (title.startsWith(query)) return 1;
    if (title.includes(query)) return 2;
    if (subtitle.startsWith(query)) return 3;
    if (subtitle.includes(query)) return 4;
    return 5;
}

async function activateQuickJumpIndex(index = state.quickJumpIndex) {
    const item = visibleQuickJumpItems()[index];
    if (!item) return;
    closeQuickJump();

    if (item.action === "event") {
        await selectQuickJumpEvent(item.eventId);
        return;
    }

    scrollToDashboardTarget(item.sectionId, item.selector);
}

async function selectQuickJumpEvent(eventId) {
    state.eventFilters.state = "ALL";
    syncEventFilterButtons();
    await refreshEvents();
    state.selectedEventId = Number(eventId);
    renderEvents(state.events);
    scrollToDashboardTarget("events", `[data-event-id="${eventId}"]`);
}

function scrollToDashboardTarget(sectionId, selector) {
    const section = document.getElementById(sectionId);
    if (!section) return;

    state.navLockUntil = Date.now() + 900;
    history.replaceState(null, "", `#${sectionId}`);
    setActiveNav(sectionId);
    section.scrollIntoView({ behavior: "smooth", block: "start" });

    if (!selector) return;
    window.setTimeout(() => {
        const target = document.querySelector(selector);
        if (!target) return;
        target.scrollIntoView({ behavior: "smooth", block: "center", inline: "nearest" });
        pulseTarget(target);
    }, 220);
}

function pulseTarget(target) {
    document.querySelectorAll(".quick-jump-highlight").forEach((node) => {
        node.classList.remove("quick-jump-highlight");
    });
    target.classList.add("quick-jump-highlight");
    window.setTimeout(() => {
        target.classList.remove("quick-jump-highlight");
    }, 1800);
}

function handleQuickJumpKeydown(event) {
    const key = event.key;
    if ((event.metaKey || event.ctrlKey) && key.toLowerCase() === "k") {
        event.preventDefault();
        openQuickJump(state.quickJumpQuery);
        return;
    }
    if (!state.quickJumpOpen) return;

    if (key === "Escape") {
        event.preventDefault();
        closeQuickJump();
        return;
    }

    if (key === "ArrowDown" || key === "ArrowUp") {
        event.preventDefault();
        const count = visibleQuickJumpItems().length;
        if (count === 0) return;
        const delta = key === "ArrowDown" ? 1 : -1;
        state.quickJumpIndex = (state.quickJumpIndex + delta + count) % count;
        renderQuickJumpResults();
        return;
    }

    if (key === "Enter") {
        event.preventDefault();
        activateQuickJumpIndex().catch(console.error);
    }
}

function setInventoryActionStatus(kind, message) {
    els.inventoryActionStatus.textContent = message;
    els.inventoryActionStatus.className = `inventory-action-status ${kind}`;
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

els.themeButtons.forEach((button) => {
    button.addEventListener("click", () => setTheme(button.dataset.themeChoice));
});

window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
    if (state.theme === "system") drawChart();
});

els.eventSeverityButtons.forEach((button) => {
    button.addEventListener("click", () => setEventFilter("severity", button.dataset.eventSeverityFilter));
});

els.eventStateButtons.forEach((button) => {
    button.addEventListener("click", () => setEventFilter("state", button.dataset.eventStateFilter));
});

els.eventsSearchInput.addEventListener("input", () => {
    clearTimeout(state.eventSearchTimer);
    state.eventSearchTimer = setTimeout(() => {
        state.eventFilters.query = els.eventsSearchInput.value;
        refreshEvents().catch(console.error);
    }, 250);
});

els.eventsList.addEventListener("click", (event) => {
    const row = event.target.closest("[data-event-id]");
    if (!row) return;
    state.selectedEventId = Number(row.dataset.eventId);
    renderEvents(state.events);
});

els.eventDetail.addEventListener("click", (event) => {
    const action = event.target.closest("[data-event-state-action]");
    if (!action) return;
    updateSelectedEventState(action.dataset.eventStateAction).catch(console.error);
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

els.refreshInventoryButton.addEventListener("click", () => {
    refreshInventory().catch(console.error);
});

els.quickJumpButton.addEventListener("click", () => {
    openQuickJump();
});

els.closeQuickJumpButton.addEventListener("click", () => {
    closeQuickJump();
});

els.quickJumpOverlay.addEventListener("click", (event) => {
    if (event.target === els.quickJumpOverlay) closeQuickJump();
});

els.quickJumpInput.addEventListener("input", () => {
    state.quickJumpQuery = els.quickJumpInput.value;
    state.quickJumpIndex = 0;
    renderQuickJumpResults();
});

els.quickJumpResults.addEventListener("mousemove", (event) => {
    const button = event.target.closest("[data-quick-jump-index]");
    if (!button) return;
    const index = Number(button.dataset.quickJumpIndex);
    if (index === state.quickJumpIndex) return;
    state.quickJumpIndex = index;
    renderQuickJumpResults();
});

els.quickJumpResults.addEventListener("click", (event) => {
    const button = event.target.closest("[data-quick-jump-index]");
    if (!button) return;
    activateQuickJumpIndex(Number(button.dataset.quickJumpIndex)).catch(console.error);
});

els.inventorySections.addEventListener("click", (event) => {
    const logButton = event.target.closest("[data-log-target-type]");
    if (logButton) {
        readLogs(logButton).catch(console.error);
        return;
    }
    const manageButton = event.target.closest("[data-manage-action]");
    if (!manageButton) return;
    runManagementAction(manageButton).catch(console.error);
});

els.closeLogPanelButton.addEventListener("click", () => {
    els.logPanel.classList.add("hidden");
});

document.addEventListener("keydown", handleQuickJumpKeydown);

bindNavigation();
applyLocale();
refreshHistory().catch(console.error);
refreshSummary().catch(console.error);
refreshInventory().catch(console.error);
connectSocket();
setInterval(() => refreshSummary().catch(console.error), 15000);
