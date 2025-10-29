module.exports = {
  testEnvironment: 'node',
  testTimeout: 30000,
  verbose: true,
  collectCoverage: false,
  testMatch: ['**/*.test.js'],
  setupFilesAfterEnv: ['<rootDir>/setup.js']
};