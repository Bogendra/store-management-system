# Build stage
FROM node:18-alpine as build
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy source code and build
COPY . ./
# Create runtime config.js that can be overridden at runtime
RUN cp src/config.js src/config.js.template

# Build the application
RUN npm run build

# Production stage with Nginx
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# Copy build output from previous stage
COPY --from=build /app/build .

# Copy nginx configuration
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Copy entrypoint script
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

# Copy config template for runtime substitution
COPY --from=build /app/src/config.js.template /usr/share/nginx/html/config.js.template

# Expose port
EXPOSE 80

# Set up entrypoint to replace environment variables
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
