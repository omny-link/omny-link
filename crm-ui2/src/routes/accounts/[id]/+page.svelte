<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { get } from 'svelte/store';
  import { goto } from '$app/navigation';
  import { keycloakStore } from '$lib/keycloak';
  import { colorSchemeStore } from '$lib/colorScheme';
  import { tenantConfigStore } from '$lib/tenantConfig';
  import type { TenantConfig } from '$lib/tenantConfig';
  import { 
    fetchAccount,
    fetchContactsByAccount,
    fetchOrdersByContacts as fetchOrdersByContactsAPI,
    deleteAccount as deleteAccountAPI
  } from '$lib/cust-mgmt';
  import type { Account, Contact, Order, PanelStates, ViewMode } from '$lib/types';
  import CustomFieldsPanel from '$lib/components/CustomFieldsPanel.svelte';
  import NotesPanel from '$lib/components/NotesPanel.svelte';
  import DocumentsPanel from '$lib/components/DocumentsPanel.svelte';
  import ContactsPanel from '$lib/components/ContactsPanel.svelte';
  import OrdersPanel from '$lib/components/OrdersPanel.svelte';
  import ActivitiesPanel from '$lib/components/ActivitiesPanel.svelte';

  let accountId: string;
  let tenant: string = 'acme';
  let selectedAccount: Account | null = null;
  let accountContacts: Contact[] = [];
  let accountOrders: Order[] = [];
  let loading: boolean = false;
  let viewMode: ViewMode = 'view';
  let colorScheme: 'light' | 'dark' = 'dark';
  let tenantConfig: TenantConfig | null = null;

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

  // Subscribe to tenant config changes
  tenantConfigStore.subscribe(config => {
    tenantConfig = config;
  });

  let panelStates: PanelStates = {
    details: true,
    additionalInfo: true,
    customFields: true,
    recordHistory: true,
    contacts: true,
    orders: true,
    activities: true,
    notes: false,
    documents: false
  };

  function togglePanel(panelName: keyof PanelStates): void {
    panelStates[panelName] = !panelStates[panelName];
  }

  function scrollToNotes(): void {
    if (!panelStates.notes) {
      panelStates.notes = true;
    }
    setTimeout(() => {
      const notesPanel = document.getElementById('notes-panel');
      if (notesPanel) {
        notesPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  function toSentenceCase(str: string): string {
    if (!str) return str;
    return str
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (match) => match.toUpperCase())
      .trim();
  }

  function formatDate(dateString: string | undefined): string {
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

  function toDateInputFormat(dateString: string | undefined): string {
    if (!dateString || dateString === '-') return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      // Format as YYYY-MM-DD for date input
      return date.toISOString().split('T')[0];
    } catch (e) {
      return '';
    }
  }

  // Tag management
  let tagInput = '';
  let tags: string[] = [];

  $: if (selectedAccount?.tags) {
    if (typeof selectedAccount.tags === 'string') {
      tags = selectedAccount.tags.split(',').map(t => t.trim()).filter(t => t.length > 0);
    } else if (Array.isArray(selectedAccount.tags)) {
      tags = selectedAccount.tags;
    }
  }

  function addTag(): void {
    if (tagInput.trim() && !tags.includes(tagInput.trim())) {
      tags = [...tags, tagInput.trim()];
      tagInput = '';
      if (selectedAccount) {
        selectedAccount.tags = tags.join(',');
      }
    }
  }

  function removeTag(tagToRemove: string): void {
    tags = tags.filter(tag => tag !== tagToRemove);
    if (selectedAccount) {
      selectedAccount.tags = tags.join(',');
    }
  }

  function handleTagKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      addTag();
    }
  }

  // Compute readable text color (black/white) for a given background color
  function getTextColorForBg(color: string): string {
    if (!color) return '#ffffff';

    function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
      const h = hex.replace('#', '').trim();
      if (h.length === 3) {
        const r = parseInt(h[0] + h[0], 16);
        const g = parseInt(h[1] + h[1], 16);
        const b = parseInt(h[2] + h[2], 16);
        return { r, g, b };
      }
      if (h.length === 6 || h.length === 8) {
        const r = parseInt(h.substring(0, 2), 16);
        const g = parseInt(h.substring(2, 4), 16);
        const b = parseInt(h.substring(4, 6), 16);
        return { r, g, b };
      }
      return null;
    }

    function parseRgb(str: string): { r: number; g: number; b: number } | null {
      const m = str.match(/rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)/i);
      if (!m) return null;
      return { r: parseInt(m[1], 10), g: parseInt(m[2], 10), b: parseInt(m[3], 10) };
    }

    function hslToRgb(h: number, s: number, l: number): { r: number; g: number; b: number } {
      s /= 100; l /= 100;
      const k = (n: number) => (n + h / 30) % 12;
      const a = s * Math.min(l, 1 - l);
      const f = (n: number) => l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
      return { r: Math.round(255 * f(0)), g: Math.round(255 * f(8)), b: Math.round(255 * f(4)) };
    }

    function parseHsl(str: string): { r: number; g: number; b: number } | null {
      const m = str.match(/hsla?\(\s*(\d+)\s*,\s*(\d+)%\s*,\s*(\d+)%/i);
      if (!m) return null;
      return hslToRgb(parseInt(m[1], 10), parseInt(m[2], 10), parseInt(m[3], 10));
    }

    let rgb: { r: number; g: number; b: number } | null = null;
    const c = color.trim();

    if (c.startsWith('#')) {
      rgb = hexToRgb(c);
    } else if (c.toLowerCase().startsWith('rgb')) {
      rgb = parseRgb(c);
    } else if (c.toLowerCase().startsWith('hsl')) {
      rgb = parseHsl(c);
    } else if (typeof window !== 'undefined') {
      try {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.fillStyle = c;
          const canonical = ctx.fillStyle as string;
          if (canonical.startsWith('#')) {
            rgb = hexToRgb(canonical);
          } else if (canonical.toLowerCase().startsWith('rgb')) {
            rgb = parseRgb(canonical);
          }
        }
      } catch (_) {
        // ignore
      }
    }

    if (!rgb) return '#ffffff';

    // Relative luminance
    const sr = rgb.r / 255, sg = rgb.g / 255, sb = rgb.b / 255;
    const rLin = sr <= 0.03928 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
    const gLin = sg <= 0.03928 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
    const bLin = sb <= 0.03928 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);
    const luminance = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;

    return luminance > 0.5 ? '#000000' : '#ffffff';
  }

  function formatTags(tags: string | string[] | undefined): string[] {
    if (!tags || tags === '-') return ['-'];
    const tagArray = typeof tags === 'string' ? tags.split(',').map(t => t.trim()) : tags;
    const lines = [];
    let currentLine = '';
    
    for (const tag of tagArray) {
      if (currentLine.length + tag.length + 2 > 60) {
        if (currentLine) lines.push(currentLine);
        currentLine = tag;
      } else {
        currentLine = currentLine ? `${currentLine}, ${tag}` : tag;
      }
    }
    
    if (currentLine) lines.push(currentLine);
    
    return lines.length > 0 ? lines : ['-'];
  }

  async function fetchFullAccount(id: string): Promise<Account | null> {
    loading = true;
    try {
      return await fetchAccount(tenant, id);
    } catch (error) {
      console.error('Error fetching account:', error);
      return null;
    } finally {
      loading = false;
    }
  }

  async function fetchAccountContacts(id: string): Promise<Contact[]> {
    try {
      return await fetchContactsByAccount(tenant, id);
    } catch (error) {
      console.error('Error fetching contacts:', error);
      return [];
    }
  }

  async function fetchOrdersByContacts(contacts: Contact[]): Promise<Order[]> {
    if (contacts.length === 0) return [];
    
    // Extract contact IDs and join with commas
    const contactIds = contacts
      .map(contact => contact.id || contact.selfRef?.split('/').pop())
      .filter(id => id)
      .join(',');
    
    if (!contactIds) return [];
    
    try {
      return await fetchOrdersByContactsAPI(tenant, contactIds);
    } catch (error) {
      console.error('Error fetching orders:', error);
      return [];
    }
  }

  async function deleteAccount(): Promise<void> {
    if (!selectedAccount?.id) return;
    
    const confirmed = confirm(`Are you sure you want to delete "${selectedAccount.name}"?`);
    if (!confirmed) return;

    try {
      const success = await deleteAccountAPI(tenant, selectedAccount.id);
      if (success) {
        alert('Account deleted successfully');
        goto('/accounts');
      } else {
        alert('Failed to delete account');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      alert('Error deleting account');
    }
  }

  function backToList(): void {
    goto('/accounts');
  }

  function toggleEditMode(): void {
    viewMode = viewMode === 'view' ? 'edit' : 'view';
  }

  onMount(() => {
    let initialized = false;
    const unsubscribe = keycloakStore.subscribe(async (state) => {
      if (!state.authenticated) return;
      const userTenant = state.userInfo?.tenant;
      // Wait for actual tenant from fetchUserAccount, don't default to 'acme'
      if (initialized || !userTenant || userTenant === 'acme') return;
      initialized = true;
      tenant = userTenant;

      // Get account ID from URL
      accountId = get(page).params.id;
      // Honor edit mode from query string
      try {
        const modeParam = get(page).url.searchParams.get('mode');
        if (modeParam === 'edit') {
          viewMode = 'edit';
        }
      } catch (_) { /* ignore */ }

      // Load account data and contacts
      const [account, contacts] = await Promise.all([
        fetchFullAccount(accountId),
        fetchAccountContacts(accountId)
      ]);

      if (account) {
        selectedAccount = account;
        accountContacts = contacts;
        accountOrders = await fetchOrdersByContacts(contacts);
      } else {
        alert('Account not found');
        goto('/accounts');
      }
    });
    return () => unsubscribe();
  });
</script>

{#if loading}
  <div class="alert alert-info">Loading account...</div>
{:else if selectedAccount}
<!-- Detail View -->
<div class="mb-3 d-flex justify-content-between align-items-center">
  <h1>
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" on:click={backToList}>
      <i class="bi bi-arrow-left"></i> Back to List
    </button>
    <span class="ms-3 h1">{selectedAccount?.name || 'Account Details'}</span>
  </h1>
  <div class="d-flex gap-2 align-items-center">
    <!-- Edit Mode Toggle Switch -->
    <div class="form-check form-switch">
      <input 
        class="form-check-input" 
        type="checkbox" 
        role="switch" 
        id="editModeSwitch"
        checked={viewMode === 'edit'}
        on:change={toggleEditMode}
        style="cursor: pointer;"
      />
      <label class="form-check-label" for="editModeSwitch" style="cursor: pointer;">
        {viewMode === 'edit' ? 'Edit' : 'View'}
      </label>
    </div>
    
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" on:click={deleteAccount} title="Delete account" aria-label="Delete account">
      <i class="bi bi-trash"></i>
    </button>
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" on:click={scrollToNotes} title="Jump to notes" aria-label="Jump to notes">
      <i class="bi bi-journal-text"></i>
    </button>
    <div class="dropdown">
      <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'} dropdown-toggle" type="button" id="customActionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="bi bi-three-dots-vertical"></i> Actions
      </button>
      <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark" aria-labelledby="customActionsDropdown">
        <li><button class="dropdown-item" type="button" on:click={() => alert('Clone feature coming soon')}>Clone Account</button></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Export feature coming soon')}>Export to PDF</button></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Merge feature coming soon')}>Merge with Another</button></li>
        <li><hr class="dropdown-divider"></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Archive feature coming soon')}>Archive Account</button></li>
      </ul>
    </div>
  </div>
</div>

<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'}">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('details')}>
    {#if panelStates.details}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">{selectedAccount?.name || 'Account Details'}</h5>
  </div>
  {#if panelStates.details}
  <div class="card-body">
    <div class="row">
      <!-- Column 1 -->
      <div class="col-md-6">
        <!-- ID -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">ID</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.id || ''} readonly disabled />
          </div>
        </div>

        <!-- Parent Org -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Parent Org</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.parentOrg || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Company Number -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Company Number</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.companyNumber || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Owner -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Owner</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.owner || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Existing Customer -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Existing Customer?</label>
          <div class="col-sm-8">
            <div class="form-check mt-2">
              <input 
                class="form-check-input" 
                type="checkbox" 
                id="existingCustomer"
                checked={selectedAccount?.existingCustomer === true || selectedAccount?.existingCustomer === 'true'}
                disabled={viewMode === 'view'}
              />
              <label class="form-check-label" for="existingCustomer">
                Yes
              </label>
            </div>
          </div>
        </div>

        <!-- Status -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Status</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.stage || selectedAccount?.accountType || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Type -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Type</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.type || selectedAccount?.businessType || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Tags -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Tags</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <!-- Edit mode: show tags with remove buttons and input -->
              <div class="tags-container mb-2">
                {#each tags as tag}
                  <span class="badge me-1 mb-1" style="background-color: {tag}; color: {getTextColorForBg(tag)}">
                    {tag}
                    <button 
                      type="button" 
                      class="btn-close btn-close-white ms-1" 
                      aria-label="Remove tag"
                      style="font-size: 0.6rem; vertical-align: middle;"
                      on:click={() => removeTag(tag)}
                    ></button>
                  </span>
                {/each}
              </div>
              <div class="input-group">
                <input 
                  type="text" 
                  class="form-control" 
                  placeholder="Add tag..."
                  bind:value={tagInput}
                  on:keydown={handleTagKeydown}
                />
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={addTag}
                >
                  <i class="bi bi-plus"></i>
                </button>
              </div>
            {:else}
              <!-- View mode: show tags as badges only -->
              <div class="tags-container">
                {#if tags.length > 0}
                  {#each tags as tag}
                    <span class="badge me-1 mb-1" style="background-color: {tag}; color: {getTextColorForBg(tag)}">{tag}</span>
                  {/each}
                {:else}
                  <span class="text-muted">-</span>
                {/if}
              </div>
            {/if}
          </div>
        </div>

        <!-- No of Employees -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">No of Employees</label>
          <div class="col-sm-8">
            <input type="number" class="form-control" value={selectedAccount?.noOfEmployees || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>
      </div>

      <!-- Column 2 -->
      <div class="col-md-6">
        <!-- Business Website -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Website</label>
          <div class="col-sm-8">
            <div class="input-group">
              <input 
                type="url" 
                class="form-control" 
                bind:value={selectedAccount.businessWebsite}
                readonly={viewMode === 'view'} 
                disabled={viewMode === 'view'} 
              />
              {#if selectedAccount?.businessWebsite}
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(selectedAccount?.businessWebsite, '_blank')}
                  title="Open website in new tab"
                  aria-label="Open website in new tab"
                >
                  <i class="bi bi-box-arrow-up-right"></i>
                </button>
              {/if}
            </div>
          </div>
        </div>

        <!-- Email -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Email</label>
          <div class="col-sm-8">
            <input type="email" class="form-control" value={selectedAccount?.email || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Email Confirmed -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Email Confirmed?</label>
          <div class="col-sm-8">
            <div class="form-check mt-2">
              <input 
                class="form-check-input" 
                type="checkbox" 
                id="emailConfirmed"
                checked={selectedAccount?.emailConfirmed === true || selectedAccount?.emailConfirmed === 'true'}
                disabled={viewMode === 'view'}
              />
              <label class="form-check-label" for="emailConfirmed">
                Yes
              </label>
            </div>
          </div>
        </div>

        <!-- Opt-in -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Opt-in?</label>
          <div class="col-sm-8">
            <div class="form-check mt-2">
              <input 
                class="form-check-input" 
                type="checkbox" 
                id="emailOptIn"
                checked={selectedAccount?.emailOptIn === true || selectedAccount?.emailOptIn === 'true' || (selectedAccount?.doNotCall !== true && selectedAccount?.doNotCall !== 'true')}
                disabled={viewMode === 'view'}
              />
              <label class="form-check-label" for="emailOptIn">
                Yes
              </label>
            </div>
          </div>
        </div>

        <!-- Phone -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Phone</label>
          <div class="col-sm-8">
            <input type="tel" class="form-control" value={selectedAccount?.phone || selectedAccount?.phoneNumber || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Address Line 1 -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Address Line 1</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.address1 || selectedAccount?.addressLine1 || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Address Line 2 -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Address Line 2</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.address2 || selectedAccount?.addressLine2 || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Address Town -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Address Town</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.town || selectedAccount?.addressTown || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Address County/City -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Address County / City</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.countyOrCity || selectedAccount?.addressCounty || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'} />
          </div>
        </div>

        <!-- Postcode -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Postcode</label>
          <div class="col-sm-8">
            <div class="input-group">
              <input 
                type="text" 
                class="form-control" 
                bind:value={selectedAccount.postCode}
                readonly={viewMode === 'view'} 
                disabled={viewMode === 'view'} 
              />
              {#if selectedAccount?.postCode || selectedAccount?.zipCode}
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(selectedAccount?.postCode || selectedAccount?.zipCode || '')}`, '_blank')}
                  title="Search postcode on Google Maps"
                  aria-label="Search postcode on Google Maps"
                >
                  <i class="bi bi-geo-alt"></i>
                </button>
              {/if}
            </div>
          </div>
        </div>

        <!-- Twitter -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Twitter</label>
          <div class="col-sm-8">
            <div class="input-group">
              <input 
                type="url" 
                class="form-control" 
                bind:value={selectedAccount.twitter}
                readonly={viewMode === 'view'} 
                disabled={viewMode === 'view'} 
              />
              {#if selectedAccount?.twitter}
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(selectedAccount?.twitter, '_blank')}
                  title="Open Twitter profile in new tab"
                  aria-label="Open Twitter profile in new tab"
                >
                  <i class="bi bi-box-arrow-up-right"></i>
                </button>
              {/if}
            </div>
          </div>
        </div>

        <!-- LinkedIn -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">LinkedIn</label>
          <div class="col-sm-8">
            <div class="input-group">
              <input 
                type="url" 
                class="form-control" 
                bind:value={selectedAccount.linkedIn}
                readonly={viewMode === 'view'} 
                disabled={viewMode === 'view'} 
              />
              {#if selectedAccount?.linkedIn}
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(selectedAccount?.linkedIn, '_blank')}
                  title="Open LinkedIn profile in new tab"
                  aria-label="Open LinkedIn profile in new tab"
                >
                  <i class="bi bi-box-arrow-up-right"></i>
                </button>
              {/if}
            </div>
          </div>
        </div>

        <!-- Facebook -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Facebook</label>
          <div class="col-sm-8">
            <div class="input-group">
              <input 
                type="url" 
                class="form-control" 
                bind:value={selectedAccount.facebook}
                readonly={viewMode === 'view'} 
                disabled={viewMode === 'view'} 
              />
              {#if selectedAccount?.facebook}
                <button 
                  class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(selectedAccount?.facebook, '_blank')}
                  title="Open Facebook profile in new tab"
                  aria-label="Open Facebook profile in new tab"
                >
                  <i class="bi bi-box-arrow-up-right"></i>
                </button>
              {/if}
            </div>
          </div>
        </div>
      </div>
    </div>

    {#if viewMode === 'edit'}
      <div class="mt-3">
        <button class="btn btn-primary me-2">Save Changes</button>
        <button class="btn btn-secondary" on:click={toggleEditMode}>Cancel</button>
      </div>
    {/if}
  </div>
  {/if}
</div>

<!-- Additional Info Panel -->
<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('additionalInfo')}>
    {#if panelStates.additionalInfo}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Additional Information</h5>
  </div>
  {#if panelStates.additionalInfo}
  <div class="card-body">
    <textarea class="form-control" rows="6" value={selectedAccount?.additionalInformation || ''} readonly={viewMode === 'view'} disabled={viewMode === 'view'}></textarea>
  </div>
  {/if}
</div>

<CustomFieldsPanel 
  customFields={selectedAccount?.customFields}
  accountFields={tenantConfig?.accountFields}
  {viewMode}
  isOpen={panelStates.customFields}
  onToggle={() => togglePanel('customFields')}
  onChange={(key, value) => {
    if (selectedAccount) {
      selectedAccount.customFields = { ...selectedAccount.customFields, [key]: value };
    }
  }}
/>

<!-- Record History Panel -->
<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('recordHistory')}>
    {#if panelStates.recordHistory}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Record History</h5>
  </div>
  {#if panelStates.recordHistory}
  <div class="card-body">
    <div class="row">
      <div class="col-md-6">
        <!-- First Contact -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">First Contact</label>
          <div class="col-sm-8">
            <input type="date" class="form-control" value={toDateInputFormat(selectedAccount?.firstContact)} readonly disabled />
          </div>
        </div>

        <!-- Created By -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Created By</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.createdBy || ''} readonly disabled />
          </div>
        </div>

      </div>

      <div class="col-md-6">
        <!-- Last Updated -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Last Updated</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={formatDate(selectedAccount?.lastUpdated)} readonly disabled />
          </div>
        </div>

        <!-- Updated By -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label form-label text-end">Updated By</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" value={selectedAccount?.lastUpdatedBy || ''} readonly disabled />
          </div>
        </div>

      </div>
    </div>
  </div>
  {/if}
</div>

<ContactsPanel
  contacts={accountContacts}
  {viewMode}
  isOpen={panelStates.contacts}
  onToggle={() => togglePanel('contacts')}
  onAdd={() => alert('Add contact feature coming soon')}
  onView={(contact) => alert('View contact feature coming soon')}
  onSetMain={(contact) => alert('Set main contact feature coming soon')}
/>

<OrdersPanel
  orders={accountOrders}
  {viewMode}
  isOpen={panelStates.orders}
  onToggle={() => togglePanel('orders')}
  onView={(order) => alert('View order feature coming soon')}
  onEdit={(order) => alert('Edit order feature coming soon')}
/>

<ActivitiesPanel
  activities={selectedAccount?.activities}
  {viewMode}
  isOpen={panelStates.activities}
  onToggle={() => togglePanel('activities')}
  onView={(activity) => alert('View activity feature coming soon')}
/>

<NotesPanel
  id="notes-panel"
  notes={selectedAccount?.notes}
  {viewMode}
  isOpen={panelStates.notes}
  onToggle={() => togglePanel('notes')}
  onAdd={() => alert('Add note feature coming soon')}
/>

<DocumentsPanel
  documents={selectedAccount?.documents}
  {viewMode}
  isOpen={panelStates.documents}
  onToggle={() => togglePanel('documents')}
  onUpload={() => alert('Upload document feature coming soon')}
  onDelete={(doc) => alert('Delete document feature coming soon')}
/>
{/if}


