import { useEffect } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router';
import { AppLayout } from '@vaadin/react-components/AppLayout.js';
import { Avatar } from '@vaadin/react-components/Avatar.js';
import { ContextMenu } from '@vaadin/react-components/ContextMenu.js';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { SideNav } from '@vaadin/react-components/SideNav.js';
import { SideNavItem } from '@vaadin/react-components/SideNavItem.js';
import { currentUser, logout, refreshUser } from 'Frontend/auth/session.js';
import { BrandMark } from 'Frontend/auth/graphics.js';
import '@vaadin/icons';

/**
 * The application shell, in React — the former `MainLayout.java`.
 *
 * <p>Because this file is `views/@layout.tsx`, every route under `views/` renders
 * inside its `<Outlet/>`. So do the Flow views: `routes.tsx` registers the `Flow`
 * component as the fallback for each branch of the route tree, so a URL like
 * `/products` that no React route claims is handed to Flow and rendered here,
 * bridge and all. One shell, both frameworks.
 *
 * <p>Note what the nav items are: plain paths. The React router resolves them
 * whether the destination turns out to be a React view or a Flow one, which is
 * why `/dashboard` and `/dashboard-modern` are written identically despite being
 * different technologies underneath.
 */

/**
 * Stage one of the demo — the whole Swing application, its own login screen
 * included — is the Flow route at `/`. It must render full-bleed and without the
 * session guard, but it cannot be declared outside this layout: the file router
 * owns path `""` and `withFallback` puts an index route inside that branch, so a
 * sibling `/` route in `routes.tsx` is always shadowed. The layout steps aside
 * for it instead.
 */
const FULL_BLEED_PATHS = new Set(['/']);

const LOGOUT_ITEM = 'Logout';

/** Nav entries, in drawer order. Paths, not view classes — see above. */
const NAV_ITEMS = [
  { path: '/dashboard', label: 'Dashboard', icon: 'vaadin:dashboard' },
  { path: '/dashboard-modern', label: 'Dashboard · Modern', icon: 'vaadin:chart-grid' },
  { path: '/products', label: 'Products', icon: 'vaadin:package' },
  { path: '/suppliers', label: 'Suppliers', icon: 'vaadin:factory' },
  { path: '/orders', label: 'Orders', icon: 'vaadin:cart' },
  { path: '/stock', label: 'Stock', icon: 'vaadin:records' },
];

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = currentUser.value;
  const fullBleed = FULL_BLEED_PATHS.has(location.pathname);

  // The guard the Flow layout used to run in beforeEnter. The server is still
  // the authority — every endpoint re-checks the session — but this keeps an
  // unauthenticated visitor from seeing the shell at all.
  useEffect(() => {
    if (fullBleed) {
      return;
    }
    refreshUser().then((resolved) => {
      if (!resolved) {
        navigate('/login', { replace: true });
      }
    });
  }, [navigate, fullBleed]);

  // No shell, no guard: the Swing application owns the whole viewport here.
  if (fullBleed) {
    return <Outlet />;
  }

  // Nothing to show until the session check comes back: rendering the shell
  // first would flash a drawer at a signed-out visitor.
  if (user === undefined || user === null) {
    return null;
  }

  const signOut = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <AppLayout className="main-layout" primarySection="navbar">
      <DrawerToggle slot="navbar" className="nav-toggle" />
      <h1 slot="navbar" className="nav-title">
        WarehousePro
      </h1>

      <ContextMenu
        slot="navbar"
        openOn="click"
        items={[{ text: LOGOUT_ITEM }]}
        onItemSelected={(e) => {
          if (e.detail.value.text === LOGOUT_ITEM) {
            signOut();
          }
        }}
      >
        <div className="nav-user-btn" tabIndex={0}>
          <Avatar className="nav-avatar" name={user.username} />
          <div className="nav-user-info">
            <span className="nav-username">{user.username}</span>
            <span className="nav-role">{user.role}</span>
          </div>
        </div>
      </ContextMenu>

      <div slot="drawer" className="drawer-header">
        <div className="drawer-icon-box">
          <BrandMark size={22} />
        </div>
        <div className="drawer-brand-text">
          <span className="drawer-brand-title">WarehousePro</span>
          <span className="drawer-brand-sub">v3.5 · Enterprise</span>
        </div>
      </div>

      <SideNav
        slot="drawer"
        className="main-nav"
        theme="aura-filled"
        location={location}
        onNavigate={({ path }) => path && navigate(path)}
      >
        {NAV_ITEMS.map(({ path, label, icon }) => (
          <SideNavItem key={path} path={path}>
            <Icon icon={icon} slot="prefix" />
            {label}
          </SideNavItem>
        ))}
      </SideNav>

      <Outlet />
    </AppLayout>
  );
}
