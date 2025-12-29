<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { Activity } from '$lib/types';
  import type { ViewMode } from '$lib/types';

  export let activities: Activity[] | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = true;
  export let onToggle: (() => void) | undefined = undefined;
  export let onView: ((activity: Activity) => void) | undefined = undefined;

  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

  function formatDate(dateString: string | undefined): string {
    if (!dateString || dateString === '-') return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return date.toLocaleDateString(navigator.language, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  }

  function getSortedActivities(activities: Activity[]): Activity[] {
    return [...activities].sort((a, b) => {
      const dateA = new Date(a.occurred || (a as any).created || (a as any).date || 0);
      const dateB = new Date(b.occurred || (b as any).created || (b as any).date || 0);
      return dateB.getTime() - dateA.getTime();
    });
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
    <h5 class="mb-0">Activities {#if activities}({activities.length}){/if}</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if activities && activities.length > 0}
      <table class="table {colorScheme === 'dark' ? 'table-dark' : ''} table-striped">
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Description</th>
            <th>User</th>
          </tr>
        </thead>
        <tbody>
          {#each getSortedActivities(activities) as activity}
            <tr>
              <td>{formatDate(activity.occurred || (activity as any).created || (activity as any).date)}</td>
              <td>{activity.type || (activity as any).activityType || '-'}</td>
              <td style="max-width: 40rem;">{(activity as any).description || activity.content || '-'}</td>
              <td>{(activity as any).user || (activity as any).createdBy || '-'}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No activities found</div>
    {/if}
  </div>
  {/if}
</div>
