FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci --only=production && \
    npm ci --only=development

COPY . .

RUN npm run build

FROM node:20-alpine

WORKDIR /opt/app

RUN apk add --no-cache dumb-init

RUN npm install -g serve

RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

COPY --from=builder --chown=nextjs:nodejs /app/dist ./dist

EXPOSE 3000

USER nextjs

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})" || exit 1

ENTRYPOINT ["dumb-init", "--"]

CMD ["serve", "-s", "dist", "-l", "3000"]
