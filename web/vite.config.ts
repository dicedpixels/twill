import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { createReadStream, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import type { Plugin } from 'vite'

function metadataPlugin(): Plugin {
  return {
    name: 'serve-metadata',
    configureServer(server) {
      server.middlewares.use('/metadata', (req, res, next) => {
        const metaDir = resolve(__dirname, '../metadata')
        const filePath = resolve(metaDir, (req.url ?? '/').replace(/^\//, ''))
        if (!existsSync(filePath)) {
          next()
          return
        }
        res.setHeader('Content-Type', 'application/json')
        createReadStream(filePath).pipe(res)
      })
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  base: '/twill/',
  plugins: [react(), tailwindcss(), metadataPlugin()],
  server: {
    fs: { allow: ['..'] },
  },
})
