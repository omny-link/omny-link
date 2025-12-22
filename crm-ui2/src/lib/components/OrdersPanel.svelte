<script lang="ts">
  import type { Order } from '$lib/types';
  import type { ViewMode } from '$lib/types';

  export let orders: Order[] | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = true;
  export let onToggle: (() => void) | undefined = undefined;
  export let onView: ((order: Order) => void) | undefined = undefined;
  export let onEdit: ((order: Order) => void) | undefined = undefined;

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
</script>

<div class="card bg-dark text-light mt-3">
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
    <h5 class="mb-0">Orders ({orders?.length || 0})</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if orders && orders.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Contact Name</th>
            <th>Order Date</th>
            <th>Delivery Date</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each orders as order}
            <tr>
              <td>{order.id || order.name || '-'}</td>
              <td>{(order as any).contactName || (order as any).contact || '-'}</td>
              <td>{formatDate((order as any).orderDate || order.date || (order as any).created)}</td>
              <td>{formatDate((order as any).deliveryDate || (order as any).dueDate)}</td>
              <td>{(order as any).status || order.stage || '-'}</td>
              <td>
                <button 
                  class="btn btn-sm btn-dark" 
                  aria-label="View order"
                  on:click={() => onView?.(order)}
                >
                  <i class="bi bi-eye"></i>
                </button>
                <button 
                  class="btn btn-sm btn-dark" 
                  aria-label="Edit order"
                  on:click={() => onEdit?.(order)}
                >
                  <i class="bi bi-pencil"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No orders found</div>
    {/if}
  </div>
  {/if}
</div>
