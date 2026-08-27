(() => {
  state.pomodoro = state.pomodoro || {
    mode: 'focus',
    focusMinutes: 25,
    breakMinutes: 5,
    seconds: 25 * 60,
    running: false,
    taskIndex: 0,
    completed: 2,
  };

  data.importantDates = data.importantDates || [
    { id: 1, title: '妈妈生日', type: '生日', calendarType: 'lunar', repeatType: 'yearly', lunarMonth: 8, lunarDay: 15, leapMonth: false },
    { id: 2, title: '我们的纪念日', type: '纪念日', calendarType: 'solar', repeatType: 'yearly', month: 9, day: 12 },
    { id: 3, title: 'LifeTrace 阶段发布', type: '里程碑', calendarType: 'solar', repeatType: 'once', year: 2026, month: 8, day: 30 },
  ];

  Object.assign(paths, {
    timer: '<circle cx="10" cy="10" r="7"/><path d="M7 2.8h6M10 3v2M10 10V6.8M10 10l2.8 1.7"/>',
    play: '<path d="m7 5 7 5-7 5z"/>',
    pause: '<path d="M7 5v10M13 5v10"/>',
    reset: '<path d="M5.3 6.2A6 6 0 1 1 4 11"/><path d="M3.8 4v4h4"/>',
    gift: '<rect x="3.5" y="8" width="13" height="9" rx="1.5"/><path d="M10 8v9M2.8 8h14.4V5.5H2.8z"/><path d="M10 5.5C8.2 5.5 6.7 4.8 6.7 3.7c0-.8.6-1.2 1.3-1.2 1.2 0 2 1.5 2 3zM10 5.5c1.8 0 3.3-.7 3.3-1.8 0-.8-.6-1.2-1.3-1.2-1.2 0-2 1.5-2 3z"/>',
    heart: '<path d="M10 16.7 4.3 11A3.8 3.8 0 0 1 9.7 5.6l.3.4.3-.4a3.8 3.8 0 0 1 5.4 5.4z"/>',
    flag: '<path d="M5 18V3.2M5 4h9l-2 3 2 3H5"/>',
    moon: '<path d="M14.8 13.8A6.3 6.3 0 0 1 6.2 5.2 6.5 6.5 0 1 0 14.8 13.8z"/>',
    sun: '<circle cx="10" cy="10" r="3.2"/><path d="M10 2.5v2M10 15.5v2M2.5 10h2M15.5 10h2M4.7 4.7l1.4 1.4M13.9 13.9l1.4 1.4M15.3 4.7l-1.4 1.4M6.1 13.9l-1.4 1.4"/>',
  });

  const baseBindEvents = bindEvents;
  let pomodoroInterval = null;

  const lunarMonths = ['正月','二月','三月','四月','五月','六月','七月','八月','九月','十月','冬月','腊月'];
  const lunarDays = ['初一','初二','初三','初四','初五','初六','初七','初八','初九','初十','十一','十二','十三','十四','十五','十六','十七','十八','十九','二十','廿一','廿二','廿三','廿四','廿五','廿六','廿七','廿八','廿九','三十'];

  function kindIcon(type) {
    if (type === '生日') return 'gift';
    if (type === '纪念日') return 'heart';
    if (type === '里程碑') return 'flag';
    return 'calendar';
  }

  function formatImportantDate(item) {
    if (item.calendarType === 'lunar') {
      const prefix = item.leapMonth ? '闰' : '';
      const date = `${prefix}${lunarMonths[(item.lunarMonth || 1) - 1]}${lunarDays[(item.lunarDay || 1) - 1]}`;
      return item.repeatType === 'once' && item.lunarYear ? `${item.lunarYear}年 ${date}` : date;
    }
    const core = `${item.month}月${item.day}日`;
    return item.repeatType === 'once' && item.year ? `${item.year}年${core}` : core;
  }

  function importantDistance(item) {
    if (item.calendarType === 'lunar') return item.repeatType === 'yearly' ? '农历每年' : '农历日期';
    const today = new Date(2026, 7, 27);
    let target = new Date(item.year || 2026, (item.month || 1) - 1, item.day || 1);
    if (item.repeatType === 'yearly' && target < today) target = new Date(2027, target.getMonth(), target.getDate());
    const days = Math.ceil((target - today) / 86400000);
    if (days === 0) return '今天';
    if (days > 0) return `${days}天后`;
    return '已过去';
  }

  function importantMiniList() {
    return data.importantDates.slice(0, 2).map(item => `
      <div class="important-mini">
        <span class="important-date-icon">${icon(kindIcon(item.type),'sm')}</span>
        <div class="important-date-main"><strong>${esc(item.title)}</strong><span>${item.calendarType === 'lunar' ? '农历' : '公历'} · ${formatImportantDate(item)} · ${item.repeatType === 'yearly' ? '每年' : '仅一次'}</span></div>
        <span class="important-date-distance">${importantDistance(item)}</span>
      </div>`).join('');
  }

  function pomodoroTotalSeconds() {
    return (state.pomodoro.mode === 'focus' ? state.pomodoro.focusMinutes : state.pomodoro.breakMinutes) * 60;
  }

  function formatTimer(seconds) {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  }

  function pomodoroCard() {
    const p = state.pomodoro;
    const total = Math.max(1, pomodoroTotalSeconds());
    const progress = Math.max(0, Math.min(100, ((total - p.seconds) / total) * 100));
    const task = data.tasks[p.taskIndex] || data.tasks[0];
    const currentRound = Math.min(3, p.completed % 4);
    const rounds = Array.from({length: 4}, (_, i) => `<i class="${i < currentRound ? 'done' : i === currentRound ? 'active' : ''}"></i>`).join('');
    return `<section class="pomodoro-card">
      <div class="pomodoro-head">
        <div class="pomodoro-title">${icon('timer','sm')}<span>番茄专注</span></div>
        <span class="pomodoro-mode">${p.mode === 'focus' ? '专注中' : '休息中'} · 今日 ${p.completed} 个</span>
      </div>
      <div class="pomodoro-main">
        <div>
          <div id="pomodoro-time" class="pomodoro-time">${formatTimer(p.seconds)}</div>
          <div class="pomodoro-task-label">当前任务</div>
          <div class="pomodoro-task">${esc(task?.title || '未关联任务')}</div>
        </div>
        <button class="pomodoro-primary" data-action="toggle-pomodoro" aria-label="${p.running ? '暂停' : '开始'}番茄钟">${icon(p.running ? 'pause' : 'play','lg')}</button>
      </div>
      <div class="pomodoro-progress"><i id="pomodoro-progress" style="width:${progress}%"></i></div>
      <div class="pomodoro-foot">
        <div class="pomodoro-rounds">${rounds}</div>
        <div class="pomodoro-actions">
          <button class="pomodoro-ghost" data-action="reset-pomodoro">重置</button>
          <button class="pomodoro-ghost" data-action="pomodoro-settings">${p.focusMinutes}/${p.breakMinutes} 设置</button>
        </div>
      </div>
    </section>`;
  }

  tasksPage = function() {
    const tasks = filteredTasks();
    const chips = ['全部','进行中','等待','已完成'].map(x=>`<button class="chip ${state.taskFilter===x?'active':''}" data-filter="${x}">${x}</button>`).join('');
    return `<div class="page">${header('任务','专注执行，把事情做完','TODAY')}
      ${pomodoroCard()}
      <div class="search-field">${icon('search')}<input id="task-search" value="${esc(state.taskQuery)}" placeholder="搜索任务、项目或状态" autocomplete="off" /></div>
      <div class="chips">${chips}</div>
      <section class="section">${sectionTitle(`今天 · ${tasks.length}`,'批量管理')}<div class="card task-card">${tasks.length ? tasks.map(t=>taskRow(t,data.tasks.indexOf(t))).join('') : '<div style="padding:34px 18px;text-align:center;color:var(--muted);font-size:10.5px">没有匹配的任务</div>'}</div></section>
      <button class="fab" data-action="new-task">${icon('plus','sm')}<span>新建任务</span></button>
    </div>`;
  };

  calendarDays = function() {
    const leading = 5;
    const total = 31;
    const cells = [];
    const solarImportantDays = new Set(data.importantDates
      .filter(item => item.calendarType === 'solar' && item.month === 8 && (item.repeatType === 'yearly' || item.year === 2026))
      .map(item => item.day));
    for (let i = 0; i < leading; i++) cells.push(`<button class="calendar-day out">${27+i}</button>`);
    for (let d = 1; d <= total; d++) {
      const hasEvent = [3,7,12,18,21,27,28].includes(d);
      const important = solarImportantDays.has(d);
      cells.push(`<button class="calendar-day ${state.selectedDay===d?'selected':''} ${hasEvent?'has-event':''} ${important?'important-date':''}" data-day="${d}">${d}</button>`);
    }
    while (cells.length % 7) cells.push(`<button class="calendar-day out">${cells.length-35}</button>`);
    return cells.join('');
  };

  function selectedDaySchedule() {
    const base = state.selectedDay === 27 ? [
      ['09:30','产品会','会议 · 45 分钟','orange'],
      ['11:00','需求评审','LifeTrace 2.0 · 60 分钟',''],
      ['16:00','运动 30 分钟','习惯 · 健身','green'],
    ] : [];
    const important = data.importantDates
      .filter(item => item.calendarType === 'solar' && item.month === 8 && item.day === state.selectedDay && (item.repeatType === 'yearly' || item.year === 2026))
      .map(item => ['全天', item.title, `${item.type} · ${item.repeatType === 'yearly' ? '每年' : '仅一次'} · 重要日期`, 'purple']);
    const all = [...important, ...base];
    return all.length ? all : [['10:00','暂无固定日程','可以安排新的任务或习惯','']];
  }

  calendarPage = function() {
    const schedule = selectedDaySchedule();
    return `<div class="page">${header('日历','任务、习惯、日程与重要日期','CALENDAR')}
      <section class="important-summary">
        <div class="important-summary-head">
          <div class="important-summary-title"><span class="important-summary-icon">${icon('heart','sm')}</span><div><strong>重要日期</strong><span>生日、纪念日与一次性重要节点</span></div></div>
          <button class="important-manage-btn" data-action="manage-important">管理 ${data.importantDates.length}</button>
        </div>
        <div class="important-mini-list">${importantMiniList()}</div>
      </section>
      <div class="calendar-toolbar" style="margin-top:16px"><strong>2026年8月</strong><div class="calendar-nav"><button data-toast="上个月">‹</button><button data-toast="下个月">›</button></div></div>
      <div class="card calendar-card"><div class="calendar-weekdays">${['一','二','三','四','五','六','日'].map(x=>`<span>${x}</span>`).join('')}</div><div class="calendar-grid">${calendarDays()}</div></div>
      <section class="section">${sectionTitle(`8月${state.selectedDay}日 ${state.selectedDay===27?'· 今天':''}`,'新建日程')}<div class="schedule-stack">${schedule.map(([t,title,meta,c])=>`<div class="schedule-item"><span class="schedule-time">${t}</span><i class="schedule-line ${c}"></i><div><div class="schedule-title">${esc(title)}</div><div class="schedule-meta">${esc(meta)}</div></div></div>`).join('')}</div></section>
    </div>`;
  };

  function importantDatesPage() {
    const yearly = data.importantDates.filter(x => x.repeatType === 'yearly').length;
    const lunar = data.importantDates.filter(x => x.calendarType === 'lunar').length;
    const rows = data.importantDates.map(item => `
      <div class="important-row" data-edit-important="${item.id}">
        <span class="important-date-icon">${icon(kindIcon(item.type),'sm')}</span>
        <div class="important-row-main">
          <strong>${esc(item.title)}</strong>
          <div class="important-row-meta">
            <span class="kind-badge">${esc(item.type)}</span>
            <span class="calendar-badge ${item.calendarType === 'lunar' ? 'lunar' : ''}">${item.calendarType === 'lunar' ? '农历' : '公历'}</span>
            <span class="repeat-badge">${item.repeatType === 'yearly' ? '每年' : '仅一次'}</span>
          </div>
        </div>
        <div class="important-row-date"><strong>${formatImportantDate(item)}</strong><span>${importantDistance(item)}</span></div>
      </div>`).join('');
    return `<div class="page">${subHeader('重要日期','生日、纪念日与关键节点')}
      <div class="important-count-strip"><div class="important-count"><strong>${data.importantDates.length}</strong><span>全部日期</span></div><div class="important-count"><strong>${yearly}</strong><span>每年重复</span></div><div class="important-count"><strong>${lunar}</strong><span>农历日期</span></div></div>
      <section class="section">${sectionTitle('日期列表')}<div class="card important-list">${rows || '<div style="padding:30px;text-align:center;color:var(--muted);font-size:10px">还没有重要日期</div>'}</div><button class="add-important-btn" data-action="add-important">+ 添加重要日期</button></section>
    </div>`;
  }

  render = function() {
    const app = document.getElementById('app');
    const pages = {
      today: todayPage,
      tasks: tasksPage,
      projects: projectsPage,
      calendar: calendarPage,
      collection: collectionPage,
      profile: profilePage,
      review: reviewPage,
      'important-dates': importantDatesPage,
    };
    app.innerHTML = (pages[state.route] || todayPage)();
    app.scrollTop = 0;
    renderBottomNav();
    bindEvents();
    updatePomodoroDom();
  };

  bindEvents = function() {
    baseBindEvents();
    document.querySelectorAll('[data-action="manage-important"]').forEach(el => el.onclick = () => go('important-dates'));
    document.querySelectorAll('[data-action="add-important"]').forEach(el => el.onclick = () => openImportantDateSheet());
    document.querySelectorAll('[data-edit-important]').forEach(el => el.onclick = () => openImportantDateSheet(Number(el.dataset.editImportant)));
    document.querySelectorAll('[data-action="toggle-pomodoro"]').forEach(el => el.onclick = togglePomodoro);
    document.querySelectorAll('[data-action="reset-pomodoro"]').forEach(el => el.onclick = resetPomodoro);
    document.querySelectorAll('[data-action="pomodoro-settings"]').forEach(el => el.onclick = openPomodoroSettings);
  };

  function updatePomodoroDom() {
    const p = state.pomodoro;
    const time = document.getElementById('pomodoro-time');
    if (time) time.textContent = formatTimer(p.seconds);
    const progress = document.getElementById('pomodoro-progress');
    if (progress) {
      const total = Math.max(1, pomodoroTotalSeconds());
      progress.style.width = `${Math.max(0, Math.min(100, ((total - p.seconds) / total) * 100))}%`;
    }
  }

  function syncPomodoroInterval() {
    if (pomodoroInterval) {
      clearInterval(pomodoroInterval);
      pomodoroInterval = null;
    }
    if (!state.pomodoro.running) return;
    pomodoroInterval = setInterval(() => {
      const p = state.pomodoro;
      if (!p.running) return;
      p.seconds = Math.max(0, p.seconds - 1);
      updatePomodoroDom();
      if (p.seconds === 0) {
        clearInterval(pomodoroInterval);
        pomodoroInterval = null;
        p.running = false;
        if (p.mode === 'focus') {
          p.completed += 1;
          p.mode = 'break';
          p.seconds = p.breakMinutes * 60;
          toast('本轮专注完成，进入休息');
        } else {
          p.mode = 'focus';
          p.seconds = p.focusMinutes * 60;
          toast('休息结束，可以开始下一轮专注');
        }
        render();
      }
    }, 1000);
  }

  function togglePomodoro() {
    state.pomodoro.running = !state.pomodoro.running;
    syncPomodoroInterval();
    render();
  }

  function resetPomodoro() {
    state.pomodoro.running = false;
    state.pomodoro.seconds = pomodoroTotalSeconds();
    syncPomodoroInterval();
    render();
    toast('番茄钟已重置');
  }

  function openPomodoroSettings() {
    const p = state.pomodoro;
    const root = document.getElementById('modal-root');
    const preset = `${p.focusMinutes}-${p.breakMinutes}`;
    root.innerHTML = `<div class="modal-backdrop" data-feature-close><div class="bottom-sheet" onclick="event.stopPropagation()">
      <div class="sheet-handle"></div>
      <div class="sheet-title"><strong>番茄钟设置</strong><button class="icon-btn" data-feature-close>×</button></div>
      <div class="feature-field"><label>专注模式</label><div class="preset-grid"><button class="preset-card ${preset==='25-5'?'active':''}" data-preset="25-5"><strong>25 / 5</strong><span>经典番茄 · 轻量节奏</span></button><button class="preset-card ${preset==='50-10'?'active':''}" data-preset="50-10"><strong>50 / 10</strong><span>深度专注 · 长任务</span></button></div></div>
      <div class="feature-field"><label>关联任务</label><select id="pomodoro-task-select" class="feature-select">${data.tasks.map((task,i)=>`<option value="${i}" ${i===p.taskIndex?'selected':''}>${esc(task.title)}</option>`).join('')}</select></div>
      <div class="sheet-actions"><button class="sheet-cancel" data-feature-close>取消</button><button class="sheet-confirm" id="save-pomodoro-settings">保存设置</button></div>
    </div></div>`;
    let selectedPreset = preset;
    root.querySelectorAll('[data-feature-close]').forEach(el => el.onclick = () => root.innerHTML = '');
    root.querySelectorAll('[data-preset]').forEach(el => el.onclick = () => {
      selectedPreset = el.dataset.preset;
      root.querySelectorAll('[data-preset]').forEach(btn => btn.classList.toggle('active', btn.dataset.preset === selectedPreset));
    });
    document.getElementById('save-pomodoro-settings').onclick = () => {
      const [focus, rest] = selectedPreset.split('-').map(Number);
      p.focusMinutes = focus;
      p.breakMinutes = rest;
      p.taskIndex = Number(document.getElementById('pomodoro-task-select').value);
      p.mode = 'focus';
      p.running = false;
      p.seconds = focus * 60;
      syncPomodoroInterval();
      root.innerHTML = '';
      render();
      toast('番茄钟设置已更新');
    };
  }

  function openImportantDateSheet(id = null) {
    const existing = id ? data.importantDates.find(item => item.id === id) : null;
    const draft = existing ? { ...existing } : {
      title: '', type: '纪念日', calendarType: 'solar', repeatType: 'once',
      year: 2026, month: 8, day: 30, lunarYear: 2026, lunarMonth: 8, lunarDay: 15, leapMonth: false,
    };
    const root = document.getElementById('modal-root');
    root.innerHTML = `<div class="modal-backdrop" data-feature-close><div class="bottom-sheet" onclick="event.stopPropagation()">
      <div class="sheet-handle"></div>
      <div class="sheet-title"><strong>${existing ? '编辑重要日期' : '添加重要日期'}</strong><button class="icon-btn" data-feature-close>×</button></div>
      <div class="feature-field"><label>名称</label><input id="important-title" class="feature-input" value="${esc(draft.title)}" placeholder="例如：妈妈生日"></div>
      <div class="feature-field"><label>类型</label><select id="important-type" class="feature-select"><option ${draft.type==='生日'?'selected':''}>生日</option><option ${draft.type==='纪念日'?'selected':''}>纪念日</option><option ${draft.type==='里程碑'?'selected':''}>里程碑</option><option ${draft.type==='其他'?'selected':''}>其他</option></select></div>
      <div class="feature-field"><label>历法</label><div class="segmented" id="calendar-segment"><button data-important-calendar="solar">公历</button><button data-important-calendar="lunar">农历</button></div></div>
      <div class="feature-field"><label>重复</label><div class="segmented" id="repeat-segment"><button data-important-repeat="once">仅一次</button><button data-important-repeat="yearly">每年</button></div></div>
      <div id="important-date-fields"></div>
      <div class="sheet-actions">${existing ? '<button class="sheet-danger" id="delete-important">删除</button>' : ''}<button class="sheet-cancel" data-feature-close>取消</button><button class="sheet-confirm" id="save-important">保存</button></div>
    </div></div>`;

    const renderFields = () => {
      root.querySelectorAll('[data-important-calendar]').forEach(btn => btn.classList.toggle('active', btn.dataset.importantCalendar === draft.calendarType));
      root.querySelectorAll('[data-important-repeat]').forEach(btn => btn.classList.toggle('active', btn.dataset.importantRepeat === draft.repeatType));
      const fields = document.getElementById('important-date-fields');
      if (draft.calendarType === 'solar') {
        const y = draft.year || 2026;
        const value = `${y}-${String(draft.month || 1).padStart(2,'0')}-${String(draft.day || 1).padStart(2,'0')}`;
        fields.innerHTML = `<div class="feature-field"><label>${draft.repeatType === 'yearly' ? '公历月日（年份仅用于选择）' : '公历日期'}</label><input id="important-solar-date" class="feature-input" type="date" value="${value}"></div>`;
      } else {
        fields.innerHTML = `<div class="feature-field"><label>农历日期</label><div class="lunar-grid ${draft.repeatType === 'yearly' ? 'yearless' : ''}">${draft.repeatType === 'once' ? `<input id="important-lunar-year" class="feature-input" type="number" min="1900" max="2100" value="${draft.lunarYear || 2026}" aria-label="农历年份">` : ''}<select id="important-lunar-month" class="feature-select">${lunarMonths.map((name,i)=>`<option value="${i+1}" ${draft.lunarMonth===i+1?'selected':''}>${name}</option>`).join('')}</select><select id="important-lunar-day" class="feature-select">${lunarDays.map((name,i)=>`<option value="${i+1}" ${draft.lunarDay===i+1?'selected':''}>${name}</option>`).join('')}</select></div><label class="feature-check"><input id="important-leap" type="checkbox" ${draft.leapMonth?'checked':''}>这是闰月日期</label></div>`;
      }
    };

    const captureFields = () => {
      if (draft.calendarType === 'solar') {
        const value = document.getElementById('important-solar-date')?.value;
        if (value) {
          const [y,m,d] = value.split('-').map(Number);
          draft.year = y; draft.month = m; draft.day = d;
        }
      } else {
        draft.lunarYear = Number(document.getElementById('important-lunar-year')?.value || draft.lunarYear || 2026);
        draft.lunarMonth = Number(document.getElementById('important-lunar-month')?.value || draft.lunarMonth || 1);
        draft.lunarDay = Number(document.getElementById('important-lunar-day')?.value || draft.lunarDay || 1);
        draft.leapMonth = Boolean(document.getElementById('important-leap')?.checked);
      }
    };

    root.querySelectorAll('[data-feature-close]').forEach(el => el.onclick = () => root.innerHTML = '');
    root.querySelectorAll('[data-important-calendar]').forEach(el => el.onclick = () => { captureFields(); draft.calendarType = el.dataset.importantCalendar; renderFields(); });
    root.querySelectorAll('[data-important-repeat]').forEach(el => el.onclick = () => { captureFields(); draft.repeatType = el.dataset.importantRepeat; renderFields(); });
    document.getElementById('important-type').onchange = e => {
      draft.type = e.target.value;
      if (draft.type === '生日') draft.repeatType = 'yearly';
      renderFields();
    };
    renderFields();

    document.getElementById('save-important').onclick = () => {
      captureFields();
      draft.title = document.getElementById('important-title').value.trim();
      draft.type = document.getElementById('important-type').value;
      if (!draft.title) { document.getElementById('important-title').focus(); return; }
      if (existing) Object.assign(existing, draft);
      else data.importantDates.unshift({ ...draft, id: Date.now() });
      root.innerHTML = '';
      render();
      toast(existing ? '重要日期已更新' : '重要日期已添加');
    };

    const deleteButton = document.getElementById('delete-important');
    if (deleteButton) deleteButton.onclick = () => {
      data.importantDates = data.importantDates.filter(item => item.id !== existing.id);
      root.innerHTML = '';
      render();
      toast('重要日期已删除');
    };
  }

  render();
})();