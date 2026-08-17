/**
 * Semantic design tokens for the mobile app.
 *
 * These tokens mirror the naming conventions used in web artifacts (index.css)
 * so that multi-artifact projects share a cohesive visual identity.
 *
 * Replace the placeholder values below with values that match the project's
 * brand. If a sibling web artifact exists, read its index.css and convert the
 * HSL values to hex so both artifacts use the same palette.
 *
 * To add dark mode, add a `dark` key with the same token names.
 * The useColors() hook will automatically pick it up.
 */

const colors = {
  light: {
    // Legacy aliases (kept for backward compatibility)
    text: '#17212B',
    tint: '#E85B51',

    // Core surfaces
    background: '#F4F7F8',
    foreground: '#17212B',

    // Cards / elevated surfaces
    card: '#FFFFFF',
    cardForeground: '#17212B',

    // Primary action color (buttons, links, active states)
    primary: '#E85B51',
    primaryForeground: '#FFFFFF',

    // Secondary / less-emphasis interactive surfaces
    secondary: '#EAF0F1',
    secondaryForeground: '#31424F',

    // Muted / subdued elements (dividers, timestamps, placeholders)
    muted: '#E8EFF0',
    mutedForeground: '#71818B',

    // Accent highlights (badges, selected items, focus rings)
    accent: '#DCEEEF',
    accentForeground: '#1D6870',

    // Destructive actions (delete, error states)
    destructive: '#C94343',
    destructiveForeground: '#FFFFFF',

    // Borders and input outlines
    border: '#D7E2E4',
    input: '#D7E2E4',
  },

  // Border radius (in px). Sync from the sibling web artifact's --radius
  // CSS variable. This value applies to cards, buttons, inputs, and modals.
  radius: 18,
};

export default colors;
