const state = {
  route: 'today',
  history: [],
  taskFilter: '全部',
  taskQuery: '',
  completedTasks: new Set([5]),
  selectedDay: 27,
  projectFilter: '全部',
  mood: 4,
  darkMode: false,
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
    { title: '回复合作方邮件', time: '16:00', project: '工作', priority: 'high', status: '等待' },
    { title: '整理竞品分析', time: '明天 10:00', project: 'LifeTrace 2.0', priority: 'normal', status: '进行中' },
    { title: '预约体检', time: '5/29 09:00', project: '生活', priority: 'normal', status: '等待' },
    { title: '学习 Kotlin 协程', time: '6/1 20:00', project: '学习', priority: 'normal', status: '已完成' },
  ],
  projects: [
    { title: 'LifeTrace 2.0 迭代', progress: .60, deadline: '6月30日', members: 5, status: '进行中', desc: 'Android 执行中心与同步能力' },
    { title: '个人品牌建设', progress: .35, deadline: '7月15日', members: 2, status: '进行中', desc: '主页、文章与内容节奏' },
    { title: '阅读计划 2026', progress: .80, deadline: '9月30日', members: 1, status: '进行中', desc: '年度 36 本主题阅读' },
    { title: '家庭旅行计划', progress: .20, deadline: '10月1日', members: 3, status: '暂停', desc: '路线、预算与行程安排' },
  ],
  buckets: [
    ['待分类收集', 12, '今天新增 3 条', 'inbox'],
    ['灵感想法', 8, '最近整理 2 条', 'sparkle'],
    ['阅读摘录', 15, '本周新增 5 条', 'book'],
    ['待办记录', 6, '2 条待转任务', 'check'],
    ['参考链接', 9, '4 条未归档', 'link'],
  ],
};

const paths = {
  home:'<path d="M3 9.5 10 3l7 6.5v6.6A1.9 1.9 0 0 1 15.1 18h-3.7v-5H8.6v5H4.9A1.9 1.9 0 0 1 3 16.1z"/>',
  check:'<circle cx="10" cy="10" r="7"/><path d="m6.7 10 2.1 2.1 4.5-4.5"/>',
  folder:'<path d="M2.7 6.7h5.1l1.5 1.5h8v7a1.6 1.6 0 0 1-1.6 1.6H4.3a1.6 1.6 0 0 1-1.6-1.6z"/><path d="M2.7 6.7V5.3A1.4 1.4 0 0 1 4.1 4h3l1.6 1.5"/>',
  calendar:'<rect x="3" y="4.5" width="14" height="13" rx="2"/><path d="M6 2.8v3.4M14 2.8v3.4M3 8h14"/>',
  inbox:'<path d="M3 4.5h14l-1 12H4z"/><path d="M3.5 12h3l1.3 2h4.4l1.3-2h3"/>',
  user:'<circle cx="10" cy="7" r="3"/><path d="M4.3 17c.7-3.3 2.7-5 5.7-5s5 1.7 5.7 5"/>',
  search:'<circle cx="8.5" cy="8.5" r="5.5"/><path d="m12.5 12.5 4 4"/>',
  plus:'<path d="M10 4v12M4 10h12"/>',
  sparkle:'<path d="m10 2 1.2 4.2L15 8l-3.8 1.8L10 14l-1.2-4.2L5 8l3.8-1.8z"/><path d="m15.5 13 .6 2 .4.2-2 .8.2.4 2 .8-2 .6-2-.6-.2-.8z"/>',
  chevron:'<path d="m8 5 5 5-5 5"/>',
  back:'<path d="m12 4-6 6 6 6"/>',
  repeat:'<path d="M4 7h9l-2-2M16 13H7l2 2"/>',
  camera:'<path d="M4 7h2l1-2h6l1 2h2a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z"/><circle cx="10" cy="11" r="3"/>',
  mic:'<rect x="7.2" y="3" width="5.6" height="9" rx="2.8"/><path d="M5 10.5a5 5 0 0 0 10 0M10 15.5V18M7.5 18h5"/>',
  link:'<path d="M8.2 12.8 6.5 14.5a3 3 0 0 1-4.2-4.2L5 7.6a3 3 0 0 1 4.2 0"/><path d="m11.8 7.2 1.7-1.7a3 3 0 1 1 4.2 4.2L15 12.4a3 3 0 0 1-4.2 0"/><path d="m7.3 12.7 5.4-5.4"/>',
  file:'<path d="M5 2.8h6l4 4v10.4H5z"/><path d="M11 2.8v4h4M7.5 11h5M7.5 14h5"/>',
  bulb:'<path d="M6.3 11.8A5 5 0 1 1 13.7 12c-1 .8-1.2 1.5-1.2 2.2h-5c0-.8-.3-1.6-1.2-2.4zM7.5 17h5"/>',
  text:'<path d="M5 4h10M10 4v12M7 16h6"/>',
  image:'<rect x="3" y="4" width="14" height="12" rx="2"/><circle cx="7" cy="8" r="1.3"/><path d="m5 14 3.2-3 2.3 2 2.2-2 2.3 3"/>',
  book:'<path d="M4 4.2h5.4A2.6 2.6 0 0 1 12 6.8V17a2.6 2.6 0 0 0-2.6-2.6H4z"/><path d="M16 4.2h-4.2M16 4.2v10.2h-1.4A2.6 2.6 0 0 0 12 17"/>',
  shield:'<path d="M10 2.5 16 5v4.8c0 3.7-2.3 6.4-6 7.7-3.7-1.3-6-4-6-7.7V5z"/><path d="m7.3 10 1.8 1.8 3.6-3.6"/>',
  devices:'<rect x="3" y="4" width="10" height="9" rx="1.5"/><path d="M6 16h4M8 13v3"/><rect x="14" y="8" width="3" height="7" rx=".8"/>',
  bell:'<path d="M5.5 14h9l-1-1.5V9a3.5 3.5 0 1 0-7 0v3.5zM8.5 16a1.6 1.6 0 0 0 3 0"/>',
  cloud:'<path d="M6 15.5h8.2a3 3 0 0 0 .7-5.9A5 5 0 0 0 5.2 8 3.8 3.8 0 0 0 6 15.5z"/><path d="m8 12 2-2 2 2M10 10v5"/>',
  palette:'<path d="M10 3a7 7 0 1 0 0 14h1a1.5 1.5 0 0 0 0-3H9.8a1.3 1.3 0 0 1 0-2.6H12A5 5 0 0 0 10 3z"/><circle cx="6.2" cy="8" r=".7"/><circle cx="8.4" cy="5.8" r=".7"/><circle cx="12" cy="6" r=".7"/>',
  info:'<circle cx="10" cy="10" r="7"/><path d="M10 9v5M10 6.5h.01"/>',
  logout:'<path d="M8 4H4v12h4M12 7l3 3-3 3M7 10h8"/>',
  target:'<circle cx="10" cy="10" r="6"/><circle cx="10" cy="10" r="2"/>',
  clock:'<circle cx="10" cy="10" r="7"/><path d="M10 6v4l2.8 1.7"/>',
  users:'<circle cx="8" cy="7" r="2.5"/><path d="M3.8 15c.5-2.6 1.9-4 4.2-4s3.7 1.4 4.2 4"/><path d="M13 5.5a2.2 2.2 0 0 1 0 4.2M13.5 11.5c1.6.3 2.6 1.4 2.9 3.1"/>',
  lock:'<rect x="4" y="8" width="12" height="9" rx="2"/><path d="M7 8V6a3 3 0 0 1 6 0v2"/>',
};

const destinations = [
  ['today','今天','home'], ['tasks','任务','check'], ['projects','项目','folder'], ['calendar','日历','calendar'], ['collection','收集','inbox']
];

function icon(name, cls='') { return `<svg class="icon ${cls}" viewBox="0 0 20 20" aria-hidden="true">${paths[name] || paths.chevron}</svg>`; }
function esc(v='') { return String(v).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function go(route, push=true) { if (push && state.route !== route) state.history.push(state.route); state.route = route; render(); }
function back() { state.route = state.history.pop() || 'today'; render(); }
function toast(message) { const el = document.getElementById('toast'); el.textContent = message; el.classList.add('show'); clearTimeout(toast.timer); toast.timer = setTimeout(() => el.classList.remove('show'), 1650); }

function header(title, subtitle, kicker='') {
  return `<header class="screen-header"><div class="screen-header-main">${kicker ? `<div class="screen-kicker">${kicker}</div>` : ''}<h1 class="screen-title">${title}</h1><p class="screen-subtitle">${subtitle}</p></div><button class="avatar-btn" data-action="profile" aria-label="打开我的">${icon('user','lg')}</button></header>`;
}
function subHeader(title, subtitle='') { return `<header class="subpage-header"><button class="icon-btn" data-action="back" aria-label="返回">${icon('back')}</button><div class="screen-header-main"><h1 class="screen-title">${title}</h1>${subtitle ? `<p class="screen-subtitle">${subtitle}</p>` : ''}</div></header>`; }
function sectionTitle(title, action='') { return `<div class="section-heading"><h2>${title}</h2>${action ? `<button class="text-action" data-toast="${esc(action)}">${action}</button>` : ''}</div>`; }
function metric(value,label,iconName) { return `<div class="metric-card"><div class="metric-top"><span class="metric-icon">${icon(iconName,'sm')}</span><span class="metric-value">${value}</span></div><span class="metric-label">${label}</span></div>`; }

function weekStrip() {
  const days = [['一',24],['二',25],['三',26],['四',27],['五',28],['六',29],['日',30]];
  return `<div class="week-strip">${days.map(([w,d]) => `<button class="day-pill ${d===27?'today':''}" data-toast="8月${d}日"><span>${w}</span><b>${d}</b></button>`).join('')}</div>`;
}
function timelineRows() { return data.timeline.map(([time,title,tag,done]) => `<div class="timeline-row ${done?'done':''}"><span class="timeline-time">${time}</span><span class="timeline-rail"><i class="timeline-dot"></i></span><span class="timeline-title">${title}</span><span class="tag ${tag==='工作'?'work':tag==='习惯'?'habit':'meeting'}">${tag}</span></div>`).join(''); }
function taskRow(task, index) {
  const complete = state.completedTasks.has(index) || task.status === '已完成';
  const meta = [task.project, task.status].filter(Boolean).join(' · ');
  return `<div class="task-row ${complete?'is-complete':''}"><button class="check-circle ${complete?'completed':''}" data-action="toggle-task" data-index="${index}" aria-label="切换任务完成状态"></button><div class="task-main"><div class="task-title-line">${task.priority==='high'?'<i class="priority-dot"></i>':''}<span class="task-title">${esc(task.title)}</span></div><div class="task-meta">${esc(meta)}</div></div><span class="task-time">${esc(task.time)}</span></div>`;
}

function todayPage() {
  return `<div class="page">${header('早上好，Alex','8月27日 · 周四','今日 · 执行中心')}
    <section class="hero-card"><div class="hero-top"><div><div class="hero-eyebrow">今日焦点</div><div class="hero-focus">完成 LifeTrace Android 交互稿</div></div><div class="hero-progress"><svg viewBox="0 0 48 48"><circle class="track" cx="24" cy="24" r="18"/><circle class="fill" cx="24" cy="24" r="18"/></svg><b>68%</b></div></div><div class="hero-bottom"><div class="hero-stat"><span>深度工作</span><strong>2h 40m</strong></div><div class="hero-stat"><span>已完成</span><strong>5 / 8</strong></div><div class="hero-stat"><span>连续执行</span><strong>12 天</strong></div></div></section>
    <section class="section">${weekStrip()}</section>
    <section class="section">${sectionTitle('今日概览')}<div class="metrics">${metric('8','待完成','check')}${metric('3','今日习惯','repeat')}${metric('2','进行中项目','folder')}</div></section>
    <section class="section">${sectionTitle('今日时间线','查看日历')}<div class="timeline-list">${timelineRows()}</div></section>
    <section class="section">${sectionTitle('今日任务','查看全部')}<div class="card task-card">${data.tasks.slice(0,3).map((t,i)=>taskRow(t,i)).join('')}</div></section>
    <section class="section"><button class="review-card" data-action="review"><span class="review-icon">${icon('sparkle','lg')}</span><span class="review-copy"><strong>今日复盘</strong><span>回顾今天 · 记录收获 · 安排明日</span></span><span class="chevron">${icon('chevron')}</span></button></section>
  </div>`;
}

function filteredTasks() {
  const q = state.taskQuery.trim().toLowerCase();
  return data.tasks.filter((t,i) => {
    const complete = state.completedTasks.has(i) || t.status === '已完成';
    const byQuery = !q || `${t.title} ${t.project} ${t.status}`.toLowerCase().includes(q);
    let byFilter = state.taskFilter === '全部' || t.status === state.taskFilter;
    if (state.taskFilter === '已完成') byFilter = complete;
    return byQuery && byFilter;
  });
}
function tasksPage() {
  const tasks = filteredTasks();
  const chips = ['全部','进行中','等待','已完成'].map(x=>`<button class="chip ${state.taskFilter===x?'active':''}" data-filter="${x}">${x}</button>`).join('');
  return `<div class="page">${header('任务','专注执行，把事情做完','TODAY')}
    <div class="search-field">${icon('search')}<input id="task-search" value="${esc(state.taskQuery)}" placeholder="搜索任务、项目或状态" autocomplete="off" /></div>
    <div class="chips">${chips}</div>
    <section class="section">${sectionTitle(`今天 · ${tasks.length}`,'批量管理')}<div class="card task-card">${tasks.length ? tasks.map(t=>taskRow(t,data.tasks.indexOf(t))).join('') : '<div style="padding:34px 18px;text-align:center;color:var(--muted);font-size:10.5px">没有匹配的任务</div>'}</div></section>
    <button class="fab" data-action="new-task">${icon('plus','sm')}<span>新建任务</span></button>
  </div>`;
}

function projectsPage() {
  const projects = data.projects.filter(p => state.projectFilter==='全部' || p.status===state.projectFilter);
  const chips = ['全部','进行中','暂停'].map(x=>`<button class="chip ${state.projectFilter===x?'active':''}" data-project-filter="${x}">${x}</button>`).join('');
  const cards = projects.map((p,idx)=>`<article class="card project-card" data-toast="打开项目：${esc(p.title)}"><div class="project-top"><div><div class="project-title">${esc(p.title)}</div><div class="project-desc">${esc(p.desc)}</div></div><span class="status-pill ${p.status==='暂停'?'paused':''}">${p.status}</span></div><div class="progress-row"><div class="progress-track"><div class="progress-fill" style="width:${Math.round(p.progress*100)}%"></div></div><span class="progress-number">${Math.round(p.progress*100)}%</span></div><div class="project-foot"><span>${icon('clock','sm')} 截止 ${p.deadline}</span><span class="member-stack">${Array.from({length:Math.min(p.members,4)},(_,i)=>`<i class="member">${String.fromCharCode(65+i+idx)}</i>`).join('')}</span></div></article>`).join('');
  return `<div class="page">${header('项目','目标与进度，一目了然','PROJECTS')}
    <div class="project-summary"><div class="project-summary-card"><strong>4</strong><span>全部项目</span></div><div class="project-summary-card"><strong>3</strong><span>进行中</span></div><div class="project-summary-card"><strong>49%</strong><span>平均进度</span></div></div>
    <div class="chips">${chips}</div>
    <section class="section">${sectionTitle('项目列表','新建项目')}<div class="project-stack">${cards}</div></section>
  </div>`;
}

function calendarDays() {
  const leading = 5; const total = 31; const cells=[];
  for(let i=0;i<leading;i++) cells.push(`<button class="calendar-day out">${27+i}</button>`);
  for(let d=1;d<=total;d++) { const has=[3,7,12,18,21,27,28].includes(d); cells.push(`<button class="calendar-day ${state.selectedDay===d?'selected':''} ${has?'has-event':''}" data-day="${d}">${d}</button>`); }
  while(cells.length%7) cells.push(`<button class="calendar-day out">${cells.length-35}</button>`);
  return cells.join('');
}
function calendarPage() {
  const schedule = state.selectedDay===27 ? [
    ['09:30','产品会','会议 · 45 分钟','orange'], ['11:00','需求评审','LifeTrace 2.0 · 60 分钟',''], ['16:00','运动 30 分钟','习惯 · 健身','green']
  ] : [['10:00','暂无固定日程','可以安排新的任务或习惯','']];
  return `<div class="page">${header('日历','任务、习惯与日程集中查看','CALENDAR')}
    <div class="calendar-toolbar"><strong>2026年8月</strong><div class="calendar-nav"><button data-toast="上个月">‹</button><button data-toast="下个月">›</button></div></div>
    <div class="card calendar-card"><div class="calendar-weekdays">${['一','二','三','四','五','六','日'].map(x=>`<span>${x}</span>`).join('')}</div><div class="calendar-grid">${calendarDays()}</div></div>
    <section class="section">${sectionTitle(`8月${state.selectedDay}日 ${state.selectedDay===27?'· 今天':''}`,'新建日程')}<div class="schedule-stack">${schedule.map(([t,title,meta,c])=>`<div class="schedule-item"><span class="schedule-time">${t}</span><i class="schedule-line ${c}"></i><div><div class="schedule-title">${title}</div><div class="schedule-meta">${meta}</div></div></div>`).join('')}</div></section>
  </div>`;
}

function collectionPage() {
  const capture = [['text','文本'],['image','图片'],['mic','语音'],['link','链接'],['file','文件'],['bulb','想法']].map(([i,l])=>`<button class="capture-btn" data-capture="${l}"><span class="capture-icon">${icon(i,'sm')}</span><span>${l}</span></button>`).join('');
  const inbox = data.buckets.map(([title,count,sub,i])=>`<div class="inbox-row" data-toast="打开：${esc(title)}"><span class="inbox-icon">${icon(i,'sm')}</span><div class="inbox-main"><strong>${title}</strong><span>${sub}</span></div><span class="inbox-count">${count}</span></div>`).join('');
  return `<div class="page">${header('收集','随时捕捉，稍后整理','INBOX')}
    <section class="section">${sectionTitle('快速收集')}<div class="capture-grid">${capture}</div></section>
    <section class="section">${sectionTitle('收集箱','整理全部')}<div class="card inbox-list">${inbox}</div></section>
    <section class="section">${sectionTitle('最近收集')}<div class="card" style="padding:2px 12px"><div class="recent-capture"><i class="dot"></i><div><strong>Android UI 交互细节</strong><span>想法 · 12 分钟前</span></div></div><div class="recent-capture"><i class="dot" style="background:var(--primary)"></i><div><strong>Material 3 Navigation 设计参考</strong><span>链接 · 今天 14:20</span></div></div><div class="recent-capture"><i class="dot" style="background:var(--green)"></i><div><strong>《高效能人士的七个习惯》摘录</strong><span>阅读摘录 · 昨天</span></div></div></div></section>
  </div>`;
}

function settingRow(iconName,title,subtitle,right='') { return `<div class="setting-row" data-toast="${esc(title)}"><span class="setting-icon">${icon(iconName,'sm')}</span><div class="setting-main"><strong>${title}</strong><span>${subtitle}</span></div><span class="setting-right">${right || icon('chevron','sm')}</span></div>`; }
function profilePage() {
  return `<div class="page">${subHeader('我的','账号、同步与应用设置')}
    <div class="card profile-hero"><div class="profile-avatar">A</div><div class="profile-main"><strong>Alex</strong><span>alex@example.com</span><span class="sync-badge">${icon('cloud','sm')} LifeTrace Cloud 已同步</span></div>${icon('chevron','sm')}</div>
    <section class="section">${sectionTitle('账号')}<div class="card settings-group">${settingRow('user','个人资料','头像、昵称与个人信息')}${settingRow('shield','账号与安全','登录方式、密码与验证')}${settingRow('devices','设备管理','2 台设备已登录')}</div></section>
    <section class="section">${sectionTitle('偏好与数据')}<div class="card settings-group">${settingRow('cloud','同步与数据','刚刚完成同步','已同步')}${settingRow('bell','通知设置','任务提醒与免打扰')}${settingRow('palette','外观','跟随系统',`<span class="toggle ${state.darkMode?'on':''}" data-action="toggle-theme"><i></i></span>`)}</div></section>
    <section class="section">${sectionTitle('其他')}<div class="card settings-group">${settingRow('info','关于 LifeTrace','版本、反馈与开源信息')}${settingRow('logout','退出登录','当前账号 Alex')}</div></section>
  </div>`;
}

function reviewPage() {
  return `<div class="page">${subHeader('今日复盘','8月27日 · 周四')}
    <div class="review-summary"><div><strong>今天完成得不错</strong><span>已完成 5 项任务 · 3 个习惯 · 深度工作 2h 40m</span></div><div class="review-score">82</div></div>
    <section class="section">${sectionTitle('今天的状态')}<div class="mood-row">${['😞','😐','🙂','😊','🤩'].map((m,i)=>`<button class="mood-btn ${state.mood===i?'active':''}" data-mood="${i}">${m}</button>`).join('')}</div></section>
    <section class="section"><div class="card review-field"><label>今天最重要的收获</label><textarea placeholder="写下一件值得记住的事情……">LifeTrace Execute 的信息架构已经稳定，可以把精力集中到高保真实现。</textarea></div></section>
    <section class="section"><div class="card review-field"><label>有什么可以改进</label><textarea placeholder="今天有哪些地方可以做得更好……">减少工具切换，优先在浏览器原型里把交互细节一次性确认。</textarea></div></section>
    <section class="section"><div class="card review-field"><label>明天的第一优先级</label><textarea placeholder="明天最先完成什么……">完成 Android 端视觉同步，并验证核心页面。</textarea></div></section>
    <section class="section"><button class="primary-btn" data-action="save-review">保存今日复盘</button></section>
  </div>`;
}

function renderBottomNav() {
  const nav = document.getElementById('bottom-nav');
  const show = destinations.some(([r]) => r===state.route);
  nav.style.display = show ? 'grid' : 'none';
  if (!show) { nav.innerHTML=''; return; }
  nav.innerHTML = destinations.map(([route,label,i])=>`<button class="nav-item ${state.route===route?'active':''}" data-route="${route}"><span class="nav-icon-wrap">${icon(i)}</span><span>${label}</span></button>`).join('');
}

function render() {
  const app = document.getElementById('app');
  const pages = { today:todayPage, tasks:tasksPage, projects:projectsPage, calendar:calendarPage, collection:collectionPage, profile:profilePage, review:reviewPage };
  app.innerHTML = (pages[state.route] || todayPage)();
  app.scrollTop = 0;
  renderBottomNav();
  bindEvents();
}

function bindEvents() {
  document.querySelectorAll('[data-route]').forEach(el=>el.onclick=()=>go(el.dataset.route,false));
  document.querySelectorAll('[data-action="profile"]').forEach(el=>el.onclick=()=>go('profile'));
  document.querySelectorAll('[data-action="review"]').forEach(el=>el.onclick=()=>go('review'));
  document.querySelectorAll('[data-action="back"]').forEach(el=>el.onclick=back);
  document.querySelectorAll('[data-action="toggle-task"]').forEach(el=>el.onclick=()=>{ const i=Number(el.dataset.index); state.completedTasks.has(i)?state.completedTasks.delete(i):state.completedTasks.add(i); render(); });
  document.querySelectorAll('[data-filter]').forEach(el=>el.onclick=()=>{ state.taskFilter=el.dataset.filter; render(); });
  document.querySelectorAll('[data-project-filter]').forEach(el=>el.onclick=()=>{ state.projectFilter=el.dataset.projectFilter; render(); });
  document.querySelectorAll('[data-day]').forEach(el=>el.onclick=()=>{ state.selectedDay=Number(el.dataset.day); render(); });
  document.querySelectorAll('[data-mood]').forEach(el=>el.onclick=()=>{ state.mood=Number(el.dataset.mood); render(); });
  document.querySelectorAll('[data-toast]').forEach(el=>el.onclick=e=>{ if (e.target.closest('[data-action="toggle-theme"]')) return; toast(el.dataset.toast); });
  document.querySelectorAll('[data-capture]').forEach(el=>el.onclick=()=>toast(`已打开${el.dataset.capture}收集`));
  const search = document.getElementById('task-search');
  if (search) search.oninput = e => { state.taskQuery=e.target.value; const cursor=e.target.selectionStart; render(); const next=document.getElementById('task-search'); if(next){next.focus();next.setSelectionRange(cursor,cursor);} };
  const newTask = document.querySelector('[data-action="new-task"]'); if(newTask) newTask.onclick=openTaskSheet;
  const theme = document.querySelector('[data-action="toggle-theme"]'); if(theme) theme.onclick=e=>{ e.stopPropagation(); state.darkMode=!state.darkMode; toast(state.darkMode?'深色模式将在 Android 端同步实现':'已切回浅色模式'); render(); };
  const save = document.querySelector('[data-action="save-review"]'); if(save) save.onclick=()=>{ toast('今日复盘已保存'); setTimeout(()=>go('today',false),700); };
}

function openTaskSheet() {
  document.getElementById('modal-root').innerHTML = `<div class="modal-backdrop" data-sheet-close><div class="bottom-sheet" onclick="event.stopPropagation()"><div class="sheet-handle"></div><div class="sheet-title"><strong>新建任务</strong><button class="icon-btn" data-sheet-close>×</button></div><input class="sheet-input" id="new-task-title" placeholder="任务名称" autofocus><input class="sheet-input" placeholder="所属项目（可选）"><div class="sheet-actions"><button class="sheet-cancel" data-sheet-close>取消</button><button class="sheet-confirm" id="create-task">创建任务</button></div></div></div>`;
  document.querySelectorAll('[data-sheet-close]').forEach(el=>el.onclick=()=>document.getElementById('modal-root').innerHTML='');
  document.getElementById('create-task').onclick=()=>{ const input=document.getElementById('new-task-title'); const title=input.value.trim(); if(!title){input.focus();return;} data.tasks.unshift({title,time:'今天',project:'',priority:'normal',status:'进行中'}); document.getElementById('modal-root').innerHTML=''; toast('任务已创建'); render(); };
  setTimeout(()=>document.getElementById('new-task-title')?.focus(),30);
}

render();
