/**
 * The decorative SVGs the Flow views carried as inline HTML strings. As JSX they
 * are plain markup, so no `innerHTML` and no escaping — and the brand mark can
 * be shared between the login page and the application shell.
 */

/** The warehouse glyph used by the login card and the drawer header. */
export function BrandMark({ size = 28 }: { readonly size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="2" y="12" width="24" height="14" rx="1" stroke="#1b3a5c" strokeWidth="1.8" fill="none" />
      <polyline points="1,13 14,4 27,13" stroke="#1b3a5c" strokeWidth="1.8" strokeLinejoin="round" fill="none" />
      <rect x="10" y="18" width="8" height="8" rx="1" stroke="#1b3a5c" strokeWidth="1.5" fill="none" />
      <rect x="4" y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5" />
      <rect x="20" y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5" />
      <circle cx="17" cy="22.5" r="0.8" fill="#1b3a5c" />
    </svg>
  );
}

/** Shelving-rack silhouettes behind the login card. */
export function LoginBackdrop() {
  return (
    <div className="login-bg-deco">
      <div className="bg-rack-right">
        <svg viewBox="0 0 200 900" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="8" y="0" width="12" height="900" fill="#7eb8e8" />
          <rect x="180" y="0" width="12" height="900" fill="#7eb8e8" />
          {[60, 200, 340, 480, 620, 760].map((y) => (
            <rect key={y} x="8" y={y} width="184" height="10" fill="#7eb8e8" />
          ))}
          {[
            [70, 200],
            [210, 340],
            [350, 480],
            [490, 620],
            [630, 760],
          ].map(([top, bottom]) => (
            <g key={top}>
              <line x1="20" y1={top} x2="180" y2={bottom} stroke="#7eb8e8" strokeWidth="4" />
              <line x1="180" y1={top} x2="20" y2={bottom} stroke="#7eb8e8" strokeWidth="4" />
            </g>
          ))}
          <rect x="20" y="100" width="48" height="90" rx="3" fill="#7eb8e8" />
          <rect x="76" y="115" width="52" height="75" rx="3" fill="#7eb8e8" />
          <rect x="136" y="90" width="44" height="100" rx="3" fill="#7eb8e8" />
          <rect x="20" y="240" width="40" height="88" rx="3" fill="#7eb8e8" />
          <rect x="68" y="255" width="60" height="73" rx="3" fill="#7eb8e8" />
          <rect x="140" y="230" width="40" height="98" rx="3" fill="#7eb8e8" />
          <rect x="20" y="380" width="56" height="84" rx="3" fill="#7eb8e8" />
          <rect x="84" y="395" width="44" height="69" rx="3" fill="#7eb8e8" />
          <rect x="136" y="370" width="44" height="98" rx="3" fill="#7eb8e8" />
        </svg>
      </div>

      <div className="bg-rack-left">
        <svg viewBox="0 0 180 600" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="8" y="0" width="10" height="600" fill="#7eb8e8" />
          <rect x="162" y="0" width="10" height="600" fill="#7eb8e8" />
          {[80, 220, 360, 500].map((y) => (
            <rect key={y} x="8" y={y} width="164" height="8" fill="#7eb8e8" />
          ))}
          <line x1="18" y1="88" x2="162" y2="220" stroke="#7eb8e8" strokeWidth="3" />
          <line x1="162" y1="88" x2="18" y2="220" stroke="#7eb8e8" strokeWidth="3" />
          <rect x="18" y="100" width="42" height="110" rx="3" fill="#7eb8e8" />
          <rect x="68" y="120" width="46" height="90" rx="3" fill="#7eb8e8" />
          <rect x="122" y="90" width="38" height="120" rx="3" fill="#7eb8e8" />
          <rect x="18" y="240" width="50" height="108" rx="3" fill="#7eb8e8" />
          <rect x="76" y="255" width="40" height="93" rx="3" fill="#7eb8e8" />
          <rect x="124" y="235" width="36" height="113" rx="3" fill="#7eb8e8" />
        </svg>
      </div>

      <div className="bg-boxes-top">
        <svg width="180" height="160" viewBox="0 0 180 160" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="0" y="60" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="62" y="60" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="124" y="60" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="0" y="110" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="62" y="110" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="31" y="10" width="55" height="45" rx="3" fill="#7eb8e8" />
          <rect x="93" y="10" width="55" height="45" rx="3" fill="#7eb8e8" />
          {[27, 89, 151].map((x) => (
            <line key={x} x1={x} y1="60" x2={x} y2="105" stroke="#0d1b2a" strokeWidth="2" opacity="0.3" />
          ))}
        </svg>
      </div>
    </div>
  );
}
