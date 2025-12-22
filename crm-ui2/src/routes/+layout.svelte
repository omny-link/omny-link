<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import '../app.css';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak, { initKeycloak, keycloakStore } from '$lib/keycloak';

  let authenticated = false;
  let username = '';
  let userEmail = '';
  let sidebarCollapsed = false;

  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
  }

  // Simple MD5 implementation for gravatar
  function md5(string) {
    function md5cycle(x, k) {
      let a = x[0], b = x[1], c = x[2], d = x[3];
      a = ff(a, b, c, d, k[0], 7, -680876936);
      d = ff(d, a, b, c, k[1], 12, -389564586);
      c = ff(c, d, a, b, k[2], 17, 606105819);
      b = ff(b, c, d, a, k[3], 22, -1044525330);
      a = ff(a, b, c, d, k[4], 7, -176418897);
      d = ff(d, a, b, c, k[5], 12, 1200080426);
      c = ff(c, d, a, b, k[6], 17, -1473231341);
      b = ff(b, c, d, a, k[7], 22, -45705983);
      a = ff(a, b, c, d, k[8], 7, 1770035416);
      d = ff(d, a, b, c, k[9], 12, -1958414417);
      c = ff(c, d, a, b, k[10], 17, -42063);
      b = ff(b, c, d, a, k[11], 22, -1990404162);
      a = ff(a, b, c, d, k[12], 7, 1804603682);
      d = ff(d, a, b, c, k[13], 12, -40341101);
      c = ff(c, d, a, b, k[14], 17, -1502002290);
      b = ff(b, c, d, a, k[15], 22, 1236535329);
      a = gg(a, b, c, d, k[1], 5, -165796510);
      d = gg(d, a, b, c, k[6], 9, -1069501632);
      c = gg(c, d, a, b, k[11], 14, 643717713);
      b = gg(b, c, d, a, k[0], 20, -373897302);
      a = gg(a, b, c, d, k[5], 5, -701558691);
      d = gg(d, a, b, c, k[10], 9, 38016083);
      c = gg(c, d, a, b, k[15], 14, -660478335);
      b = gg(b, c, d, a, k[4], 20, -405537848);
      a = gg(a, b, c, d, k[9], 5, 568446438);
      d = gg(d, a, b, c, k[14], 9, -1019803690);
      c = gg(c, d, a, b, k[3], 14, -187363961);
      b = gg(b, c, d, a, k[8], 20, 1163531501);
      a = gg(a, b, c, d, k[13], 5, -1444681467);
      d = gg(d, a, b, c, k[2], 9, -51403784);
      c = gg(c, d, a, b, k[7], 14, 1735328473);
      b = gg(b, c, d, a, k[12], 20, -1926607734);
      a = hh(a, b, c, d, k[5], 4, -378558);
      d = hh(d, a, b, c, k[8], 11, -2022574463);
      c = hh(c, d, a, b, k[11], 16, 1839030562);
      b = hh(b, c, d, a, k[14], 23, -35309556);
      a = hh(a, b, c, d, k[1], 4, -1530992060);
      d = hh(d, a, b, c, k[4], 11, 1272893353);
      c = hh(c, d, a, b, k[7], 16, -155497632);
      b = hh(b, c, d, a, k[10], 23, -1094730640);
      a = hh(a, b, c, d, k[13], 4, 681279174);
      d = hh(d, a, b, c, k[0], 11, -358537222);
      c = hh(c, d, a, b, k[3], 16, -722521979);
      b = hh(b, c, d, a, k[6], 23, 76029189);
      a = hh(a, b, c, d, k[9], 4, -640364487);
      d = hh(d, a, b, c, k[12], 11, -421815835);
      c = hh(c, d, a, b, k[15], 16, 530742520);
      b = hh(b, c, d, a, k[2], 23, -995338651);
      a = ii(a, b, c, d, k[0], 6, -198630844);
      d = ii(d, a, b, c, k[7], 10, 1126891415);
      c = ii(c, d, a, b, k[14], 15, -1416354905);
      b = ii(b, c, d, a, k[5], 21, -57434055);
      a = ii(a, b, c, d, k[12], 6, 1700485571);
      d = ii(d, a, b, c, k[3], 10, -1894986606);
      c = ii(c, d, a, b, k[10], 15, -1051523);
      b = ii(b, c, d, a, k[1], 21, -2054922799);
      a = ii(a, b, c, d, k[8], 6, 1873313359);
      d = ii(d, a, b, c, k[15], 10, -30611744);
      c = ii(c, d, a, b, k[6], 15, -1560198380);
      b = ii(b, c, d, a, k[13], 21, 1309151649);
      a = ii(a, b, c, d, k[4], 6, -145523070);
      d = ii(d, a, b, c, k[11], 10, -1120210379);
      c = ii(c, d, a, b, k[2], 15, 718787259);
      b = ii(b, c, d, a, k[9], 21, -343485551);
      x[0] = add32(a, x[0]);
      x[1] = add32(b, x[1]);
      x[2] = add32(c, x[2]);
      x[3] = add32(d, x[3]);
    }
    function cmn(q, a, b, x, s, t) {
      a = add32(add32(a, q), add32(x, t));
      return add32((a << s) | (a >>> (32 - s)), b);
    }
    function ff(a, b, c, d, x, s, t) {
      return cmn((b & c) | ((~b) & d), a, b, x, s, t);
    }
    function gg(a, b, c, d, x, s, t) {
      return cmn((b & d) | (c & (~d)), a, b, x, s, t);
    }
    function hh(a, b, c, d, x, s, t) {
      return cmn(b ^ c ^ d, a, b, x, s, t);
    }
    function ii(a, b, c, d, x, s, t) {
      return cmn(c ^ (b | (~d)), a, b, x, s, t);
    }
    function add32(a, b) {
      return (a + b) & 0xFFFFFFFF;
    }
    const n = string.length;
    const state = [1732584193, -271733879, -1732584194, 271733878];
    let i;
    for (i = 64; i <= string.length; i += 64) {
      md5cycle(state, md5blk(string.substring(i - 64, i)));
    }
    string = string.substring(i - 64);
    const tail = [0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0];
    for (i = 0; i < string.length; i++)
      tail[i>>2] |= string.charCodeAt(i) << ((i % 4) << 3);
    tail[i>>2] |= 0x80 << ((i % 4) << 3);
    if (i > 55) {
      md5cycle(state, tail);
      for (i = 0; i < 16; i++) tail[i] = 0;
    }
    tail[14] = n * 8;
    md5cycle(state, tail);
    return state;
  }
  function md5blk(s) {
    const md5blks = [];
    for (let i = 0; i < 64; i += 4) {
      md5blks[i>>2] = s.charCodeAt(i) + (s.charCodeAt(i+1) << 8) + (s.charCodeAt(i+2) << 16) + (s.charCodeAt(i+3) << 24);
    }
    return md5blks;
  }
  const hex_chr = '0123456789abcdef'.split('');
  function rhex(n) {
    let s = '';
    for (let j = 0; j < 4; j++)
      s += hex_chr[(n >> (j * 8 + 4)) & 0x0F] + hex_chr[(n >> (j * 8)) & 0x0F];
    return s;
  }
  function hex(x) {
    for (let i = 0; i < x.length; i++)
      x[i] = rhex(x[i]);
    return x.join('');
  }

  function getGravatarUrl(email) {
    if (!email || email === '-') return 'https://www.gravatar.com/avatar/?d=mp&s=32';
    const hash = hex(md5(email.toLowerCase().trim()));
    return `https://www.gravatar.com/avatar/${hash}?d=mp&s=32`;
  }

  onMount(async () => {
    applyBootstrapTheme();

    try {
      await initKeycloak();
      authenticated = keycloak.authenticated;
      username = keycloak?.tokenParsed?.preferred_username || '';
      userEmail = keycloak?.tokenParsed?.email || username;
    } catch (err) {
      console.error('Keycloak init error:', err);
    }
  });

  function login() {
    keycloak.login();
  }

  function logout() {
    keycloak.logout({ redirectUri: window.location.origin });
  }

  $: currentPath = $page.url.pathname;
</script>

 <!-- Sidebar -->
  <nav id="sidebar" class="bg-dark {sidebarCollapsed ? 'collapsed' : ''}">
    <div class="p-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        {#if !sidebarCollapsed}
          <h4 class="text-white mb-0"><img src="https://crm.knowprocess.com/images/icon/omny-link-icon.svg" alt="KnowProcess" style="width: 1.75rem; height: 1.75rem; margin: -0.25rem 0.5rem 0 0 ;" /> KnowProcess</h4>
        {/if}
        <button class="btn btn-link text-white p-0" on:click={toggleSidebar} aria-label="Toggle sidebar">
          <i class="bi bi-{sidebarCollapsed ? 'chevron-right' : 'chevron-left'}" style="font-size: 1.25rem;"></i>
        </button>
      </div>

      <ul class="nav flex-column">
        <li class="nav-item">
          <a class="nav-link {currentPath === '/' ? 'active' : ''}" href="/" title="Dashboard">
            <i class="bi bi-speedometer2 nav-icon"></i> {#if !sidebarCollapsed}<span>Dashboard</span>{/if}
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link {currentPath === '/accounts' ? 'active' : ''}" href="/accounts" title="Accounts">
            <i class="bi bi-people nav-icon"></i> {#if !sidebarCollapsed}<span>Accounts</span>{/if}
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="#" title="Settings">
            <i class="bi bi-gear nav-icon"></i> {#if !sidebarCollapsed}<span>Settings</span>{/if}
          </a>
        </li>
        <li class="nav-item">
          {#if authenticated}
            <button class="nav-link" on:click={logout} title="Logout">
              <i class="bi bi-box-arrow-right nav-icon"></i> {#if !sidebarCollapsed}<span>Logout</span>{/if}
            </button>
          {:else}
            <button class="nav-link" on:click={login} title="Login">
              <i class="bi bi-box-arrow-in-right nav-icon"></i> {#if !sidebarCollapsed}<span>Login</span>{/if}
            </button>
          {/if}
        </li>
        <li class="nav-item" style="position: absolute; bottom: 1rem; width: calc(100% - 1rem);">
          <a class="nav-link d-flex align-items-center" href="/profile" title="Profile">
            {#if authenticated}
              <img src={getGravatarUrl(userEmail)} alt={username} class="rounded-circle {sidebarCollapsed ? '' : 'me-2'}" style="width: 32px; height: 32px;" />
              {#if !sidebarCollapsed}<span>{username}</span>{/if}
            {:else}
              <i class="bi bi-person nav-icon"></i>
              {#if !sidebarCollapsed}<span>Profile</span>{/if}
            {/if}
          </a>
        </li>
      </ul>
    </div>
  </nav>

<main id="content" class="{sidebarCollapsed ? 'sidebar-collapsed' : ''} container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>

<style>
    body {
      overflow-x: hidden;
    }

    #sidebar {
      position: fixed;
      top: 0;
      left: 0;
      min-height: 100vh;
      background-color: #343a40;
      transition: all 0.3s ease;
      width: 250px;
    }

    #sidebar.collapsed {
      width: 50px;
    }

    #sidebar.collapsed .p-3 {
      padding: 0.5rem !important;
    }

    #sidebar .nav-link {
      color: #ccc;
      padding-left: 0;
      margin-left: 0;
      white-space: nowrap;
      display: flex;
      align-items: center;
    }

    #sidebar button.nav-link {
      width: 100%;
      border: none;
      background: none;
      text-align: left;
    }

    #sidebar.collapsed .nav-link {
      justify-content: center;
      padding-left: 0;
      padding-right: 0;
    }

    #sidebar.collapsed .nav-item {
      width: 100%;
    }

    #sidebar .nav-link:hover,
    #sidebar .nav-link.active {
      background-color: #495057;
      color: #fff;
    }

    #sidebarCollapse {
      border: none;
    }

    @media (max-width: 768px) {
      #sidebar {
        position: fixed;
        left: -250px;
        width: 250px;
        z-index: 1030;
      }

      #sidebar.collapsed {
        left: -50px;
        width: 50px;
      }

      #sidebar.active {
        left: 0;
      }

      #content {
        margin-left: auto;
        margin-right: auto;
        max-width: 1200px;
      }
    }

    @media (min-width: 769px) {
      #sidebar {
        width: 250px;
      }

      #sidebar.collapsed {
        width: 60px;
      }

      #content {
        margin-left: 295px;
        margin-right: auto;
        padding-left: 2rem;
        padding-right: 2rem;
        transition: margin-left 0.3s ease;
      }

      /* Sidebar reduces by 200px (250px - 50px), so content margin reduces by same amount */
      #content.sidebar-collapsed {
        margin-left: calc(295px - 200px); /* 95px */
      }
    }

    .nav-icon {
      margin-right: 10px;
    }
  </style>