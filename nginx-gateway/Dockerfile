FROM nginx:alpine

# Copy config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy certificates (these must be generated first)
COPY certs/nginx-selfsigned.crt /etc/nginx/certs/nginx-selfsigned.crt
COPY certs/nginx-selfsigned.key /etc/nginx/certs/nginx-selfsigned.key
