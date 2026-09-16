import { RouterConfigurationBuilder } from '@vaadin/hilla-file-router/runtime.js';
import Flow from 'Frontend/generated/flow/Flow';
import fileRoutes from 'Frontend/generated/file-routes.js';
import LoginPage from 'Frontend/auth/LoginPage.js';

/**
 * Custom route configuration, replacing the generated default.
 *
 * <p>It exists for `/login`, which must sit *outside* `views/@layout.tsx` —
 * that layout wraps everything under `views/`:
 *
 * <ul>
 *   <li>`/login` — a login page inside the application shell would be absurd.
 * </ul>
 *
 * <p>`/` also has to escape the shell — it is stage one of the demo, the
 * *entire* Swing application including its own Swing login screen — but it
 * cannot be declared here. The file router owns path `""`, and `withFallback`
 * adds an index route inside that branch, so a sibling `/` route is always
 * shadowed by the layout. `@layout.tsx` steps aside for it instead.</li>
 *
 * <p>The two other calls matter as much:
 *
 * <ul>
 *   <li>`withFileRoutes` keeps file-based routing working. A `routes.tsx` that
 *   omits it doesn't disable the file router, it just ignores its output, and
 *   the React dashboard would quietly stop resolving.</li>
 *   <li>`withFallback(Flow)` adds the Flow component to every branch, which is
 *   what lets the Java views — `/dashboard`, `/products`,
 *   `/orders`, `/stock`, each embedding a Swing bridge — render inside the React
 *   layout above.</li>
 * </ul>
 *
 * <p>There is deliberately no `.protect()`. That hooks Hilla's own authorization,
 * which reads Spring Security, and this application authenticates against the
 * Swing `User` entity instead. The session check lives in `@layout.tsx`, and the
 * server re-checks on every endpoint call regardless.
 */
export const { router, routes } = new RouterConfigurationBuilder()
  .withFileRoutes(fileRoutes)
  .withReactRoutes([{ path: '/login', element: <LoginPage />, handle: { title: 'Sign In' } }])
  .withFallback(Flow)
  .build();
