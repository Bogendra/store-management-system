#!/bin/sh

# Replace environment variables in the configuration file
envsubst < /usr/share/nginx/html/config.js.template > /usr/share/nginx/html/config.js

# Execute the CMD from the Dockerfile
exec "$@"
