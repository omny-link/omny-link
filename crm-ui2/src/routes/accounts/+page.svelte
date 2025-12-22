<script>
  import { onMount } from 'svelte';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';

  let userInfo = null;
  let tenant = 'acme';
  let accounts = [];
  let filteredAccounts = [];
  let searchQuery = '';
  let loading = false;
  let page = 1;
  let allLoaded = false;

  // Stale-while-refresh: show what we have, keep loading in background
  async function fetchAccounts(nextPage) {
    if (loading || allLoaded) return;
    loading = true;
    // const url = `http://localhost:8080/${tenant}/accounts/?page=${nextPage}`;
    const url = `https://crm.knowprocess.com/${tenant}/accounts/?page=${nextPage}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
          accounts = [...accounts, ...data];
          filteredAccounts = accounts;
          page = nextPage;
          // Load next page in background
          fetchAccounts(nextPage + 1);
        } else {
          allLoaded = true;
        }
      } else {
        allLoaded = true;
      }
    } finally {
      loading = false;
    }
  }

  function filterAccounts() {
    if (!searchQuery.trim()) {
      filteredAccounts = accounts;
      return;
    }
    const query = searchQuery.toLowerCase();
    filteredAccounts = accounts.filter(account => 
      (account.name && account.name.toLowerCase().includes(query)) ||
      (account.email && account.email.toLowerCase().includes(query)) ||
      (account.created && account.created.toLowerCase().includes(query))
    );
  }

  function formatDate(dateString) {
    if (!dateString || dateString === '-') return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return date.toLocaleDateString(navigator.language, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  }

  function formatTags(tags) {
    if (!tags || tags === '-') return ['-'];
    const tagArray = tags.split(',').map(t => t.trim());
    const lines = [];
    let currentLine = '';
    
    tagArray.forEach((tag, index) => {
      const separator = index > 0 ? ', ' : '';
      const testLine = currentLine + separator + tag;
      
      if (currentLine && testLine.length > 20) {
        lines.push(currentLine);
        currentLine = tag;
      } else {
        currentLine = testLine;
      }
    });
    
    if (currentLine) {
      lines.push(currentLine);
    }
    
    return lines.length > 0 ? lines : ['-'];
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
    // Wait for keycloak to initialize first
    await initKeycloak();
    
    if (!keycloak.authenticated) {
      await keycloak.login({ redirectUri: window.location.href });
    }
    
    // Fetch user account info to get tenant
    if (keycloak.authenticated) {
      userInfo = keycloak.tokenParsed;
      const { tenant: userTenant } = await fetchUserAccount();
      tenant = userTenant;
      fetchAccounts(1);
    }
  });
</script>

<div class="d-flex align-items-center mb-3">
  <h2 class="display-5 mb-0 me-3">
    Accounts
    {#if !loading}
      ({filteredAccounts.length})
    {/if}
  </h2>
  <input 
    type="text" 
    class="form-control" 
    style="width: 300px;" 
    placeholder="Search accounts..." 
    bind:value={searchQuery}
    on:blur={filterAccounts}
    aria-label="Search accounts"
  />
  {#if !loading}
    <button class="btn btn-dark ms-auto" aria-label="Refresh accounts" on:click={() => { accounts = []; filteredAccounts = []; searchQuery = ''; page = 1; allLoaded = false; fetchAccounts(1); }}>
      <i class="bi bi-arrow-clockwise"></i>
    </button>
  {/if}
</div>

{#if loading}
  <div class="alert alert-info">Loading accounts...</div>
{:else}
  <table class="table table-striped mt-4">
    <thead>
      <tr>
        <th>Name</th>
        <th>Status</th>
        <th>Type</th>
        <th>Owner</th>
        <th>Created</th>
        <th>Last Updated</th>
        <th>Tags</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each filteredAccounts as account}
        <tr>
          <td>{account.name}</td>
          <td>{account.stage || account.accountType || '-'}</td>
          <td>{account.type || account.businessType || '-'}</td>
          <td>
            <img 
              src={getGravatarUrl(account.email || account.ownerEmail)} 
              alt={account.owner || 'Owner'} 
              title={account.owner || ''}
              class="rounded-circle"
              style="width: 32px; height: 32px;"
            />
          </td>
          <td>{formatDate(account.created)}</td>
          <td>{formatDate(account.lastUpdated || account.updated)}</td>
          <td style="max-width: 20rem;">
            {#each formatTags(account.tags) as line, i}
              {line}{#if i < formatTags(account.tags).length - 1}<br />{/if}
            {/each}
          </td>
          <td>
            <button class="btn btn-sm btn-outline-primary me-1" aria-label="View account">
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-sm btn-outline-secondary" aria-label="Edit account">
              <i class="bi bi-pencil"></i>
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

