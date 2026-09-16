import { signal } from '@vaadin/hilla-react-signals';
import { AuthService } from 'Frontend/generated/endpoints.js';
import type CurrentUser from 'Frontend/generated/com/example/auth/CurrentUser.js';

/**
 * Client-side view of the server session, shared by the shell and the login
 * page. Those two live in different branches of the route tree — the login page
 * deliberately renders outside the layout — so a React context above both would
 * have nowhere to mount. A module-level signal is the simpler answer: one value,
 * imported wherever it is needed.
 *
 * The server remains the authority. Nothing here grants access on its own; it
 * caches what `AuthService` last reported so the shell can render a username and
 * decide whether to redirect.
 */

/** `undefined` while the first check is in flight, `null` when signed out. */
export const currentUser = signal<CurrentUser | null | undefined>(undefined);

/** Re-reads the session from the server. */
export async function refreshUser(): Promise<CurrentUser | null> {
  const user = (await AuthService.currentUser()) ?? null;
  currentUser.value = user;
  return user;
}

/**
 * @returns the signed-in user, or `null` when the credentials were rejected.
 */
export async function login(username: string, password: string): Promise<CurrentUser | null> {
  const user = (await AuthService.login(username, password)) ?? null;
  currentUser.value = user;
  return user;
}

export async function logout(): Promise<void> {
  await AuthService.logout();
  currentUser.value = null;
}
