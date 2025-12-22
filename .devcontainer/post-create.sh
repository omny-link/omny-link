#!/bin/bash
set -e

echo "Setting up CRM development environment..."

# Setup Node.js crm-ui2 project
if [ -d "crm-ui2" ]; then
    echo "Setting up crm-ui2..."
    cd crm-ui2
    npm install
    cd ..
fi

echo "✓ Development environment setup complete!"
echo ""
echo "Available commands:"
echo "  - crm-server: mvn install && cd crm-server && mvn spring-boot:run"
echo "  - crm-ui: cd crm-ui && npm install && gulp server"
echo "  - crm-ui2: cd crm-ui2 && npm run dev"
