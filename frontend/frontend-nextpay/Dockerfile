# Stage 1: Build
FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
# Install deps including peer deps (react/react-dom are listed as peerDeps)
RUN npm install --legacy-peer-deps

COPY . .

# VITE_API_URL=/ makes axios use relative paths so Nginx can proxy /api/ to backend
ARG VITE_API_URL=/
ENV VITE_API_URL=${VITE_API_URL}

RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
