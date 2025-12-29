<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { ViewMode } from '$lib/types';

  export let customFields: Record<string, any> | undefined = undefined;
  export let accountFields: any[] | undefined = undefined;
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

  function getFieldValue(fieldName: string): string {
    return customFields?.[fieldName] || '';
  }

  function getFieldLabel(field: any): string {
    return field.label || toSentenceCase(field.name);
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
    {#if accountFields && accountFields.length > 0}
      <div class="row">
        {#each accountFields as field, index}
          <div class="col-md-6">
            <div class="mb-3 row">
              <label class="col-sm-4 col-form-label form-label text-end" for="custom-field-{field.name}">
                {getFieldLabel(field)}
              </label>
              <div class="col-sm-8">
                <input 
                  type="text" 
                  id="custom-field-{field.name}"
                  class="form-control" 
                  value={getFieldValue(field.name)} 
                  readonly={viewMode === 'view'}
                  disabled={viewMode === 'view'}
                  on:input={(e) => handleChange(field.name, e)}
                />
                {#if field.hint}
                  <small class="form-text text-muted d-block mt-1">{field.hint}</small>
                {/if}
              </div>
            </div>
          </div>
        {/each}
      </div>
    {:else}
      <div class="text-muted">No custom fields defined</div>
    {/if}
  </div>
  {/if}
</div>


