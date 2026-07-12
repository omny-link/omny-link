/**
 * Customer Management API Service
 * Centralized API calls to CRM backend with JWT authentication
 */

import type { Account, Contact, Order, Note } from '$lib/types';
import * as api from '$lib/apiClient';

/**
 * Get tenant from environment or default
 */
export function getTenant(): string {
  return api.getCurrentTenant();
}

/**
 * Fetch paginated accounts
 */
export async function fetchAccounts(tenant: string, page: number = 0): Promise<Account[]> {
  try {
    const accounts = await api.get<Account[]>(`/${tenant}/accounts/?page=${page}`);
    return accounts || [];
  } catch (error) {
    console.error('Error fetching accounts:', error);
    return [];
  }
}

/**
 * Fetch full account details by ID
 */
export async function fetchAccount(tenant: string, id: string): Promise<Account | null> {
  try {
    return await api.get<Account>(`/${tenant}/accounts/${id}`);
  } catch (error) {
    console.error('Error fetching account:', error);
    return null;
  }
}

/**
 * Delete an account
 */
export async function deleteAccount(tenant: string, accountId: string): Promise<boolean> {
  try {
    await api.del(`/${tenant}/accounts/${accountId}`);
    return true;
  } catch (error) {
    console.error('Error deleting account:', error);
    return false;
  }
}

/**
 * Fetch contacts by account ID
 */
export async function fetchContactsByAccount(tenant: string, accountId: string): Promise<Contact[]> {
  try {
    const contacts = await api.get<Contact[]>(
      `/${tenant}/contacts/findByAccountId?accountId=${accountId}`
    );
    return contacts || [];
  } catch (error) {
    console.error('Error fetching contacts:', error);
    return [];
  }
}

/**
 * Fetch orders by contact IDs (comma-separated)
 */
export async function fetchOrdersByContacts(tenant: string, contactIds: string): Promise<Order[]> {
  if (!contactIds) return [];
  
  try {
    const orders = await api.get<Order[]>(`/${tenant}/orders/findByContacts/${contactIds}`);
    return orders || [];
  } catch (error) {
    console.error('Error fetching orders:', error);
    return [];
  }
}

/**
 * Toggle note favorite status
 */
export async function toggleNoteFavorite(
  tenant: string,
  noteId: string,
  favorite: boolean
): Promise<boolean> {
  try {
    // This endpoint uses form data instead of JSON
    const formData = new URLSearchParams();
    formData.append('favorite', favorite.toString());

    await api.post(`/${tenant}/notes/${noteId}/favorite`, null, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: formData.toString()
    });
    return true;
  } catch (error) {
    console.error('Error toggling note favorite:', error);
    return false;
  }
}
