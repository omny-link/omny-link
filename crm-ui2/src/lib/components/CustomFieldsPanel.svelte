<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { ViewMode } from '$lib/types';

  export let customFields: Record<string, any> | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = true;
  export let onToggle: (() => void) | undefined = undefined;
  export let onChange: ((key: string, value: any) => void) | undefined = undefined;

  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

  function toSentenceCase(str: string): string {
    if (!str) return str;
    // Convert camelCase to Sentence case
    // e.g., "firstName" -> "First name", "emailAddress" -> "Email address"
    return str
      .replace(/([A-Z])/g, ' $1') // Add space before capital letters
      .replace(/^./, (match) => match.toUpperCase()) // Capitalize first letter
      .trim();
  }

  function handleChange(key: string, event: Event): void {
    if (onChange) {
      const target = event.target as HTMLInputElement;
      onChange(key, target.value);
    }
  }
</script>

<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
  <div 
    class="card-header d-flex align-items-center" 
    style="cursor: pointer;" 
    on:click={onToggle}
    on:keydown={(e) => e.key === 'Enter' && onToggle?.()}
    role="button"
    tabindex="0"
    aria-expanded={isOpen}
  >
    {#if isOpen}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Custom Fields</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if customFields && Object.keys(customFields).length > 0}
      {#each Object.entries(customFields) as [key, value]}
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end" for="custom-field-{key}">
            {toSentenceCase(key)}
          </label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input 
                type="text" 
                id="custom-field-{key}"
                class="form-control {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} border-secondary" 
                value={value || ''} 
                on:input={(e) => handleChange(key, e)}
              />
            {:else}
              <div class="form-control-plaintext {colorScheme === 'dark' ? 'text-light' : 'text-dark'}">{value || '-'}</div>
            {/if}
          </div>
        </div>
      {/each}
    {:else}
      <div class="text-muted">No custom fields defined</div>
    {/if}
  </div>
  {/if}
</div>

<style>
  .field-label {
    font-size: 0.875rem;
    color: #adb5bd;
  }
</style>
