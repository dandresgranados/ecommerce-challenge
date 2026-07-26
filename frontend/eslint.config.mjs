// @ts-check

/**
 * ESLint 9 flat config para el proyecto Angular 21.
 * Combina reglas de:
 *   - typescript-eslint (reglas para código TS)
 *   - angular-eslint (reglas específicas de Angular)
 *
 * Ejecutar:
 *   npx eslint .              (solo diagnóstico)
 *   npx eslint . --fix        (aplica correcciones automáticas)
 */

import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import angular from 'angular-eslint';

export default tseslint.config(
  // ==================== TypeScript source files ====================
  {
    files: ['**/*.ts'],
    ignores: ['dist/**', '.angular/**', 'coverage/**', 'node_modules/**'],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.stylistic,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // Prefijo obligatorio "app" en selectores para no colisionar con Material.
      '@angular-eslint/directive-selector': [
        'error',
        { type: 'attribute', prefix: 'app', style: 'camelCase' },
      ],
      '@angular-eslint/component-selector': [
        'error',
        { type: 'element', prefix: 'app', style: 'kebab-case' },
      ],

      // Reglas más estrictas para calidad del código.
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/consistent-type-definitions': ['error', 'interface'],

      // Estilo general.
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'prefer-const': 'error',
      'no-var': 'error',
      eqeqeq: ['error', 'always'],
    },
  },

  // ==================== Angular templates ====================
  {
    files: ['**/*.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
    rules: {
      // Nada extra — con recommended + accessibility cubrimos todo lo importante.
    },
  },

  // ==================== Archivos de test — reglas más laxas ====================
  {
    files: ['**/*.spec.ts', '**/*.test.ts'],
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-non-null-assertion': 'off',
    },
  },
);
