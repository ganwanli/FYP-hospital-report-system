import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@/components': path.resolve(__dirname, './src/components'),
        '@/pages': path.resolve(__dirname, './src/pages'),
        '@/hooks': path.resolve(__dirname, './src/hooks'),
        '@/utils': path.resolve(__dirname, './src/utils'),
        '@/services': path.resolve(__dirname, './src/services'),
        '@/stores': path.resolve(__dirname, './src/stores'),
        '@/types': path.resolve(__dirname, './src/types'),
        '@/assets': path.resolve(__dirname, './src/assets'),
        '@/styles': path.resolve(__dirname, './src/styles'),
        '@/layouts': path.resolve(__dirname, './src/layouts'),
        '@/routes': path.resolve(__dirname, './src/routes'),
      },
    },
    define: {
      __APP_ENV__: JSON.stringify(env.VITE_APP_ENV),
    },
    server: {
      host: '0.0.0.0',
      port: 3000,
      open: true,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080/api',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
    build: {
      target: 'es2015',
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: mode === 'development',
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
          manualChunks: {
            vendor: ['react', 'react-dom'],
            antd: ['antd', '@ant-design/icons'],
            router: ['react-router-dom'],
            utils: ['lodash-es', 'dayjs', 'axios', 'zustand'],
            charts: ['recharts'],
          },
        },
      },
    },
    optimizeDeps: {
      include: [
        'react',
        'react-dom',
        'react-router-dom',
        'antd',
        '@ant-design/icons',
        'axios',
        'dayjs',
        'lodash-es',
        'zustand',
        'recharts',
        'ahooks',
      ],
    },
    css: {
      preprocessorOptions: {
        less: {
          modifyVars: {
            '@primary-color': '#1890ff',
            '@border-radius-base': '6px',
          },
          javascriptEnabled: true,
        },
      },
    },
  }
})