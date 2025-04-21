export function applyBootstrapTheme() {
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

  const themeUrl = prefersDark
    ? 'https://cdn.jsdelivr.net/npm/bootswatch@5.3.3/dist/darkly/bootstrap.min.css'
    : 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css';

  const existing = document.getElementById('bootstrap-theme');
  if (!existing) {
    const link = document.createElement('link');
    link.id = 'bootstrap-theme';
    link.rel = 'stylesheet';
    link.href = themeUrl;
    document.head.appendChild(link);
  } else {
    existing.href = themeUrl;
  }
}
