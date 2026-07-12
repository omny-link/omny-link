/**
 * API Client Usage Examples
 * 
 * This file demonstrates how to use the centralized API client
 * with automatic JWT authentication, token refresh, and error handling.
 */

import * as api from '$lib/apiClient';
import type { Account, Contact, Order } from '$lib/types';

/**
 * Example 1: GET request - Fetch all accounts
 */
export async function exampleGetAccounts(tenant: string) {
  try {
    const accounts = await api.get<Account[]>(`/${tenant}/accounts/`);
    console.log('Fetched accounts:', accounts);
    return accounts;
  } catch (error) {
    console.error('Failed to fetch accounts:', error);
    if (error instanceof api.ApiError) {
      console.error('Status:', error.status);
      console.error('Response:', error.response);
    }
    return [];
  }
}

/**
 * Example 2: GET request with query parameters - Fetch paginated accounts
 */
export async function exampleGetAccountsPaginated(tenant: string, page: number = 0) {
  try {
    // Query params can be added to the path
    const accounts = await api.get<Account[]>(`/${tenant}/accounts/?page=${page}&size=20`);
    return accounts;
  } catch (error) {
    console.error('Failed to fetch paginated accounts:', error);
    return [];
  }
}

/**
 * Example 3: GET single resource - Fetch account by ID
 */
export async function exampleGetAccount(tenant: string, accountId: string) {
  try {
    const account = await api.get<Account>(`/${tenant}/accounts/${accountId}`);
    console.log('Fetched account:', account);
    return account;
  } catch (error) {
    if (error instanceof api.ApiError && error.status === 404) {
      console.warn('Account not found');
    } else {
      console.error('Failed to fetch account:', error);
    }
    return null;
  }
}

/**
 * Example 4: POST request - Create new account
 */
export async function exampleCreateAccount(tenant: string, accountData: Partial<Account>) {
  try {
    const newAccount = await api.post<Account>(`/${tenant}/accounts/`, accountData);
    console.log('Created account:', newAccount);
    return newAccount;
  } catch (error) {
    if (error instanceof api.ApiError && error.status === 400) {
      console.error('Validation error:', error.response);
    }
    console.error('Failed to create account:', error);
    return null;
  }
}

/**
 * Example 5: PUT request - Update entire account
 */
export async function exampleUpdateAccount(
  tenant: string,
  accountId: string,
  accountData: Account
) {
  try {
    const updated = await api.put<Account>(`/${tenant}/accounts/${accountId}`, accountData);
    console.log('Updated account:', updated);
    return updated;
  } catch (error) {
    console.error('Failed to update account:', error);
    return null;
  }
}

/**
 * Example 6: PATCH request - Partial update
 */
export async function examplePatchAccount(
  tenant: string,
  accountId: string,
  changes: Partial<Account>
) {
  try {
    const updated = await api.patch<Account>(`/${tenant}/accounts/${accountId}`, changes);
    console.log('Patched account:', updated);
    return updated;
  } catch (error) {
    console.error('Failed to patch account:', error);
    return null;
  }
}

/**
 * Example 7: DELETE request - Delete account
 */
export async function exampleDeleteAccount(tenant: string, accountId: string) {
  try {
    await api.del(`/${tenant}/accounts/${accountId}`);
    console.log('Deleted account:', accountId);
    return true;
  } catch (error) {
    if (error instanceof api.ApiError && error.status === 404) {
      console.warn('Account not found, may already be deleted');
      return true;
    }
    console.error('Failed to delete account:', error);
    return false;
  }
}

/**
 * Example 8: Complex workflow - Fetch account with related data
 */
export async function exampleGetAccountWithDetails(tenant: string, accountId: string) {
  try {
    // All these requests will automatically include JWT token
    // and handle token refresh if needed
    const [account, contacts] = await Promise.all([
      api.get<Account>(`/${tenant}/accounts/${accountId}`),
      api.get<Contact[]>(`/${tenant}/contacts/findByAccountId?accountId=${accountId}`)
    ]);

    // Get orders for all contacts
    const contactIds = contacts.map(c => c.id).join(',');
    const orders = contactIds
      ? await api.get<Order[]>(`/${tenant}/orders/findByContacts/${contactIds}`)
      : [];

    return {
      account,
      contacts,
      orders
    };
  } catch (error) {
    console.error('Failed to fetch account details:', error);
    return null;
  }
}

/**
 * Example 9: Error handling patterns
 */
export async function exampleErrorHandling(tenant: string) {
  try {
    const accounts = await api.get<Account[]>(`/${tenant}/accounts/`);
    return { success: true, data: accounts };
  } catch (error) {
    if (error instanceof api.ApiError) {
      // HTTP error with status code
      switch (error.status) {
        case 401:
          // Unauthorized - user will be redirected to login automatically
          return { success: false, error: 'Authentication required' };
        case 403:
          // Forbidden - user doesn't have permission
          return { success: false, error: 'Access denied' };
        case 404:
          // Not found
          return { success: false, error: 'Resource not found' };
        case 500:
          // Server error
          return { success: false, error: 'Server error, please try again' };
        default:
          return { success: false, error: error.message };
      }
    } else {
      // Network or other error
      return { success: false, error: 'Network error, please check your connection' };
    }
  }
}

/**
 * Example 10: Check authentication status
 */
export function exampleCheckAuth() {
  const isAuth = api.isAuthenticated();
  const tenant = api.getCurrentTenant();
  
  console.log('Authenticated:', isAuth);
  console.log('Current tenant:', tenant);
  
  return { isAuth, tenant };
}

/**
 * Example 11: Custom headers (if needed)
 */
export async function exampleCustomHeaders(tenant: string) {
  try {
    const accounts = await api.get<Account[]>(`/${tenant}/accounts/`, {
      headers: {
        'X-Custom-Header': 'custom-value',
        'Accept-Language': 'en-US'
      }
    });
    return accounts;
  } catch (error) {
    console.error('Request failed:', error);
    return [];
  }
}

/**
 * Example 12: Form data POST
 */
export async function exampleFormDataPost(tenant: string, noteId: string, favorite: boolean) {
  try {
    // For endpoints that expect form data instead of JSON
    const formData = new URLSearchParams();
    formData.append('favorite', favorite.toString());

    await api.post(`/${tenant}/notes/${noteId}/favorite`, null, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: formData.toString()
    });
    
    return true;
  } catch (error) {
    console.error('Failed to update note:', error);
    return false;
  }
}
