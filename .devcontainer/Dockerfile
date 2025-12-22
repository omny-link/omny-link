FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install Cairo and other system dependencies
RUN apt-get update && apt-get install -y \
    libcairo2-dev \
    pkg-config \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*
