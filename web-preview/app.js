const state = {
  route: 'today',
  history: [],
  taskFilter: '全部',
  taskQuery: '',
  completedTasks: new Set(),
};

const data = {
  timeline: [
    ['08:00', '晨间例行', '习惯', true],
    ['09:30', '产品会', '会议', false],
    ['11:00', '需求评审', '工作', false],
    ['14:00', '回顾邮件', '工作', false],
    ['16:00', '运动 30 分钟', '习惯', false],
  ],
  tasks: [
    { title: '撰写 PRD 文档', time: '11:00', project: 'LifeTrace 2.0', priority: 'high', status: '进行中' },
    { title: '设计登录页', time: '14:00', project: 'LifeTrace 2.0', priority: 'normal', status: '进行中' },
    { title: '回复合作方邮件', time: '16:00', project: '', priority: 'high', status: '等待' },
    { title: '整理竞品分析', time: '明天 10:00', project: '', priority: 'normal', status: '全部' },
    { title: '预约体检', time: '5/29 09:00', project: '', priority: 'normal', status: '全部' },
    { title: '学习 Kotlin 协程', time: '6/1 20:00', project: '', priority: 'normal', status: '全部' },
  ],
  projects: [
    ['LifeTrace 2.0 迭代', .60, '6月30日', 5, 'active'],
    ['个人品牌建设', .35, '7月15日', 2, 'active'],
    ['阅读计划 2026', .80, '9月30日', 1, 'active'],
    ['家庭旅行计划', .20, '10月1日', 3, 'paused'],
  ],
  buckets: [['待分类收集',12], ['灵感想法',8], ['阅读摘录',15], ['待办记录',6], ['参考链接',9]],
};

const iconPaths = {
  home: '<path d="M3 10.5 10 4l7 6.5v6.2a1.3 1.3 0 0 1-1.3 1.3h-3.8v-5H8.1v5H4.3A1.3 1.3 0 0 1 3 16.7z"/>',
  check: '<circle cx="10" cy="10" r="7"/><path d="m6.7 10 2.1 2.1 4.5-4.5"/>',
  folder: '<path d="M2.8 6.5h5l1.5 1.6h7.9v7.1a1.5 1.5 0 0 1-1.5 1.5H4.3a1.5 1.5 0 0 1-1.5-1.5z"/><path d="M2.8 6.5V5.4A1.4 1.4 0 0 1 4.2 4h3l1.5 1.5"/>',
  calendar: '<rect x="3" y="4.5" width="14" height="13" rx="2"/><path d="M6 2.8v3.4M14 2.8v3.4M3 8h14"/>',
  inbox: '<path d="M3 4.5h14l-1 12H4z"/><path d="M3.5 12h3l1.3 2h4.4l1.3-2h3"/>',
  user: '<circle cx="10" cy="7" r="3"/><path d="M4.3 17c.7-3.3 2.7-5 5.7-5s5 1.7 5.7 5"/>',
  search: '<circle cx="8.5" cy="8.5" r="5.5"/><path d="m12.5 12.5 4 4"/>',
  plus: '<path d="M10 4v12M4 10h12"/>',
  sparkle: '<path d="m10 2 1.2 4.2L15 8l-3.8 1.8L10 14l-1.2-4.2L5 8l3.8-1.8z"/><path d="m15.5 13 .6 2 .4.2-2 .8.2.4 2 .8-2 .6-2-.6-.2-.8z"/>',
  chevron: '<path d="m8 5 5 5-5 5"/>',
  back: '<path d="m12 4-6 6 6 6"/>',
  clock: '<circle cx="10" cy="10" r="7"/><path d="M10 6v4l2.8 1.7"/>',
  repeat: '<path d="M4 7h9l-2-2M16 13H7l2 2"/>',
  camera: '<path d="M4 7h2l1-2h6l1 2h2a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z"/><circle cx="10" cy="11" r="3"/>',
  mic: '<rect x="7.2" y="3" width="5.6" height="9" rx="2.8"/><path d="M5 10.5a5 5 0 0 0 10 0M10 15.5V18M7.5 18h5"/>',
  link: '<path d="M8.2 12.8 6.5 14.5a3 3 0 0 1-4.2-4.2L5 7.6a3 3 0 0 1 4.2 0"/><path d="m11.8 7.2 1.7-1.7a3 3 0 1 1 4.2 4.2L15 12.4a3 3 0 0 1-4.2 0"/><path d="m7.3 12.7 5.4-5.4"/>',
  file: '<path d="M5 2.8h6l4 4v10.4H5z"/><path d="M11 2.8v4h4M7.5 11h5M7.5 14h5"/>',
  bulb: '<path d="M6.3 11.8A5 5 0 1 1 13.7 12c-1 .8-1.2 1.5-1.2 2.2h-5c0-.8-.3-1.6-1.2-2.4zM7.5 17h5"/>',
  text: '<path d="M5 4h10M10 4v12M7 16h6"/>',
  image: '<rect x="3" y="4" width="14" height="12" rx="2"/><circle cx="7" cy="8" r="1.3"/><path d="m5 14 3.2-3 2.3 2 2.2-2 2.3 3"/>',
  settings: '<circle cx="10" cy="10" r="2.5"/><path d="M10 2.8v2M10 15.2v2M2.8 10h2M15.2 10h2M4.9 4.9l1.4 1.4M13.7 13.7l1.4 1.4M15.1 4.9l-1.4 1.4M6.3 13.7l-1.4 1.4"/>',
  shield: '<path d="M10 2.5 16 5v4.8c0 3.7-2.3 6.4-6 7.7-3.7-1.3-6-4-6-7.7V5z"/><path d="m7.3 10 1.8 1.8 3.6-3.6"/>',
  devices: '<rect x="3" y="4" width="10" height="9" rx="1.5"/><path d="M6 16h4M8 13v3"/><rect x="14" y="8" width="3" height="7" rx=".8"/>',
  bell: '<path d="M5.5 14h9l-1-1.5V9a3.5 3.5 0 1 0-7 0v3.5zM8.5 16a1.6 1.6 0 0 0 3 0"/>',
  cloud: '<path d="M6 15.5h8.2a3 3 0 0 0 .7-5.9A5 5 0 0 0 5.2 8 3.8 3.8 0 0 0 6 15.5z"/><path d="m8 12 2-2 2 2M10 10v5"/>',
  palette: '<path d="M10 3a7 7 0 1 0 0 14h1a1.5 1.5 0 0 0 0-3H9.8a1.3 1.3 0 0 1 0-2.6H12A5 5 0 0 0 10 3z"/><circle cx="6.2" cy="8" r=".7"/><circle cx="8.4" cy="5.8" r=".7"/><circle cx="12" cy="6" r=".7"/>',
  logout: '<path d="M8 4H4v12h4M12 7l3 3-3 3M7 10h8"/>',
};

function icon(name, cls = '') {
  return `<svg class="icon ${cls}" viewBox="0 0 20 20" aria-hidden="true">${iconPaths[name] || iconPaths.chevron}</svg>`;
}

const destinations = [
  ['today', '今天', 'home'],
  ['tasks', '任务', 'check'],
  ['projects', '项目', 'folder'],
  ['calendar', '日历', 'calendar'],
  ['collection', '收集', 'inbox'],
];

function go(route, push = true) {
  if (push && state.route !== route) state.history.push(state.route);
  state.route = route;
  render();
}

function back() {
  const previous = state.history.pop() || 'today';
  state.route = previous;
  render();
}

function header(title, subtitle) {
  return `<header class="screen-header">
    <div class="screen-header-main"><h1 class="screen-title">${title}</h1><p class="screen-subtitle">${subtitle}</p></div>
    <button class="avatar-btn" data-action="profile" aria-label="打开我的">${icon('user','lg')}</button>
  </header>`;
}

function sectionTitle(title, action = '') {
  return `<div class="section-heading"><h2>${title}</h2>${action ? `<button class="text-action" data-toast="${action}">${action}</button>` : ''}</div>`;
}

function metric(value, label, iconName) {
  return `<div class="metric-card"><span class="metric-icon">${icon(iconName,'sm')}</span><span class="metric-value">${value}</span><span class="metric-label">${label}</span></div>`;
}

function timelineRows() {
  return data.timeline.map(([time,title,tag,done]) => `<div class="timeline-row ${done ? 'done' : ''}">
    <span class="timeline-time">${time}</span><span class="timeline-rail"><i class="timeline-dot"></i></span>
    <span class="timeline-title">${title}</span><span class="tag ${tag === '工作' ? 'work' : tag === '习惯' ? 'habit' : 'meeting'}">${tag}</span>
  </div>`).join('');
}

function taskRow(task, index) {
  const complete = state.completedTasks.has(index);
  const meta = [task.project, task.status !== '全部' ? task.status : '待办'].filter(Boolean).join(' · ');
  return `<div class="task-row" data-task="${index}">
    <button class="check-circle ${complete ? 'completed' : ''}" data-action="toggle-task" data-index="${index}" aria-label="完成任务"></button>
    <div class="task-main"><div class="task-title-line">${task.priority === 'high' ? '<i class="priority-dot"></i>' : ''}<span class="task-title">${task.title}</span></div><div class="task-meta">${meta}</div></div>
    <span class="task-time">${task.time}</span>
  </div>`;
}

function todayPage() {
  return `<div class="page">
    ${header('早上好，Alex', '8月27日 · 周四')}
    <section class="section">${sectionTitle('今日概览')}<div class="metrics">${metric('8','待完成','check')}${metric('3','今日习惯','repeat')}${metric('2','进行中项目','folder')}</div></section>
    <section class="section">${sectionTitle('今日时间线','全部')}<div class="timeline-list">${timelineRows()}</div></section>
    <section class="section">${sectionTitle('今日任务','查看全部')}<div class="card task-card">${data.tasks.slice(0,2).map(taskRow).join('')}</div></section>
    <section class="section"><button class="review-card" data-action="review"><span class="review-icon">${icon('sparkle','lg')}</span><span class="review-copy"><strong>今日复盘</strong><span>回顾今天 · 记录收获 · 安排明日</span></span><span class="chevron">${icon('chevron')}</span></button></section>
  </div>`;
}

function filteredTasks() {
  const q = state.taskQuery.trim().toLowerCase();
  return data.tasks.filter(t => {
    const byQuery = !q || `${t.title} ${t.project}`.toLowerCase().includes(q);
    const byFilter = state.taskFilter === '全部' || t.status === state.taskFilter || (state.taskFilter === '已完成' && false);
    return byQuery && byFilter;
  });
}

function tasksPage() {
  const tasks = filteredTasks();
  const chips = ['全部','进行中','等待','已完成'].map(label => `<button class="chip ${state.taskFilter === label ? 'active' : ''}" data-filter="${label}">${label}</button>`).join('');
  return `<div class="page">
    ${header('任务', '专注执行')}
    <div class="search-field">${icon('search')}<input id="task-search" value="${state.taskQuery.replace(/"/g,'&quot;')}" placeholder="搜索任务" autocomplete="off" /></div>
    <div class="chips">${chips}</div>
    <section class="section">${sectionTitle(`今天 · ${tasks.length}`)}<div class="card task-card">${tasks.length ? tasks.map(task => taskRow(task, data.tasks.indexOf(task))).join('') : '<div style="padding:28px;text-align:center;color:var(--muted);font-size:11px">没有匹配的任务</div>'}</div></section>
    <button class="fab" data-toast="新建任务">${icon('plus')}<span>新建任务</span></button>
  </div>`;
}

function projectCard([title,progress,deadline,members,status]) {
  const avatars = Array.from({length: Math.min(members,3)}, () => '<i class="member-dot"></i>').join('');
  return `<article class="card project-card">
    <div class="project-top"><span class="project-title">${title}</span><span class="status-pill ${status === 'paused' ? 'paused' : ''}">${status === 'paused' ? '已暂停' : '进行中'}</span></div>
    <div class="progress-row"><div class="progress-track"><div class="progress-fill" style="width:${Math.round(progress*100)}%"></div></div><span class="progress-text">${Math.round(progress*100)}%</span></div>
    <div class="project-meta"><span>截止 ${deadline}</span><span class="avatar-stack">${avatars}<span style="margin-left:6px">${members} 人</span></span></div>
  </article>`;
}

function projectsPage() {
  return `<div class="page">${header('项目','目标与进度')}
    <section class="section">${sectionTitle('进行中的项目','管理')}<div class="project-stack">${data.projects.map(projectCard).join('')}</div></section>
    <button class="fab" data-toast="新建项目">${icon('plus')}<span>新建项目</span></button>
  </div>`;
}

function calendarDays() {
  const leading = [27,28,29,30,31].map(d => `<span class="day muted">${d}</span>`).join('');
  const days = Array.from({length:31},(_,i)=>i+1).map(d => `<span class="day ${d===27?'today':''} ${[4,8,14,21,27,30].includes(d)?'has-event':''}">${d}</span>`).join('');
  return leading + days;
}

function calendarPage() {
  return `<div class="page">${header('日历','规划时间')}
    <section class="section"><div class="card calendar-card"><div class="calendar-top"><button class="calendar-arrow" data-toast="上个月">${icon('back','sm')}</button><span class="calendar-month">2026年8月</span><button class="calendar-arrow" data-toast="下个月">${icon('chevron','sm')}</button></div><div class="weekdays"><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span><span>日</span></div><div class="calendar-grid">${calendarDays()}</div></div></section>
    <section class="section">${sectionTitle('8月27日 · 今天','新增')}<div class="card schedule-card">
      <div class="schedule-row"><span class="schedule-time">09:30</span><i class="schedule-color orange"></i><div class="schedule-copy"><strong>产品会</strong><span>会议 · 45 分钟</span></div></div>
      <div class="schedule-row"><span class="schedule-time">11:00</span><i class="schedule-color"></i><div class="schedule-copy"><strong>需求评审</strong><span>工作 · LifeTrace 2.0</span></div></div>
      <div class="schedule-row"><span class="schedule-time">16:00</span><i class="schedule-color green"></i><div class="schedule-copy"><strong>运动 30 分钟</strong><span>习惯 · 健身</span></div></div>
    </div></section>
  </div>`;
}

function captureButton(label, iconName) { return `<button class="capture-btn" data-toast="${label}"><span class="capture-icon">${icon(iconName,'lg')}</span><span>${label}</span></button>`; }
function collectionPage() {
  const bucketIcons = ['inbox','bulb','file','check','link'];
  return `<div class="page">${header('收集','随时捕捉，稍后整理')}
    <section class="section">${sectionTitle('快速收集')}<div class="capture-grid">${captureButton('文本','text')}${captureButton('图片','image')}${captureButton('语音','mic')}${captureButton('链接','link')}${captureButton('文件','file')}${captureButton('想法','bulb')}</div></section>
    <section class="section">${sectionTitle('收集箱','全部')}<div class="card inbox-list">${data.buckets.map(([title,count],i)=>`<div class="inbox-row"><span class="inbox-symbol">${icon(bucketIcons[i],'sm')}</span><span class="inbox-title">${title}</span><span class="inbox-count">${count}</span><span class="chevron">${icon('chevron','sm')}</span></div>`).join('')}</div></section>
  </div>`;
}

function profilePage() {
  const rows = [
    ['user','个人资料','头像、昵称、个人信息'],
    ['shield','账号与安全','登录方式、密码与验证'],
    ['devices','设备管理','已登录设备与会话'],
    ['bell','通知设置','提醒、免打扰与通知渠道'],
    ['cloud','同步与数据','LifeTrace Cloud · 已同步'],
    ['palette','通用设置','主题、语言、时区'],
    ['settings','关于 LifeTrace','版本、反馈与开源信息'],
  ];
  return `<div class="page"><div class="top-back"><button class="back-btn" data-action="back" aria-label="返回">${icon('back')}</button><strong>我的</strong></div>
    <div class="card profile-hero"><div class="profile-avatar">A</div><div class="profile-copy"><strong>Alex</strong><span>alex@example.com</span><div class="cloud-state"><i></i>LifeTrace Cloud 已登录</div></div><span class="chevron">${icon('chevron')}</span></div>
    <section class="section">${sectionTitle('账号与设置')}<div class="card settings-card">${rows.map(([ic,title,sub])=>`<div class="setting-row" data-toast="${title}"><span class="setting-icon">${icon(ic,'sm')}</span><div class="setting-copy"><strong>${title}</strong><span>${sub}</span></div><span class="chevron">${icon('chevron','sm')}</span></div>`).join('')}</div></section>
    <section class="section"><button class="primary-btn" style="background:#fff;color:var(--red);border:1px solid var(--border);box-shadow:none" data-toast="退出登录">退出当前账号</button></section>
  </div>`;
}

function reviewPage() {
  return `<div class="page"><div class="top-back"><button class="back-btn" data-action="back" aria-label="返回">${icon('back')}</button><strong>今日复盘</strong></div>
    <div class="card review-hero"><span class="eyebrow">Daily Review</span><h2>把今天收好，再去明天。</h2><p>用几分钟记录完成、收获和下一步。复盘结果会回到执行中心，成为明天的上下文。</p></div>
    <section class="section"><div class="review-form">
      <div class="card prompt-card"><label>今天最重要的完成是什么？</label><textarea placeholder="记录最值得保留的进展…"></textarea></div>
      <div class="card prompt-card"><label>有什么阻碍或需要等待？</label><textarea placeholder="问题、等待事项、风险…"></textarea></div>
      <div class="card prompt-card"><label>明天最重要的一件事</label><textarea placeholder="只写一个最重要的下一步…"></textarea></div>
      <button class="primary-btn" data-toast="复盘已保存">完成今日复盘</button>
    </div></section>
  </div>`;
}

function renderBottomNav() {
  const nav = document.getElementById('bottom-nav');
  const visible = destinations.some(([route]) => route === state.route);
  nav.style.display = visible ? 'grid' : 'none';
  document.querySelector('.gesture-bar').style.background = visible ? 'rgba(255,255,255,.97)' : 'var(--bg)';
  nav.innerHTML = destinations.map(([route,label,ic]) => `<button class="nav-btn ${state.route===route?'active':''}" data-route="${route}" aria-label="${label}"><span class="nav-icon">${icon(ic)}</span><span>${label}</span></button>`).join('');
}

function render() {
  const app = document.getElementById('app');
  const pages = { today: todayPage, tasks: tasksPage, projects: projectsPage, calendar: calendarPage, collection: collectionPage, profile: profilePage, review: reviewPage };
  app.innerHTML = (pages[state.route] || todayPage)();
  app.scrollTop = 0;
  renderBottomNav();
  bindEvents();
}

function toast(message) {
  const device = document.getElementById('device');
  let node = device.querySelector('.toast');
  if (!node) { node = document.createElement('div'); node.className = 'toast'; device.appendChild(node); }
  node.textContent = `${message} · 原型交互`;
  node.classList.add('show');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => node.classList.remove('show'), 1400);
}

function bindEvents() {
  document.querySelectorAll('[data-route]').forEach(btn => btn.onclick = () => go(btn.dataset.route));
  document.querySelectorAll('[data-action="profile"]').forEach(btn => btn.onclick = () => go('profile'));
  document.querySelectorAll('[data-action="review"]').forEach(btn => btn.onclick = () => go('review'));
  document.querySelectorAll('[data-action="back"]').forEach(btn => btn.onclick = back);
  document.querySelectorAll('[data-toast]').forEach(btn => btn.onclick = () => toast(btn.dataset.toast));
  document.querySelectorAll('[data-filter]').forEach(btn => btn.onclick = () => { state.taskFilter = btn.dataset.filter; render(); });
  document.querySelectorAll('[data-action="toggle-task"]').forEach(btn => btn.onclick = () => {
    const index = Number(btn.dataset.index);
    state.completedTasks.has(index) ? state.completedTasks.delete(index) : state.completedTasks.add(index);
    render();
  });
  const search = document.getElementById('task-search');
  if (search) {
    search.oninput = e => {
      state.taskQuery = e.target.value;
      const value = e.target.value;
      const selection = e.target.selectionStart;
      render();
      const next = document.getElementById('task-search');
      if (next) { next.focus(); next.value = value; next.setSelectionRange(selection, selection); }
    };
  }
}

render();
