/**
 * Runtime configuration. Points at the local Spring Boot backend during development.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'ws://localhost:8080/ws',
};
