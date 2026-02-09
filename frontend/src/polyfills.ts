/**
 * Polyfill for libraries that depend on the Node.js 'global' object.
 * This is required for sockjs-client when using the new Angular application builder.
 */
(window as any).global = window;